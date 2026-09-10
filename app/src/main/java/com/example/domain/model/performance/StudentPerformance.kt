package com.example.domain.model.performance

import com.example.domain.model.practice.PracticeAttempt

/**
 * Domain model representing aggregated performance metrics for a specific subtopic.
 *
 * @property subtopicId Unique identifier of the subtopic.
 * @property subtopicTitle Human-readable name of the subtopic, if resolved.
 * @property attemptsCount Total number of completed practice attempts for this subtopic.
 * @property questionsAnswered Total questions answered across completed attempts.
 * @property correctAnswers Total questions answered correctly.
 * @property incorrectAnswers Total questions answered incorrectly.
 * @property accuracyPercentage Calculated accuracy percentage (0.0 to 100.0).
 */
data class SubtopicPerformance(
    val subtopicId: String,
    val subtopicTitle: String? = null,
    val attemptsCount: Int,
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val accuracyPercentage: Double
)

/**
 * Domain model representing aggregated student progress and performance
 * derived directly from completed [PracticeAttempt] records.
 *
 * Maintains a single source of truth without redundant database persistence.
 *
 * @property totalCompletedAttempts Total count of completed practice sessions.
 * @property totalQuestionsAttempted Total sum of answered questions across completed attempts.
 * @property totalCorrectAnswers Total sum of correct answers.
 * @property totalIncorrectAnswers Total sum of incorrect answers.
 * @property overallAccuracyPercentage Overall accuracy percentage (0.0 to 100.0).
 * @property recentAttempts Ordered recent practice attempts (newest completed first).
 * @property subtopicPerformances Subtopic breakdown ordered deterministically.
 */
data class StudentPerformance(
    val totalCompletedAttempts: Int,
    val totalQuestionsAttempted: Int,
    val totalCorrectAnswers: Int,
    val totalIncorrectAnswers: Int,
    val overallAccuracyPercentage: Double,
    val recentAttempts: List<PracticeAttempt> = emptyList(),
    val subtopicPerformances: List<SubtopicPerformance> = emptyList()
) {
    companion object {
        val EMPTY = StudentPerformance(
            totalCompletedAttempts = 0,
            totalQuestionsAttempted = 0,
            totalCorrectAnswers = 0,
            totalIncorrectAnswers = 0,
            overallAccuracyPercentage = 0.0,
            recentAttempts = emptyList(),
            subtopicPerformances = emptyList()
        )
    }
}
