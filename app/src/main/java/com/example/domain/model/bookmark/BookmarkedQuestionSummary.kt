package com.example.domain.model.bookmark

import com.example.domain.model.Question

/**
 * Composite domain model linking a [BookmarkedQuestion] bookmark record with its canonical [Question]
 * and associated [subtopicName] for presentation, listing, and revision (Step 14).
 *
 * Adheres strictly to single source of truth:
 * Question content, options, answer key, and explanation remain owned by [Question].
 */
data class BookmarkedQuestionSummary(
    val bookmarkedQuestion: BookmarkedQuestion,
    val question: Question,
    val subtopicName: String
)
