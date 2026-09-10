package com.example.domain.model.bookmark

/**
 * Pure Kotlin domain model representing a student's saved bookmark metadata for a question (Step 14).
 *
 * Encapsulates:
 * - [questionId]: Stable question ID linking directly to the canonical question
 * - [subtopicId]: Identifier of the subtopic this question belongs to
 * - [bookmarkedAt]: Timestamp (ms) when the question was bookmarked
 *
 * CRITICAL SINGLE SOURCE OF TRUTH RULE:
 * This model strictly does NOT duplicate question text, option text, correct answer, or explanation.
 * The canonical Question entity remains the single source of truth for all educational content.
 */
data class BookmarkedQuestion(
    val questionId: String,
    val subtopicId: String,
    val bookmarkedAt: Long
)
