package com.example.domain.model.pyq

import com.example.domain.model.Question

/**
 * Composite domain presentation model linking a [PreviousYearQuestion] metadata record with
 * its canonical [Question] and associated educational hierarchy display names (Step 15).
 *
 * Adheres strictly to single source of truth:
 * Question content, options, answer key, and explanation remain owned solely by [Question].
 */
data class PreviousYearQuestionSummary(
    val pyq: PreviousYearQuestion,
    val question: Question,
    val examName: String? = null,
    val paperName: String? = null,
    val subjectName: String? = null,
    val subtopicName: String? = null
)
