package com.example.ui.feature.mocktest.model

import com.example.domain.model.Question
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionOption
import com.example.ui.feature.question.model.QuestionOptionPresentationModel
import com.example.ui.feature.question.model.toPresentationModel

/**
 * Isolated presentation model for an active Mock Test question.
 *
 * CRITICAL SECURITY & CHEATING PREVENTION REQUIREMENT:
 * During an active Mock Test, the active-test UI state must NOT expose:
 * - correct option ID
 * - isCorrect
 * - explanation
 * - correct/incorrect result
 *
 * Only question text and sanitized options (labels A, B, C, D and option text) are provided.
 */
data class MockTestQuestionPresentationModel(
    val id: String,
    val subtopicId: String,
    val questionNumber: Int,
    val questionText: String,
    val difficulty: QuestionDifficulty,
    val options: List<QuestionOptionPresentationModel>
)

/**
 * Maps a domain [Question] into a [MockTestQuestionPresentationModel] for active test taking.
 * Deterministically sorts options by sortOrder ASC, id ASC.
 * Explanations and correct answer indicators are strictly omitted.
 */
fun Question.toMockTestPresentationModel(questionNumber: Int): MockTestQuestionPresentationModel {
    val sortedOptions = this.options.sortedWith(compareBy({ it.sortOrder }, { it.id }))
    return MockTestQuestionPresentationModel(
        id = this.id,
        subtopicId = this.subtopicId,
        questionNumber = questionNumber,
        questionText = this.questionText,
        difficulty = this.difficulty,
        options = sortedOptions.mapIndexed { index, option ->
            option.toPresentationModel(index)
        }
    )
}
