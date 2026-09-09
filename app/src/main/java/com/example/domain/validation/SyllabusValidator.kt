package com.example.domain.validation

import com.example.domain.model.SyllabusNode
import com.example.domain.model.SyllabusNodeType

/**
 * Lightweight structural validator for syllabus nodes and trees.
 * Catches structural defects, missing identifiers, improper parent references,
 * negative sort orders, duplicate IDs, and orphan references gracefully.
 */
object SyllabusValidator {

    /**
     * Validates an individual syllabus node's intrinsic properties.
     */
    fun validateNode(node: SyllabusNode): SyllabusValidationResult {
        val errors = mutableListOf<SyllabusValidationError>()

        if (node.id.isBlank()) {
            errors.add(SyllabusValidationError.EmptyId())
        }

        if (node.title.isBlank()) {
            errors.add(SyllabusValidationError.EmptyTitle(node.id))
        }

        if (node.sortOrder < 0) {
            errors.add(SyllabusValidationError.InvalidSortOrder(node.id, node.sortOrder))
        }

        // Structural parent rule checks:
        when (node.nodeType) {
            SyllabusNodeType.EXAM -> {
                if (node.parentId != null) {
                    errors.add(
                        SyllabusValidationError.InvalidParentRelationship(
                            nodeId = node.id,
                            nodeType = node.nodeType.name,
                            parentId = node.parentId,
                            message = "Root Exam node '${node.id}' must not have a parent ID."
                        )
                    )
                }
            }
            SyllabusNodeType.PAPER,
            SyllabusNodeType.SUBJECT,
            SyllabusNodeType.TOPIC,
            SyllabusNodeType.SUBTOPIC -> {
                if (node.parentId.isNullOrBlank()) {
                    errors.add(
                        SyllabusValidationError.InvalidParentRelationship(
                            nodeId = node.id,
                            nodeType = node.nodeType.name,
                            parentId = node.parentId,
                            message = "Child node '${node.id}' of type '${node.nodeType.name}' requires a non-blank parent ID."
                        )
                    )
                }
            }
        }

        return SyllabusValidationResult(errors)
    }

    /**
     * Validates a collection of syllabus nodes representing a hierarchy set.
     * Verifies individual validity, uniqueness of IDs, and absence of orphan parent references.
     */
    fun validateHierarchy(nodes: List<SyllabusNode>): SyllabusValidationResult {
        val errors = mutableListOf<SyllabusValidationError>()

        // 1. Validate individual nodes
        for (node in nodes) {
            errors.addAll(validateNode(node).errors)
        }

        // 2. Check for duplicate IDs
        val seenIds = mutableSetOf<String>()
        for (node in nodes) {
            if (node.id.isNotBlank()) {
                if (!seenIds.add(node.id)) {
                    errors.add(SyllabusValidationError.DuplicateNodeId(node.id))
                }
            }
        }

        // 3. Check for orphan references (except for EXAM which has null parent)
        val validIds = nodes.map { it.id }.toSet()
        for (node in nodes) {
            val parent = node.parentId
            if (node.nodeType != SyllabusNodeType.EXAM && !parent.isNullOrBlank()) {
                if (!validIds.contains(parent)) {
                    errors.add(
                        SyllabusValidationError.OrphanReference(
                            nodeId = node.id,
                            missingParentId = parent
                        )
                    )
                }
            }
        }

        return SyllabusValidationResult(errors)
    }
}
