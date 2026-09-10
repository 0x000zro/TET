package com.example.ui.feature.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.practice.PracticeAttempt
import com.example.domain.repository.EducationalRepository
import com.example.ui.feature.question.model.toPresentationModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Focused ViewModel responsible for question presentation, browsing,
 * and subtopic practice attempt history (Step 11).
 * Decoupled from Room/DAO; depends strictly on [EducationalRepository].
 *
 * Enforces:
 * - Deterministic ordering: sortOrder ASC, id ASC
 * - Active-only questions filtering
 * - Proper state handling: Loading, Success, Empty, Error, NotFound, Retry
 * - Safe presentation models without answer key exposure
 */
class QuestionViewModel(
    private val repository: EducationalRepository = EducationalRepositoryImpl()
) : ViewModel() {

    private val _listState = MutableStateFlow<QuestionListUiState>(QuestionListUiState.Loading)
    val listState: StateFlow<QuestionListUiState> = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<QuestionDetailUiState>(QuestionDetailUiState.Loading)
    val detailState: StateFlow<QuestionDetailUiState> = _detailState.asStateFlow()

    private val _subtopicAttempts = MutableStateFlow<List<PracticeAttempt>>(emptyList())
    val subtopicAttempts: StateFlow<List<PracticeAttempt>> = _subtopicAttempts.asStateFlow()

    private var currentSubtopicId: String? = null
    private var currentQuestionId: String? = null
    private var currentQuestionIndex: Int = 1

    private var listJob: Job? = null
    private var detailJob: Job? = null
    private var attemptsJob: Job? = null

    /**
     * Observes questions and practice attempts for the specified subtopic.
     * Orders deterministically: sortOrder ASC, id ASC.
     * Maps to presentation models stripping the answer key.
     */
    fun loadQuestionsForSubtopic(subtopicId: String) {
        currentSubtopicId = subtopicId
        listJob?.cancel()
        attemptsJob?.cancel()
        _listState.value = QuestionListUiState.Loading

        attemptsJob = viewModelScope.launch {
            repository.observeAttemptsBySubtopicId(subtopicId)
                .catch { emit(emptyList()) }
                .collect { attempts ->
                    _subtopicAttempts.value = attempts
                }
        }

        listJob = viewModelScope.launch {
            repository.observeQuestionsForSubtopic(subtopicId, activeOnly = true)
                .catch { error ->
                    _listState.value = QuestionListUiState.Error(
                        error.localizedMessage ?: "Failed to load questions."
                    )
                }
                .collect { questions ->
                    val activeSorted = questions
                        .filter { it.isActive }
                        .sortedWith(compareBy({ it.sortOrder }, { it.id }))

                    _listState.value = if (activeSorted.isEmpty()) {
                        QuestionListUiState.Empty
                    } else {
                        val presentationQuestions = activeSorted.mapIndexed { index, question ->
                            question.toPresentationModel(questionNumber = index + 1)
                        }
                        QuestionListUiState.Success(presentationQuestions)
                    }
                }
        }
    }

    /**
     * Observes a specific question by its ID.
     * Emits [QuestionDetailUiState.NotFound] if the question does not exist or is inactive.
     */
    fun loadQuestionDetail(questionId: String, questionIndex: Int = 1) {
        currentQuestionId = questionId
        currentQuestionIndex = questionIndex
        detailJob?.cancel()
        _detailState.value = QuestionDetailUiState.Loading

        detailJob = viewModelScope.launch {
            repository.observeQuestion(questionId)
                .catch { error ->
                    _detailState.value = QuestionDetailUiState.Error(
                        error.localizedMessage ?: "Failed to load question details."
                    )
                }
                .collect { question ->
                    if (question == null || !question.isActive) {
                        _detailState.value = QuestionDetailUiState.NotFound
                    } else {
                        _detailState.value = QuestionDetailUiState.Success(
                            question.toPresentationModel(questionNumber = questionIndex)
                        )
                    }
                }
        }
    }

    /**
     * Retries loading questions for the active subtopic.
     */
    fun retryQuestions() {
        currentSubtopicId?.let { loadQuestionsForSubtopic(it) }
    }

    /**
     * Retries loading detail for the active question.
     */
    fun retryQuestionDetail() {
        currentQuestionId?.let { loadQuestionDetail(it, currentQuestionIndex) }
    }
}
