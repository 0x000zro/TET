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
import com.example.ui.feature.mocktest.model.PaletteQuestionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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
 * Focused tests for Step 19 — Mock Test Question Palette & Direct Navigation:
 * - palette contains all question numbers
 * - current question state is correct
 * - answered state updates after selecting an answer
 * - unanswered state updates after clearing an answer
 * - direct navigation to arbitrary question
 * - navigation preserves previously selected answers
 * - currentQuestionIndex stays synchronized
 * - answered/unanswered counts remain correct
 * - previous/next still work
 * - palette navigation does not stop/reset the timer
 * - finish flow remains functional
 * - abandon flow remains functional
 * - out of bounds navigation safely ignored
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MockTestQuestionPaletteTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var repository: TestEducationalRepository
    private lateinit var createUseCase: CreateMockTestSessionUseCase
    private lateinit var finishUseCase: FinishMockTestSessionUseCase
    private lateinit var viewModel: MockTestViewModel

    private var virtualTimeMillis: Long = 100_000L

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
        id = "cfg_palette_test",
        title = "Palette Test Configuration",
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

        // Seed 5 questions
        (1..5).forEach { i ->
            val q = createQuestion("q_$i", sortOrder = i, correctIndex = 0)
            repository.questionsMap[q.id] = q
        }

        virtualTimeMillis = 100_000L

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

    private fun kotlinx.coroutines.test.TestScope.startTestSession(): MockTestUiState.ActiveTest {
        viewModel.loadTestConfiguration(testConfig)
        advanceUntilIdle()
        viewModel.startTest()
        advanceUntilIdle()
        return viewModel.uiState.value as MockTestUiState.ActiveTest
    }

    @Test
    fun paletteContainsAllQuestionNumbers() = runTest {
        val active = startTestSession()

        assertEquals(5, active.paletteItems.size)
        assertEquals(5, active.totalQuestions)

        active.paletteItems.forEachIndexed { index, item ->
            assertEquals(index, item.index)
            assertEquals(index + 1, item.questionNumber)
        }
    }

    @Test
    fun currentQuestionStateIsCorrectInitially() = runTest {
        val active = startTestSession()

        // First question should be current
        assertTrue(active.paletteItems[0].isCurrent)
        assertEquals(PaletteQuestionStatus.CURRENT, active.paletteItems[0].status)
        assertFalse(active.paletteItems[0].isAnswered)

        // All other questions should be not current and unanswered
        for (i in 1..4) {
            assertFalse(active.paletteItems[i].isCurrent)
            assertFalse(active.paletteItems[i].isAnswered)
            assertEquals(PaletteQuestionStatus.UNANSWERED, active.paletteItems[i].status)
        }
    }

    @Test
    fun answeredStateUpdatesAfterSelectingAnswer() = runTest {
        startTestSession()

        // Select an option for question 1 (index 0)
        viewModel.selectAnswer("opt_q_1_0")
        advanceUntilIdle()

        val active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertTrue(active.paletteItems[0].isAnswered)
        assertTrue(active.paletteItems[0].isCurrent)
        assertEquals(1, active.answeredCount)
        assertEquals(4, active.unansweredCount)

        // Navigate to question 2 (index 1)
        viewModel.nextQuestion()
        advanceUntilIdle()

        val updated = viewModel.uiState.value as MockTestUiState.ActiveTest
        // Question 1 should now show answered and not current
        assertTrue(updated.paletteItems[0].isAnswered)
        assertFalse(updated.paletteItems[0].isCurrent)
        assertEquals(PaletteQuestionStatus.ANSWERED, updated.paletteItems[0].status)

        // Question 2 should now show current and unanswered
        assertFalse(updated.paletteItems[1].isAnswered)
        assertTrue(updated.paletteItems[1].isCurrent)
        assertEquals(PaletteQuestionStatus.CURRENT, updated.paletteItems[1].status)
    }

    @Test
    fun unansweredStateUpdatesAfterClearingAnswer() = runTest {
        startTestSession()

        viewModel.selectAnswer("opt_q_1_0")
        advanceUntilIdle()

        var active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertTrue(active.paletteItems[0].isAnswered)
        assertEquals(1, active.answeredCount)

        // Clear the answer
        viewModel.clearAnswer()
        advanceUntilIdle()

        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertFalse(active.paletteItems[0].isAnswered)
        assertEquals(0, active.answeredCount)
        assertEquals(5, active.unansweredCount)
        assertNull(active.selectedOptionId)
    }

    @Test
    fun directNavigationToArbitraryQuestion() = runTest {
        startTestSession()

        // Jump directly to Question 4 (index 3)
        viewModel.navigateToQuestion(3)
        advanceUntilIdle()

        val active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals(4, active.currentQuestionIndex)
        assertEquals("q_4", active.currentQuestion.id)
        assertTrue(active.paletteItems[3].isCurrent)
        assertEquals(PaletteQuestionStatus.CURRENT, active.paletteItems[3].status)
        assertFalse(active.paletteItems[0].isCurrent)

        // Jump directly using 1-based question number
        viewModel.navigateToQuestionNumber(2)
        advanceUntilIdle()

        val updated = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals(2, updated.currentQuestionIndex)
        assertEquals("q_2", updated.currentQuestion.id)
        assertTrue(updated.paletteItems[1].isCurrent)
        assertFalse(updated.paletteItems[3].isCurrent)
    }

    @Test
    fun navigationPreservesPreviouslySelectedAnswers() = runTest {
        startTestSession()

        // Answer Q1 with option 0
        viewModel.selectAnswer("opt_q_1_0")
        advanceUntilIdle()

        // Direct navigate to Q3 (index 2)
        viewModel.navigateToQuestion(2)
        advanceUntilIdle()

        var active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals("q_3", active.currentQuestion.id)
        assertNull(active.selectedOptionId) // Q3 not yet answered

        // Answer Q3 with option 2
        viewModel.selectAnswer("opt_q_3_2")
        advanceUntilIdle()

        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals("opt_q_3_2", active.selectedOptionId)
        assertEquals(2, active.answeredCount)
        assertEquals(3, active.unansweredCount)

        // Direct navigate to Q5 (index 4)
        viewModel.navigateToQuestion(4)
        advanceUntilIdle()

        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals("q_5", active.currentQuestion.id)
        assertNull(active.selectedOptionId)

        // Direct navigate back to Q1 (index 0)
        viewModel.navigateToQuestion(0)
        advanceUntilIdle()

        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals("q_1", active.currentQuestion.id)
        assertEquals("opt_q_1_0", active.selectedOptionId) // Preserved!

        // Direct navigate to Q3 (index 2)
        viewModel.navigateToQuestion(2)
        advanceUntilIdle()

        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals("q_3", active.currentQuestion.id)
        assertEquals("opt_q_3_2", active.selectedOptionId) // Preserved!
    }

    @Test
    fun currentQuestionIndexStaysSynchronized() = runTest {
        startTestSession()

        viewModel.navigateToQuestion(4) // Last question (index 4)
        advanceUntilIdle()

        val active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals(5, active.currentQuestionIndex)
        assertEquals(5, active.totalQuestions)
        assertFalse(active.hasNext)
        assertTrue(active.hasPrevious)
        assertTrue(active.isLastQuestion)
    }

    @Test
    fun answeredAndUnansweredCountsRemainCorrectAcrossDirectNavigation() = runTest {
        startTestSession()

        // Answer Q1
        viewModel.selectAnswer("opt_q_1_1")
        advanceUntilIdle()

        // Jump to Q5 and answer
        viewModel.navigateToQuestion(4)
        viewModel.selectAnswer("opt_q_5_0")
        advanceUntilIdle()

        val active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals(2, active.answeredCount)
        assertEquals(3, active.unansweredCount)
        assertTrue(active.paletteItems[0].isAnswered)
        assertTrue(active.paletteItems[4].isAnswered)
        assertFalse(active.paletteItems[1].isAnswered)
        assertFalse(active.paletteItems[2].isAnswered)
        assertFalse(active.paletteItems[3].isAnswered)
    }

    @Test
    fun previousAndNextStillWorkAlongsidePaletteNavigation() = runTest {
        startTestSession()

        // Direct navigate to Q3 (index 2)
        viewModel.navigateToQuestion(2)
        advanceUntilIdle()

        var active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals(3, active.currentQuestionIndex)

        // Use Next
        viewModel.nextQuestion()
        advanceUntilIdle()

        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals(4, active.currentQuestionIndex)
        assertTrue(active.paletteItems[3].isCurrent)

        // Use Previous
        viewModel.previousQuestion()
        advanceUntilIdle()

        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertEquals(3, active.currentQuestionIndex)
        assertTrue(active.paletteItems[2].isCurrent)
    }

    @Test
    fun paletteNavigationDoesNotStopOrResetTimer() = runTest {
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

            // Advance 10 seconds of time
            virtualTimeMillis += 10_000L
            testDispatcher.scheduler.advanceTimeBy(10_000L)
            testDispatcher.scheduler.runCurrent()

            var active = timerVm.uiState.value as MockTestUiState.ActiveTest
            assertEquals(590L, active.remainingSeconds)
            assertEquals("09:50", active.formattedRemainingTime)

            // Perform multiple direct navigations
            timerVm.navigateToQuestion(3)
            testDispatcher.scheduler.runCurrent()
            timerVm.navigateToQuestion(1)
            testDispatcher.scheduler.runCurrent()
            timerVm.navigateToQuestion(4)
            testDispatcher.scheduler.runCurrent()

            // Timer is still active and remaining seconds are still accurate
            assertTrue(timerVm.isTimerActive)
            active = timerVm.uiState.value as MockTestUiState.ActiveTest
            assertEquals(590L, active.remainingSeconds)

            // Advance another 5 seconds
            virtualTimeMillis += 5_000L
            testDispatcher.scheduler.advanceTimeBy(5_000L)
            testDispatcher.scheduler.runCurrent()

            active = timerVm.uiState.value as MockTestUiState.ActiveTest
            assertEquals(585L, active.remainingSeconds)
            assertEquals("09:45", active.formattedRemainingTime)
        } finally {
            timerVm.stopTimer()
        }
    }

    @Test
    fun finishFlowRemainsFunctionalAfterPaletteNavigation() = runTest {
        startTestSession()

        // Answer Q1, Q2, Q4 via direct navigation
        viewModel.navigateToQuestion(0)
        viewModel.selectAnswer("opt_q_1_0") // correct
        advanceUntilIdle()

        viewModel.navigateToQuestion(1)
        viewModel.selectAnswer("opt_q_2_1") // incorrect
        advanceUntilIdle()

        viewModel.navigateToQuestion(3)
        viewModel.selectAnswer("opt_q_4_0") // correct
        advanceUntilIdle()

        // Request finish dialog
        viewModel.setFinishConfirmationVisible(true)
        advanceUntilIdle()

        var active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertTrue(active.showFinishConfirmation)
        assertEquals(3, active.answeredCount)
        assertEquals(2, active.unansweredCount)

        // Confirm finish
        viewModel.confirmFinish()
        advanceUntilIdle()

        val finishedState = viewModel.uiState.value
        assertTrue(finishedState is MockTestUiState.ResultSummary)
        val result = (finishedState as MockTestUiState.ResultSummary).result

        assertEquals(5, result.totalQuestions)
        assertEquals(2, result.correctAnswers)
        assertEquals(1, result.incorrectAnswers)
        assertEquals(2, result.unansweredQuestions)
        assertEquals(40.0, result.scorePercentage, 0.01)
        assertFalse(viewModel.isTimerActive)
    }

    @Test
    fun abandonFlowRemainsFunctionalAfterPaletteNavigation() = runTest {
        startTestSession()

        viewModel.navigateToQuestion(2)
        advanceUntilIdle()

        // Show abandon confirmation
        viewModel.setAbandonConfirmationVisible(true)
        advanceUntilIdle()

        var active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertTrue(active.showAbandonConfirmation)

        // Dismiss abandon confirmation
        viewModel.setAbandonConfirmationVisible(false)
        advanceUntilIdle()

        active = viewModel.uiState.value as MockTestUiState.ActiveTest
        assertFalse(active.showAbandonConfirmation)

        // Abandon test
        viewModel.abandonTest()
        advanceUntilIdle()

        assertFalse(viewModel.isTimerActive)
    }

    @Test
    fun outOfBoundsDirectNavigationIsSafelyIgnored() = runTest {
        startTestSession()

        val initialIndex = (viewModel.uiState.value as MockTestUiState.ActiveTest).currentQuestionIndex
        assertEquals(1, initialIndex)

        // Negative index
        viewModel.navigateToQuestion(-1)
        advanceUntilIdle()
        assertEquals(1, (viewModel.uiState.value as MockTestUiState.ActiveTest).currentQuestionIndex)

        // Index equal to total questions (out of bounds for 0-indexed 0..4)
        viewModel.navigateToQuestion(5)
        advanceUntilIdle()
        assertEquals(1, (viewModel.uiState.value as MockTestUiState.ActiveTest).currentQuestionIndex)

        // Index 100
        viewModel.navigateToQuestion(100)
        advanceUntilIdle()
        assertEquals(1, (viewModel.uiState.value as MockTestUiState.ActiveTest).currentQuestionIndex)
    }
}
