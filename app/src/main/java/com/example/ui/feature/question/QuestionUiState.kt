package com.example.ui.feature.question

import com.example.ui.feature.question.model.QuestionPresentationModel

/**
 * UI State container for the Question List browsing view.
 */
sealed interface QuestionListUiState {
    data object Loading : QuestionListUiState
    data class Success(val questions: List<QuestionPresentationModel>) : QuestionListUiState
    data object Empty : QuestionListUiState
    data class Error(val message: String) : QuestionListUiState
}

/**
 * UI State container for the Question Detail view.
 */
sealed interface QuestionDetailUiState {
    data object Loading : QuestionDetailUiState
    data class Success(val question: QuestionPresentationModel) : QuestionDetailUiState
    data object NotFound : QuestionDetailUiState
    data class Error(val message: String) : QuestionDetailUiState
}
