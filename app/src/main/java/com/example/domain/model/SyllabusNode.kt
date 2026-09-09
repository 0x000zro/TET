package com.example.domain.model

/**
 * Identifies the structural level of a node in the educational syllabus hierarchy.
 */
enum class SyllabusNodeType {
    EXAM,
    PAPER,
    SUBJECT,
    TOPIC,
    SUBTOPIC
}

/**
 * Pure domain model representing a generic syllabus node within the structured hierarchy:
 * Exam -> Paper -> Subject -> Topic -> Subtopic.
 *
 * Completely independent of Android, Room, and SQLite.
 * Uses stable String IDs for robust references and foreign keys.
 *
 * @property id Stable unique identifier for this syllabus node.
 * @property parentId Stable unique identifier of the parent node, or null if root (EXAM).
 * @property title The primary display title/name of this syllabus node.
 * @property description Descriptive summary or syllabus scope explanation.
 * @property nodeType The structural level ([SyllabusNodeType]) of this node.
 * @property isActive Whether this syllabus node is currently active and visible in navigation.
 * @property sortOrder Deterministic sequence ordering (ascending).
 * @property learningObjective Optional specific learning goal or objective for this syllabus item.
 * @property shortNote Optional brief revision tip, key concept summary, or short reference note.
 */
data class SyllabusNode(
    val id: String,
    val parentId: String?,
    val title: String,
    val description: String = "",
    val nodeType: SyllabusNodeType,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val learningObjective: String = "",
    val shortNote: String = ""
) {
    /**
     * True if this node is at the root level (EXAM level with no parent).
     */
    val isRoot: Boolean
        get() = parentId == null && nodeType == SyllabusNodeType.EXAM
}
