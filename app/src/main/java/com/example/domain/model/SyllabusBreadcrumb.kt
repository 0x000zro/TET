package com.example.domain.model

/**
 * Pure domain model representing the complete ancestral breadcrumb path
 * for a syllabus node, enabling intuitive hierarchy navigation.
 */
data class SyllabusBreadcrumb(
    val exam: SyllabusNode? = null,
    val paper: SyllabusNode? = null,
    val subject: SyllabusNode? = null,
    val topic: SyllabusNode? = null,
    val subtopic: SyllabusNode? = null
) {
    /**
     * Complete list of ordered breadcrumb segments from root to current node.
     */
    val segments: List<SyllabusNode>
        get() = listOfNotNull(exam, paper, subject, topic, subtopic)

    /**
     * Formatted human-readable trail, e.g. "NET > Paper 1 > Teaching Aptitude".
     */
    val formattedTrail: String
        get() = segments.joinToString(separator = " > ") { it.title }
}
