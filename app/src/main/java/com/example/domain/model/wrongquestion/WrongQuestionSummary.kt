package com.example.domain.model.wrongquestion

import com.example.domain.model.Question

/**
 * Composite domain model linking a [WrongQuestion] mistake record with its canonical [Question]
 * and associated [subtopicName] for presentation and review.
 *
 * Adheres strictly to single source of truth:
 * Question content, options, answer key, and explanation remain owned by [Question].
 */
data class WrongQuestionSummary(
    val wrongQuestion: WrongQuestion,
    val question: Question,
    val subtopicName: String
)
