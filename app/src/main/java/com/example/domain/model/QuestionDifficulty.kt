package com.example.domain.model

/**
 * Question difficulty grading levels.
 */
enum class QuestionDifficulty {
    EASY,
    MEDIUM,
    HARD;

    companion object {
        fun fromString(value: String): QuestionDifficulty {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}
