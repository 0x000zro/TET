package com.example.domain.model

/**
 * Supported educational question types.
 * Initially supports single-choice multiple choice questions (MCQ_SINGLE).
 */
enum class QuestionType {
    MCQ_SINGLE;

    companion object {
        fun fromString(value: String): QuestionType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MCQ_SINGLE
        }
    }
}
