package com.example.domain.model.mocktest

/**
 * Pure Kotlin domain model representing the comprehensive and deterministic outcome
 * of a completed or finished Mock Test.
 *
 * Requirements:
 * - [configurationId]: Identifier of the associated [MockTestConfiguration]
 * - [totalQuestions]: Total questions present in the mock test
 * - [answeredQuestions]: Total questions answered by the student
 * - [correctAnswers]: Total questions answered correctly
 * - [incorrectAnswers]: Total questions answered incorrectly
 * - [unansweredQuestions]: Total questions left unanswered
 * - [scorePercentage]: Calculated percentage score (0.0 to 100.0)
 * - [startedAt]: Timestamp when test started (epoch millis)
 * - [finishedAt]: Timestamp when test finished (epoch millis)
 *
 * Immutability and pure domain validation rules are enforced.
 */
data class MockTestResult(
    val configurationId: String,
    val totalQuestions: Int,
    val answeredQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val unansweredQuestions: Int,
    val scorePercentage: Double,
    val startedAt: Long = 0L,
    val finishedAt: Long = 0L,
    val questionOutcomes: List<MockTestQuestionOutcome> = emptyList(),
    val performanceRating: MockTestPerformanceRating = MockTestPerformanceRating.fromPercentage(scorePercentage)
) {
    companion object {
        /**
         * Pure deterministic factory method ensuring bounds and safe math.
         */
        fun from(
            configurationId: String,
            totalQuestions: Int,
            answeredQuestions: Int,
            correctAnswers: Int,
            startedAt: Long = 0L,
            finishedAt: Long = 0L,
            questionOutcomes: List<MockTestQuestionOutcome> = emptyList()
        ): MockTestResult {
            val safeTotal = totalQuestions.coerceAtLeast(0)
            val safeAnswered = answeredQuestions.coerceAtLeast(0).coerceAtMost(safeTotal)
            val safeCorrect = correctAnswers.coerceAtLeast(0).coerceAtMost(safeAnswered)
            val incorrect = (safeAnswered - safeCorrect).coerceAtLeast(0)
            val unanswered = (safeTotal - safeAnswered).coerceAtLeast(0)
            val percentage = if (safeTotal > 0) {
                (safeCorrect.toDouble() / safeTotal.toDouble()) * 100.0
            } else {
                0.0
            }

            return MockTestResult(
                configurationId = configurationId,
                totalQuestions = safeTotal,
                answeredQuestions = safeAnswered,
                correctAnswers = safeCorrect,
                incorrectAnswers = incorrect,
                unansweredQuestions = unanswered,
                scorePercentage = percentage,
                startedAt = startedAt,
                finishedAt = finishedAt,
                questionOutcomes = questionOutcomes,
                performanceRating = MockTestPerformanceRating.fromPercentage(percentage)
            )
        }
    }
}
