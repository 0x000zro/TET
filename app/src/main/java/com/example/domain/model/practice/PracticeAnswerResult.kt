package com.example.domain.model.practice

/**
 * Semantic status for evaluating a student's answer in practice mode.
 */
enum class PracticeEvaluationStatus {
    CORRECT,
    INCORRECT
}

/**
 * Pure Kotlin domain model representing the evaluation result of a single practice question answer.
 *
 * Contains strictly the fields required by the practice UI:
 * - [questionId]: ID of the question practiced
 * - [selectedOptionId]: ID of the option selected by the student
 * - [correctOptionId]: ID of the stored correct option (revealed post-submission)
 * - [status]: Semantic correctness evaluation status
 * - [explanation]: Explanatory text for the solution (empty string if none)
 *
 * Strictly omits speculative analytics, tracking, or persistence metrics.
 */
data class PracticeAnswerResult(
    val questionId: String,
    val selectedOptionId: String,
    val correctOptionId: String,
    val status: PracticeEvaluationStatus,
    val explanation: String = ""
)
