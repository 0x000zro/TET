package com.example.domain.model

/**
 * Pure domain model representing a Subtopic enriched with its optional syllabus metadata
 * and its actual local content availability (active questions count).
 *
 * Free from Room, SQLite, and Android dependencies.
 *
 * @property subtopic The core Subtopic hierarchy model.
 * @property metadata Optional pedagogical metadata (learning objectives, notes, duration).
 * @property questionCount Actual number of active questions locally available for this subtopic.
 */
data class SubtopicWithDetails(
    val subtopic: Subtopic,
    val metadata: SyllabusMetadata? = null,
    val questionCount: Int = 0
) {
    val hasQuestions: Boolean
        get() = questionCount > 0

    val hasMetadata: Boolean
        get() = metadata != null && (
            metadata.learningObjective.isNotBlank() ||
            metadata.shortNote.isNotBlank() ||
            metadata.estimatedMinutes > 0
        )
}
