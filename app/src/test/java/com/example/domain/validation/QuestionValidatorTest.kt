package com.example.domain.validation

import com.example.domain.model.Question
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionOption
import com.example.domain.model.QuestionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionValidatorTest {

    private fun createValidQuestion(
        id: String = "q1",
        subtopicId: String = "sub_01",
        text: String = "What is the capital of France?",
        optionsCount: Int = 4,
        correctIndex: Int = 0
    ): Question {
        val options = (0 until optionsCount).map { i ->
            QuestionOption(
                id = "${id}_opt_$i",
                questionId = id,
                optionText = "Option $i text",
                sortOrder = i,
                isCorrect = (i == correctIndex)
            )
        }
        return Question(
            id = id,
            subtopicId = subtopicId,
            questionText = text,
            questionType = QuestionType.MCQ_SINGLE,
            difficulty = QuestionDifficulty.MEDIUM,
            explanation = "Paris is the capital of France.",
            isActive = true,
            sortOrder = 1,
            options = options
        )
    }

    @Test
    fun validQuestion_passesValidation() {
        val question = createValidQuestion()
        val result = QuestionValidator.validateQuestion(question, setOf("sub_01"))

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun emptyQuestionId_failsValidation() {
        val question = createValidQuestion(id = "")
        val result = QuestionValidator.validateQuestion(question)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is QuestionValidationError.EmptyQuestionId })
    }

    @Test
    fun emptyQuestionText_failsValidation() {
        val question = createValidQuestion(text = "   ")
        val result = QuestionValidator.validateQuestion(question)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is QuestionValidationError.EmptyQuestionText })
    }

    @Test
    fun invalidSubtopicId_blankOrNotInAllowedSet_failsValidation() {
        val questionBlank = createValidQuestion(subtopicId = "")
        val resultBlank = QuestionValidator.validateQuestion(questionBlank)
        assertFalse(resultBlank.isValid)
        assertTrue(resultBlank.errors.any { it is QuestionValidationError.InvalidSubtopicId })

        val questionUnlisted = createValidQuestion(subtopicId = "unknown_subtopic")
        val resultUnlisted = QuestionValidator.validateQuestion(questionUnlisted, setOf("sub_01", "sub_02"))
        assertFalse(resultUnlisted.isValid)
        assertTrue(resultUnlisted.errors.any { it is QuestionValidationError.InvalidSubtopicId })
    }

    @Test
    fun negativeSortOrder_failsValidation() {
        val question = createValidQuestion().copy(sortOrder = -5)
        val result = QuestionValidator.validateQuestion(question)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is QuestionValidationError.InvalidSortOrder })
    }

    @Test
    fun mcqWithLessThanTwoOptions_failsValidation() {
        val questionOneOption = createValidQuestion(optionsCount = 1, correctIndex = 0)
        val resultOne = QuestionValidator.validateQuestion(questionOneOption)
        assertFalse(resultOne.isValid)
        assertTrue(resultOne.errors.any { it is QuestionValidationError.InsufficientOptions })

        val questionZeroOptions = createValidQuestion(optionsCount = 0, correctIndex = -1)
        val resultZero = QuestionValidator.validateQuestion(questionZeroOptions)
        assertFalse(resultZero.isValid)
        assertTrue(resultZero.errors.any { it is QuestionValidationError.InsufficientOptions })
    }

    @Test
    fun mcqWithZeroCorrectOptions_failsValidation() {
        val options = listOf(
            QuestionOption("opt_1", "q1", "Option 1", 1, false),
            QuestionOption("opt_2", "q1", "Option 2", 2, false)
        )
        val question = createValidQuestion().copy(options = options)
        val result = QuestionValidator.validateQuestion(question)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is QuestionValidationError.InvalidCorrectOptionCount })
    }

    @Test
    fun mcqWithMultipleCorrectOptions_failsValidation() {
        val options = listOf(
            QuestionOption("opt_1", "q1", "Option 1", 1, true),
            QuestionOption("opt_2", "q1", "Option 2", 2, true)
        )
        val question = createValidQuestion().copy(options = options)
        val result = QuestionValidator.validateQuestion(question)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is QuestionValidationError.InvalidCorrectOptionCount })
    }

    @Test
    fun duplicateOptionIds_failsValidation() {
        val options = listOf(
            QuestionOption("opt_same", "q1", "Option 1", 1, true),
            QuestionOption("opt_same", "q1", "Option 2", 2, false)
        )
        val question = createValidQuestion().copy(options = options)
        val result = QuestionValidator.validateQuestion(question)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is QuestionValidationError.DuplicateOptionId })
    }

    @Test
    fun emptyOptionText_failsValidation() {
        val options = listOf(
            QuestionOption("opt_1", "q1", "Option 1", 1, true),
            QuestionOption("opt_2", "q1", "  ", 2, false)
        )
        val question = createValidQuestion().copy(options = options)
        val result = QuestionValidator.validateQuestion(question)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is QuestionValidationError.EmptyOptionText })
    }

    @Test
    fun negativeOptionSortOrder_failsValidation() {
        val options = listOf(
            QuestionOption("opt_1", "q1", "Option 1", -1, true),
            QuestionOption("opt_2", "q1", "Option 2", 2, false)
        )
        val question = createValidQuestion().copy(options = options)
        val result = QuestionValidator.validateQuestion(question)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is QuestionValidationError.InvalidOptionSortOrder })
    }

    @Test
    fun orphanOptionReference_failsValidation() {
        val options = listOf(
            QuestionOption("opt_1", "other_q", "Option 1", 1, true),
            QuestionOption("opt_2", "q1", "Option 2", 2, false)
        )
        val question = createValidQuestion(id = "q1").copy(options = options)
        val result = QuestionValidator.validateQuestion(question)

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is QuestionValidationError.OrphanOptionReference })
    }

    @Test
    fun duplicateQuestionIdInBatch_failsValidation() {
        val q1 = createValidQuestion(id = "q_duplicate")
        val q2 = createValidQuestion(id = "q_duplicate")
        val result = QuestionValidator.validateQuestions(listOf(q1, q2))

        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is QuestionValidationError.DuplicateQuestionId })
    }
}
