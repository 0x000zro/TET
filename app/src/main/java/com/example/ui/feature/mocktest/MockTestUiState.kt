package com.example.ui.feature.mocktest

import com.example.domain.model.mocktest.MockTestConfiguration
import com.example.domain.model.mocktest.MockTestPerformance
import com.example.domain.model.mocktest.MockTestResult
import com.example.domain.model.mocktest.MockTestSession
import com.example.ui.feature.mocktest.model.MockTestPaletteItem
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
        val showAbandonConfirmation: Boolean = false,
        val remainingSeconds: Long = 0L,
        val formattedRemainingTime: String = "00:00",
        val paletteItems: List<MockTestPaletteItem> = emptyList()
    ) : MockTestUiState {
        val palette: List<MockTestPaletteItem> get() = paletteItems
    }

    /**
     * Final completed state displaying the immutable test result and performance summary (Step 21).
     */
    data class ResultSummary(
        val result: MockTestResult,
        val configuration: MockTestConfiguration,
        val timeUsedSeconds: Long = 0L,
        val timeRemainingSeconds: Long = 0L,
        val performance: MockTestPerformance? = null
    ) : MockTestUiState
}
