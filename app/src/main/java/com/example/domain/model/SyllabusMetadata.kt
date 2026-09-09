package com.example.domain.model

/**
 * Pure domain model representing optional syllabus metadata associated with a hierarchy node.
 * Contains only genuinely useful educational attributes without speculative bloat.
 *
 * @property nodeId The stable ID of the hierarchy entity (Exam, Paper, Subject, Topic, or Subtopic).
 * @property learningObjective Primary learning outcome or target for this syllabus node.
 * @property shortNote Quick revision summary or conceptual highlight note.
 * @property estimatedMinutes Estimated study duration in minutes (0 if unspecified).
 */
data class SyllabusMetadata(
    val nodeId: String,
    val learningObjective: String = "",
    val shortNote: String = "",
    val estimatedMinutes: Int = 0
)
