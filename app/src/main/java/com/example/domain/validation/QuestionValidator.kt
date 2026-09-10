package com.example.domain.validation

import com.example.domain.model.Question
import com.example.domain.model.QuestionType

/**
 * Pure domain validator for Questions and Options.
 *
 * Validates:
 * - Question: non-empty ID, non-empty text, valid question type, valid subtopic, non-negative sort order.
 * - MCQ: at least 2 options, exactly 1 correct option, unique option IDs, non-empty option text, non-negative sort order.
 */
object QuestionValidator {

    fun validateQuestion(
        question: Question,
        validSubtopicIds: Set<String>? = null
    ): QuestionValidationResult {
        val errors = mutableListOf<QuestionValidationError>()

        if (question.id.isBlank()) {
            errors.add(QuestionValidationError.EmptyQuestionId())
        }

        if (question.questionText.isBlank()) {
            errors.add(QuestionValidationError.EmptyQuestionText(question.id))
        }

        if (question.subtopicId.isBlank()) {
            errors.add(QuestionValidationError.InvalidSubtopicId(question.id, question.subtopicId))
        } else if (validSubtopicIds != null && !validSubtopicIds.contains(question.subtopicId)) {
            errors.add(QuestionValidationError.InvalidSubtopicId(question.id, question.subtopicId))
        }

        if (question.sortOrder < 0) {
            errors.add(QuestionValidationError.InvalidSortOrder(question.id, question.sortOrder))
        }

        when (question.questionType) {
            QuestionType.MCQ_SINGLE -> {
                validateSingleChoiceMcqOptions(question, errors)
            }
        }

        return QuestionValidationResult(errors)
    }

    private fun validateSingleChoiceMcqOptions(
        question: Question,
        errors: MutableList<QuestionValidationError>
    ) {
        val options = question.options

        // 1. At least 2 options
        if (options.size < 2) {
            errors.add(QuestionValidationError.InsufficientOptions(question.id, options.size))
        }

        // 2. Exactly one correct option
        val correctCount = options.count { it.isCorrect }
        if (correctCount != 1) {
            errors.add(QuestionValidationError.InvalidCorrectOptionCount(question.id, correctCount))
        }

        // 3. Option IDs are unique
        val seenOptionIds = mutableSetOf<String>()
        for (option in options) {
            if (option.id.isNotBlank()) {
                if (!seenOptionIds.add(option.id)) {
                    errors.add(QuestionValidationError.DuplicateOptionId(question.id, option.id))
                }
            }
        }

        // 4. Option text is not empty and ordering is non-negative
        for (option in options) {
            if (option.optionText.isBlank()) {
                errors.add(QuestionValidationError.EmptyOptionText(question.id, option.id))
            }
            if (option.sortOrder < 0) {
                errors.add(
                    QuestionValidationError.InvalidOptionSortOrder(
                        question.id,
                        option.id,
                        option.sortOrder
                    )
                )
            }
            if (question.id.isNotBlank() && option.questionId.isNotBlank() && option.questionId != question.id) {
                errors.add(
                    QuestionValidationError.OrphanOptionReference(
                        questionId = question.id,
                        optionId = option.id,
                        optionQuestionId = option.questionId
                    )
                )
            }
        }
    }

    fun validateQuestions(
        questions: List<Question>,
        validSubtopicIds: Set<String>? = null
    ): QuestionValidationResult {
        val errors = mutableListOf<QuestionValidationError>()
        val seenQuestionIds = mutableSetOf<String>()

        for (question in questions) {
            errors.addAll(validateQuestion(question, validSubtopicIds).errors)
            if (question.id.isNotBlank()) {
                if (!seenQuestionIds.add(question.id)) {
                    errors.add(QuestionValidationError.DuplicateQuestionId(question.id))
                }
            }
        }

        return QuestionValidationResult(errors)
    }
}
