package com.example.ui.feature.syllabus

import com.example.domain.model.AppFoundationInfo
import com.example.domain.model.AppState
import com.example.domain.model.ContentSyncState
import com.example.domain.model.EducationalModule
import com.example.domain.model.Exam
import com.example.domain.model.LocalPreference
import com.example.domain.model.Paper
import com.example.domain.model.Question
import com.example.domain.model.QuestionOption
import com.example.domain.model.Subject
import com.example.domain.model.Subtopic
import com.example.domain.model.SubtopicWithDetails
import com.example.domain.model.SyllabusBreadcrumb
import com.example.domain.model.SyllabusMetadata
import com.example.domain.model.SyllabusNode
import com.example.domain.model.SyllabusNodeType
import com.example.domain.model.SyllabusTreeNode
import com.example.domain.model.Topic
import com.example.domain.repository.EducationalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyllabusViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeEducationalRepository
    private lateinit var viewModel: SyllabusViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeEducationalRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsActiveExamsInDeterministicOrder() = runTest(testDispatcher) {
        val exam1 = Exam("e1", "CTET Exam", "CTET", "Central Teacher Eligibility", true, 2)
        val exam2 = Exam("e2", "UPTET Exam", "UPTET", "UP Teacher Eligibility", true, 1)
        val examInactive = Exam("e3", "Hidden Exam", "HIDDEN", "Inactive exam", false, 0)
        fakeRepository.activeExamsFlow.value = listOf(exam1, exam2)

        viewModel = SyllabusViewModel(fakeRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(SyllabusNavigationLevel.Exams, state.currentLevel)
        assertTrue(state.examsState is SyllabusContentState.Success)
        val items = (state.examsState as SyllabusContentState.Success).items
        assertEquals(2, items.size)
        // Deterministic sorting: sortOrder 1 (exam2) before sortOrder 2 (exam1)
        assertEquals("e2", items[0].id)
        assertEquals("e1", items[1].id)
    }

    @Test
    fun emptyExams_emitsEmptyContentState() = runTest(testDispatcher) {
        fakeRepository.activeExamsFlow.value = emptyList()

        viewModel = SyllabusViewModel(fakeRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(SyllabusContentState.Empty, state.examsState)
    }

    @Test
    fun selectExam_loadsPapersAndUpdatesBreadcrumb() = runTest(testDispatcher) {
        val exam = Exam("exam_01", "Central TET", "CTET", "Eligibility Test", true, 1)
        val paper1 = Paper("paper_01", "exam_01", "Paper 1", "P1", "Primary Stage", true, 1)
        val paper2 = Paper("paper_02", "exam_01", "Paper 2", "P2", "Upper Primary", true, 2)
        fakeRepository.activeExamsFlow.value = listOf(exam)
        fakeRepository.activePapersFlow.value = listOf(paper2, paper1)

        viewModel = SyllabusViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.selectExam(exam)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.currentLevel is SyllabusNavigationLevel.Papers)
        assertEquals(exam, (state.currentLevel as SyllabusNavigationLevel.Papers).exam)
        assertEquals("Central TET", state.breadcrumb.exam?.title)
        assertTrue(state.papersState is SyllabusContentState.Success)
        val papers = (state.papersState as SyllabusContentState.Success).items
        assertEquals(2, papers.size)
        assertEquals("paper_01", papers[0].id) // sortOrder 1 before sortOrder 2
        assertEquals("paper_02", papers[1].id)
    }

    @Test
    fun selectPaper_loadsSubjectsAndUpdatesBreadcrumb() = runTest(testDispatcher) {
        val exam = Exam("exam_01", "Central TET", "CTET", "", true, 1)
        val paper = Paper("paper_01", "exam_01", "Paper 1", "P1", "", true, 1)
        val subject = Subject("sub_01", "paper_01", "Child Development", "CDP", "", true, 1)
        fakeRepository.activeExamsFlow.value = listOf(exam)
        fakeRepository.activePapersFlow.value = listOf(paper)
        fakeRepository.activeSubjectsFlow.value = listOf(subject)

        viewModel = SyllabusViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.selectExam(exam)
        advanceUntilIdle()

        viewModel.selectPaper(paper)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.currentLevel is SyllabusNavigationLevel.Subjects)
        assertEquals(paper, (state.currentLevel as SyllabusNavigationLevel.Subjects).paper)
        assertEquals("Central TET", state.breadcrumb.exam?.title)
        assertEquals("Paper 1", state.breadcrumb.paper?.title)
        assertTrue(state.subjectsState is SyllabusContentState.Success)
        val subjects = (state.subjectsState as SyllabusContentState.Success).items
        assertEquals(1, subjects.size)
        assertEquals("sub_01", subjects[0].id)
    }

    @Test
    fun selectSubject_loadsTopicsAndUpdatesBreadcrumb() = runTest(testDispatcher) {
        val exam = Exam("exam_01", "CTET", "CTET", "", true, 1)
        val paper = Paper("paper_01", "exam_01", "Paper 1", "P1", "", true, 1)
        val subject = Subject("sub_01", "paper_01", "Pedagogy", "CDP", "", true, 1)
        val topic = Topic("top_01", "sub_01", "Development Principles", "", true, 1)
        fakeRepository.activeExamsFlow.value = listOf(exam)
        fakeRepository.activePapersFlow.value = listOf(paper)
        fakeRepository.activeSubjectsFlow.value = listOf(subject)
        fakeRepository.activeTopicsFlow.value = listOf(topic)

        viewModel = SyllabusViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.selectExam(exam)
        viewModel.selectPaper(paper)
        viewModel.selectSubject(subject)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.currentLevel is SyllabusNavigationLevel.Topics)
        assertEquals("Pedagogy", state.breadcrumb.subject?.title)
        assertTrue(state.topicsState is SyllabusContentState.Success)
        val topics = (state.topicsState as SyllabusContentState.Success).items
        assertEquals(1, topics.size)
        assertEquals("top_01", topics[0].id)
    }

    @Test
    fun selectTopic_loadsSubtopicsWithDetailsAndQuestionCount() = runTest(testDispatcher) {
        val exam = Exam("exam_01", "CTET", "CTET", "", true, 1)
        val paper = Paper("paper_01", "exam_01", "Paper 1", "P1", "", true, 1)
        val subject = Subject("sub_01", "paper_01", "Pedagogy", "CDP", "", true, 1)
        val topic = Topic("top_01", "sub_01", "Development Principles", "", true, 1)
        val subtopic = Subtopic("subtop_01", "top_01", "Piaget Theory", "Cognitive stages", true, 1)
        val metadata = SyllabusMetadata("subtop_01", "Understand stages", "Four main stages", 30)
        val subtopicWithDetails = SubtopicWithDetails(subtopic, metadata, questionCount = 15)

        fakeRepository.activeExamsFlow.value = listOf(exam)
        fakeRepository.activePapersFlow.value = listOf(paper)
        fakeRepository.activeSubjectsFlow.value = listOf(subject)
        fakeRepository.activeTopicsFlow.value = listOf(topic)
        fakeRepository.subtopicsWithDetailsFlow.value = listOf(subtopicWithDetails)

        viewModel = SyllabusViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.selectExam(exam)
        viewModel.selectPaper(paper)
        viewModel.selectSubject(subject)
        viewModel.selectTopic(topic)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.currentLevel is SyllabusNavigationLevel.Subtopics)
        assertEquals("Development Principles", state.breadcrumb.topic?.title)
        assertTrue(state.subtopicsState is SyllabusContentState.Success)
        val items = (state.subtopicsState as SyllabusContentState.Success).items
        assertEquals(1, items.size)
        assertEquals("subtop_01", items[0].subtopic.id)
        assertEquals(15, items[0].questionCount)
        assertTrue(items[0].hasQuestions)
        assertEquals("Understand stages", items[0].metadata?.learningObjective)
        assertEquals(30, items[0].metadata?.estimatedMinutes)
    }

    @Test
    fun navigateBack_navigatesStepByStepToExams() = runTest(testDispatcher) {
        val exam = Exam("exam_01", "CTET", "CTET", "", true, 1)
        val paper = Paper("paper_01", "exam_01", "Paper 1", "P1", "", true, 1)
        val subject = Subject("sub_01", "paper_01", "Pedagogy", "CDP", "", true, 1)
        val topic = Topic("top_01", "sub_01", "Principles", "", true, 1)
        fakeRepository.activeExamsFlow.value = listOf(exam)
        fakeRepository.activePapersFlow.value = listOf(paper)
        fakeRepository.activeSubjectsFlow.value = listOf(subject)
        fakeRepository.activeTopicsFlow.value = listOf(topic)
        fakeRepository.subtopicsWithDetailsFlow.value = emptyList()

        viewModel = SyllabusViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.selectExam(exam)
        viewModel.selectPaper(paper)
        viewModel.selectSubject(subject)
        viewModel.selectTopic(topic)
        advanceUntilIdle()

        // At Subtopics -> Back to Topics
        assertTrue(viewModel.navigateBack())
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.currentLevel is SyllabusNavigationLevel.Topics)

        // At Topics -> Back to Subjects
        assertTrue(viewModel.navigateBack())
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.currentLevel is SyllabusNavigationLevel.Subjects)

        // At Subjects -> Back to Papers
        assertTrue(viewModel.navigateBack())
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.currentLevel is SyllabusNavigationLevel.Papers)

        // At Papers -> Back to Exams
        assertTrue(viewModel.navigateBack())
        advanceUntilIdle()
        assertEquals(SyllabusNavigationLevel.Exams, viewModel.uiState.value.currentLevel)

        // At Exams -> Back returns false
        assertFalse(viewModel.navigateBack())
    }

    @Test
    fun selectSubtopic_andQuestion_navigatesToQuestionDetail_andStepsBack() = runTest(testDispatcher) {
        val exam = Exam("exam_01", "CTET", "CTET", "", true, 1)
        val paper = Paper("paper_01", "exam_01", "Paper 1", "P1", "", true, 1)
        val subject = Subject("sub_01", "paper_01", "Pedagogy", "CDP", "", true, 1)
        val topic = Topic("top_01", "sub_01", "Principles", "", true, 1)
        val subtopic = Subtopic("subtop_01", "top_01", "Development Stages", "", true, 1)

        fakeRepository.activeExamsFlow.value = listOf(exam)
        fakeRepository.activePapersFlow.value = listOf(paper)
        fakeRepository.activeSubjectsFlow.value = listOf(subject)
        fakeRepository.activeTopicsFlow.value = listOf(topic)
        fakeRepository.subtopicsWithDetailsFlow.value = emptyList()

        viewModel = SyllabusViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.selectExam(exam)
        viewModel.selectPaper(paper)
        viewModel.selectSubject(subject)
        viewModel.selectTopic(topic)
        viewModel.selectSubtopic(subtopic)
        advanceUntilIdle()

        // Should be at Questions level
        val questionsLevel = viewModel.uiState.value.currentLevel as SyllabusNavigationLevel.Questions
        assertEquals("subtop_01", questionsLevel.subtopic.id)

        // Navigate to QuestionDetail
        viewModel.selectQuestion("q_01", 1)
        advanceUntilIdle()

        val detailLevel = viewModel.uiState.value.currentLevel as SyllabusNavigationLevel.QuestionDetail
        assertEquals("q_01", detailLevel.questionId)
        assertEquals(1, detailLevel.questionIndex)

        // Step back -> should return to Questions level
        assertTrue(viewModel.navigateBack())
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.currentLevel is SyllabusNavigationLevel.Questions)

        // Step back again -> should return to Subtopics level
        assertTrue(viewModel.navigateBack())
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.currentLevel is SyllabusNavigationLevel.Subtopics)
    }

    @Test
    fun startQuestionPractice_navigatesToPractice_andStepsBackToQuestionDetail() = runTest(testDispatcher) {
        val exam = Exam("exam_01", "CTET", "CTET", "", true, 1)
        val paper = Paper("paper_01", "exam_01", "Paper 1", "P1", "", true, 1)
        val subject = Subject("sub_01", "paper_01", "Pedagogy", "CDP", "", true, 1)
        val topic = Topic("top_01", "sub_01", "Principles", "", true, 1)
        val subtopic = Subtopic("subtop_01", "top_01", "Development Stages", "", true, 1)

        fakeRepository.activeExamsFlow.value = listOf(exam)
        fakeRepository.activePapersFlow.value = listOf(paper)
        fakeRepository.activeSubjectsFlow.value = listOf(subject)
        fakeRepository.activeTopicsFlow.value = listOf(topic)
        fakeRepository.subtopicsWithDetailsFlow.value = emptyList()

        viewModel = SyllabusViewModel(fakeRepository)
        advanceUntilIdle()

        viewModel.selectExam(exam)
        viewModel.selectPaper(paper)
        viewModel.selectSubject(subject)
        viewModel.selectTopic(topic)
        viewModel.selectSubtopic(subtopic)
        viewModel.selectQuestion("q_01", 1)
        advanceUntilIdle()

        // From QuestionDetail, start practice
        viewModel.startQuestionPractice("q_01", 1)
        advanceUntilIdle()

        val practiceLevel = viewModel.uiState.value.currentLevel as SyllabusNavigationLevel.QuestionPractice
        assertEquals("q_01", practiceLevel.questionId)
        assertEquals(1, practiceLevel.questionIndex)

        // Stepping back should return to QuestionDetail
        assertTrue(viewModel.navigateBack())
        advanceUntilIdle()

        val returnedDetailLevel = viewModel.uiState.value.currentLevel as SyllabusNavigationLevel.QuestionDetail
        assertEquals("q_01", returnedDetailLevel.questionId)
        assertEquals(1, returnedDetailLevel.questionIndex)
    }

    @Test
    fun errorState_andRetry_reloadsLevel() = runTest(testDispatcher) {
        fakeRepository.throwExamsError = true

        viewModel = SyllabusViewModel(fakeRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.examsState is SyllabusContentState.Error)

        fakeRepository.throwExamsError = false
        fakeRepository.activeExamsFlow.value = listOf(Exam("e1", "Exam 1", "E1", "", true, 1))

        viewModel.retryCurrentLevel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.examsState is SyllabusContentState.Success)
    }
}

