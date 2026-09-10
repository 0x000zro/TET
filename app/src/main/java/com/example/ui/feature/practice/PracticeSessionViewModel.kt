package com.example.ui.feature.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.practice.PracticeAttempt
import com.example.domain.model.practice.PracticeSession
import com.example.domain.repository.EducationalRepository
import com.example.domain.usecase.practice.EvaluatePracticeAnswerUseCase
import com.example.domain.usecase.practice.PracticeEvaluationOutcome
import com.example.ui.feature.question.model.toPresentationModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Focused ViewModel managing multi-question practice session lifecycle (Step 10)
 * and completed practice attempt persistence (Step 11).
 *
 * Architecture:
 * UI -> PracticeSessionViewModel -> EducationalRepository (Room) & EvaluatePracticeAnswerUseCase
 *
 * Key guarantees:
 * 1. Session isolation: State is maintained strictly in memory during the session; no premature persistence.
 * 2. Answer key security: Questions are exposed to UI via [QuestionPresentationModel] which omits correct answers.
 * 3. Idempotent evaluation: Prevents duplicate answer submission; answers are frozen once submitted.
 * 4. Step-by-step navigation: Question 1 -> Question 2 -> ... -> Finish -> Result Summary.
 * 5. Robust persistence: Completed sessions are persisted once locally with a stable attempt ID.
 * 6. Resilient error handling: Persistence errors never corrupt or crash the session result screen.
 */
