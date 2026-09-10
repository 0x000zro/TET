package com.example.domain.model.practice

/**
 * Pure Kotlin domain model representing the aggregated outcome of a completed multi-question practice session.
 *
 * Encapsulates:
 * - [totalQuestions]: Total number of questions in the practice session
 * - [answeredQuestions]: Count of questions answered by the student
 * - [correctAnswers]: Count of correctly answered questions
 * - [incorrectAnswers]: Count of incorrectly answered questions
 * - [scorePercentage]: Calculated percentage score (0.0 to 100.0)
 *
 * Strictly in-memory; no persistence dependencies.
 */
data class PracticeSessionResult(
    val totalQuestions: Int,
    val answeredQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val scorePercentage: Double
) {
    companion object {
        fun from(
            totalQuestions: Int,
            answeredQuestions: Int,
            correctAnswers: Int
        ): PracticeSessionResult {
            val safeTotal = totalQuestions.coerceAtLeast(0)
            val safeAnswered = answeredQuestions.coerceAtLeast(0).coerceAtMost(safeTotal)
            val safeCorrect = correctAnswers.coerceAtLeast(0).coerceAtMost(safeAnswered)
            val incorrect = (safeAnswered - safeCorrect).coerceAtLeast(0)
            val percentage = if (safeTotal > 0) {
                (safeCorrect.toDouble() / safeTotal.toDouble()) * 100.0
            } else {
                0.0
            }
            return PracticeSessionResult(
                totalQuestions = safeTotal,
                answeredQuestions = safeAnswered,
                correctAnswers = safeCorrect,
                incorrectAnswers = incorrect,
                scorePercentage = percentage
            )
        }
    }
}
