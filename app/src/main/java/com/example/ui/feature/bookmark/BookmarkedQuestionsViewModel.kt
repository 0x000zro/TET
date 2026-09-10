package com.example.ui.feature.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.bookmark.BookmarkedQuestion
import com.example.domain.model.bookmark.BookmarkedQuestionSummary
import com.example.domain.repository.EducationalRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * UI state for the Bookmarked Questions / Saved Questions feature (Step 14).
 */
sealed interface BookmarkedQuestionsUiState {
    data object Loading : BookmarkedQuestionsUiState
    data object Empty : BookmarkedQuestionsUiState
    data class Success(
        val items: List<BookmarkedQuestionSummary>,
        val availableSubtopics: List<Pair<String, String>>, // (subtopicId, subtopicName)
        val selectedSubtopicId: String? = null
    ) : BookmarkedQuestionsUiState
    data class Error(val message: String) : BookmarkedQuestionsUiState
}

/**
 * ViewModel managing student saved bookmarks for intentional revision (Step 14).
 *
 * Enforces:
 * - Deterministic ordering: `bookmarkedAt DESC, questionId ASC`.
 * - Aggregation of bookmark metadata with canonical Question and Subtopic data.
 * - Single source of truth: Question content and answer keys are never duplicated.
 * - Targeted removal: deleting bookmark records only without touching syllabus, questions, or wrong questions.
 */
class BookmarkedQuestionsViewModel(
    private val repository: EducationalRepository = EducationalRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BookmarkedQuestionsUiState>(BookmarkedQuestionsUiState.Loading)
    val uiState: StateFlow<BookmarkedQuestionsUiState> = _uiState.asStateFlow()

    private val _selectedQuestion = MutableStateFlow<BookmarkedQuestionSummary?>(null)
    val selectedQuestion: StateFlow<BookmarkedQuestionSummary?> = _selectedQuestion.asStateFlow()

    private var selectedSubtopicFilter: String? = null
    private var observeJob: Job? = null

    init {
        loadBookmarkedQuestions()
    }

    /**
     * Observes all bookmarked questions reactively.
     */
    fun loadBookmarkedQuestions() {
        observeJob?.cancel()
        _uiState.value = BookmarkedQuestionsUiState.Loading

        observeJob = viewModelScope.launch {
            repository.observeAllBookmarkedQuestions()
                .catch { error ->
                    _uiState.value = BookmarkedQuestionsUiState.Error(
                        error.localizedMessage ?: "Failed to load bookmarks."
                    )
                }
                .collect { bookmarks ->
                    if (bookmarks.isEmpty()) {
                        _uiState.value = BookmarkedQuestionsUiState.Empty
                        if (_selectedQuestion.value != null) {
                            _selectedQuestion.value = null
                        }
                    } else {
                        // Enrich each bookmark record with canonical Question and Subtopic
                        val enrichedItems = mutableListOf<BookmarkedQuestionSummary>()
                        val subtopicsMap = mutableMapOf<String, String>()

                        for (bookmark in bookmarks) {
                            val question = repository.getQuestionById(bookmark.questionId)
                            if (question != null) {
                                val subtopicName = subtopicsMap.getOrPut(bookmark.subtopicId) {
                                    repository.getSubtopicById(bookmark.subtopicId)?.name ?: "Subtopic"
                                }
                                enrichedItems.add(
                                    BookmarkedQuestionSummary(
                                        bookmarkedQuestion = bookmark,
                                        question = question,
                                        subtopicName = subtopicName
                                    )
                                )
                            }
                        }

                        if (enrichedItems.isEmpty()) {
                            _uiState.value = BookmarkedQuestionsUiState.Empty
                        } else {
                            val availableSubtopics = subtopicsMap.entries
                                .map { it.key to it.value }
                                .sortedBy { it.second }

                            val filteredItems = if (selectedSubtopicFilter != null) {
                                enrichedItems.filter { it.bookmarkedQuestion.subtopicId == selectedSubtopicFilter }
                            } else {
                                enrichedItems
                            }

                            // Keep selected question updated if it was edited/removed
                            _selectedQuestion.value?.let { currentSelected ->
                                _selectedQuestion.value = enrichedItems.find { it.bookmarkedQuestion.questionId == currentSelected.bookmarkedQuestion.questionId }
                            }

                            _uiState.value = BookmarkedQuestionsUiState.Success(
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
     * Filters the displayed bookmarks by subtopic.
     * Passing null removes the subtopic filter.
     */
    fun filterBySubtopic(subtopicId: String?) {
        selectedSubtopicFilter = subtopicId
        loadBookmarkedQuestions()
    }

    /**
     * Opens an item for revision.
     */
    fun selectQuestionForReview(summary: BookmarkedQuestionSummary) {
        _selectedQuestion.value = summary
    }

    /**
     * Clears the current review selection and returns to the bookmarks list.
     */
    fun clearSelectedQuestion() {
        _selectedQuestion.value = null
    }

    /**
     * Removes a bookmark explicitly upon student request.
     * Guaranteed to delete only the bookmark record, never touching Question, Syllabus, or Wrong Questions.
     */
    fun removeBookmark(questionId: String) {
        viewModelScope.launch {
            val result = repository.deleteBookmark(questionId)
            if (result.isSuccess) {
                if (_selectedQuestion.value?.bookmarkedQuestion?.questionId == questionId) {
                    _selectedQuestion.value = null
                }
            }
        }
    }

    /**
     * Toggles a bookmark for a question.
     */
    fun toggleBookmark(questionId: String, subtopicId: String) {
        viewModelScope.launch {
            repository.toggleBookmark(questionId, subtopicId)
        }
    }

    /**
     * Retries loading bookmark records.
     */
    fun retry() {
        loadBookmarkedQuestions()
    }
}
