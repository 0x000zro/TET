package com.example.ui.feature.mocktest

import com.example.data.repository.InMemoryMockTestPerformanceRepository
import com.example.domain.model.Exam
import com.example.domain.model.Paper
import com.example.domain.model.Question
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionOption
import com.example.domain.model.QuestionType
import com.example.domain.model.Subject
import com.example.domain.model.Subtopic
import com.example.domain.model.Topic
import com.example.domain.model.mocktest.MockTestPerformanceRating
import com.example.domain.model.mocktest.MockTestQuestionOutcomeStatus
import com.example.domain.usecase.mocktest.CreateMockTestSessionUseCase
import com.example.domain.usecase.mocktest.FinishMockTestSessionUseCase
import com.example.domain.usecase.mocktest.RecordMockTestPerformanceUseCase
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
 * Step 21 Verification: Mock Test Result & Local Performance Foundation.
 *
 * Verifies:
 * 1. Detailed ResultSummary metrics (Total, Answered, Unanswered, Correct, Incorrect, Percentage, Time Used, Time Remaining).
 * 2. Deterministic Performance Rating (Excellent >= 75%, Good >= 50%, Needs Improvement < 50%).
 * 3. Question outcomes correctly categorized as Correct, Incorrect, and Unanswered.
 * 4. MockTestPerformance record creation upon test finish, strictly segregated from PracticeAttempt.
 * 5. Abandoned tests do NOT record performance.
 * 6. Restart creates a fresh session without reusing completed session/result.
 * 7. Return to overview resets active state.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MockTestResultPerformanceTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var repository: TestEducationalRepository
    private lateinit var performanceRepository: InMemoryMockTestPerformanceRepository
    private lateinit var createUseCase: CreateMockTestSessionUseCase
    private lateinit var finishUseCase: FinishMockTestSessionUseCase
    private lateinit var recordPerformanceUseCase: RecordMockTestPerformanceUseCase
    private lateinit var viewModel: MockTestViewModel

    private var currentTime: Long = 1_000_000L

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
        repository.examsMap[testExam.id] = testExam
        repository.papersMap[testPaper.id] = testPaper
        repository.subjectsMap[testSubject.id] = testSubject
        repository.topicsMap[testTopic.id] = testTopic
        repository.subtopicsMap[testSubtopic.id] = testSubtopic

        performanceRepository = InMemoryMockTestPerformanceRepository()
        createUseCase = CreateMockTestSessionUseCase(repository)
        finishUseCase = FinishMockTestSessionUseCase(repository)
        recordPerformanceUseCase = RecordMockTestPerformanceUseCase(performanceRepository)

        currentTime = 1_000_000L
        viewModel = MockTestViewModel(
            repository = repository,
            createSessionUseCase = createUseCase,
            finishSessionUseCase = finishUseCase,
            performanceRepository = performanceRepository,
            recordPerformanceUseCase = recordPerformanceUseCase,
            timeProvider = { currentTime }
        )
    }

    @After
    fun tearDown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    @Test
    fun resultSummary_containsAllRequiredMetricsAndOutcomes() = runTest {
        // Prepare 4 questions
        val q1 = createQuestion("q_1", sortOrder = 1, correctIndex = 0)
        val q2 = createQuestion("q_2", sortOrder = 2, correctIndex = 1)
        val q3 = createQuestion("q_3", sortOrder = 3, correctIndex = 2)
        val q4 = createQuestion("q_4", sortOrder = 4, correctIndex = 3)
        repository.questionsMap[q1.id] = q1
        repository.questionsMap[q2.id] = q2
        repository.questionsMap[q3.id] = q3
        repository.questionsMap[q4.id] = q4

        viewModel.loadTestConfiguration()
        advanceUntilIdle()
        viewModel.startTest()
        advanceUntilIdle()

        // Q1: Answer correctly (opt_q_1_0)
        viewModel.selectAnswer("opt_q_1_0")
        advanceUntilIdle()

        // Q2: Navigate to Q2, answer incorrectly (opt_q_2_0 instead of opt_q_2_1)
        viewModel.nextQuestion()
        advanceUntilIdle()
        viewModel.selectAnswer("opt_q_2_0")
        advanceUntilIdle()

        // Q3: Navigate to Q3, answer correctly (opt_q_3_2)
        viewModel.nextQuestion()
        advanceUntilIdle()
        viewModel.selectAnswer("opt_q_3_2")
        advanceUntilIdle()

        // Q4: Navigate to Q4, leave unanswered
        viewModel.nextQuestion()
        advanceUntilIdle()

        // Fast-forward time by 120 seconds (2 minutes)
        currentTime += 120_000L

        // Finish test
        viewModel.confirmFinish()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should be ResultSummary, but was $state", state is MockTestUiState.ResultSummary)
        val resultSummary = state as MockTestUiState.ResultSummary

        // 1. Result summary metrics
        assertEquals(4, resultSummary.result.totalQuestions)
        assertEquals(3, resultSummary.result.answeredQuestions)
        assertEquals(1, resultSummary.result.unansweredQuestions)
        assertEquals(2, resultSummary.result.correctAnswers)
        assertEquals(1, resultSummary.result.incorrectAnswers)
        assertEquals(50.0, resultSummary.result.scorePercentage, 0.01)

        // Timing metrics
        assertEquals(120L, resultSummary.timeUsedSeconds)
        assertTrue(resultSummary.timeRemainingSeconds >= 0L)

        // 2. Performance interpretation (50% is GOOD)
        assertEquals(MockTestPerformanceRating.GOOD, resultSummary.result.performanceRating)

        // 3. Question outcomes breakdown
        assertEquals(4, resultSummary.result.questionOutcomes.size)

        val o1 = resultSummary.result.questionOutcomes[0]
        assertEquals("q_1", o1.questionId)
        assertEquals(1, o1.questionNumber)
        assertEquals(MockTestQuestionOutcomeStatus.CORRECT, o1.status)

        val o2 = resultSummary.result.questionOutcomes[1]
        assertEquals("q_2", o2.questionId)
        assertEquals(2, o2.questionNumber)
        assertEquals(MockTestQuestionOutcomeStatus.INCORRECT, o2.status)

        val o3 = resultSummary.result.questionOutcomes[2]
        assertEquals("q_3", o3.questionId)
        assertEquals(3, o3.questionNumber)
        assertEquals(MockTestQuestionOutcomeStatus.CORRECT, o3.status)

        val o4 = resultSummary.result.questionOutcomes[3]
        assertEquals("q_4", o4.questionId)
        assertEquals(4, o4.questionNumber)
        assertEquals(MockTestQuestionOutcomeStatus.UNANSWERED, o4.status)

        // 4. Performance record in repository
        val records = performanceRepository.getAllPerformances()
        assertEquals(1, records.size)
        val perf = records.first()
        assertEquals(4, perf.totalQuestions)
        assertEquals(3, perf.answered)
        assertEquals(2, perf.correct)
        assertEquals(1, perf.incorrect)
        assertEquals(1, perf.unanswered)
        assertEquals(50.0, perf.percentage, 0.01)
        assertEquals(120L, perf.timeUsedSeconds)
        assertEquals(MockTestPerformanceRating.GOOD, perf.rating)
    }

    @Test
    fun performanceInterpretation_deterministicRatings() {
        assertEquals(MockTestPerformanceRating.EXCELLENT, MockTestPerformanceRating.fromPercentage(100.0))
        assertEquals(MockTestPerformanceRating.EXCELLENT, MockTestPerformanceRating.fromPercentage(75.0))
        assertEquals(MockTestPerformanceRating.GOOD, MockTestPerformanceRating.fromPercentage(74.9))
        assertEquals(MockTestPerformanceRating.GOOD, MockTestPerformanceRating.fromPercentage(50.0))
        assertEquals(MockTestPerformanceRating.NEEDS_IMPROVEMENT, MockTestPerformanceRating.fromPercentage(49.9))
        assertEquals(MockTestPerformanceRating.NEEDS_IMPROVEMENT, MockTestPerformanceRating.fromPercentage(0.0))
    }

    @Test
    fun abandonTest_doesNotRecordPerformance() = runTest {
        val q1 = createQuestion("q_1", sortOrder = 1)
        repository.questionsMap[q1.id] = q1

        viewModel.loadTestConfiguration()
        advanceUntilIdle()
        viewModel.startTest()
        advanceUntilIdle()

        viewModel.abandonTest()
        advanceUntilIdle()

        val records = performanceRepository.getAllPerformances()
        assertTrue("Abandoned tests must not create performance records", records.isEmpty())
    }

    @Test
    fun restartTest_createsFreshSessionAndResetsCompletedState() = runTest {
        val q1 = createQuestion("q_1", sortOrder = 1, correctIndex = 0)
        repository.questionsMap[q1.id] = q1

        viewModel.loadTestConfiguration()
        advanceUntilIdle()
        viewModel.startTest()
        advanceUntilIdle()

        viewModel.selectAnswer("opt_q_1_0")
        advanceUntilIdle()

        viewModel.confirmFinish()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is MockTestUiState.ResultSummary)

        // Restart test
        viewModel.restartTest()
        advanceUntilIdle()

        val newState = viewModel.uiState.value
        assertTrue("New state after restart must be ActiveTest", newState is MockTestUiState.ActiveTest)
        val activeTest = newState as MockTestUiState.ActiveTest

        // Option should be cleared for fresh session
        assertNull("Selected option must be cleared in new session", activeTest.selectedOptionId)
        assertEquals(1, activeTest.currentQuestionIndex)
    }

    @Test
    fun returnToOverview_clearsActiveSessionAndShowsConfiguration() = runTest {
        val q1 = createQuestion("q_1", sortOrder = 1, correctIndex = 0)
        repository.questionsMap[q1.id] = q1

        viewModel.loadTestConfiguration()
        advanceUntilIdle()
        viewModel.startTest()
        advanceUntilIdle()

        viewModel.confirmFinish()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is MockTestUiState.ResultSummary)

        viewModel.returnToOverview()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State should return to ConfigurationOverview", state is MockTestUiState.ConfigurationOverview)
    }
}
