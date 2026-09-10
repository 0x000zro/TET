package com.example.ui.feature.practice

import com.example.domain.model.practice.PracticeAnswerResult
import com.example.domain.model.practice.PracticeSession
import com.example.domain.model.practice.PracticeSessionResult
import com.example.ui.feature.question.model.QuestionPresentationModel

/**
 * UI state container for multi-question practice session (Step 10).
 */
sealed interface PracticeSessionUiState {

    /**
     * Questions for the practice session are being retrieved.
     */
    data object Loading : PracticeSessionUiState

    /**
     * No active questions found for the selected subtopic.
     */
    data object Empty : PracticeSessionUiState

    /**
     * A failure occurred during session loading or answer evaluation.
     */
    data class Error(val message: String) : PracticeSessionUiState

    /**
     * An interactive question is actively displayed in the practice session.
     * Before submission, [result] is null and [question] uses [QuestionPresentationModel]
     * which strictly excludes the correct answer key.
     */
    data class ActiveQuestion(
        val session: PracticeSession,
        val question: QuestionPresentationModel,
        val currentQuestionIndex: Int,
        val totalQuestions: Int,
        val selectedOptionId: String? = null,
        val isSubmitted: Boolean = false,
        val isLastQuestion: Boolean = false,
        val result: PracticeAnswerResult? = null,
        val canSubmit: Boolean = false
    ) : PracticeSessionUiState

    /**
     * The practice session has ended and aggregated final results are available.
     */
    data class Completed(
        val session: PracticeSession,
        val result: PracticeSessionResult
    ) : PracticeSessionUiState
}
