package com.example.domain.model.mocktest

/**
 * Performance rating category derived deterministically from the percentage score (Step 21).
 */
enum class MockTestPerformanceRating(val label: String) {
    EXCELLENT("Excellent"),
    GOOD("Good"),
    NEEDS_IMPROVEMENT("Needs Improvement");

    companion object {
        fun fromPercentage(percentage: Double): MockTestPerformanceRating {
            return when {
                percentage >= 75.0 -> EXCELLENT
                percentage >= 50.0 -> GOOD
                else -> NEEDS_IMPROVEMENT
            }
        }
    }
}

/**
 * In-memory domain model representing the recorded outcome of a completed Mock Test (Step 21).
 *
 * Requirements:
 * - test/session id
 * - exam/paper/scope
 * - total questions
 * - answered
 * - correct
 * - incorrect
 * - unanswered
 * - percentage
 * - startedAt
 * - completedAt
 * - time used
 *
 * Logically and architecturally separate from [com.example.domain.model.PracticeAttempt].
 */
data class MockTestPerformance(
    val sessionId: String,
    val configurationId: String,
    val examId: String,
    val paperId: String,
    val scopeDescription: String,
    val totalQuestions: Int,
    val answered: Int,
    val correct: Int,
    val incorrect: Int,
    val unanswered: Int,
    val percentage: Double,
    val startedAt: Long,
    val completedAt: Long,
    val timeUsedSeconds: Long,
    val rating: MockTestPerformanceRating = MockTestPerformanceRating.fromPercentage(percentage)
)
