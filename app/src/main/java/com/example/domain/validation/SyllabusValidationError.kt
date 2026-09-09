package com.example.domain.validation

/**
 * Structural validation issues identified on a syllabus node or hierarchy relationship.
 */
sealed interface SyllabusValidationError {
    val message: String

    data class EmptyId(
        override val message: String = "Syllabus node ID cannot be blank."
    ) : SyllabusValidationError

    data class EmptyTitle(
        val nodeId: String,
        override val message: String = "Syllabus node '$nodeId' has an empty or blank title."
    ) : SyllabusValidationError

    data class InvalidParentRelationship(
        val nodeId: String,
        val nodeType: String,
        val parentId: String?,
        override val message: String = "Node '$nodeId' of type '$nodeType' has an invalid parent reference: '$parentId'."
    ) : SyllabusValidationError

    data class DuplicateNodeId(
        val duplicateId: String,
        override val message: String = "Duplicate syllabus node ID detected: '$duplicateId'."
    ) : SyllabusValidationError

    data class InvalidSortOrder(
        val nodeId: String,
        val sortOrder: Int,
        override val message: String = "Syllabus node '$nodeId' has negative sort order ($sortOrder). Must be non-negative."
    ) : SyllabusValidationError

    data class OrphanReference(
        val nodeId: String,
        val missingParentId: String,
        override val message: String = "Syllabus node '$nodeId' references non-existent parent '$missingParentId'."
    ) : SyllabusValidationError
}

/**
 * Encapsulates the structured result of syllabus hierarchy validation.
 */
data class SyllabusValidationResult(
    val errors: List<SyllabusValidationError> = emptyList()
) {
    val isValid: Boolean
        get() = errors.isEmpty()
}
