package com.example.domain.model.practice

/**
 * Focused domain model representing one completed practice attempt.
 * Persisted locally on the student's device for local practice history tracking (Step 11).
 *
 * Contains only aggregate metrics required for history and progress tracking.
 * Strictly avoids storing question text or answer keys.
 *
 * @property id Stable unique identifier for the attempt (UUID).
 * @property subtopicId Identifier of the subtopic that was practiced.
 * @property totalQuestions Total number of questions presented in the session.
 * @property answeredQuestions Total number of questions answered by the student.
 * @property correctAnswers Total number of correctly answered questions.
 * @property incorrectAnswers Total number of incorrectly answered questions.
 * @property percentageScore Calculated score percentage (0.0 to 100.0).
 * @property startedAt Timestamp in milliseconds when the practice session began.
 * @property completedAt Timestamp in milliseconds when the practice session finished.
 */
data class PracticeAttempt(
    val id: String,
    val subtopicId: String,
    val totalQuestions: Int,
    val answeredQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int,
    val percentageScore: Double,
    val startedAt: Long,
    val completedAt: Long
) {
    /**
     * Unanswered questions in the session.
     */
    val unansweredQuestions: Int
        get() = (totalQuestions - answeredQuestions).coerceAtLeast(0)
}
