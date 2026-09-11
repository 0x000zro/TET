package com.example.ui.feature.mocktest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.Question
import com.example.domain.model.mocktest.MockTestConfiguration
import com.example.domain.model.mocktest.MockTestScope
import com.example.domain.model.mocktest.MockTestSession
import com.example.domain.repository.EducationalRepository
import com.example.domain.usecase.mocktest.CreateMockTestOutcome
import com.example.domain.usecase.mocktest.CreateMockTestSessionUseCase
import com.example.domain.usecase.mocktest.FinishMockTestOutcome
import com.example.domain.usecase.mocktest.FinishMockTestSessionUseCase
import com.example.ui.feature.mocktest.model.toMockTestPresentationModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel managing the Mock Test UI and lifecycle (Step 17).
 *
 * Flow:
 * Configuration Overview -> Start Test -> Active Questions (Select, Navigate, Clear)
 * -> Finish Confirmation -> Result Summary
 *
 * Architecture:
 * UI -> MockTestViewModel -> UseCases -> EducationalRepository
 *
 * Guarantees:
 * 1. Single source of truth: Questions are fetched canonically from [EducationalRepository].
 * 2. Immutable state machine: Delegates to [MockTestSession] and domain use cases.
 * 3. QuestionPresentationModel: Ensures correct answers are NOT exposed to the client during active test.
 */
class MockTestViewModel(
    private val repository: EducationalRepository = EducationalRepositoryImpl(),
    private val createSessionUseCase: CreateMockTestSessionUseCase = CreateMockTestSessionUseCase(repository),
    private val finishSessionUseCase: FinishMockTestSessionUseCase = FinishMockTestSessionUseCase(repository)
) : ViewModel() {

    private val _uiState = MutableStateFlow<MockTestUiState>(MockTestUiState.Loading)
    val uiState: StateFlow<MockTestUiState> = _uiState.asStateFlow()

    private var activeConfiguration: MockTestConfiguration? = null
    private var activeSession: MockTestSession? = null
    private val loadedQuestionsCache = mutableMapOf<String, Question>()

    /**
     * Initializes the mock test with a given configuration or loads the default exam/paper configuration.
     */
    fun loadTestConfiguration(configuration: MockTestConfiguration? = null) {
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

                // If available questions are fewer than configured questionCount, fail with error:
                // Do NOT silently reduce requested question count!
                if (availableCount < resolvedConfig.questionCount) {
                    _uiState.value = MockTestUiState.Error(
                        "Insufficient active questions available: requested ${resolvedConfig.questionCount}, but only $availableCount are available for this scope."
                    )
                    return@launch
                }

                activeConfiguration = resolvedConfig

                _uiState.value = MockTestUiState.ConfigurationOverview(
                    configuration = resolvedConfig,
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
                        val session = outcome.session.start(System.currentTimeMillis())
                        activeSession = session
                        loadedQuestionsCache.clear()

                        // Preload questions into memory cache for quick navigation
                        for (qId in session.questionIds) {
                            val q = repository.getQuestionById(qId)
                            if (q != null) {
                                loadedQuestionsCache[qId] = q
                            }
                        }

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
     * Finalizes the mock test session and evaluates results against canonical questions.
     */
    fun confirmFinish() {
        val session = activeSession ?: return
        _uiState.value = MockTestUiState.Loading

        viewModelScope.launch {
            try {
                when (val outcome = finishSessionUseCase.execute(session, System.currentTimeMillis())) {
                    is FinishMockTestOutcome.Failure -> {
                        _uiState.value = MockTestUiState.Error(outcome.errorMessage)
                    }
                    is FinishMockTestOutcome.Success -> {
                        activeSession = outcome.finalSession
                        _uiState.value = MockTestUiState.ResultSummary(
                            result = outcome.result,
                            configuration = session.configuration
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MockTestUiState.Error(e.localizedMessage ?: "Failed to evaluate mock test.")
            }
        }
    }

    /**
     * Restarts the test with the same configuration.
     */
    fun restartTest() {
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
            showFinishConfirmation = false,
            showAbandonConfirmation = false
        )
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
