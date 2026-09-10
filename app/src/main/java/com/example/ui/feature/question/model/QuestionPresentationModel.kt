package com.example.ui.feature.question.model

import com.example.domain.model.Question
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionOption

/**
 * UI presentation model for an individual question option.
 *
 * CRITICAL SECURITY & CHEATING PREVENTION REQUIREMENT:
 * This model strictly and deliberately omits the `isCorrect` field from the stored entity/domain model.
 * During standard question presentation and browsing, correct answer keys must never be leaked to the UI layer.
 */
data class QuestionOptionPresentationModel(
    val id: String,
    val questionId: String,
    val label: String,
    val optionText: String,
    val sortOrder: Int
)

/**
 * UI presentation model for a Question.
 *
 * Provides a clean, presentation-ready representation with deterministic option ordering,
 * 1-based question indexing, and no answer-key leaks.
 */
data class QuestionPresentationModel(
    val id: String,
    val subtopicId: String,
    val questionNumber: Int,
    val questionText: String,
    val difficulty: QuestionDifficulty,
    val explanation: String,
    val options: List<QuestionOptionPresentationModel>
)

/**
 * Maps a domain [QuestionOption] into a [QuestionOptionPresentationModel].
 * Determines option label deterministically: index 0 -> "A", index 1 -> "B", index 2 -> "C", index 3 -> "D".
 * The `isCorrect` property is discarded and never exposed in the presentation model.
 */
fun QuestionOption.toPresentationModel(index: Int): QuestionOptionPresentationModel {
    val optionLabel = if (index in 0..25) {
        ('A'.code + index).toChar().toString()
    } else {
        (index + 1).toString()
    }
    return QuestionOptionPresentationModel(
        id = this.id,
        questionId = this.questionId,
        label = optionLabel,
        optionText = this.optionText,
        sortOrder = this.sortOrder
    )
}

/**
 * Maps a domain [Question] into a [QuestionPresentationModel].
 * Options are ordered deterministically by `sortOrder ASC`, `id ASC`.
 */
fun Question.toPresentationModel(questionNumber: Int): QuestionPresentationModel {
    val sortedOptions = this.options.sortedWith(compareBy({ it.sortOrder }, { it.id }))
    return QuestionPresentationModel(
        id = this.id,
        subtopicId = this.subtopicId,
        questionNumber = questionNumber,
        questionText = this.questionText,
        difficulty = this.difficulty,
        explanation = this.explanation,
        options = sortedOptions.mapIndexed { index, option ->
            option.toPresentationModel(index)
        }
    )
}
