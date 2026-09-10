package com.example.domain.model.wrongquestion

/**
 * Pure Kotlin domain model representing a student's mistake record for a question.
 *
 * Encapsulates:
 * - [questionId]: Stable question ID linking directly to the canonical question
 * - [subtopicId]: Identifier of the subtopic this question belongs to
 * - [firstWrongAt]: Timestamp (ms) when this question was first answered incorrectly
 * - [lastWrongAt]: Timestamp (ms) when this question was most recently answered incorrectly
 * - [wrongCount]: Number of times this question has been answered incorrectly
 * - [lastAttemptId]: ID of the practice attempt where the most recent mistake occurred (if known)
 *
 * CRITICAL SINGLE SOURCE OF TRUTH RULE:
 * This model strictly does NOT duplicate question text, option text, correct answer, or explanation.
 * The canonical Question entity remains the single source of truth for all educational content.
 */
data class WrongQuestion(
    val questionId: String,
    val subtopicId: String,
    val firstWrongAt: Long,
    val lastWrongAt: Long,
    val wrongCount: Int,
    val lastAttemptId: String? = null
)
