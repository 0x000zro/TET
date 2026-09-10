package com.example.ui.feature.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.repository.EducationalRepository
import com.example.domain.usecase.practice.EvaluatePracticeAnswerUseCase
import com.example.domain.usecase.practice.PracticeEvaluationOutcome
import com.example.ui.feature.question.model.toPresentationModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Focused ViewModel managing single-question practice interaction.
 *
 * Architecture:
 * UI -> PracticeViewModel -> EvaluatePracticeAnswerUseCase / EducationalRepository -> LocalDataSource -> Room
 *
 * Enforces:
 * - Clean separation: does not access DAO directly.
 * - Single-option selection: selecting replaces previous choice.
 * - Frozen submission: once submitted, choice cannot be modified and duplicate submission is blocked.
 * - Controlled domain evaluation: receives semantic results without leaking answer keys.
 * - In-memory state: does not persist attempts (strictly out of scope for Step 9).
 */
class PracticeViewModel(
    private val repository: EducationalRepository = EducationalRepositoryImpl(),
    private val evaluateAnswerUseCase: EvaluatePracticeAnswerUseCase = EvaluatePracticeAnswerUseCase(repository)
) : ViewModel() {

    private val _uiState = MutableStateFlow<PracticeUiState>(PracticeUiState.Loading)
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    private var currentQuestionId: String? = null
    private var currentQuestionIndex: Int = 1
    private var loadJob: Job? = null
    private var isSubmitting: Boolean = false

    /**
     * Loads the target question for practice.
     */
    fun loadQuestion(questionId: String, questionIndex: Int = 1) {
        if (currentQuestionId == questionId && _uiState.value is PracticeUiState.Submitted) {
            return
        }

        currentQuestionId = questionId
        currentQuestionIndex = questionIndex
        loadJob?.cancel()
        _uiState.value = PracticeUiState.Loading

        loadJob = viewModelScope.launch {
            repository.observeQuestion(questionId)
                .catch { error ->
                    _uiState.value = PracticeUiState.Error(
                        error.localizedMessage ?: "Failed to load practice question."
                    )
                }
                .collect { question ->
                    if (question == null || !question.isActive) {
                        _uiState.value = PracticeUiState.NotFound
                    } else {
                        val presentation = question.toPresentationModel(questionIndex)
                        val current = _uiState.value
                        if (current is PracticeUiState.Submitted && current.question.id == question.id) {
                            _uiState.value = current.copy(question = presentation)
                        } else {
                            _uiState.value = PracticeUiState.Ready(
                                question = presentation,
                                selectedOptionId = null
                            )
                        }
                    }
                }
        }
    }

    /**
     * Selects an option for the question.
     * Replaces previous selection.
     * Ignored if the question is already in the submitted state.
     */
    fun selectOption(optionId: String) {
        val currentState = _uiState.value
        if (currentState is PracticeUiState.Ready) {
            _uiState.value = currentState.copy(selectedOptionId = optionId)
        }
    }

    /**
     * Submits the chosen answer for evaluation.
     * Requires an option to be selected.
     * Prevents duplicate submissions.
     */
    fun submitAnswer() {
        val currentState = _uiState.value
        if (currentState !is PracticeUiState.Ready) {
            return
        }

        val selectedOptionId = currentState.selectedOptionId
        if (selectedOptionId.isNullOrBlank()) {
            return
        }

        if (isSubmitting) {
            return
        }
        isSubmitting = true

        viewModelScope.launch {
            try {
                when (val outcome = evaluateAnswerUseCase.execute(currentState.question.id, selectedOptionId)) {
                    is PracticeEvaluationOutcome.Success -> {
                        _uiState.value = PracticeUiState.Submitted(
                            question = currentState.question,
                            selectedOptionId = selectedOptionId,
                            result = outcome.result
                        )
                    }
                    is PracticeEvaluationOutcome.Failure -> {
                        _uiState.value = PracticeUiState.Error(outcome.errorMessage)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PracticeUiState.Error(
                    e.localizedMessage ?: "Failed to evaluate answer."
                )
            } finally {
                isSubmitting = false
            }
        }
    }

    /**
     * Retries loading the practice question.
     */
    fun retry() {
        currentQuestionId?.let { loadQuestion(it, currentQuestionIndex) }
    }
}
