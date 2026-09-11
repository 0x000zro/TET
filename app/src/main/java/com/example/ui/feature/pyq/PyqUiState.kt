package com.example.ui.feature.pyq

import com.example.domain.model.pyq.PreviousYearQuestionSummary
import com.example.domain.model.pyq.PyqVerificationStatus

/**
 * UI State for the Previous Year Questions (PYQ) feature (Step 15).
 *
 * Supports deterministic state modeling across:
 * - Loading: Initial asynchronous loading and enrichment.
 * - Empty: No PYQ records exist or match current filter parameters.
 * - Success: Filtered list of verified/unverified PYQs with active filter options.
 * - Error: Repository failure or network/disk read failure with retry capability.
 */
sealed interface PyqUiState {
    data object Loading : PyqUiState

    data class Empty(
        val isFiltered: Boolean = false
    ) : PyqUiState

    data class Success(
        val items: List<PreviousYearQuestionSummary>,
        val availableExams: List<Pair<String, String>>, // (examId, examName)
        val availablePapers: List<Pair<String, String>>, // (paperId, paperName)
        val availableYears: List<Int>,
        val availableSessions: List<String>,
        val availableStatuses: List<PyqVerificationStatus>,
        val selectedExamId: String? = null,
        val selectedPaperId: String? = null,
        val selectedYear: Int? = null,
        val selectedSession: String? = null,
        val selectedVerificationStatus: PyqVerificationStatus? = null,
        val totalCount: Int = items.size
    ) : PyqUiState {
        val hasActiveFilters: Boolean
            get() = selectedExamId != null ||
                    selectedPaperId != null ||
                    selectedYear != null ||
                    selectedSession != null ||
                    selectedVerificationStatus != null
    }

    data class Error(val message: String) : PyqUiState
}
