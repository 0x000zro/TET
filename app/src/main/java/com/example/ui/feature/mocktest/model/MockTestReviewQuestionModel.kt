package com.example.ui.feature.mocktest.model

import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.mocktest.MockTestQuestionOutcomeStatus

/**
 * Presentation model for an individual option in post-test detailed question review (Step 22).
 * Strictly read-only and available only after test completion.
 */
data class MockTestReviewOptionModel(
    val id: String,
    val label: String,
    val text: String,
    val isCorrect: Boolean,
    val isSelected: Boolean
)

/**
 * Presentation model for an individual question in post-test detailed question review (Step 22).
 *
 * Guarantees:
 * - Read-only: No answer selection, submission, or mutation capability.
 * - Answer key availability: Displays correct answer and outcome status ONLY after test completion.
 * - Explanations: Safely displayed from canonical Question model if present.
 * - Order: Preserves exact original session question order.
 */
data class MockTestReviewQuestionModel(
    val questionId: String,
    val questionNumber: Int,
    val questionText: String,
    val difficulty: QuestionDifficulty,
    val status: MockTestQuestionOutcomeStatus,
    val selectedOptionId: String?,
    val selectedOptionLabel: String?,
    val correctOptionId: String,
    val correctOptionLabel: String,
    val options: List<MockTestReviewOptionModel>,
    val explanation: String = ""
)
