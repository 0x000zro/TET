package com.example.domain.usecase.practice

import com.example.domain.model.Question
import com.example.domain.model.practice.PracticeAnswerResult
import com.example.domain.model.practice.PracticeEvaluationStatus
import com.example.domain.repository.EducationalRepository

/**
 * Controlled outcome of evaluating a student's answer for a practice question.
 */
sealed interface PracticeEvaluationOutcome {
    data class Success(val result: PracticeAnswerResult) : PracticeEvaluationOutcome

    sealed interface Failure : PracticeEvaluationOutcome {
        val errorMessage: String

        data class QuestionNotFound(
            override val errorMessage: String = "Question not found"
        ) : Failure

        data class QuestionInactive(
            override val errorMessage: String = "Question is not currently active"
        ) : Failure

        data class InvalidOption(
            override val errorMessage: String = "Selected option is invalid or does not belong to this question"
        ) : Failure

        data class MalformedQuestion(
            override val errorMessage: String = "Question configuration is invalid: MCQ must have exactly one correct option"
        ) : Failure
    }
}

/**
 * Focused domain use case for evaluating a single question practice submission.
 *
 * Validates:
 * 1. Target question exists.
 * 2. Target question is marked active.
 * 3. Selected option belongs to the target question.
 * 4. MCQ structure validity (has exactly one correct option).
 *
 * Emits controlled [PracticeEvaluationOutcome] results without throwing unhandled exceptions.
 */
class EvaluatePracticeAnswerUseCase(
    private val repository: EducationalRepository
) {

    suspend fun execute(questionId: String, selectedOptionId: String): PracticeEvaluationOutcome {
        val question = repository.getQuestionById(questionId)
            ?: return PracticeEvaluationOutcome.Failure.QuestionNotFound()
        return evaluate(question, selectedOptionId)
    }

    /**
     * Pure in-memory evaluation logic for a loaded [Question].
     * Can be invoked directly during unit and isolated domain tests.
     */
    fun evaluate(question: Question, selectedOptionId: String): PracticeEvaluationOutcome {
        if (!question.isActive) {
            return PracticeEvaluationOutcome.Failure.QuestionInactive()
        }

        if (selectedOptionId.isBlank()) {
            return PracticeEvaluationOutcome.Failure.InvalidOption("Option ID cannot be empty")
        }

        val optionExists = question.options.any { it.id == selectedOptionId }
        if (!optionExists) {
            return PracticeEvaluationOutcome.Failure.InvalidOption()
        }

        val correctOptions = question.options.filter { it.isCorrect }
        if (correctOptions.size != 1) {
            return PracticeEvaluationOutcome.Failure.MalformedQuestion()
        }

        val correctOption = correctOptions.first()
        val isCorrect = selectedOptionId == correctOption.id

        val answerResult = PracticeAnswerResult(
            questionId = question.id,
            selectedOptionId = selectedOptionId,
            correctOptionId = correctOption.id,
            status = if (isCorrect) PracticeEvaluationStatus.CORRECT else PracticeEvaluationStatus.INCORRECT,
            explanation = question.explanation
        )

        return PracticeEvaluationOutcome.Success(answerResult)
    }
}
