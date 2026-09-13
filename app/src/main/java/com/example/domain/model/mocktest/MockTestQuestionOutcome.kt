package com.example.domain.model.mocktest

/**
 * Outcome status for an individual question in a completed Mock Test (Step 21).
 * Strictly omits correct answer keys, options, and explanations.
 */
enum class MockTestQuestionOutcomeStatus {
    CORRECT,
    INCORRECT,
    UNANSWERED
}

/**
 * Question outcome metadata for results summary display.
 */
data class MockTestQuestionOutcome(
    val questionNumber: Int,
    val questionId: String,
    val status: MockTestQuestionOutcomeStatus
)
