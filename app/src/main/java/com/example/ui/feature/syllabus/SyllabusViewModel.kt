package com.example.ui.feature.syllabus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.Exam
import com.example.domain.model.Paper
import com.example.domain.model.Subject
import com.example.domain.model.SubtopicWithDetails
import com.example.domain.model.SyllabusBreadcrumb
import com.example.domain.model.Topic
import com.example.domain.model.toSyllabusNode
import com.example.domain.repository.EducationalRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Structural level in the hierarchical syllabus navigation.
 * Exam -> Paper -> Subject -> Topic -> Subtopic.
 */
sealed interface SyllabusNavigationLevel {
    data object Exams : SyllabusNavigationLevel
    data class Papers(val exam: Exam) : SyllabusNavigationLevel
    data class Subjects(val exam: Exam, val paper: Paper) : SyllabusNavigationLevel
    data class Topics(val exam: Exam, val paper: Paper, val subject: Subject) : SyllabusNavigationLevel
    data class Subtopics(
        val exam: Exam,
        val paper: Paper,
        val subject: Subject,
        val topic: Topic
    ) : SyllabusNavigationLevel
    data class Questions(
        val exam: Exam,
        val paper: Paper,
        val subject: Subject,
        val topic: Topic,
        val subtopic: com.example.domain.model.Subtopic
    ) : SyllabusNavigationLevel
    data class QuestionDetail(
        val exam: Exam,
        val paper: Paper,
        val subject: Subject,
        val topic: Topic,
        val subtopic: com.example.domain.model.Subtopic,
        val questionId: String,
        val questionIndex: Int
    ) : SyllabusNavigationLevel
    data class QuestionPractice(
        val exam: Exam,
        val paper: Paper,
        val subject: Subject,
        val topic: Topic,
        val subtopic: com.example.domain.model.Subtopic,
        val questionId: String,
        val questionIndex: Int
    ) : SyllabusNavigationLevel
    data class SubtopicPractice(
        val exam: Exam,
        val paper: Paper,
        val subject: Subject,
        val topic: Topic,
        val subtopic: com.example.domain.model.Subtopic
    ) : SyllabusNavigationLevel
}

/**
 * Universal content state representation for any syllabus hierarchy level.
 */
sealed interface SyllabusContentState<out T> {
    data object Loading : SyllabusContentState<Nothing>
    data class Success<T>(val items: List<T>) : SyllabusContentState<T>
    data object Empty : SyllabusContentState<Nothing>
    data class Error(val message: String) : SyllabusContentState<Nothing>
}

/**
 * State container for the Syllabus Screen.
 */
data class SyllabusUiState(
    val currentLevel: SyllabusNavigationLevel = SyllabusNavigationLevel.Exams,
    val breadcrumb: SyllabusBreadcrumb = SyllabusBreadcrumb(),
    val examsState: SyllabusContentState<Exam> = SyllabusContentState.Loading,
    val papersState: SyllabusContentState<Paper> = SyllabusContentState.Loading,
    val subjectsState: SyllabusContentState<Subject> = SyllabusContentState.Loading,
    val topicsState: SyllabusContentState<Topic> = SyllabusContentState.Loading,
    val subtopicsState: SyllabusContentState<SubtopicWithDetails> = SyllabusContentState.Loading
) {
    val canNavigateBack: Boolean
        get() = currentLevel !is SyllabusNavigationLevel.Exams
}

/**
 * Focused ViewModel driving syllabus hierarchy navigation and content discovery.
 * Strictly decoupled from Room / SQLite; depends solely on [EducationalRepository].
 */