/**
 * Lightweight in-memory fake implementation of [EducationalRepository] for unit testing.
 */
private class FakeEducationalRepository : com.example.testutil.TestEducationalRepository() {
    val activeExamsFlow = MutableStateFlow<List<Exam>>(emptyList())
    val activePapersFlow = MutableStateFlow<List<Paper>>(emptyList())
    val activeSubjectsFlow = MutableStateFlow<List<Subject>>(emptyList())
    val activeTopicsFlow = MutableStateFlow<List<Topic>>(emptyList())
    val subtopicsWithDetailsFlow = MutableStateFlow<List<SubtopicWithDetails>>(emptyList())
    var throwExamsError = false

    override fun observeActiveExams(): Flow<List<Exam>> {
        if (throwExamsError) {
            return flow { throw RuntimeException("Simulated error") }
        }
        return activeExamsFlow
    }

    override fun observeActivePapersByExamId(examId: String): Flow<List<Paper>> = activePapersFlow
    override fun observeActiveSubjectsByPaperId(paperId: String): Flow<List<Subject>> = activeSubjectsFlow
    override fun observeActiveTopicsBySubjectId(subjectId: String): Flow<List<Topic>> = activeTopicsFlow
    override fun observeActiveSubtopicsWithDetailsByTopicId(topicId: String): Flow<List<SubtopicWithDetails>> = subtopicsWithDetailsFlow
    override fun observeAllExams(): Flow<List<Exam>> = activeExamsFlow
    override fun observePapersByExamId(examId: String): Flow<List<Paper>> = activePapersFlow
    override fun observeSubjectsByPaperId(paperId: String): Flow<List<Subject>> = activeSubjectsFlow
    override fun observeTopicsBySubjectId(subjectId: String): Flow<List<Topic>> = activeTopicsFlow
}
