package com.example.domain.validation

import com.example.data.local.seed.GenericSyllabusFixture
import com.example.domain.model.SyllabusNode
import com.example.domain.model.SyllabusNodeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure domain unit tests for SyllabusValidator.
 * Tests structural validation, missing ID, invalid hierarchy relations,
 * duplicate IDs, and orphan references without Android dependencies.
 */
class SyllabusValidatorTest {

    @Test
    fun validFixtureHierarchy_passesValidation() {
        val result = SyllabusValidator.validateHierarchy(GenericSyllabusFixture.fixtureSyllabusNodes)
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun emptyNodeId_failsValidation() {
        val invalidNode = SyllabusNode(
            id = "",
            parentId = null,
            title = "Invalid Node",
            nodeType = SyllabusNodeType.EXAM
        )
        val result = SyllabusValidator.validateNode(invalidNode)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is SyllabusValidationError.EmptyId })
    }

    @Test
    fun emptyNodeTitle_failsValidation() {
        val invalidNode = SyllabusNode(
            id = "node_01",
            parentId = null,
            title = "   ",
            nodeType = SyllabusNodeType.EXAM
        )
        val result = SyllabusValidator.validateNode(invalidNode)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is SyllabusValidationError.EmptyTitle })
    }

    @Test
    fun rootExamWithParent_failsValidation() {
        val invalidNode = SyllabusNode(
            id = "exam_01",
            parentId = "some_parent",
            title = "Exam with illegal parent",
            nodeType = SyllabusNodeType.EXAM
        )
        val result = SyllabusValidator.validateNode(invalidNode)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is SyllabusValidationError.InvalidParentRelationship })
    }

    @Test
    fun childNodeWithoutParent_failsValidation() {
        val invalidPaper = SyllabusNode(
            id = "paper_01",
            parentId = null,
            title = "Paper with missing parent",
            nodeType = SyllabusNodeType.PAPER
        )
        val result = SyllabusValidator.validateNode(invalidPaper)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is SyllabusValidationError.InvalidParentRelationship })
    }

    @Test
    fun negativeSortOrder_failsValidation() {
        val invalidNode = SyllabusNode(
            id = "node_negative",
            parentId = null,
            title = "Node with negative sort order",
            nodeType = SyllabusNodeType.EXAM,
            sortOrder = -5
        )
        val result = SyllabusValidator.validateNode(invalidNode)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is SyllabusValidationError.InvalidSortOrder })
    }

    @Test
    fun duplicateNodeId_failsValidation() {
        val duplicateNodes = listOf(
            SyllabusNode(
                id = "duplicate_id",
                parentId = null,
                title = "Exam 1",
                nodeType = SyllabusNodeType.EXAM
            ),
            SyllabusNode(
                id = "duplicate_id",
                parentId = null,
                title = "Exam 2",
                nodeType = SyllabusNodeType.EXAM
            )
        )
        val result = SyllabusValidator.validateHierarchy(duplicateNodes)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is SyllabusValidationError.DuplicateNodeId })
    }

    @Test
    fun orphanParentReference_failsValidation() {
        val nodesWithOrphan = listOf(
            SyllabusNode(
                id = "paper_orphan",
                parentId = "non_existent_exam_id",
                title = "Orphan Paper",
                nodeType = SyllabusNodeType.PAPER
            )
        )
        val result = SyllabusValidator.validateHierarchy(nodesWithOrphan)
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is SyllabusValidationError.OrphanReference })
    }
}
