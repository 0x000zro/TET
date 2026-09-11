package com.example.ui.feature.pyq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.pyq.PreviousYearQuestion
import com.example.domain.model.pyq.PreviousYearQuestionSummary
import com.example.domain.model.pyq.PyqVerificationStatus
import com.example.domain.repository.EducationalRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel managing the Previous Year Questions (PYQ) feature (Step 15).
 *
 * Architecture & Design:
 * - Reactive Flow consumption of PYQ metadata from [EducationalRepository].
 * - Single source of truth: Question content, options, answer keys, and solutions
 *   are fetched from the canonical [com.example.domain.model.Question].
 * - Deterministic ordering: Year DESC, Session ASC, Question ID ASC.
 * - Supports multi-dimensional filtering by Exam, Paper, Year, Session, and Verification Status.
 * - Manages review selection for deep inspection using the existing question presentation engine.
 */
class PyqViewModel(
    private val repository: EducationalRepository = EducationalRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<PyqUiState>(PyqUiState.Loading)
    val uiState: StateFlow<PyqUiState> = _uiState.asStateFlow()

    private val _selectedPyq = MutableStateFlow<PreviousYearQuestionSummary?>(null)
    val selectedPyq: StateFlow<PreviousYearQuestionSummary?> = _selectedPyq.asStateFlow()

    private var observeJob: Job? = null
    private var allEnrichedSummaries: List<PreviousYearQuestionSummary> = emptyList()

    // Filter states
    private var selectedExamId: String? = null
    private var selectedPaperId: String? = null
    private var selectedYear: Int? = null
    private var selectedSession: String? = null
    private var selectedVerificationStatus: PyqVerificationStatus? = null

    init {
        loadPreviousYearQuestions()
    }

    /**
     * Observes all PYQ records reactively from the repository and enriches them with canonical questions.
     */
    fun loadPreviousYearQuestions() {
        observeJob?.cancel()
        _uiState.value = PyqUiState.Loading

        observeJob = viewModelScope.launch {
            repository.observeAllPreviousYearQuestions()
                .catch { error ->
                    _uiState.value = PyqUiState.Error(
                        error.localizedMessage ?: "Failed to load previous year questions."
                    )
                }
                .collect { pyqList ->
                    if (pyqList.isEmpty()) {
                        allEnrichedSummaries = emptyList()
                        _uiState.value = PyqUiState.Empty(isFiltered = false)
                        _selectedPyq.value = null
                    } else {
                        // Enrich PYQs with canonical Question and syllabus metadata
                        val enriched = mutableListOf<PreviousYearQuestionSummary>()
                        val examMap = mutableMapOf<String, String>()
                        val paperMap = mutableMapOf<String, String>()
                        val subjectMap = mutableMapOf<String, String>()
                        val subtopicMap = mutableMapOf<String, String>()

                        for (pyq in pyqList) {
                            val question = repository.getQuestionById(pyq.questionId)
                            if (question != null) {
                                val examName = examMap.getOrPut(pyq.examId) {
                                    repository.getExamById(pyq.examId)?.name ?: pyq.examId
                                }
                                val paperName = paperMap.getOrPut(pyq.paperId) {
                                    repository.getPaperById(pyq.paperId)?.name ?: pyq.paperId
                                }
                                val subtopicName = subtopicMap.getOrPut(question.subtopicId) {
                                    repository.getSubtopicById(question.subtopicId)?.name ?: "Subtopic"
                                }

                                enriched.add(
                                    PreviousYearQuestionSummary(
                                        pyq = pyq,
                                        question = question,
                                        examName = examName,
                                        paperName = paperName,
                                        subjectName = null,
                                        subtopicName = subtopicName
                                    )
                                )
                            }
                        }

                        allEnrichedSummaries = enriched
                        applyFiltersAndEmit()
                    }
                }
        }
    }

    /**
     * Applies current filters to [allEnrichedSummaries] and updates [_uiState].
     */
    private fun applyFiltersAndEmit() {
        if (allEnrichedSummaries.isEmpty()) {
            _uiState.value = PyqUiState.Empty(isFiltered = false)
            return
        }

        // Compute available filter options from the full pool
        val availableExams = allEnrichedSummaries
            .map { it.pyq.examId to (it.examName ?: it.pyq.examId) }
            .distinctBy { it.first }
            .sortedBy { it.second }

        val availablePapers = allEnrichedSummaries
            .filter { selectedExamId == null || it.pyq.examId == selectedExamId }
            .map { it.pyq.paperId to (it.paperName ?: it.pyq.paperId) }
            .distinctBy { it.first }
            .sortedBy { it.second }

        val availableYears = allEnrichedSummaries
            .map { it.pyq.year }
            .distinct()
            .sortedDescending()

        val availableSessions = allEnrichedSummaries
            .mapNotNull { it.pyq.session }
            .distinct()
            .sorted()

        val availableStatuses = allEnrichedSummaries
            .map { it.pyq.verificationStatus }
            .distinct()

        // Filter items
        val filtered = allEnrichedSummaries.filter { summary ->
            val matchExam = selectedExamId == null || summary.pyq.examId == selectedExamId
            val matchPaper = selectedPaperId == null || summary.pyq.paperId == selectedPaperId
            val matchYear = selectedYear == null || summary.pyq.year == selectedYear
            val matchSession = selectedSession == null || summary.pyq.session == selectedSession
            val matchStatus = selectedVerificationStatus == null || summary.pyq.verificationStatus == selectedVerificationStatus

            matchExam && matchPaper && matchYear && matchSession && matchStatus
        }.sortedWith(
            compareByDescending<PreviousYearQuestionSummary> { it.pyq.year }
                .thenBy { it.pyq.session ?: "" }
                .thenBy { it.pyq.questionId }
        )

        val hasFilters = selectedExamId != null ||
                selectedPaperId != null ||
                selectedYear != null ||
                selectedSession != null ||
                selectedVerificationStatus != null

        if (filtered.isEmpty()) {
            _uiState.value = PyqUiState.Empty(isFiltered = hasFilters)
        } else {
            // Keep selected question in sync
            _selectedPyq.value?.let { current ->
                _selectedPyq.value = filtered.find { it.pyq.id == current.pyq.id }
            }

            _uiState.value = PyqUiState.Success(
                items = filtered,
                availableExams = availableExams,
                availablePapers = availablePapers,
                availableYears = availableYears,
                availableSessions = availableSessions,
                availableStatuses = availableStatuses,
                selectedExamId = selectedExamId,
                selectedPaperId = selectedPaperId,
                selectedYear = selectedYear,
                selectedSession = selectedSession,
                selectedVerificationStatus = selectedVerificationStatus,
                totalCount = filtered.size
            )
        }
    }

    fun filterByExam(examId: String?) {
        selectedExamId = examId
        // If the selected paper does not belong to the newly selected exam, reset paper
        if (examId != null && selectedPaperId != null) {
            val paperBelongsToExam = allEnrichedSummaries.any {
                it.pyq.examId == examId && it.pyq.paperId == selectedPaperId
            }
            if (!paperBelongsToExam) {
                selectedPaperId = null
            }
        }
        applyFiltersAndEmit()
    }

    fun filterByPaper(paperId: String?) {
        selectedPaperId = paperId
        applyFiltersAndEmit()
    }

    fun filterByYear(year: Int?) {
        selectedYear = year
        applyFiltersAndEmit()
    }

    fun filterBySession(session: String?) {
        selectedSession = session
        applyFiltersAndEmit()
    }

    fun filterByVerificationStatus(status: PyqVerificationStatus?) {
        selectedVerificationStatus = status
        applyFiltersAndEmit()
    }

    fun clearFilters() {
        selectedExamId = null
        selectedPaperId = null
        selectedYear = null
        selectedSession = null
        selectedVerificationStatus = null
        applyFiltersAndEmit()
    }

    fun selectPyqForReview(summary: PreviousYearQuestionSummary) {
        _selectedPyq.value = summary
    }

    fun clearSelectedPyq() {
        _selectedPyq.value = null
    }

    fun retry() {
        loadPreviousYearQuestions()
    }
}
