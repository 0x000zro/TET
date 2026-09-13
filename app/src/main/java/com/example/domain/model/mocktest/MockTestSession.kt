package com.example.domain.model.mocktest

import com.example.domain.model.Question

/**
 * Pure Kotlin domain model representing an in-progress or completed Mock Test Session.
 *
 * Guarantees:
 * 1. Single source of truth: Stores ordered question IDs [questionIds]. Questions themselves
 *    are supplied or referenced from canonical Question entities and are never duplicated.
 * 2. Navigation state: [currentIndex] tracks the active question (0-indexed).
 * 3. Answer selections: [selectedAnswers] maps questionId -> selectedOptionId.
 *    Supports selection, update, or clearing prior to session submission.
 * 4. Completion state: [isStarted], [isCompleted], [startedAt], [finishedAt].
 * 5. Strict immutability: All state mutations return a new instance via pure transition methods.
 */
data class MockTestSession(
    val configuration: MockTestConfiguration,
    val questionIds: List<String>,
    val currentIndex: Int = 0,
    val selectedAnswers: Map<String, String> = emptyMap(),
    val isStarted: Boolean = false,
    val isCompleted: Boolean = false,
    val startedAt: Long = 0L,
    val finishedAt: Long = 0L
) {
    /**
     * Total number of questions scheduled in this mock test session.
     */
    val totalQuestions: Int get() = questionIds.size

    /**
     * Display-friendly 1-based index of current question.
     */
    val displayQuestionNumber: Int get() = if (totalQuestions > 0) currentIndex + 1 else 0

    /**
     * Question ID at current index, or null if session is empty or out of bounds.
     */
    val currentQuestionId: String?
        get() = if (currentIndex in questionIds.indices) questionIds[currentIndex] else null

    /**
     * Selected option ID for the current active question (if any).
     */
    val currentSelectedOptionId: String?
        get() = currentQuestionId?.let { selectedAnswers[it] }

    /**
     * Number of questions with an recorded option selection.
     */
    val answeredCount: Int
        get() = questionIds.count { id -> !selectedAnswers[id].isNullOrBlank() }

    /**
     * Number of questions not yet answered.
     */
    val unansweredCount: Int
        get() = (totalQuestions - answeredCount).coerceAtLeast(0)

    /**
     * Whether the session currently points to the final question.
     */
    val isLastQuestion: Boolean
        get() = questionIds.isNotEmpty() && currentIndex == questionIds.lastIndex

    /**
     * Whether there is a next question available.
     */
    val hasNextQuestion: Boolean
        get() = questionIds.isNotEmpty() && currentIndex < questionIds.lastIndex

    /**
     * Whether there is a previous question available.
     */
    val hasPreviousQuestion: Boolean
        get() = questionIds.isNotEmpty() && currentIndex > 0

    /**
     * Starts the mock test session with the given timestamp.
     * Idempotent if already started.
     */
    fun start(timestamp: Long = System.currentTimeMillis()): MockTestSession {
        if (isStarted || isCompleted) return this
        return copy(
            isStarted = true,
            startedAt = timestamp
        )
    }

    /**
     * Records or updates the selected option for the question at the current index.
     * Disallowed if the session has not started or has completed.
     */
    fun selectAnswer(optionId: String): MockTestSession {
        if (!isStarted || isCompleted) return this
        val qId = currentQuestionId ?: return this
        if (optionId.isBlank()) return this
        return copy(
            selectedAnswers = selectedAnswers + (qId to optionId)
        )
    }

    /**
     * Clears any selected option for the question at the current index.
     */
    fun clearAnswer(): MockTestSession {
        if (!isStarted || isCompleted) return this
        val qId = currentQuestionId ?: return this
        return copy(
            selectedAnswers = selectedAnswers - qId
        )
    }

    /**
     * Advances to the next question in the session.
     */
    fun nextQuestion(): MockTestSession {
        if (!isStarted || isCompleted || !hasNextQuestion) return this
        return copy(currentIndex = currentIndex + 1)
    }

    /**
     * Returns to the previous question in the session.
     */
    fun previousQuestion(): MockTestSession {
        if (!isStarted || isCompleted || !hasPreviousQuestion) return this
        return copy(currentIndex = currentIndex - 1)
    }

    /**
     * Navigates directly to an arbitrary question by its 0-based index.
     */
    fun navigateToIndex(index: Int): MockTestSession {
        if (!isStarted || isCompleted) return this
        if (index !in questionIds.indices) return this
        return copy(currentIndex = index)
    }

    /**
     * Marks the session as finished and sets [finishedAt].
     * Idempotent if already completed.
     */
    fun finish(timestamp: Long = System.currentTimeMillis()): MockTestSession {
        if (isCompleted) return this
        return copy(
            isCompleted = true,
            finishedAt = timestamp
        )
    }

    /**
     * Calculates the deterministic [MockTestResult] using the canonical list of [Question]s.
     * Evaluates correct answers by checking whether the student's selected option ID has isCorrect = true.
     */
    fun calculateResult(canonicalQuestions: List<Question>): MockTestResult {
        val questionsMap = canonicalQuestions.associateBy { it.id }
        var correctCount = 0
        val outcomes = mutableListOf<MockTestQuestionOutcome>()

        for ((index, qId) in questionIds.withIndex()) {
            val selectedOptionId = selectedAnswers[qId]
            val status = when {
                selectedOptionId.isNullOrBlank() -> MockTestQuestionOutcomeStatus.UNANSWERED
                questionsMap[qId]?.options?.any { it.id == selectedOptionId && it.isCorrect } == true -> {
                    correctCount++
                    MockTestQuestionOutcomeStatus.CORRECT
                }
                else -> MockTestQuestionOutcomeStatus.INCORRECT
            }
            outcomes.add(
                MockTestQuestionOutcome(
                    questionNumber = index + 1,
                    questionId = qId,
                    status = status
                )
            )
        }

        return MockTestResult.from(
            configurationId = configuration.id,
            totalQuestions = totalQuestions,
            answeredQuestions = answeredCount,
            correctAnswers = correctCount,
            startedAt = startedAt,
            finishedAt = finishedAt,
            questionOutcomes = outcomes
        )
    }
}