class PracticeSessionViewModel(
    private val repository: EducationalRepository = EducationalRepositoryImpl(),
    private val evaluateAnswerUseCase: EvaluatePracticeAnswerUseCase = EvaluatePracticeAnswerUseCase(repository)
) : ViewModel() {

    private val _sessionState = MutableStateFlow<PracticeSessionUiState>(PracticeSessionUiState.Loading)
    val sessionState: StateFlow<PracticeSessionUiState> = _sessionState.asStateFlow()

    private val _subtopicAttempts = MutableStateFlow<List<PracticeAttempt>>(emptyList())
    val subtopicAttempts: StateFlow<List<PracticeAttempt>> = _subtopicAttempts.asStateFlow()

    private var activeJob: Job? = null
    private var attemptsJob: Job? = null
    private var isSubmitting: Boolean = false
    private var isFinishing: Boolean = false
    private var isAttemptSaved: Boolean = false
    private var currentSubtopicId: String? = null
    private var currentAttemptId: String = UUID.randomUUID().toString()
    private var sessionStartedAt: Long = 0L

    /**
     * Initializes and starts a multi-question practice session for the specified subtopic.
     * Resets any existing session state in memory.
     */
    fun startSession(subtopicId: String) {
        if (subtopicId.isBlank()) {
            _sessionState.value = PracticeSessionUiState.Error("Invalid subtopic ID.")
            return
        }

        currentSubtopicId = subtopicId
        currentAttemptId = UUID.randomUUID().toString()
        sessionStartedAt = System.currentTimeMillis()
        isAttemptSaved = false
        isFinishing = false
        isSubmitting = false

        attemptsJob?.cancel()
        attemptsJob = viewModelScope.launch {
            repository.observeAttemptsBySubtopicId(subtopicId)
                .catch { emit(emptyList()) }
                .collect { attempts ->
                    _subtopicAttempts.value = attempts
                }
        }

        activeJob?.cancel()
        _sessionState.value = PracticeSessionUiState.Loading

        activeJob = viewModelScope.launch {
            repository.observeQuestionsForSubtopic(subtopicId, activeOnly = true)
                .catch { error ->
                    _sessionState.value = PracticeSessionUiState.Error(
                        error.localizedMessage ?: "Failed to load practice questions."
                    )
                }
                .collect { questions ->
                    if (questions.isEmpty()) {
                        _sessionState.value = PracticeSessionUiState.Empty
                    } else {
                        val sortedQuestions = questions.sortedWith(
                            compareBy({ it.sortOrder }, { it.id })
                        )
                        val session = PracticeSession(
                            subtopicId = subtopicId,
                            questions = sortedQuestions,
                            currentQuestionIndex = 0
                        )
                        val firstQuestion = sortedQuestions.first()
                        val presentation = firstQuestion.toPresentationModel(questionNumber = 1)

                        _sessionState.value = PracticeSessionUiState.ActiveQuestion(
                            session = session,
                            question = presentation,
                            currentQuestionIndex = 1,
                            totalQuestions = sortedQuestions.size,
                            selectedOptionId = null,
                            isSubmitted = false,
                            isLastQuestion = sortedQuestions.size == 1,
                            result = null,
                            canSubmit = false
                        )
                    }
                }
        }
    }

    /**
     * Selects an option for the active question.
     * Replaces any previous selection; rejected if already submitted.
     */
    fun selectOption(optionId: String) {
        val current = _sessionState.value
        if (current !is PracticeSessionUiState.ActiveQuestion || current.isSubmitted) {
            return
        }

        val updatedSession = current.session.selectOption(optionId)
        _sessionState.value = current.copy(
            session = updatedSession,
            selectedOptionId = optionId,
            canSubmit = optionId.isNotBlank()
        )
    }

    /**
     * Submits the chosen option for evaluation.
     * Re-evaluates via [EvaluatePracticeAnswerUseCase] in memory and reveals feedback.
     * Prevents duplicate submission.
     */
    fun submitAnswer() {
        val current = _sessionState.value
        if (current !is PracticeSessionUiState.ActiveQuestion) {
            return
        }

        if (current.isSubmitted || current.selectedOptionId.isNullOrBlank()) {
            return
        }

        if (isSubmitting) {
            return
        }
        isSubmitting = true

        val activeQuestion = current.session.currentQuestion
        if (activeQuestion == null) {
            isSubmitting = false
            _sessionState.value = PracticeSessionUiState.Error("Active question not found.")
            return
        }

        val selectedOptionId = current.selectedOptionId

        viewModelScope.launch {
            try {
                when (val outcome = evaluateAnswerUseCase.evaluate(activeQuestion, selectedOptionId)) {
                    is PracticeEvaluationOutcome.Success -> {
                        val updatedSession = current.session.submitAnswer(outcome.result)
                        _sessionState.value = current.copy(
                            session = updatedSession,
                            isSubmitted = true,
                            result = outcome.result,
                            canSubmit = false,
                            isLastQuestion = updatedSession.isLastQuestion
                        )
                    }
                    is PracticeEvaluationOutcome.Failure -> {
                        _sessionState.value = PracticeSessionUiState.Error(outcome.errorMessage)
                    }
                }
            } catch (e: Exception) {
                _sessionState.value = PracticeSessionUiState.Error(
                    e.localizedMessage ?: "Failed to evaluate practice answer."
                )
            } finally {
                isSubmitting = false
            }
        }
    }

    /**
     * Advances to the next question in the practice session.
     * Only permitted if the current question has been submitted and a next question exists.
     */
    fun nextQuestion() {
        val current = _sessionState.value
        if (current !is PracticeSessionUiState.ActiveQuestion || !current.isSubmitted) {
            return
        }

        if (!current.session.hasNextQuestion) {
            return
        }

        val updatedSession = current.session.nextQuestion()
        val nextQuestion = updatedSession.currentQuestion ?: return
        val presentation = nextQuestion.toPresentationModel(questionNumber = updatedSession.displayQuestionNumber)

        _sessionState.value = PracticeSessionUiState.ActiveQuestion(
            session = updatedSession,
            question = presentation,
            currentQuestionIndex = updatedSession.displayQuestionNumber,
            totalQuestions = updatedSession.totalQuestions,
            selectedOptionId = null,
            isSubmitted = false,
            isLastQuestion = updatedSession.isLastQuestion,
            result = null,
            canSubmit = false
        )
    }

    /**
     * Finishes the practice session, transitions to the summary result view,
     * and persists the completed practice attempt locally (Step 11).
     *
     * Guarantees:
     * - Only completed sessions are persisted.
     * - Idempotent: repeated finish calls will not generate duplicate attempt records.
     * - Resilience: database persistence failures never corrupt or break the summary result screen.
     */
    fun finishSession() {
        val current = _sessionState.value
        if (current !is PracticeSessionUiState.ActiveQuestion) {
            return
        }
        if (isFinishing || isAttemptSaved) {
            return
        }
        isFinishing = true

        val completedSession = current.session.finish()
        val sessionResult = completedSession.sessionResult

        _sessionState.value = PracticeSessionUiState.Completed(
            session = completedSession,
            result = sessionResult
        )

        val attempt = PracticeAttempt(
            id = currentAttemptId,
            subtopicId = completedSession.subtopicId,
            totalQuestions = sessionResult.totalQuestions,
            answeredQuestions = sessionResult.answeredQuestions,
            correctAnswers = sessionResult.correctAnswers,
            incorrectAnswers = sessionResult.incorrectAnswers,
            percentageScore = sessionResult.scorePercentage,
            startedAt = if (sessionStartedAt > 0L) sessionStartedAt else System.currentTimeMillis(),
            completedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                val saveResult = repository.savePracticeAttempt(attempt)
                if (saveResult.isSuccess) {
                    isAttemptSaved = true
                }
            } catch (_: Exception) {
                // Failure to persist locally must never corrupt or crash the in-memory session result
            } finally {
                isFinishing = false
            }
        }
    }

    /**
     * Completely resets the active practice session in memory.
     */
    fun resetSession() {
        activeJob?.cancel()
        attemptsJob?.cancel()
        currentSubtopicId = null
        currentAttemptId = UUID.randomUUID().toString()
        sessionStartedAt = 0L
        isAttemptSaved = false
        isFinishing = false
        isSubmitting = false
        _sessionState.value = PracticeSessionUiState.Loading
    }

    /**
     * Retries loading the current session.
     */
    fun retry() {
        currentSubtopicId?.let { startSession(it) }
    }
}
