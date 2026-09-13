package com.example.ui.feature.mocktest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EducationalRepositoryImpl
import com.example.data.repository.InMemoryMockTestPerformanceRepository
import com.example.domain.model.Question
import com.example.domain.model.mocktest.MockTestConfiguration
import com.example.domain.model.mocktest.MockTestPerformance
import com.example.domain.model.mocktest.MockTestScope
import com.example.domain.model.mocktest.MockTestSession
import com.example.domain.repository.EducationalRepository
import com.example.domain.repository.MockTestPerformanceRepository
import com.example.domain.usecase.mocktest.CreateMockTestOutcome
import com.example.domain.usecase.mocktest.CreateMockTestSessionUseCase
import com.example.domain.usecase.mocktest.FinishMockTestOutcome
import com.example.domain.usecase.mocktest.FinishMockTestSessionUseCase
import com.example.domain.usecase.mocktest.RecordMockTestPerformanceUseCase
import com.example.ui.feature.mocktest.model.MockTestPaletteItem
import com.example.ui.feature.mocktest.model.toMockTestPresentationModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * ViewModel managing the Mock Test UI, timer countdown, and lifecycle (Steps 17, 18, 19, 20 & 21).
 *
 * Flow:
 * Configuration Overview -> Start Test -> Active Questions + Countdown Timer
 * -> Finish Confirmation / Time Expiry -> Result Summary
 *
 * Architecture:
 * UI -> MockTestViewModel -> UseCases -> EducationalRepository & MockTestPerformanceRepository
 *
 * Guarantees:
 * 1. Single source of truth: Questions are fetched canonically from [EducationalRepository].
 * 2. Immutable state machine: Delegates to [MockTestSession] and domain use cases.
 * 3. QuestionPresentationModel: Ensures correct answers are NOT exposed to the client during active test.
 * 4. Timer safety: Started only when test becomes active, stopped on finish/abandon/lifecycle-clear,
 *    and accurately calculated using wall-clock delta against target end time.
 * 5. Isolated performance foundation: MockTestPerformance is stored in separate abstraction, never touching PracticeAttempt.
 */
