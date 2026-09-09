package com.example.domain.model

/**
 * Represents the future modular components of the educational platform.
 * These declare the modular boundaries without implementing the features in Step 1.
 */
enum class EducationalModuleId {
    HOME,
    EXAMS,
    SYLLABUS,
    PRACTICE,
    MOCK_TESTS,
    PREVIOUS_YEAR_QUESTIONS,
    EBOOKS,
    VIDEOS,
    BOOKMARKS,
    WRONG_QUESTIONS,
    PERFORMANCE,
    SETTINGS
}

data class EducationalModule(
    val id: EducationalModuleId,
    val title: String,
    val category: String,
    val description: String,
    val isReadyForImplementation: Boolean = true
)
