package com.example.domain.usecase.performance

import com.example.domain.model.performance.StudentPerformance
import com.example.domain.model.performance.SubtopicPerformance
import com.example.domain.model.practice.PracticeAttempt

/**
 * Pure domain calculation use case that derives student performance metrics
 * from a list of completed [PracticeAttempt] records.
 *
 * Rules:
 * - totalCompletedAttempts = count of completed attempts
 * - totalQuestionsAttempted = sum of answered questions from completed attempts
 * - correctAnswers = sum of correct answers
 * - incorrectAnswers = sum of incorrect answers
 * - accuracy = correct answers / answered questions * 100.0 (safe division, zero-attempt and zero-answered return 0.0)
 * - Deterministic ordering for recent attempts: completedAt DESC, then id DESC
 * - Deterministic ordering for subtopics: attemptsCount DESC, then questionsAnswered DESC, then subtopicId ASC
 */
class CalculateStudentPerformanceUseCase {

    operator fun invoke(
        attempts: List<PracticeAttempt>,
        subtopicTitles: Map<String, String> = emptyMap()
    ): StudentPerformance {
        if (attempts.isEmpty()) {
            return StudentPerformance.EMPTY
        }

        val totalCompletedAttempts = attempts.size
        val totalQuestionsAttempted = attempts.sumOf { it.answeredQuestions }
        val totalCorrectAnswers = attempts.sumOf { it.correctAnswers }
        val totalIncorrectAnswers = attempts.sumOf { it.incorrectAnswers }

        val overallAccuracyPercentage = if (totalQuestionsAttempted > 0) {
            (totalCorrectAnswers.toDouble() / totalQuestionsAttempted.toDouble()) * 100.0
        } else {
            0.0
        }

        val recentAttempts = attempts.sortedWith(
            compareByDescending<PracticeAttempt> { it.completedAt }
                .thenByDescending { it.id }
        ).take(10)

        val subtopicGroups = attempts.groupBy { it.subtopicId }
        val subtopicPerformances = subtopicGroups.map { (subtopicId, groupAttempts) ->
            val attemptsCount = groupAttempts.size
            val questionsAnswered = groupAttempts.sumOf { it.answeredQuestions }
            val correct = groupAttempts.sumOf { it.correctAnswers }
            val incorrect = groupAttempts.sumOf { it.incorrectAnswers }
            val accuracy = if (questionsAnswered > 0) {
                (correct.toDouble() / questionsAnswered.toDouble()) * 100.0
            } else {
                0.0
            }

            SubtopicPerformance(
                subtopicId = subtopicId,
                subtopicTitle = subtopicTitles[subtopicId],
                attemptsCount = attemptsCount,
                questionsAnswered = questionsAnswered,
                correctAnswers = correct,
                incorrectAnswers = incorrect,
                accuracyPercentage = accuracy
            )
        }.sortedWith(
            compareByDescending<SubtopicPerformance> { it.attemptsCount }
                .thenByDescending { it.questionsAnswered }
                .thenBy { it.subtopicId }
        )

        return StudentPerformance(
            totalCompletedAttempts = totalCompletedAttempts,
            totalQuestionsAttempted = totalQuestionsAttempted,
            totalCorrectAnswers = totalCorrectAnswers,
            totalIncorrectAnswers = totalIncorrectAnswers,
            overallAccuracyPercentage = overallAccuracyPercentage,
            recentAttempts = recentAttempts,
            subtopicPerformances = subtopicPerformances
        )
    }
}