class MockTestViewModel(
    private val repository: EducationalRepository = EducationalRepositoryImpl(),
    private val createSessionUseCase: CreateMockTestSessionUseCase = CreateMockTestSessionUseCase(repository),
    private val finishSessionUseCase: FinishMockTestSessionUseCase = FinishMockTestSessionUseCase(repository),
    private val performanceRepository: MockTestPerformanceRepository = InMemoryMockTestPerformanceRepository(),
    private val recordPerformanceUseCase: RecordMockTestPerformanceUseCase = RecordMockTestPerformanceUseCase(performanceRepository),
    private val timeProvider: () -> Long = { System.currentTimeMillis() },
    private val timerDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {

    private val _uiState = MutableStateFlow<MockTestUiState>(MockTestUiState.Loading)
    val uiState: StateFlow<MockTestUiState> = _uiState.asStateFlow()

    private var activeConfiguration: MockTestConfiguration? = null
    private var activeSession: MockTestSession? = null
    private val loadedQuestionsCache = mutableMapOf<String, Question>()

    private var timerJob: Job? = null
    private var targetEndTimeMillis: Long = 0L
    private var isFinishing: Boolean = false

    val isTimerActive: Boolean
        get() = timerJob?.isActive == true

    /**
     * Observable palette items for the active test session (Step 19).
     */
    val paletteItems: List<MockTestPaletteItem>
        get() = (_uiState.value as? MockTestUiState.ActiveTest)?.paletteItems ?: emptyList()

    /**
     * Initializes the mock test with a given configuration or loads the default exam/paper configuration.
     */
    fun loadTestConfiguration(configuration: MockTestConfiguration? = null) {
        stopTimer()
        isFinishing = false
        _uiState.value = MockTestUiState.Loading
        viewModelScope.launch {
            try {
                val resolvedConfig = configuration ?: resolveDefaultConfiguration()
                if (resolvedConfig == null) {
                    _uiState.value = MockTestUiState.Empty("No exam papers found to configure a mock test.")
                    return@launch
                }

                val validation = resolvedConfig.validate()
                if (validation.isFailure) {
                    _uiState.value = MockTestUiState.Error(
                        validation.exceptionOrNull()?.message ?: "Invalid mock test configuration."
                    )
                    return@launch
                }

                activeConfiguration = resolvedConfig

                // Resolve Exam and Paper titles for display
                val exam = repository.getExamById(resolvedConfig.examId)
                val paper = repository.getPaperById(resolvedConfig.paperId)

                val examTitle = exam?.name ?: "CTET Exam"
                val paperTitle = paper?.name ?: "Paper 1"

                val scopeDesc = when (resolvedConfig.scope) {
                    is MockTestScope.FullPaper -> "Full Paper Comprehensive"
                    is MockTestScope.SubjectScope -> "Subject Specific"
                    is MockTestScope.TopicScope -> "Topic Focused"
                    is MockTestScope.SubtopicScope -> "Subtopic Focused"
                }

                // Gather subtopics to calculate available count
                val subtopics = createSessionUseCase.resolveSubtopicIds(resolvedConfig.paperId, resolvedConfig.scope)
                var availableCount = 0
                for (sId in subtopics) {
                    availableCount += repository.getActiveQuestionCountBySubtopicId(sId)
                }

                if (availableCount == 0) {
                    _uiState.value = MockTestUiState.Empty("No active questions available for this mock test scope.")
                    return@launch
                }

                val finalConfig = if (configuration == null) {
                    // Default configuration adapts question count to available questions
                    resolvedConfig.copy(questionCount = minOf(resolvedConfig.questionCount, availableCount))
                } else {
                    // Custom user-requested configuration requires sufficient questions
                    if (availableCount < resolvedConfig.questionCount) {
                        _uiState.value = MockTestUiState.Error(
                            "Insufficient active questions available: requested ${resolvedConfig.questionCount}, but only $availableCount are available for this scope."
                        )
                        return@launch
                    }
                    resolvedConfig
                }

                activeConfiguration = finalConfig

                _uiState.value = MockTestUiState.ConfigurationOverview(
                    configuration = finalConfig,
                    examTitle = examTitle,
                    paperTitle = paperTitle,
                    scopeDescription = scopeDesc,
                    availableQuestionsCount = availableCount
                )
            } catch (e: Exception) {
                _uiState.value = MockTestUiState.Error(e.localizedMessage ?: "Failed to load test configuration.")
            }
        }
    }

    /**
     * Starts the test session from the current configuration overview.
     */
    fun startTest() {
        val config = activeConfiguration ?: return
        _uiState.value = MockTestUiState.Loading

        viewModelScope.launch {
            try {
                when (val outcome = createSessionUseCase.execute(config)) {
                    is CreateMockTestOutcome.Failure -> {
                        _uiState.value = MockTestUiState.Error(outcome.errorMessage)
                    }
                    is CreateMockTestOutcome.Success -> {
                        val session = outcome.session.start(timeProvider())
                        activeSession = session
                        loadedQuestionsCache.clear()
                        isFinishing = false

                        // Preload questions into memory cache for quick navigation
                        for (qId in session.questionIds) {
                            val q = repository.getQuestionById(qId)
                            if (q != null) {
                                loadedQuestionsCache[qId] = q
                            }
                        }

                        startTimer(config.durationMinutes)
                        renderCurrentQuestion(session)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MockTestUiState.Error(e.localizedMessage ?: "Failed to start mock test.")
            }
        }
    }

    /**
     * Selects or updates an option answer for the current active question.
     */
    fun selectAnswer(optionId: String) {
        val current = activeSession ?: return
        if (!current.isStarted || current.isCompleted) return

        val updated = current.selectAnswer(optionId)
        activeSession = updated
        renderCurrentQuestion(updated)
    }

    /**
     * Clears the current answer selection for the active question.
     */
    fun clearAnswer() {
        val current = activeSession ?: return
        if (!current.isStarted || current.isCompleted) return

        val updated = current.clearAnswer()
        activeSession = updated
        renderCurrentQuestion(updated)
    }

    /**
     * Advances to the next question.
     */
    fun nextQuestion() {
        val current = activeSession ?: return
        if (current.hasNextQuestion) {
            val updated = current.nextQuestion()
            activeSession = updated
            renderCurrentQuestion(updated)
        }
    }

    /**
     * Navigates back to the previous question.
     */
    fun previousQuestion() {
        val current = activeSession ?: return
        if (current.hasPreviousQuestion) {
            val updated = current.previousQuestion()
            activeSession = updated
            renderCurrentQuestion(updated)
        }
    }

    /**
     * Directly navigates to a question by its 0-based index in the session (Step 19).
     *
     * Guarantees:
     * - Preserves previously selected answers across navigation.
     * - Does NOT reset, pause, or alter the running countdown timer.
     * - Synchronizes currentQuestionIndex, answeredCount, unansweredCount, and palette state.
     */
    fun navigateToQuestion(index: Int) {
        val current = activeSession ?: return
        if (!current.isStarted || current.isCompleted) return
        if (index !in current.questionIds.indices) return
        if (index == current.currentIndex) return

        val updated = current.navigateToIndex(index)
        activeSession = updated
        renderCurrentQuestion(updated)
    }

    /**
     * Directly navigates to a question by its 1-based question number (Step 19).
     */
    fun navigateToQuestionNumber(questionNumber: Int) {
        navigateToQuestion(questionNumber - 1)
    }

    /**
     * Alias for [navigateToQuestion] using 0-based index (Step 19).
     */
    fun navigateToQuestionIndex(index: Int) {
        navigateToQuestion(index)
    }

    /**
     * Requests finish confirmation dialog to be shown or dismissed.
     */
    fun setFinishConfirmationVisible(visible: Boolean) {
        val currentState = _uiState.value
        if (currentState is MockTestUiState.ActiveTest) {
            _uiState.value = currentState.copy(showFinishConfirmation = visible)
        }
    }

    /**
     * Requests abandon confirmation dialog to be shown or dismissed.
     */
    fun setAbandonConfirmationVisible(visible: Boolean) {
        val currentState = _uiState.value
        if (currentState is MockTestUiState.ActiveTest) {
            _uiState.value = currentState.copy(showAbandonConfirmation = visible)
        }
    }

    /**
     * Finalizes the mock test session manually and evaluates results against canonical questions.
     */
    fun confirmFinish() {
        val session = activeSession ?: return
        executeFinish(session)
    }

    /**
     * Abandons the active mock test session, cancelling the timer and discarding the in-progress session.
     */
    fun abandonTest() {
        stopTimer()
        activeSession = null
        isFinishing = false
    }

    /**
     * Internal unified finish routine with duplicate execution guard.
     */
    private fun executeFinish(session: MockTestSession) {
        if (session.isCompleted || isFinishing) return
        isFinishing = true
        stopTimer()
        _uiState.value = MockTestUiState.Loading

        viewModelScope.launch {
            try {
                when (val outcome = finishSessionUseCase.execute(session, timeProvider())) {
                    is FinishMockTestOutcome.Failure -> {
                        isFinishing = false
                        _uiState.value = MockTestUiState.Error(outcome.errorMessage)
                    }
                    is FinishMockTestOutcome.Success -> {
                        activeSession = outcome.finalSession
                        val recordedPerformance = recordPerformanceUseCase.execute(
                            session = outcome.finalSession,
                            result = outcome.result
                        )
                        val totalDurationSeconds = (session.configuration.durationMinutes * 60L).coerceAtLeast(0L)
                        val timeUsedSeconds = recordedPerformance?.timeUsedSeconds ?: if (outcome.result.finishedAt > outcome.result.startedAt && outcome.result.startedAt > 0L) {
                            ((outcome.result.finishedAt - outcome.result.startedAt) / 1000L).coerceAtLeast(0L)
                        } else 0L
                        val timeRemainingSeconds = if (totalDurationSeconds > 0L) {
                            (totalDurationSeconds - timeUsedSeconds).coerceAtLeast(0L)
                        } else 0L

                        _uiState.value = MockTestUiState.ResultSummary(
                            result = outcome.result,
                            configuration = session.configuration,
                            timeUsedSeconds = timeUsedSeconds,
                            timeRemainingSeconds = timeRemainingSeconds,
                            performance = recordedPerformance
                        )
                    }
                }
            } catch (e: Exception) {
                isFinishing = false
                _uiState.value = MockTestUiState.Error(e.localizedMessage ?: "Failed to evaluate mock test.")
            }
        }
    }

    /**
     * Starts the countdown timer based on the configuration's duration in minutes.
     */
    private fun startTimer(durationMinutes: Int) {
        stopTimer()
        val totalDurationSeconds = (durationMinutes * 60L).coerceAtLeast(0L)
        val totalDurationMillis = totalDurationSeconds * 1000L
        val startTimeMillis = timeProvider()
        targetEndTimeMillis = startTimeMillis + totalDurationMillis

        val initialRemaining = calculateRemainingSeconds(startTimeMillis)
        updateRemainingTimeInState(initialRemaining)

        if (totalDurationSeconds <= 0L) {
            onTimerExpired()
            return
        }

        timerJob = viewModelScope.launch(timerDispatcher) {
            while (isActive) {
                delay(1000L)
                val current = timeProvider()
                val remainingSeconds = calculateRemainingSeconds(current)
                updateRemainingTimeInState(remainingSeconds)

                if (remainingSeconds <= 0L) {
                    onTimerExpired()
                    break
                }
            }
        }
    }

    /**
     * Halts and clears any running timer job.
     */
    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    /**
     * Computes remaining seconds using targetEndTimeMillis - currentTime to prevent tick distortion.
     */
    private fun calculateRemainingSeconds(currentTime: Long): Long {
        if (targetEndTimeMillis <= 0L) return 0L
        val remainingMillis = targetEndTimeMillis - currentTime
        return if (remainingMillis <= 0L) 0L else (remainingMillis + 999L) / 1000L
    }

    /**
     * Pushes the latest remaining time into the active UI state without altering dialog or question state.
     */
    private fun updateRemainingTimeInState(remainingSeconds: Long) {
        val current = _uiState.value
        if (current is MockTestUiState.ActiveTest) {
            _uiState.value = current.copy(
                remainingSeconds = remainingSeconds,
                formattedRemainingTime = formatRemainingTime(remainingSeconds)
            )
        }
    }

    /**
     * Triggered automatically when the countdown timer hits zero.
     */
    private fun onTimerExpired() {
        val session = activeSession ?: return
        executeFinish(session)
    }

    /**
     * Restarts the test with the same configuration by instantiating a completely new session.
     * Ensures previous completed session and result are never reused, and timer is fresh.
     */
    fun restartTest() {
        stopTimer()
        isFinishing = false
        activeSession = null
        if (activeConfiguration != null) {
            startTest()
        } else {
            loadTestConfiguration()
        }
    }

    /**
     * Returns to the mock test configuration overview screen.
     */
    fun returnToOverview() {
        stopTimer()
        isFinishing = false
        activeSession = null
        loadTestConfiguration(activeConfiguration)
    }

    private fun renderCurrentQuestion(session: MockTestSession) {
        val qId = session.currentQuestionId
        if (qId == null) {
            _uiState.value = MockTestUiState.Error("Invalid question index.")
            return
        }

        val canonicalQuestion = loadedQuestionsCache[qId]
        if (canonicalQuestion == null) {
            _uiState.value = MockTestUiState.Error("Question details could not be found.")
            return
        }

        val presentation = canonicalQuestion.toMockTestPresentationModel(
            questionNumber = session.displayQuestionNumber
        )

        val currentRemaining = calculateRemainingSeconds(timeProvider())
        val currentState = _uiState.value
        val showFinish = if (currentState is MockTestUiState.ActiveTest) currentState.showFinishConfirmation else false
        val showAbandon = if (currentState is MockTestUiState.ActiveTest) currentState.showAbandonConfirmation else false

        val palette = session.questionIds.mapIndexed { index, questionId ->
            MockTestPaletteItem(
                index = index,
                questionNumber = index + 1,
                isCurrent = (index == session.currentIndex),
                isAnswered = !session.selectedAnswers[questionId].isNullOrBlank()
            )
        }

        _uiState.value = MockTestUiState.ActiveTest(
            session = session,
            currentQuestion = presentation,
            currentQuestionIndex = session.displayQuestionNumber,
            totalQuestions = session.totalQuestions,
            selectedOptionId = session.currentSelectedOptionId,
            answeredCount = session.answeredCount,
            unansweredCount = session.unansweredCount,
            hasPrevious = session.hasPreviousQuestion,
            hasNext = session.hasNextQuestion,
            isLastQuestion = session.isLastQuestion,
            showFinishConfirmation = showFinish,
            showAbandonConfirmation = showAbandon,
            remainingSeconds = currentRemaining,
            formattedRemainingTime = formatRemainingTime(currentRemaining),
            paletteItems = palette
        )
    }

    public override fun onCleared() {
        super.onCleared()
        stopTimer()
    }

    companion object {
        /**
         * Formats remaining duration into MM:SS.
         */
        fun formatRemainingTime(remainingSeconds: Long): String {
            val clamped = remainingSeconds.coerceAtLeast(0L)
            val minutes = clamped / 60
            val seconds = clamped % 60
            return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
        }
    }

    private suspend fun resolveDefaultConfiguration(): MockTestConfiguration? {
        val exams = repository.observeActiveExams().first()
        val defaultExam = exams.firstOrNull() ?: return null

        val papers = repository.observeActivePapersByExamId(defaultExam.id).first()
        val defaultPaper = papers.firstOrNull() ?: return null

        return MockTestConfiguration(
            id = "mock_${defaultExam.id}_${defaultPaper.id}",
            title = "${defaultExam.name} - ${defaultPaper.name} Mock Test",
            examId = defaultExam.id,
            paperId = defaultPaper.id,
            scope = MockTestScope.FullPaper,
            questionCount = 10,
            durationMinutes = 15
        )
    }
}
