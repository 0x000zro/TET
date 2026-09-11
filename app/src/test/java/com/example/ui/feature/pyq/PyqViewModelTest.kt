package com.example.ui.feature.pyq

import com.example.domain.model.Exam
import com.example.domain.model.Paper
import com.example.domain.model.Question
import com.example.domain.model.QuestionOption
import com.example.domain.model.Subtopic
import com.example.domain.model.pyq.PreviousYearQuestion
import com.example.domain.model.pyq.PyqVerificationStatus
import com.example.testutil.TestEducationalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying [PyqViewModel] state transitions, multi-dimensional filtering,
 * selection flows, error handling, and retry capability (Step 15).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PyqViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: TestEducationalRepository
    private lateinit var viewModel: PyqViewModel

    private val testExam1 = Exam("exam_1", "CTET Exam", "CTET", isActive = true)
    private val testExam2 = Exam("exam_2", "UPTET Exam", "UPTET", isActive = true)

    private val testPaper1 = Paper("paper_1", "exam_1", "Paper 1 (Primary)", "P1", isActive = true)
    private val testPaper2 = Paper("paper_2", "exam_2", "Paper 2 (Upper Primary)", "P2", isActive = true)

    private val testSubtopic1 = Subtopic("sub_1", "topic_1", "Child Development", isActive = true)

    private val testQuestion1 = Question(
        id = "q_1",
        subtopicId = "sub_1",
        questionText = "What is the primary stage of Piaget's theory?",
        options = listOf(
            QuestionOption("opt_1", "q_1", "Sensorimotor", 1, true),
            QuestionOption("opt_2", "q_1", "Preoperational", 2, false)
        )
    )

    private val testQuestion2 = Question(
        id = "q_2",
        subtopicId = "sub_1",
        questionText = "Which principle emphasizes learning by doing?",
        options = listOf(
            QuestionOption("opt_3", "q_2", "Constructivism", 1, true),
            QuestionOption("opt_4", "q_2", "Behaviorism", 2, false)
        )
    )

    private val testQuestion3 = Question(
        id = "q_3",
        subtopicId = "sub_1",
        questionText = "What does ZPD stand for in Vygotsky's theory?",
        options = listOf(
            QuestionOption("opt_5", "q_3", "Zone of Proximal Development", 1, true),
            QuestionOption("opt_6", "q_3", "Zone of Potential Development", 2, false)
        )
    )

    private val testPyq1 = PreviousYearQuestion(
        id = "pyq_1",
        questionId = "q_1",
        examId = "exam_1",
        paperId = "paper_1",
        year = 2023,
        session = "Shift 1",
        source = "Official CTET Jan 2023",
        verificationStatus = PyqVerificationStatus.VERIFIED
    )

    private val testPyq2 = PreviousYearQuestion(
        id = "pyq_2",
        questionId = "q_2",
        examId = "exam_1",
        paperId = "paper_1",
        year = 2022,
        session = "Shift 2",
        source = "Official CTET Dec 2022",
        verificationStatus = PyqVerificationStatus.UNVERIFIED
    )

    private val testPyq3 = PreviousYearQuestion(
        id = "pyq_3",
        questionId = "q_3",
        examId = "exam_2",
        paperId = "paper_2",
        year = 2023,
        session = "Morning",
        source = "Official UPTET 2023",
        verificationStatus = PyqVerificationStatus.VERIFIED
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = TestEducationalRepository()

        // Seed metadata lookups
        fakeRepository.examsMap[testExam1.id] = testExam1
        fakeRepository.examsMap[testExam2.id] = testExam2
        fakeRepository.papersMap[testPaper1.id] = testPaper1
        fakeRepository.papersMap[testPaper2.id] = testPaper2
        fakeRepository.subtopicsMap[testSubtopic1.id] = testSubtopic1

        fakeRepository.questionsMap[testQuestion1.id] = testQuestion1
        fakeRepository.questionsMap[testQuestion2.id] = testQuestion2
        fakeRepository.questionsMap[testQuestion3.id] = testQuestion3
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state before coroutines execute is Loading`() {
        viewModel = PyqViewModel(repository = fakeRepository)
        assertEquals(PyqUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `empty database produces Empty state without active filter`() = runTest(testDispatcher) {
        viewModel = PyqViewModel(repository = fakeRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Empty state, but was $state", state is PyqUiState.Empty)
        assertFalse((state as PyqUiState.Empty).isFiltered)
    }

    @Test
    fun `populated PYQs produce Success state with enriched questions and sorted order`() = runTest(testDispatcher) {
        fakeRepository.pyqList.addAll(listOf(testPyq2, testPyq1, testPyq3)) // Unordered input

        viewModel = PyqViewModel(repository = fakeRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Success state, but was $state", state is PyqUiState.Success)
        val success = state as PyqUiState.Success

        assertEquals(3, success.items.size)
        // Deterministic sorting: 2023 "Morning", 2023 "Shift 1", 2022 "Shift 2"
        assertEquals(2023, success.items[0].pyq.year)
        assertEquals("Morning", success.items[0].pyq.session)
        assertEquals("UPTET Exam", success.items[0].examName)
        assertEquals("Paper 2 (Upper Primary)", success.items[0].paperName)

        assertEquals(2023, success.items[1].pyq.year)
        assertEquals("Shift 1", success.items[1].pyq.session)
        assertEquals("CTET Exam", success.items[1].examName)
        assertEquals("Paper 1 (Primary)", success.items[1].paperName)
        assertEquals("Child Development", success.items[1].subtopicName)

        assertEquals(2022, success.items[2].pyq.year)
    }

    @Test
    fun `filterByYear filters items and updates uiState`() = runTest(testDispatcher) {
        fakeRepository.pyqList.addAll(listOf(testPyq1, testPyq2, testPyq3))

        viewModel = PyqViewModel(repository = fakeRepository)
        advanceUntilIdle()

        viewModel.filterByYear(2022)
        advanceUntilIdle()

        val state = viewModel.uiState.value as PyqUiState.Success
        assertEquals(2022, state.selectedYear)
        assertEquals(1, state.items.size)
        assertEquals("pyq_2", state.items[0].pyq.id)
    }

    @Test
    fun `filterByExam filters items and available papers`() = runTest(testDispatcher) {
        fakeRepository.pyqList.addAll(listOf(testPyq1, testPyq2, testPyq3))

        viewModel = PyqViewModel(repository = fakeRepository)
        advanceUntilIdle()

        viewModel.filterByExam("exam_2")
        advanceUntilIdle()

        val state = viewModel.uiState.value as PyqUiState.Success
        assertEquals("exam_2", state.selectedExamId)
        assertEquals(1, state.items.size)
        assertEquals("pyq_3", state.items[0].pyq.id)
        assertEquals("UPTET Exam", state.items[0].examName)
    }

    @Test
    fun `filterByVerificationStatus filters by VERIFIED`() = runTest(testDispatcher) {
        fakeRepository.pyqList.addAll(listOf(testPyq1, testPyq2, testPyq3))

        viewModel = PyqViewModel(repository = fakeRepository)
        advanceUntilIdle()

        viewModel.filterByVerificationStatus(PyqVerificationStatus.VERIFIED)
        advanceUntilIdle()

        val state = viewModel.uiState.value as PyqUiState.Success
        assertEquals(PyqVerificationStatus.VERIFIED, state.selectedVerificationStatus)
        assertEquals(2, state.items.size)
        assertTrue(state.items.all { it.pyq.verificationStatus == PyqVerificationStatus.VERIFIED })
    }

    @Test
    fun `filterBySession filters items by specific session`() = runTest(testDispatcher) {
        fakeRepository.pyqList.addAll(listOf(testPyq1, testPyq2, testPyq3))

        viewModel = PyqViewModel(repository = fakeRepository)
        advanceUntilIdle()

        viewModel.filterBySession("Shift 1")
        advanceUntilIdle()

        val state = viewModel.uiState.value as PyqUiState.Success
        assertEquals("Shift 1", state.selectedSession)
        assertEquals(1, state.items.size)
        assertEquals("pyq_1", state.items[0].pyq.id)
    }

    @Test
    fun `clearFilters resets all filters to null and returns all items`() = runTest(testDispatcher) {
        fakeRepository.pyqList.addAll(listOf(testPyq1, testPyq2, testPyq3))

        viewModel = PyqViewModel(repository = fakeRepository)
        advanceUntilIdle()

        viewModel.filterByYear(2022)
        viewModel.filterByVerificationStatus(PyqVerificationStatus.UNVERIFIED)
        advanceUntilIdle()

        var state = viewModel.uiState.value as PyqUiState.Success
        assertEquals(1, state.items.size)

        viewModel.clearFilters()
        advanceUntilIdle()

        state = viewModel.uiState.value as PyqUiState.Success
        assertNull(state.selectedYear)
        assertNull(state.selectedVerificationStatus)
        assertFalse(state.hasActiveFilters)
        assertEquals(3, state.items.size)
    }

    @Test
    fun `filter matching nothing produces Empty state with isFiltered true`() = runTest(testDispatcher) {
        fakeRepository.pyqList.addAll(listOf(testPyq1, testPyq2))

        viewModel = PyqViewModel(repository = fakeRepository)
        advanceUntilIdle()

        viewModel.filterByYear(2010) // Non-existent year
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PyqUiState.Empty)
        assertTrue((state as PyqUiState.Empty).isFiltered)
    }

    @Test
    fun `selectPyqForReview sets selectedPyq and clearSelectedPyq clears it`() = runTest(testDispatcher) {
        fakeRepository.pyqList.add(testPyq1)

        viewModel = PyqViewModel(repository = fakeRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value as PyqUiState.Success
        val summary = state.items.first()

        viewModel.selectPyqForReview(summary)
        assertNotNull(viewModel.selectedPyq.value)
        assertEquals("pyq_1", viewModel.selectedPyq.value?.pyq?.id)

        viewModel.clearSelectedPyq()
        assertNull(viewModel.selectedPyq.value)
    }

    @Test
    fun `repository error produces Error state and retry reloads data`() = runTest(testDispatcher) {
        fakeRepository.shouldThrowOnPyq = true

        viewModel = PyqViewModel(repository = fakeRepository)
        advanceUntilIdle()

        val errorState = viewModel.uiState.value
        assertTrue("Expected Error state, but was $errorState", errorState is PyqUiState.Error)

        // Fix failure condition and retry
        fakeRepository.shouldThrowOnPyq = false
        fakeRepository.pyqList.add(testPyq1)

        viewModel.retry()
        advanceUntilIdle()

        val successState = viewModel.uiState.value
        assertTrue("Expected Success state after retry, but was $successState", successState is PyqUiState.Success)
        assertEquals(1, (successState as PyqUiState.Success).items.size)
    }
}
