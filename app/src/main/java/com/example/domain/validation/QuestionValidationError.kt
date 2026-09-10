package com.example.domain.validation

/**
 * Structured validation errors for Question and QuestionOption domain models.
 */
sealed interface QuestionValidationError {
    val message: String

    data class EmptyQuestionId(
        override val message: String = "Question ID cannot be blank."
    ) : QuestionValidationError

    data class EmptyQuestionText(
        val questionId: String,
        override val message: String = "Question '$questionId' has an empty or blank question text."
    ) : QuestionValidationError

    data class InvalidSubtopicId(
        val questionId: String,
        val subtopicId: String,
        override val message: String = "Question '$questionId' has an invalid or blank subtopic reference: '$subtopicId'."
    ) : QuestionValidationError

    data class InvalidSortOrder(
        val questionId: String,
        val sortOrder: Int,
        override val message: String = "Question '$questionId' has negative sort order ($sortOrder). Must be non-negative."
    ) : QuestionValidationError

    data class InsufficientOptions(
        val questionId: String,
        val count: Int,
        override val message: String = "Question '$questionId' requires at least 2 options, but has $count."
    ) : QuestionValidationError

    data class InvalidCorrectOptionCount(
        val questionId: String,
        val correctCount: Int,
        override val message: String = "Single-choice MCQ '$questionId' must have exactly 1 correct option, but found $correctCount."
    ) : QuestionValidationError

    data class DuplicateOptionId(
        val questionId: String,
        val optionId: String,
        override val message: String = "Question '$questionId' has duplicate option ID: '$optionId'."
    ) : QuestionValidationError

    data class EmptyOptionText(
        val questionId: String,
        val optionId: String,
        override val message: String = "Option '$optionId' on question '$questionId' has empty or blank text."
    ) : QuestionValidationError

    data class InvalidOptionSortOrder(
        val questionId: String,
        val optionId: String,
        val sortOrder: Int,
        override val message: String = "Option '$optionId' on question '$questionId' has negative sort order ($sortOrder)."
    ) : QuestionValidationError

    data class OrphanOptionReference(
        val questionId: String,
        val optionId: String,
        val optionQuestionId: String,
        override val message: String = "Option '$optionId' references question '$optionQuestionId', but expected '$questionId'."
    ) : QuestionValidationError

    data class DuplicateQuestionId(
        val duplicateId: String,
        override val message: String = "Duplicate question ID detected: '$duplicateId'."
    ) : QuestionValidationError
}

/**
 * Encapsulates the structured result of question validation.
 */
data class QuestionValidationResult(
    val errors: List<QuestionValidationError> = emptyList()
) {
    val isValid: Boolean
        get() = errors.isEmpty()
}
