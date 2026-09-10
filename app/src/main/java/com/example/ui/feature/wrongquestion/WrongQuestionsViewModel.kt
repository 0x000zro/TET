package com.example.ui.feature.wrongquestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.wrongquestion.WrongQuestion
import com.example.domain.model.wrongquestion.WrongQuestionSummary
import com.example.domain.repository.EducationalRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * UI state for the Wrong Questions / Mistake Review feature (Step 13).
 */
sealed interface WrongQuestionsUiState {
    data object Loading : WrongQuestionsUiState
    data object Empty : WrongQuestionsUiState
    data class Success(
        val items: List<WrongQuestionSummary>,
        val availableSubtopics: List<Pair<String, String>>, // (subtopicId, subtopicName)
        val selectedSubtopicId: String? = null
    ) : WrongQuestionsUiState
    data class Error(val message: String) : WrongQuestionsUiState
}

/**
 * ViewModel managing student mistake records and intentional review.
 *
 * Enforces:
 * - Deterministic ordering: `lastWrongAt DESC, questionId ASC`.
 * - Aggregation of mistake metadata with canonical Question and Subtopic data.
 * - Single source of truth: Question content and answer keys are never stored here.
 * - Targeted removal: deleting mistake records only without touching syllabus or questions.
 */
class WrongQuestionsViewModel(
    private val repository: EducationalRepository = EducationalRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<WrongQuestionsUiState>(WrongQuestionsUiState.Loading)
    val uiState: StateFlow<WrongQuestionsUiState> = _uiState.asStateFlow()

    private val _selectedQuestion = MutableStateFlow<WrongQuestionSummary?>(null)
    val selectedQuestion: StateFlow<WrongQuestionSummary?> = _selectedQuestion.asStateFlow()

    private var selectedSubtopicFilter: String? = null
    private var observeJob: Job? = null

    init {
        loadWrongQuestions()
    }

    /**
     * Observes all wrong questions reactively.
     */
    fun loadWrongQuestions() {
        observeJob?.cancel()
        _uiState.value = WrongQuestionsUiState.Loading

        observeJob = viewModelScope.launch {
            repository.observeAllWrongQuestions()
                .catch { error ->
                    _uiState.value = WrongQuestionsUiState.Error(
                        error.localizedMessage ?: "Failed to load wrong questions."
                    )
                }
                .collect { wrongQuestions ->
                    if (wrongQuestions.isEmpty()) {
                        _uiState.value = WrongQuestionsUiState.Empty
                        if (_selectedQuestion.value != null) {
                            _selectedQuestion.value = null
                        }
                    } else {
                        // Enrich each wrong question record with its canonical Question and Subtopic
                        val enrichedItems = mutableListOf<WrongQuestionSummary>()
                        val subtopicsMap = mutableMapOf<String, String>()

                        for (wrongQ in wrongQuestions) {
                            val question = repository.getQuestionById(wrongQ.questionId)
                            if (question != null) {
                                val subtopicName = subtopicsMap.getOrPut(wrongQ.subtopicId) {
                                    repository.getSubtopicById(wrongQ.subtopicId)?.name ?: "Subtopic"
                                }
                                enrichedItems.add(
                                    WrongQuestionSummary(
                                        wrongQuestion = wrongQ,
                                        question = question,
                                        subtopicName = subtopicName
                                    )
                                )
                            }
                        }

                        if (enrichedItems.isEmpty()) {
                            _uiState.value = WrongQuestionsUiState.Empty
                        } else {
                            val availableSubtopics = subtopicsMap.entries
                                .map { it.key to it.value }
                                .sortedBy { it.second }

                            val filteredItems = if (selectedSubtopicFilter != null) {
                                enrichedItems.filter { it.wrongQuestion.subtopicId == selectedSubtopicFilter }
                            } else {
                                enrichedItems
                            }

                            // Keep selected question updated if it was edited/removed
                            _selectedQuestion.value?.let { currentSelected ->
                                _selectedQuestion.value = enrichedItems.find { it.wrongQuestion.questionId == currentSelected.wrongQuestion.questionId }
                            }

                            _uiState.value = WrongQuestionsUiState.Success(
                                items = filteredItems,
                                availableSubtopics = availableSubtopics,
                                selectedSubtopicId = selectedSubtopicFilter
                            )
                        }
                    }
                }
        }
    }

    /**
     * Filters the displayed mistake records by subtopic.
     * Passing null removes the subtopic filter.
     */
    fun filterBySubtopic(subtopicId: String?) {
        selectedSubtopicFilter = subtopicId
        loadWrongQuestions()
    }

    /**
     * Opens an item for intentional mistake review.
     */
    fun selectQuestionForReview(summary: WrongQuestionSummary) {
        _selectedQuestion.value = summary
    }

    /**
     * Clears the current review selection and returns to the mistake list.
     */
    fun clearSelectedQuestion() {
        _selectedQuestion.value = null
    }

    /**
     * Removes a mistake record explicitly upon student request.
     * Guaranteed to delete only the mistake record, never touching Question or Syllabus data.
     */
    fun removeWrongQuestion(questionId: String) {
        viewModelScope.launch {
            val result = repository.deleteWrongQuestion(questionId)
            if (result.isSuccess) {
                if (_selectedQuestion.value?.wrongQuestion?.questionId == questionId) {
                    _selectedQuestion.value = null
                }
            }
        }
    }

    /**
     * Retries loading mistake records.
     */
    fun retry() {
        loadWrongQuestions()
    }
}
