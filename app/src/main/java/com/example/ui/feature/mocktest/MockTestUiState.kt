package com.example.ui.feature.mocktest

import com.example.domain.model.mocktest.MockTestConfiguration
import com.example.domain.model.mocktest.MockTestResult
import com.example.domain.model.mocktest.MockTestSession
import com.example.ui.feature.mocktest.model.MockTestQuestionPresentationModel

/**
 * UI state hierarchy for the Mock Test feature (Step 17).
 */
sealed interface MockTestUiState {
    /**
     * Initial loading state while fetching configuration and question set.
     */
    data object Loading : MockTestUiState

    /**
     * Empty state when no questions match the mock test configuration.
     */
    data class Empty(
        val message: String
    ) : MockTestUiState

    /**
     * Error state encountered during setup, question retrieval, or evaluation.
     */
    data class Error(
        val message: String
    ) : MockTestUiState

    /**
     * Pre-test summary overview displaying configuration, scope, question count, and instructions.
     */
    data class ConfigurationOverview(
        val configuration: MockTestConfiguration,
        val examTitle: String,
        val paperTitle: String,
        val scopeDescription: String,
        val availableQuestionsCount: Int
    ) : MockTestUiState

    /**
     * Active test state displaying the current question, answer options, navigation controls,
     * and answering indicators. Explanations and answer keys are strictly omitted.
     */
    data class ActiveTest(
        val session: MockTestSession,
        val currentQuestion: MockTestQuestionPresentationModel,
        val currentQuestionIndex: Int, // 1-based for UI display
        val totalQuestions: Int,
        val selectedOptionId: String?,
        val answeredCount: Int,
        val unansweredCount: Int,
        val hasPrevious: Boolean,
        val hasNext: Boolean,
        val isLastQuestion: Boolean,
        val showFinishConfirmation: Boolean = false,
        val showAbandonConfirmation: Boolean = false
    ) : MockTestUiState

    /**
     * Final completed state displaying the immutable test result.
     */
    data class ResultSummary(
        val result: MockTestResult,
        val configuration: MockTestConfiguration
    ) : MockTestUiState
}
