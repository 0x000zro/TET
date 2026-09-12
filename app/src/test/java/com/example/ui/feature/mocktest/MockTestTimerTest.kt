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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Focused unit tests for Step 18 — Mock Test Timer & Time Management Foundation:
 * - timer starts correctly
 * - initial duration
 * - countdown progression
 * - timer reaches zero
 * - automatic finish
 * - selected answers preserved
 * - manual finish cancels timer
 * - abandon cancels timer
 * - duplicate finish protection
 * - no timer on Configuration
 * - no timer on Result
 * - ViewModel/timer lifecycle cancellation
 * - delayed timer tick does not incorrectly lose extra time
 * - time formatting (MM:SS)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MockTestTimerTest {

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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        virtualTimeMillis = 100_000L
        repository = TestEducationalRepository()
        createUseCase = CreateMockTestSessionUseCase(repository)
        finishUseCase = FinishMockTestSessionUseCase(repository)

        repository.examsMap[testExam.id] = testExam
        repository.papersMap[testPaper.id] = testPaper
        repository.subjectsMap[testSubject.id] = testSubject
        repository.topicsMap[testTopic.id] = testTopic
        repository.subtopicsMap[testSubtopic.id] = testSubtopic

        val q1 = createQuestion("q_1", sortOrder = 1, correctIndex = 0)
        val q2 = createQuestion("q_2", sortOrder = 2, correctIndex = 1)
        repository.questionsMap[q1.id] = q1
        repository.questionsMap[q2.id] = q2

        viewModel = MockTestViewModel(
            repository = repository,
            createSessionUseCase = createUseCase,
            finishSessionUseCase = finishUseCase,
            timeProvider = { virtualTimeMillis },
            timerDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        viewModel.onCleared()
        Dispatchers.resetMain()
    }

    @Test
    fun noTimerOnConfigurationOverview() = runTest {
        viewModel.loadTestConfiguration()
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value is MockTestUiState.ConfigurationOverview)
        assertFalse("Timer must not run on Configuration Overview screen", viewModel.isTimerActive)
    }

    @Test
    fun timerStartsCorrectlyOnActiveTest() = runTest {
        try {
            viewModel.loadTestConfiguration()
            testDispatcher.scheduler.runCurrent()
            viewModel.startTest()
            testDispatcher.scheduler.runCurrent()

            assertTrue(viewModel.uiState.value is MockTestUiState.ActiveTest)
            assertTrue("Timer must start when Mock Test becomes active", viewModel.isTimerActive)
        } finally {
            viewModel.stopTimer()
        }
    }

    @Test
    fun initialDurationMatchesConfiguration() = runTest {
        try {
            val config = MockTestConfiguration(
                id = "cfg_timer",
                title = "Timer Test Config",
                examId = "exam_1",
                paperId = "paper_1",
                scope = MockTestScope.FullPaper,
                questionCount = 2,
                durationMinutes = 15
            )

            viewModel.loadTestConfiguration(config)
            testDispatcher.scheduler.runCurrent()
            viewModel.startTest()
            testDispatcher.scheduler.runCurrent()

            val active = viewModel.uiState.value as MockTestUiState.ActiveTest
            assertEquals(900L, active.remainingSeconds)
            assertEquals("15:00", active.formattedRemainingTime)
        } finally {
            viewModel.stopTimer()
        }
    }

    @Test
    fun countdownProgressionUpdatesOncePerSecond() = runTest {
        try {
            val config = MockTestConfiguration(
                id = "cfg_timer",
                title = "Timer Progression",
                examId = "exam_1",
                paperId = "paper_1",
                scope = MockTestScope.FullPaper,
                questionCount = 2,
                durationMinutes = 5 // 300 seconds
            )

            viewModel.loadTestConfiguration(config)
            testDispatcher.scheduler.runCurrent()
            viewModel.startTest()
            testDispatcher.scheduler.runCurrent()

            var active = viewModel.uiState.value as MockTestUiState.ActiveTest
            assertEquals(300L, active.remainingSeconds)
            assertEquals("05:00", active.formattedRemainingTime)

            // Advance 1 second in time and scheduler
            virtualTimeMillis += 1000L
            testDispatcher.scheduler.advanceTimeBy(1000L)
            testDispatcher.scheduler.runCurrent()

            active = viewModel.uiState.value as MockTestUiState.ActiveTest
            assertEquals(299L, active.remainingSeconds)
            assertEquals("04:59", active.formattedRemainingTime)

            // Advance 2nd second
            virtualTimeMillis += 1000L
            testDispatcher.scheduler.advanceTimeBy(1000L)
            testDispatcher.scheduler.runCurrent()

            active = viewModel.uiState.value as MockTestUiState.ActiveTest
            assertEquals(298L, active.remainingSeconds)
            assertEquals("04:58", active.formattedRemainingTime)
        } finally {
            viewModel.stopTimer()
        }
    }

    @Test
    fun delayedTimerTickDoesNotDistortRemainingTime() = runTest {
        try {
            val config = MockTestConfiguration(
                id = "cfg_timer",
                title = "Timer Drift Protection",
                examId = "exam_1",
                paperId = "paper_1",
                scope = MockTestScope.FullPaper,
                questionCount = 2,
                durationMinutes = 10 // 600 seconds
            )

            viewModel.loadTestConfiguration(config)
            testDispatcher.scheduler.runCurrent()
            viewModel.startTest()
            testDispatcher.scheduler.runCurrent()

            // Suppose a 7-second scheduling delay occurs before next coroutine tick
            virtualTimeMillis += 7000L
            testDispatcher.scheduler.advanceTimeBy(1000L)
            testDispatcher.scheduler.runCurrent()

            val active = viewModel.uiState.value as MockTestUiState.ActiveTest
            // Accurate delta: 600 - 7 = 593 seconds
            assertEquals(593L, active.remainingSeconds)
            assertEquals("09:53", active.formattedRemainingTime)
        } finally {
            viewModel.stopTimer()
        }
    }

    @Test
    fun timerReachesZero_triggersAutomaticFinishAndPreservesSelectedAnswers() = runTest {
        val config = MockTestConfiguration(
            id = "cfg_auto_finish",
            title = "Auto Finish",
            examId = "exam_1",
            paperId = "paper_1",
            scope = MockTestScope.FullPaper,
            questionCount = 2,
            durationMinutes = 1 // 60 seconds
        )

        viewModel.loadTestConfiguration(config)
        testDispatcher.scheduler.runCurrent()
        viewModel.startTest()
        testDispatcher.scheduler.runCurrent()

        // User answers question 1 correctly (option 0 is correct)
        viewModel.selectAnswer("opt_q_1_0")
        testDispatcher.scheduler.runCurrent()

        // Time expires
        virtualTimeMillis += 60_000L
        testDispatcher.scheduler.advanceTimeBy(1000L)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("State must transition to ResultSummary on time expiration", state is MockTestUiState.ResultSummary)
        val resultState = state as MockTestUiState.ResultSummary
        assertEquals(2, resultState.result.totalQuestions)
        assertEquals(1, resultState.result.answeredQuestions)
        assertEquals(1, resultState.result.correctAnswers)
        assertEquals(1, resultState.result.unansweredQuestions)
        assertEquals(50.0, resultState.result.scorePercentage, 0.001)
        assertFalse("Timer must stop after auto-finish", viewModel.isTimerActive)
    }

    @Test
    fun manualFinishCancelsTimerAndPreventsDuplicateFinish() = runTest {
        viewModel.loadTestConfiguration()
        testDispatcher.scheduler.runCurrent()
        viewModel.startTest()
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.isTimerActive)

        viewModel.confirmFinish()
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value is MockTestUiState.ResultSummary)
        assertFalse("Timer must be cancelled on manual finish", viewModel.isTimerActive)

        // Advancing time past the original timer duration does not trigger another finish or change state
        virtualTimeMillis += 1_000_000L
        testDispatcher.scheduler.advanceTimeBy(10_000L)
        testDispatcher.scheduler.runCurrent()

        assertTrue("State remains ResultSummary", viewModel.uiState.value is MockTestUiState.ResultSummary)
    }

    @Test
    fun abandonCancelsTimerAndDoesNotAutoFinishAfterward() = runTest {
        viewModel.loadTestConfiguration()
        testDispatcher.scheduler.runCurrent()
        viewModel.startTest()
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.isTimerActive)

        viewModel.abandonTest()

        assertFalse("Timer must be cancelled when test is abandoned", viewModel.isTimerActive)

        // Advancing time past duration should not trigger finish or crash
        virtualTimeMillis += 1_000_000L
        testDispatcher.scheduler.advanceTimeBy(10_000L)
        testDispatcher.scheduler.runCurrent()

        assertFalse("No result summary should be created after abandon", viewModel.uiState.value is MockTestUiState.ResultSummary)
    }

    @Test
    fun restartTestCancelsExistingTimerAndResetsOverview() = runTest {
        viewModel.loadTestConfiguration()
        testDispatcher.scheduler.runCurrent()
        viewModel.startTest()
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.isTimerActive)

        viewModel.restartTest()
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.uiState.value is MockTestUiState.ConfigurationOverview)
        assertFalse("Timer must be stopped on restart", viewModel.isTimerActive)
    }

    @Test
    fun lifecycleCancellation_onClearedCancelsTimer() = runTest {
        viewModel.loadTestConfiguration()
        testDispatcher.scheduler.runCurrent()
        viewModel.startTest()
        testDispatcher.scheduler.runCurrent()

        assertTrue(viewModel.isTimerActive)

        viewModel.onCleared()

        assertFalse("Timer job must be cancelled when ViewModel is cleared", viewModel.isTimerActive)
    }

    @Test
    fun formatRemainingTime_handlesZeroEdgeCasesAndPaddedMinutes() {
        assertEquals("00:00", MockTestViewModel.formatRemainingTime(0L))
        assertEquals("00:00", MockTestViewModel.formatRemainingTime(-10L))
        assertEquals("00:05", MockTestViewModel.formatRemainingTime(5L))
        assertEquals("00:59", MockTestViewModel.formatRemainingTime(59L))
        assertEquals("01:00", MockTestViewModel.formatRemainingTime(60L))
        assertEquals("15:00", MockTestViewModel.formatRemainingTime(900L))
        assertEquals("65:30", MockTestViewModel.formatRemainingTime(3930L))
    }
}
