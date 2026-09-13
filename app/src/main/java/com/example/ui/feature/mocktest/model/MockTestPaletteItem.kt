package com.example.ui.feature.mocktest.model

import com.example.domain.model.mocktest.MockTestQuestionOutcomeStatus

/**
 * Visual status of a question in the Mock Test Question Palette (Step 19).
 *
 * Guarantees:
 * - Distinguishes Current question, Answered question, and Unanswered question.
 * - Does NOT expose correctness, correct answer, or explanation during active test.
 */
enum class PaletteQuestionStatus {
    CURRENT,
    ANSWERED,
    UNANSWERED
}

/**
 * Presentation model for an individual question entry in the Question Palette.
 *
 * @property index 0-based index of the question within the session.
 * @property questionNumber 1-based display number of the question.
 * @property isCurrent Whether this question is the one currently active/selected.
 * @property isAnswered Whether an answer has been recorded for this question.
 * @property outcomeStatus Outcome status when used in post-test review (Step 22). Strictly null during active test.
 */
data class MockTestPaletteItem(
    val index: Int,
    val questionNumber: Int,
    val isCurrent: Boolean,
    val isAnswered: Boolean,
    val outcomeStatus: MockTestQuestionOutcomeStatus? = null
) {
    /**
     * Primary status indicator distinguishing Current, Answered, and Unanswered states.
     */
    val status: PaletteQuestionStatus
        get() = when {
            isCurrent -> PaletteQuestionStatus.CURRENT
            isAnswered -> PaletteQuestionStatus.ANSWERED
            else -> PaletteQuestionStatus.UNANSWERED
        }
}
