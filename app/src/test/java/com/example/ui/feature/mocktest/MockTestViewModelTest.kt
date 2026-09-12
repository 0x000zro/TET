package com.example.ui.feature.mocktest

import com.example.domain.model.Exam
import com.example.domain.model.Paper
import com.example.domain.model.Question
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionOption
import com.example.domain.model.QuestionType
import com.example.domain.model.Subject
import com.example.domain.model.Subtopic
import com.example.domain.model.Topic
import com.example.domain.model.mocktest.MockTestConfiguration
import com.example.domain.model.mocktest.MockTestScope
import com.example.domain.usecase.mocktest.CreateMockTestSessionUseCase
import com.example.domain.usecase.mocktest.FinishMockTestSessionUseCase
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
 * Focused unit tests for [MockTestViewModel] verifying:
 * - initial UI state
 * - question loading / overview
 * - starting test
 * - option selection
 * - answer changing
 * - answer clearing
 * - next/previous navigation
 * - finish confirmation dialog state
 * - result evaluation state
 * - error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MockTestViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var repository: TestEducationalRepository
    private lateinit var createUseCase: CreateMockTestSessionUseCase
    private lateinit var finishUseCase: FinishMockTestSessionUseCase
    private lateinit var viewModel: MockTestViewModel

    private val testExam = Exam("exam_1", "CTET Exam", "CTET", isActive = true)
    private val testPaper = Paper("paper_1", "exam_1", "Paper 1", "P1", isActive = true)
    private val testSubject = Subject("subj_1", "paper_1", "Child Development", "CDP", isActive = true)
    private val testTopic = Topic("topic_1", "subj_1", "Cognitive Development", isActive = true)
    private val testSubtopic = Subtopic("sub_1", "topic_1", "Piaget Theory", isActive = true)

    private fun createQuestion(id: String, sortOrder: Int = 0, correctIndex: Int = 0): Question {
        return Question(
            id = id,
            subtopicId = "sub_1",
            questionText = "Question text $id",
            questionType = QuestionType.MCQ_SINGLE,
            difficulty = QuestionDifficulty.MEDIUM,
            explanation = "Explanation $id",
            isActive = true,
            sortOrder = sortOrder,
            options = (0..3).map { i ->
                QuestionOption(
                    id = "opt_${id}_$i",
                    questionId = id,
                    optionText = "Option $i for $id",
                    sortOrder = i,
                    isCorrect = (i == correctIndex)
                )
            }
        )
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = TestEducationalRepository()
        createUseCase = CreateMockTestSessionUseCase(repository)
        finishUseCase = FinishMockTestSessionUseCase(repository)

        repository.examsMap[testExam.id] = testExam
        repository.papersMap[testPaper.id] = testPaper
        repository.subjectsMap[testSubject.id] = testSubject
        repository.topicsMap[testTopic.id] = testTopic
        repository.subtopicsMap[testSubtopic.id] = testSubtopic

        viewModel = MockTestViewModel(
            repository = repository,
            createSessionUseCase = createUseCase,
            finishSessionUseCase = finishUseCase
        )
    }

    @After
    fun tearDown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoading() {
        assertTrue(viewModel.uiState.value is MockTestUiState.Loading)
    }

    @Test
    fun loadTestConfiguration_whenNoQuestions_emitsEmpty() = runTest {
        viewModel.loadTestConfiguration()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MockTestUiState.Empty)
    }

    @Test
    fun loadTestConfiguration_withQuestions_emitsConfigurationOverview() = runTest {
        val q1 = createQuestion("q_1")
        val q2 = createQuestion("q_2")
        repository.questionsMap[q1.id] = q1
        repository.questionsMap[q2.id] = q2

        viewModel.loadTestConfiguration()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MockTestUiState.ConfigurationOverview)
        val overview = state as MockTestUiState.ConfigurationOverview
        assertEquals("exam_1", overview.configuration.examId)
        assertEquals("paper_1", overview.configuration.paperId)
        assertEquals(2, overview.availableQuestionsCount)
    }

    @Test
    fun startTest_createsActiveTestState() = runTest {
        val q1 = createQuestion("q_1", sortOrder = 1)
        val q2 = createQuestion("q_2", sortOrder = 2)
        repository.questionsMap[q1.id] = q1
        repository.questionsMap[q2.id] = q2

        viewModel.loadTestConfiguration()
        advanceUntilIdle()

        viewModel.startTest()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MockTestUiState.ActiveTest)
        val active = state as MockTestUiState.ActiveTest
        assertEquals(1, active.currentQuestionIndex)
        assertEquals(2, active.totalQuestions)
        assertEquals("q_1", active.currentQuestion.id)
        assertNull(active.selectedOptionId)
        assertEquals(0, active.answeredCount)
        assertEquals(2, active.unansweredCount)
        assertFalse(active.hasPrevious)
        assertTrue(active.hasNext)
        assertFalse(active.isLastQuestion)
    }

    @Test
    fun selectAnswer_and_changeAnswer_updatesSelection() = runTest {
        val q1 = createQuestion("q_1", sortOrder = 1)
        repository.questionsMap[q1.id] = q1

        viewModel.loadTestConfiguration()
        advanceUntilIdle()
        viewModel.startTest()
        advanceUntilIdle()

        // Select option 0
        viewModel.selectAnswer("opt_q_1_0")
        var active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals("opt_q_1_0", active.selectedOptionId)
        assertEquals(1, active.answeredCount)
        assertEquals(0, active.unansweredCount)

        // Change to option 2
        viewModel.selectAnswer("opt_q_1_2")
        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals("opt_q_1_2", active.selectedOptionId)
        assertEquals(1, active.answeredCount)
    }

    @Test
    fun clearAnswer_removesSelection() = runTest {
        val q1 = createQuestion("q_1", sortOrder = 1)
        repository.questionsMap[q1.id] = q1

        viewModel.loadTestConfiguration()
        advanceUntilIdle()
        viewModel.startTest()
        advanceUntilIdle()

        viewModel.selectAnswer("opt_q_1_0")
        var active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertNotNull(active.selectedOptionId)

        viewModel.clearAnswer()
        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertNull(active.selectedOptionId)
        assertEquals(0, active.answeredCount)
        assertEquals(1, active.unansweredCount)
    }

    @Test
    fun navigation_nextAndPrevious_updatesCurrentQuestion() = runTest {
        val q1 = createQuestion("q_1", sortOrder = 1)
        val q2 = createQuestion("q_2", sortOrder = 2)
        repository.questionsMap[q1.id] = q1
        repository.questionsMap[q2.id] = q2

        viewModel.loadTestConfiguration()
        advanceUntilIdle()
        viewModel.startTest()
        advanceUntilIdle()

        // Initially at Q1
        var active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals("q_1", active.currentQuestion.id)
        assertEquals(1, active.currentQuestionIndex)
        assertFalse(active.hasPrevious)
        assertTrue(active.hasNext)

        // Move to Q2
        viewModel.nextQuestion()
        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals("q_2", active.currentQuestion.id)
        assertEquals(2, active.currentQuestionIndex)
        assertTrue(active.hasPrevious)
        assertFalse(active.hasNext)
        assertTrue(active.isLastQuestion)

        // Move back to Q1
        viewModel.previousQuestion()
        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals("q_1", active.currentQuestion.id)
        assertEquals(1, active.currentQuestionIndex)
    }

    @Test
    fun finishConfirmation_toggleControlsDialog() = runTest {
        val q1 = createQuestion("q_1", sortOrder = 1)
        repository.questionsMap[q1.id] = q1

        viewModel.loadTestConfiguration()
        advanceUntilIdle()
        viewModel.startTest()
        advanceUntilIdle()

        var active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertFalse(active.showFinishConfirmation)

        viewModel.setFinishConfirmationVisible(true)
        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertTrue(active.showFinishConfirmation)

        viewModel.setFinishConfirmationVisible(false)
        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertFalse(active.showFinishConfirmation)
    }

    @Test
    fun confirmFinish_evaluatesAndEmitsResultSummary() = runTest {
        val q1 = createQuestion("q_1", sortOrder = 1, correctIndex = 0)
        val q2 = createQuestion("q_2", sortOrder = 2, correctIndex = 1)
        repository.questionsMap[q1.id] = q1
        repository.questionsMap[q2.id] = q2

        viewModel.loadTestConfiguration()
        advanceUntilIdle()
        viewModel.startTest()
        advanceUntilIdle()

        // Answer Q1 correctly
        viewModel.selectAnswer("opt_q_1_0")

        // Finish without answering Q2
        viewModel.confirmFinish()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MockTestUiState.ResultSummary)
        val summary = state as MockTestUiState.ResultSummary
        assertEquals(2, summary.result.totalQuestions)
        assertEquals(1, summary.result.answeredQuestions)
        assertEquals(1, summary.result.correctAnswers)
        assertEquals(0, summary.result.incorrectAnswers)
        assertEquals(1, summary.result.unansweredQuestions)
        assertEquals(50.0, summary.result.scorePercentage, 0.01)
    }

    @Test
    fun abandonConfirmation_toggleControlsDialog() = runTest {
        val q1 = createQuestion("q_1", sortOrder = 1)
        repository.questionsMap[q1.id] = q1

        viewModel.loadTestConfiguration()
        advanceUntilIdle()
        viewModel.startTest()
        advanceUntilIdle()

        var active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertFalse(active.showAbandonConfirmation)

        viewModel.setAbandonConfirmationVisible(true)
        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertTrue(active.showAbandonConfirmation)

        viewModel.setAbandonConfirmationVisible(false)
        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertFalse(active.showAbandonConfirmation)
    }

    @Test
    fun loadTestConfiguration_withInsufficientQuestionsForCustomConfig_emitsError() = runTest {
        val q1 = createQuestion("q_1", sortOrder = 1)
        repository.questionsMap[q1.id] = q1

        val customConfig = MockTestConfiguration(
            id = "cfg_high",
            title = "High Count Config",
            examId = "exam_1",
            paperId = "paper_1",
            scope = MockTestScope.FullPaper,
            questionCount = 10, // requires 10, only 1 available
            durationMinutes = 15
        )

        viewModel.loadTestConfiguration(customConfig)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MockTestUiState.Error)
        val err = state as MockTestUiState.Error
        assertTrue(err.message.contains("Insufficient active questions"))
    }

    @Test
    fun loadTestConfiguration_withInvalidScope_handlesErrorSafely() = runTest {
        val invalidConfig = MockTestConfiguration(
            id = "cfg_err",
            title = "Invalid Scope Config",
            examId = "exam_1",
            paperId = "paper_1",
            scope = MockTestScope.SubjectScope(""), // blank subject ID
            questionCount = 5,
            durationMinutes = 10
        )

        viewModel.loadTestConfiguration(invalidConfig)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MockTestUiState.Error)
    }
}
