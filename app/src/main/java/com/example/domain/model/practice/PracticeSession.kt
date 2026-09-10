package com.example.domain.model.practice

import com.example.domain.model.Question

/**
 * Focused domain model representing an in-memory multi-question practice session.
 *
 * Maintains:
 * - [subtopicId]: Identifier of the subtopic being practiced
 * - [questions]: Ordered list of questions included in this session
 * - [currentQuestionIndex]: 0-based index of the currently active question
 * - [selectedOptionId]: ID of the option selected for the current question (if any)
 * - [isSubmitted]: Whether the current question's answer has been submitted
 * - [currentAnswerResult]: Detailed evaluation of the current question post-submission
 * - [answeredCount]: Total number of answered questions in this session
 * - [correctCount]: Total number of correct answers in this session
 * - [isCompleted]: Whether the practice session has finished
 *
 * Implements strict state transitions and domain immutability.
 */
data class PracticeSession(
    val subtopicId: String,
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOptionId: String? = null,
    val isSubmitted: Boolean = false,
    val currentAnswerResult: PracticeAnswerResult? = null,
    val answeredCount: Int = 0,
    val correctCount: Int = 0,
    val isCompleted: Boolean = false
) {
    /**
     * Total count of questions in the practice session.
     */
    val totalQuestions: Int get() = questions.size

    /**
     * Display-friendly 1-based index of the current question.
     */
    val displayQuestionNumber: Int get() = currentQuestionIndex + 1

    /**
     * The active [Question] object, or null if the session is empty or out of bounds.
     */
    val currentQuestion: Question?
        get() = if (currentQuestionIndex in questions.indices) questions[currentQuestionIndex] else null

    /**
     * True if the session is currently on its final question.
     */
    val isLastQuestion: Boolean
        get() = questions.isNotEmpty() && currentQuestionIndex == questions.lastIndex

    /**
     * True if there is a subsequent question to advance to.
     */
    val hasNextQuestion: Boolean
        get() = questions.isNotEmpty() && currentQuestionIndex < questions.lastIndex

    /**
     * True if an option is selected, submission hasn't occurred yet, and session is not completed.
     */
    val canSubmit: Boolean
        get() = !isSubmitted && !selectedOptionId.isNullOrBlank() && !isCompleted && currentQuestion != null

    /**
     * Aggregated final result of this session.
     */
    val sessionResult: PracticeSessionResult
        get() = PracticeSessionResult.from(
            totalQuestions = totalQuestions,
            answeredQuestions = answeredCount,
            correctAnswers = correctCount
        )

    /**
     * Returns a new session with an option selected for the current question.
     * Ignored if the current question is already submitted or the session is completed.
     */
    fun selectOption(optionId: String): PracticeSession {
        if (isSubmitted || isCompleted) return this
        return copy(selectedOptionId = optionId)
    }

    /**
     * Returns a new session with the answer submitted and evaluated.
     * Prevents duplicate submission.
     */
    fun submitAnswer(result: PracticeAnswerResult): PracticeSession {
        if (isSubmitted || isCompleted) return this
        val isCorrect = result.status == PracticeEvaluationStatus.CORRECT
        return copy(
            isSubmitted = true,
            currentAnswerResult = result,
            answeredCount = answeredCount + 1,
            correctCount = if (isCorrect) correctCount + 1 else correctCount
        )
    }

    /**
     * Advances to the next question in the session.
     * Resets question-level state (selectedOptionId, isSubmitted, currentAnswerResult).
     */
    fun nextQuestion(): PracticeSession {
        if (!hasNextQuestion || isCompleted) return this
        return copy(
            currentQuestionIndex = currentQuestionIndex + 1,
            selectedOptionId = null,
            isSubmitted = false,
            currentAnswerResult = null
        )
    }

    /**
     * Marks the session as completed.
     */
    fun finish(): PracticeSession {
        return copy(isCompleted = true)
    }
}
