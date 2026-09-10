package com.example.ui.feature.performance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.performance.StudentPerformance
import com.example.domain.repository.EducationalRepository
import com.example.domain.usecase.performance.CalculateStudentPerformanceUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * UI state for the Student Progress & Performance screen.
 */
sealed interface PerformanceUiState {
    data object Loading : PerformanceUiState
    data object Empty : PerformanceUiState
    data class Success(val performance: StudentPerformance) : PerformanceUiState
    data class Error(val message: String) : PerformanceUiState
}

/**
 * ViewModel managing student performance metrics aggregated from completed practice attempts.
 *
 * Adheres strictly to the architectural flow:
 * UI -> ViewModel -> Repository -> Local Data Source -> DAO -> Room
 */
class PerformanceViewModel(
    private val repository: EducationalRepository = EducationalRepositoryImpl(),
    private val calculatePerformance: CalculateStudentPerformanceUseCase = CalculateStudentPerformanceUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PerformanceUiState>(PerformanceUiState.Loading)
    val uiState: StateFlow<PerformanceUiState> = _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        loadPerformance()
    }

    /**
     * Observes practice attempts from the repository and computes aggregated performance.
     */
    fun loadPerformance() {
        observationJob?.cancel()
        _uiState.value = PerformanceUiState.Loading

        observationJob = viewModelScope.launch {
            repository.observeAllPracticeAttempts()
                .catch { error ->
                    _uiState.value = PerformanceUiState.Error(
                        error.message ?: "Failed to load practice attempts"
                    )
                }
                .collect { attempts ->
                    try {
                        if (attempts.isEmpty()) {
                            _uiState.value = PerformanceUiState.Empty
                        } else {
                            val subtopics = mutableMapOf<String, String>()
                            val distinctSubtopicIds = attempts.map { it.subtopicId }.distinct()
                            for (subtopicId in distinctSubtopicIds) {
                                repository.getSubtopicById(subtopicId)?.let { sub ->
                                    subtopics[subtopicId] = sub.name
                                }
                            }

                            val performance = calculatePerformance(attempts, subtopics)
                            if (performance.totalCompletedAttempts == 0) {
                                _uiState.value = PerformanceUiState.Empty
                            } else {
                                _uiState.value = PerformanceUiState.Success(performance)
                            }
                        }
                    } catch (e: Exception) {
                        _uiState.value = PerformanceUiState.Error(
                            e.message ?: "Failed to calculate performance metrics"
                        )
                    }
                }
        }
    }

    /**
     * Retries loading performance metrics.
     */
    fun retry() {
        loadPerformance()
    }
}
