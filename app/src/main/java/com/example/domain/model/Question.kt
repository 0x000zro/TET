package com.example.domain.model

/**
 * Pure Kotlin domain model representing an educational question.
 *
 * Associated canonically with the syllabus hierarchy at the Subtopic level:
 * Exam -> Paper -> Subject -> Topic -> Subtopic -> Question.
 *
 * Uses stable String IDs.
 */
data class Question(
    val id: String,
    val subtopicId: String,
    val questionText: String,
    val questionType: QuestionType = QuestionType.MCQ_SINGLE,
    val difficulty: QuestionDifficulty = QuestionDifficulty.MEDIUM,
    val explanation: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val options: List<QuestionOption> = emptyList()
)
