package com.example.ui.feature.practice

import com.example.domain.model.practice.PracticeAnswerResult
import com.example.ui.feature.question.model.QuestionPresentationModel

/**
 * UI State representation for the Single Question Practice screen.
 */
sealed interface PracticeUiState {

    /**
     * Practice question is being fetched and prepared.
     */
    data object Loading : PracticeUiState

    /**
     * Practice question is ready for interactive single-choice answering.
     * Options are neutral and no correctness metadata is present.
     */
    data class Ready(
        val question: QuestionPresentationModel,
        val selectedOptionId: String? = null
    ) : PracticeUiState {
        val canSubmit: Boolean get() = !selectedOptionId.isNullOrBlank()
    }

    /**
     * The student has submitted an answer for this question.
     * This state is strictly frozen and immutable (selection cannot be changed, no resubmission).
     * Exposes feedback, reveals the correct option, and displays explanation when available.
     */
    data class Submitted(
        val question: QuestionPresentationModel,
        val selectedOptionId: String,
        val result: PracticeAnswerResult
    ) : PracticeUiState

    /**
     * Target question was not found in the database or is marked inactive.
     */
    data object NotFound : PracticeUiState

    /**
     * An error occurred while retrieving or evaluating the practice question.
     */
    data class Error(val message: String) : PracticeUiState
}
