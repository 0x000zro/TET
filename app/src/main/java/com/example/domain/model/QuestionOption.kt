package com.example.domain.model

/**
 * Pure Kotlin domain model representing an option for a multiple choice question.
 *
 * Characteristics:
 * - Stable String option IDs
 * - References parent Question via questionId
 * - Deterministic ordering via sortOrder
 * - Correctness indication via isCorrect
 */
data class QuestionOption(
    val id: String,
    val questionId: String,
    val optionText: String,
    val sortOrder: Int = 0,
    val isCorrect: Boolean = false
)