class SyllabusViewModel(
    private val repository: EducationalRepository = EducationalRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SyllabusUiState())
    val uiState: StateFlow<SyllabusUiState> = _uiState.asStateFlow()

    private var activeJob: Job? = null

    init {
        loadExams()
    }

    /**
     * Loads root level active exams ordered deterministically by sortOrder ASC, name ASC.
     */
    fun loadExams() {
        activeJob?.cancel()
        _uiState.value = _uiState.value.copy(
            currentLevel = SyllabusNavigationLevel.Exams,
            breadcrumb = SyllabusBreadcrumb(),
            examsState = SyllabusContentState.Loading
        )

        activeJob = viewModelScope.launch {
            repository.observeActiveExams()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        examsState = SyllabusContentState.Error(error.localizedMessage ?: "Failed to load exams.")
                    )
                }
                .collect { exams ->
                    val sorted = exams.sortedWith(compareBy({ it.sortOrder }, { it.name }))
                    _uiState.value = _uiState.value.copy(
                        examsState = if (sorted.isEmpty()) SyllabusContentState.Empty else SyllabusContentState.Success(sorted)
                    )
                }
        }
    }

    /**
     * Selects an exam and loads its active papers.
     */
    fun selectExam(exam: Exam) {
        activeJob?.cancel()
        val breadcrumb = SyllabusBreadcrumb(exam = exam.toSyllabusNode())
        _uiState.value = _uiState.value.copy(
            currentLevel = SyllabusNavigationLevel.Papers(exam),
            breadcrumb = breadcrumb,
            papersState = SyllabusContentState.Loading
        )

        activeJob = viewModelScope.launch {
            repository.observeActivePapersByExamId(exam.id)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        papersState = SyllabusContentState.Error(error.localizedMessage ?: "Failed to load papers.")
                    )
                }
                .collect { papers ->
                    val sorted = papers.sortedWith(compareBy({ it.sortOrder }, { it.name }))
                    _uiState.value = _uiState.value.copy(
                        papersState = if (sorted.isEmpty()) SyllabusContentState.Empty else SyllabusContentState.Success(sorted)
                    )
                }
        }
    }

    /**
     * Selects a paper and loads its active subjects.
     */
    fun selectPaper(paper: Paper) {
        val currentExam = when (val level = _uiState.value.currentLevel) {
            is SyllabusNavigationLevel.Papers -> level.exam
            is SyllabusNavigationLevel.Subjects -> level.exam
            is SyllabusNavigationLevel.Topics -> level.exam
            is SyllabusNavigationLevel.Subtopics -> level.exam
            is SyllabusNavigationLevel.Questions -> level.exam
            is SyllabusNavigationLevel.QuestionDetail -> level.exam
            is SyllabusNavigationLevel.QuestionPractice -> level.exam
            is SyllabusNavigationLevel.SubtopicPractice -> level.exam
            else -> return
        }

        activeJob?.cancel()
        val breadcrumb = SyllabusBreadcrumb(
            exam = currentExam.toSyllabusNode(),
            paper = paper.toSyllabusNode()
        )
        _uiState.value = _uiState.value.copy(
            currentLevel = SyllabusNavigationLevel.Subjects(currentExam, paper),
            breadcrumb = breadcrumb,
            subjectsState = SyllabusContentState.Loading
        )

        activeJob = viewModelScope.launch {
            repository.observeActiveSubjectsByPaperId(paper.id)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        subjectsState = SyllabusContentState.Error(error.localizedMessage ?: "Failed to load subjects.")
                    )
                }
                .collect { subjects ->
                    val sorted = subjects.sortedWith(compareBy({ it.sortOrder }, { it.name }))
                    _uiState.value = _uiState.value.copy(
                        subjectsState = if (sorted.isEmpty()) SyllabusContentState.Empty else SyllabusContentState.Success(sorted)
                    )
                }
        }
    }

    /**
     * Selects a subject and loads its active topics.
     */
    fun selectSubject(subject: Subject) {
        val (currentExam, currentPaper) = when (val level = _uiState.value.currentLevel) {
            is SyllabusNavigationLevel.Subjects -> Pair(level.exam, level.paper)
            is SyllabusNavigationLevel.Topics -> Pair(level.exam, level.paper)
            is SyllabusNavigationLevel.Subtopics -> Pair(level.exam, level.paper)
            is SyllabusNavigationLevel.Questions -> Pair(level.exam, level.paper)
            is SyllabusNavigationLevel.QuestionDetail -> Pair(level.exam, level.paper)
            is SyllabusNavigationLevel.QuestionPractice -> Pair(level.exam, level.paper)
            is SyllabusNavigationLevel.SubtopicPractice -> Pair(level.exam, level.paper)
            else -> return
        }

        activeJob?.cancel()
        val breadcrumb = SyllabusBreadcrumb(
            exam = currentExam.toSyllabusNode(),
            paper = currentPaper.toSyllabusNode(),
            subject = subject.toSyllabusNode()
        )
        _uiState.value = _uiState.value.copy(
            currentLevel = SyllabusNavigationLevel.Topics(currentExam, currentPaper, subject),
            breadcrumb = breadcrumb,
            topicsState = SyllabusContentState.Loading
        )

        activeJob = viewModelScope.launch {
            repository.observeActiveTopicsBySubjectId(subject.id)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        topicsState = SyllabusContentState.Error(error.localizedMessage ?: "Failed to load topics.")
                    )
                }
                .collect { topics ->
                    val sorted = topics.sortedWith(compareBy({ it.sortOrder }, { it.name }))
                    _uiState.value = _uiState.value.copy(
                        topicsState = if (sorted.isEmpty()) SyllabusContentState.Empty else SyllabusContentState.Success(sorted)
                    )
                }
        }
    }

    /**
     * Selects a topic and loads its active subtopics with metadata and questions count.
     */
    fun selectTopic(topic: Topic) {
        val (currentExam, currentPaper, currentSubject) = when (val level = _uiState.value.currentLevel) {
            is SyllabusNavigationLevel.Topics -> Triple(level.exam, level.paper, level.subject)
            is SyllabusNavigationLevel.Subtopics -> Triple(level.exam, level.paper, level.subject)
            is SyllabusNavigationLevel.Questions -> Triple(level.exam, level.paper, level.subject)
            is SyllabusNavigationLevel.QuestionDetail -> Triple(level.exam, level.paper, level.subject)
            is SyllabusNavigationLevel.QuestionPractice -> Triple(level.exam, level.paper, level.subject)
            is SyllabusNavigationLevel.SubtopicPractice -> Triple(level.exam, level.paper, level.subject)
            else -> return
        }

        activeJob?.cancel()
        val breadcrumb = SyllabusBreadcrumb(
            exam = currentExam.toSyllabusNode(),
            paper = currentPaper.toSyllabusNode(),
            subject = currentSubject.toSyllabusNode(),
            topic = topic.toSyllabusNode()
        )
        _uiState.value = _uiState.value.copy(
            currentLevel = SyllabusNavigationLevel.Subtopics(currentExam, currentPaper, currentSubject, topic),
            breadcrumb = breadcrumb,
            subtopicsState = SyllabusContentState.Loading
        )

        activeJob = viewModelScope.launch {
            repository.observeActiveSubtopicsWithDetailsByTopicId(topic.id)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        subtopicsState = SyllabusContentState.Error(error.localizedMessage ?: "Failed to load subtopics.")
                    )
                }
                .collect { subtopicsWithDetails ->
                    _uiState.value = _uiState.value.copy(
                        subtopicsState = if (subtopicsWithDetails.isEmpty()) {
                            SyllabusContentState.Empty
                        } else {
                            SyllabusContentState.Success(subtopicsWithDetails)
                        }
                    )
                }
        }
    }

    /**
     * Selects a subtopic and navigates to the Questions level.
     */
    fun selectSubtopic(subtopic: com.example.domain.model.Subtopic) {
        val (currentExam, currentPaper, currentSubject, currentTopic) = when (val level = _uiState.value.currentLevel) {
            is SyllabusNavigationLevel.Subtopics -> arrayOf(level.exam, level.paper, level.subject, level.topic)
            is SyllabusNavigationLevel.Questions -> arrayOf(level.exam, level.paper, level.subject, level.topic)
            is SyllabusNavigationLevel.QuestionDetail -> arrayOf(level.exam, level.paper, level.subject, level.topic)
            is SyllabusNavigationLevel.QuestionPractice -> arrayOf(level.exam, level.paper, level.subject, level.topic)
            is SyllabusNavigationLevel.SubtopicPractice -> arrayOf(level.exam, level.paper, level.subject, level.topic)
            else -> return
        }

        activeJob?.cancel()
        val breadcrumb = SyllabusBreadcrumb(
            exam = (currentExam as Exam).toSyllabusNode(),
            paper = (currentPaper as Paper).toSyllabusNode(),
            subject = (currentSubject as Subject).toSyllabusNode(),
            topic = (currentTopic as Topic).toSyllabusNode(),
            subtopic = subtopic.toSyllabusNode()
        )
        _uiState.value = _uiState.value.copy(
            currentLevel = SyllabusNavigationLevel.Questions(currentExam, currentPaper, currentSubject, currentTopic, subtopic),
            breadcrumb = breadcrumb
        )
    }

    /**
     * Selects a question and navigates to the QuestionDetail level.
     */
    fun selectQuestion(questionId: String, questionIndex: Int) {
        val (currentExam, currentPaper, currentSubject, currentTopic, currentSubtopic) = when (val level = _uiState.value.currentLevel) {
            is SyllabusNavigationLevel.Questions -> arrayOf(level.exam, level.paper, level.subject, level.topic, level.subtopic)
            is SyllabusNavigationLevel.QuestionDetail -> arrayOf(level.exam, level.paper, level.subject, level.topic, level.subtopic)
            is SyllabusNavigationLevel.QuestionPractice -> arrayOf(level.exam, level.paper, level.subject, level.topic, level.subtopic)
            is SyllabusNavigationLevel.SubtopicPractice -> arrayOf(level.exam, level.paper, level.subject, level.topic, level.subtopic)
            else -> return
        }

        _uiState.value = _uiState.value.copy(
            currentLevel = SyllabusNavigationLevel.QuestionDetail(
                exam = currentExam as Exam,
                paper = currentPaper as Paper,
                subject = currentSubject as Subject,
                topic = currentTopic as Topic,
                subtopic = currentSubtopic as com.example.domain.model.Subtopic,
                questionId = questionId,
                questionIndex = questionIndex
            )
        )
    }

    /**
     * Starts Single Question Practice mode for the specified question.
     */
    fun startQuestionPractice(questionId: String, questionIndex: Int) {
        val (currentExam, currentPaper, currentSubject, currentTopic, currentSubtopic) = when (val level = _uiState.value.currentLevel) {
            is SyllabusNavigationLevel.Questions -> arrayOf(level.exam, level.paper, level.subject, level.topic, level.subtopic)
            is SyllabusNavigationLevel.QuestionDetail -> arrayOf(level.exam, level.paper, level.subject, level.topic, level.subtopic)
            is SyllabusNavigationLevel.QuestionPractice -> arrayOf(level.exam, level.paper, level.subject, level.topic, level.subtopic)
            is SyllabusNavigationLevel.SubtopicPractice -> arrayOf(level.exam, level.paper, level.subject, level.topic, level.subtopic)
            else -> return
        }

        _uiState.value = _uiState.value.copy(
            currentLevel = SyllabusNavigationLevel.QuestionPractice(
                exam = currentExam as Exam,
                paper = currentPaper as Paper,
                subject = currentSubject as Subject,
                topic = currentTopic as Topic,
                subtopic = currentSubtopic as com.example.domain.model.Subtopic,
                questionId = questionId,
                questionIndex = questionIndex
            )
        )
    }

    /**
     * Starts Multi-Question Practice mode for the specified subtopic (Step 10).
     */
    fun startSubtopicPractice(subtopic: com.example.domain.model.Subtopic) {
        val (currentExam, currentPaper, currentSubject, currentTopic) = when (val level = _uiState.value.currentLevel) {
            is SyllabusNavigationLevel.Subtopics -> arrayOf(level.exam, level.paper, level.subject, level.topic)
            is SyllabusNavigationLevel.Questions -> arrayOf(level.exam, level.paper, level.subject, level.topic)
            is SyllabusNavigationLevel.QuestionDetail -> arrayOf(level.exam, level.paper, level.subject, level.topic)
            is SyllabusNavigationLevel.QuestionPractice -> arrayOf(level.exam, level.paper, level.subject, level.topic)
            is SyllabusNavigationLevel.SubtopicPractice -> arrayOf(level.exam, level.paper, level.subject, level.topic)
            else -> return
        }

        val breadcrumb = SyllabusBreadcrumb(
            exam = (currentExam as Exam).toSyllabusNode(),
            paper = (currentPaper as Paper).toSyllabusNode(),
            subject = (currentSubject as Subject).toSyllabusNode(),
            topic = (currentTopic as Topic).toSyllabusNode(),
            subtopic = subtopic.toSyllabusNode()
        )

        _uiState.value = _uiState.value.copy(
            currentLevel = SyllabusNavigationLevel.SubtopicPractice(
                exam = currentExam,
                paper = currentPaper as Paper,
                subject = currentSubject as Subject,
                topic = currentTopic as Topic,
                subtopic = subtopic
            ),
            breadcrumb = breadcrumb
        )
    }

    /**
     * Steps backward up the syllabus hierarchy by one level.
     * Returns true if back navigation was handled internally, or false if at root (Exams).
     */
    fun navigateBack(): Boolean {
        return when (val level = _uiState.value.currentLevel) {
            is SyllabusNavigationLevel.SubtopicPractice -> {
                selectSubtopic(level.subtopic)
                true
            }
            is SyllabusNavigationLevel.QuestionPractice -> {
                selectQuestion(level.questionId, level.questionIndex)
                true
            }
            is SyllabusNavigationLevel.QuestionDetail -> {
                selectSubtopic(level.subtopic)
                true
            }
            is SyllabusNavigationLevel.Questions -> {
                selectTopic(level.topic)
                true
            }
            is SyllabusNavigationLevel.Subtopics -> {
                selectSubject(level.subject)
                true
            }
            is SyllabusNavigationLevel.Topics -> {
                selectPaper(level.paper)
                true
            }
            is SyllabusNavigationLevel.Subjects -> {
                selectExam(level.exam)
                true
            }
            is SyllabusNavigationLevel.Papers -> {
                loadExams()
                true
            }
            SyllabusNavigationLevel.Exams -> {
                false
            }
        }
    }

    /**
     * Navigates directly to an ancestor level via breadcrumb tap.
     */
    fun navigateToBreadcrumbLevel(level: SyllabusNavigationLevel) {
        when (level) {
            SyllabusNavigationLevel.Exams -> loadExams()
            is SyllabusNavigationLevel.Papers -> selectExam(level.exam)
            is SyllabusNavigationLevel.Subjects -> selectPaper(level.paper)
            is SyllabusNavigationLevel.Topics -> selectSubject(level.subject)
            is SyllabusNavigationLevel.Subtopics -> selectTopic(level.topic)
            is SyllabusNavigationLevel.Questions -> selectSubtopic(level.subtopic)
            is SyllabusNavigationLevel.QuestionDetail -> selectQuestion(level.questionId, level.questionIndex)
            is SyllabusNavigationLevel.QuestionPractice -> startQuestionPractice(level.questionId, level.questionIndex)
            is SyllabusNavigationLevel.SubtopicPractice -> startSubtopicPractice(level.subtopic)
        }
    }

    /**
     * Retries loading for the current level.
     */
    fun retryCurrentLevel() {
        when (val level = _uiState.value.currentLevel) {
            SyllabusNavigationLevel.Exams -> loadExams()
            is SyllabusNavigationLevel.Papers -> selectExam(level.exam)
            is SyllabusNavigationLevel.Subjects -> selectPaper(level.paper)
            is SyllabusNavigationLevel.Topics -> selectSubject(level.subject)
            is SyllabusNavigationLevel.Subtopics -> selectTopic(level.topic)
            is SyllabusNavigationLevel.Questions -> selectSubtopic(level.subtopic)
            is SyllabusNavigationLevel.QuestionDetail -> selectQuestion(level.questionId, level.questionIndex)
            is SyllabusNavigationLevel.QuestionPractice -> startQuestionPractice(level.questionId, level.questionIndex)
            is SyllabusNavigationLevel.SubtopicPractice -> startSubtopicPractice(level.subtopic)
        }
    }
}
