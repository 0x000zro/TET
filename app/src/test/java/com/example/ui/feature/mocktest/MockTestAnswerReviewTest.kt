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
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
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
 * Focused tests for Step 20 — Mock Test Answer Review & Navigation Polish:
 * - answered count
 * - unanswered count
 * - last-question behavior
 * - finish with all questions answered
 * - finish with unanswered questions
 * - Continue Review
 * - Finish Test
 * - palette navigation after returning from finish confirmation
 * - answers preserved
 * - timer remains active during review
 * - automatic expiration still finishes normally
 * - abandon still works
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MockTestAnswerReviewTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var repository: TestEducationalRepository
    private lateinit var createUseCase: CreateMockTestSessionUseCase
    private lateinit var finishUseCase: FinishMockTestSessionUseCase
    private lateinit var viewModel: MockTestViewModel

    private var virtualTimeMillis: Long = 200_000L

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

    private val testConfig = MockTestConfiguration(
        id = "cfg_review_test",
        title = "Review Test Configuration",
        examId = "exam_1",
        paperId = "paper_1",
        scope = MockTestScope.FullPaper,
        questionCount = 5,
        durationMinutes = 10
    )

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

        (1..5).forEach { i ->
            val q = createQuestion("q_$i", sortOrder = i, correctIndex = 0)
            repository.questionsMap[q.id] = q
        }

        virtualTimeMillis = 200_000L

        viewModel = MockTestViewModel(
            repository = repository,
            createSessionUseCase = createUseCase,
            finishSessionUseCase = finishUseCase,
            timeProvider = { virtualTimeMillis }
        )
    }

    @After
    fun tearDown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    private fun TestScope.startTestSession(): MockTestUiState.ActiveTest {
        viewModel.loadTestConfiguration(testConfig)
        advanceUntilIdle()
        viewModel.startTest()
        advanceUntilIdle()
        return viewModel.uiState.value as MockTestUiState.ActiveTest
    }

    @Test
    fun answeredCount_and_unansweredCount_stayAccurateAndSynchronized() = runTest {
        val initial = startTestSession()
        assertEquals(0, initial.answeredCount)
        assertEquals(5, initial.unansweredCount)
        assertEquals(5, initial.totalQuestions)

        // Answer Q1
        viewModel.selectAnswer("opt_q_1_0")
        advanceUntilIdle()

        var active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals(1, active.answeredCount)
        assertEquals(4, active.unansweredCount)

        // Navigate to Q2 via Next and answer it
        viewModel.nextQuestion()
        viewModel.selectAnswer("opt_q_2_1")
        advanceUntilIdle()

        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals(2, active.answeredCount)
        assertEquals(3, active.unansweredCount)

        // Clear answer on Q2
        viewModel.clearAnswer()
        advanceUntilIdle()

        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals(1, active.answeredCount)
        assertEquals(4, active.unansweredCount)
    }

    @Test
    fun lastQuestionBehavior_nextButtonDoesNotAdvanceBeyondEnd() = runTest {
        startTestSession()

        // Jump to last question (index 4 = Q5)
        viewModel.navigateToQuestion(4)
        advanceUntilIdle()

        val active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals(5, active.currentQuestionIndex)
        assertEquals(5, active.totalQuestions)
        assertTrue(active.isLastQuestion)
        assertFalse(active.hasNext)
        assertTrue(active.hasPrevious)

        // Attempting to advance next does not exceed index or throw
        viewModel.nextQuestion()
        advanceUntilIdle()

        val afterNext = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals(5, afterNext.currentQuestionIndex)
        assertEquals("q_5", afterNext.currentQuestion.id)
    }

    @Test
    fun finishWithAllQuestionsAnswered_showsConfirmationAndPreservesAnswers() = runTest {
        startTestSession()

        // Answer all 5 questions
        (0..4).forEach { i ->
            viewModel.navigateToQuestion(i)
            viewModel.selectAnswer("opt_q_${i + 1}_0")
            advanceUntilIdle()
        }

        var active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals(5, active.answeredCount)
        assertEquals(0, active.unansweredCount)

        // Request finish
        viewModel.setFinishConfirmationVisible(true)
        advanceUntilIdle()

        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertTrue(active.showFinishConfirmation)
        assertEquals(0, active.unansweredCount)
        assertEquals(5, active.answeredCount)
    }

    @Test
    fun finishWithUnansweredQuestions_indicatesUnansweredRemaining() = runTest {
        startTestSession()

        // Answer only Q1 and Q3
        viewModel.navigateToQuestion(0)
        viewModel.selectAnswer("opt_q_1_0")
        advanceUntilIdle()

        viewModel.navigateToQuestion(2)
        viewModel.selectAnswer("opt_q_3_2")
        advanceUntilIdle()

        // Request finish
        viewModel.setFinishConfirmationVisible(true)
        advanceUntilIdle()

        val active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertTrue(active.showFinishConfirmation)
        assertEquals(2, active.answeredCount)
        assertEquals(3, active.unansweredCount)
        assertTrue(active.unansweredCount > 0)
    }

    @Test
    fun continueReview_closesConfirmation_keepsTestActive_and_timerRunning() = runTest {
        startTestSession()

        viewModel.navigateToQuestion(1) // on Q2
        viewModel.selectAnswer("opt_q_2_0")
        advanceUntilIdle()

        // Request finish dialog
        viewModel.setFinishConfirmationVisible(true)
        advanceUntilIdle()
        assertTrue((viewModel.uiState.value as MockTestUiState.ActiveTest).showFinishConfirmation)

        // Continue Review (dismiss dialog)
        viewModel.setFinishConfirmationVisible(false)
        advanceUntilIdle()

        val active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertFalse(active.showFinishConfirmation)
        assertEquals(2, active.currentQuestionIndex)
        assertEquals("opt_q_2_0", active.selectedOptionId)
        assertTrue(viewModel.isTimerActive)
    }

    @Test
    fun finishTest_transitionsToResultSummary_and_stopsTimer() = runTest {
        startTestSession()

        // Answer Q1 correctly and Q2 incorrectly
        viewModel.navigateToQuestion(0)
        viewModel.selectAnswer("opt_q_1_0")
        advanceUntilIdle()

        viewModel.navigateToQuestion(1)
        viewModel.selectAnswer("opt_q_2_3")
        advanceUntilIdle()

        // Confirm Finish
        viewModel.confirmFinish()
        advanceUntilIdle()

        assertFalse(viewModel.isTimerActive)
        val state = viewModel.uiState.value
        assertTrue(state is MockTestUiState.ResultSummary)

        val result = (state as MockTestUiState.ResultSummary).result
        assertEquals(5, result.totalQuestions)
        assertEquals(2, result.answeredQuestions)
        assertEquals(1, result.correctAnswers)
        assertEquals(1, result.incorrectAnswers)
        assertEquals(3, result.unansweredQuestions)
        assertEquals(20.0, result.scorePercentage, 0.01)
    }

    @Test
    fun paletteNavigationAfterReturningFromFinishConfirmation_preservesAnswers() = runTest {
        startTestSession()

        // Answer Q1, Q4
        viewModel.navigateToQuestion(0)
        viewModel.selectAnswer("opt_q_1_0")
        advanceUntilIdle()

        viewModel.navigateToQuestion(3)
        viewModel.selectAnswer("opt_q_4_2")
        advanceUntilIdle()

        // Trigger finish confirmation and return via Continue Review
        viewModel.setFinishConfirmationVisible(true)
        advanceUntilIdle()
        viewModel.setFinishConfirmationVisible(false)
        advanceUntilIdle()

        // Use Palette to navigate to Q1
        viewModel.navigateToQuestion(0)
        advanceUntilIdle()

        var active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals("opt_q_1_0", active.selectedOptionId)

        // Use Palette to navigate to Q4
        viewModel.navigateToQuestion(3)
        advanceUntilIdle()

        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals("opt_q_4_2", active.selectedOptionId)

        // Answers and counts are intact
        assertEquals(2, active.answeredCount)
        assertEquals(3, active.unansweredCount)
    }

    @Test
    fun timerRemainsActiveDuringReview() = runTest {
        val timerVm = MockTestViewModel(
            repository = repository,
            createSessionUseCase = createUseCase,
            finishSessionUseCase = finishUseCase,
            timeProvider = { virtualTimeMillis },
            timerDispatcher = testDispatcher
        )
        try {
            timerVm.loadTestConfiguration(testConfig)
            testDispatcher.scheduler.runCurrent()
            timerVm.startTest()
            testDispatcher.scheduler.runCurrent()

            assertTrue(timerVm.isTimerActive)

            // Tick 30 seconds
            virtualTimeMillis += 30_000L
            testDispatcher.scheduler.advanceTimeBy(30_000L)
            testDispatcher.scheduler.runCurrent()

            var active = timerVm.uiState.value as MockTestUiState.ActiveTest
            assertEquals(570L, active.remainingSeconds)

            // Open finish confirmation (student pauses to decide)
            timerVm.setFinishConfirmationVisible(true)
            testDispatcher.scheduler.runCurrent()

            // Another 15 seconds pass during review deliberation
            virtualTimeMillis += 15_000L
            testDispatcher.scheduler.advanceTimeBy(15_000L)
            testDispatcher.scheduler.runCurrent()

            active = timerVm.uiState.value as MockTestUiState.ActiveTest
            assertEquals(555L, active.remainingSeconds)

            // Student chooses Continue Review
            timerVm.setFinishConfirmationVisible(false)
            testDispatcher.scheduler.runCurrent()

            active = timerVm.uiState.value as MockTestUiState.ActiveTest
            assertEquals(555L, active.remainingSeconds)
            assertTrue(timerVm.isTimerActive)
        } finally {
            timerVm.stopTimer()
        }
    }

    @Test
    fun automaticExpirationStillFinishesNormally() = runTest {
        val timerVm = MockTestViewModel(
            repository = repository,
            createSessionUseCase = createUseCase,
            finishSessionUseCase = finishUseCase,
            timeProvider = { virtualTimeMillis },
            timerDispatcher = testDispatcher
        )
        try {
            timerVm.loadTestConfiguration(testConfig)
            testDispatcher.scheduler.runCurrent()
            timerVm.startTest()
            testDispatcher.scheduler.runCurrent()

            // Answer Q1
            timerVm.selectAnswer("opt_q_1_0")
            testDispatcher.scheduler.runCurrent()

            // Fast forward time to expire full 10 minutes (600 seconds)
            virtualTimeMillis += 601_000L
            testDispatcher.scheduler.advanceTimeBy(601_000L)
            testDispatcher.scheduler.runCurrent()

            // Timer expiration finishes test automatically into ResultSummary
            assertFalse(timerVm.isTimerActive)
            val state = timerVm.uiState.value
            assertTrue(state is MockTestUiState.ResultSummary)

            val result = (state as MockTestUiState.ResultSummary).result
            assertEquals(5, result.totalQuestions)
            assertEquals(1, result.answeredQuestions)
            assertEquals(1, result.correctAnswers)
            assertEquals(4, result.unansweredQuestions)
        } finally {
            timerVm.stopTimer()
        }
    }

    @Test
    fun abandonStillWorks_discardsSessionAndStopsTimer() = runTest {
        startTestSession()

        // Student answers a question then abandons
        viewModel.selectAnswer("opt_q_1_0")
        advanceUntilIdle()

        viewModel.setAbandonConfirmationVisible(true)
        advanceUntilIdle()

        val active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertTrue(active.showAbandonConfirmation)

        // Abandon test
        viewModel.abandonTest()
        advanceUntilIdle()

        assertFalse(viewModel.isTimerActive)
    }
}
