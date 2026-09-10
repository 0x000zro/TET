package com.example.ui.feature.performance

import com.example.domain.model.practice.PracticeAttempt
import com.example.domain.usecase.performance.CalculateStudentPerformanceUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PerformanceViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: TestEducationalRepository
    private lateinit var calculateUseCase: CalculateStudentPerformanceUseCase
    private lateinit var viewModel: PerformanceViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = TestEducationalRepository()
        calculateUseCase = CalculateStudentPerformanceUseCase()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state before coroutines finish is Loading`() {
        viewModel = PerformanceViewModel(
            repository = fakeRepository,
            calculatePerformance = calculateUseCase
        )

        assertEquals(PerformanceUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `empty attempts produces Empty UI state`() = runTest(testDispatcher) {
        viewModel = PerformanceViewModel(
            repository = fakeRepository,
            calculatePerformance = calculateUseCase
        )

        advanceUntilIdle()

        assertEquals(PerformanceUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `saved practice attempts produce Success UI state with aggregated metrics`() = runTest(testDispatcher) {
        fakeRepository.practiceAttempts.addAll(
            listOf(
                PracticeAttempt(
                    id = "att_1",
                    subtopicId = "sub_algebra",
                    totalQuestions = 5,
                    answeredQuestions = 5,
                    correctAnswers = 4,
                    incorrectAnswers = 1,
                    percentageScore = 80.0,
                    startedAt = 1000L,
                    completedAt = 2000L
                ),
                PracticeAttempt(
                    id = "att_2",
                    subtopicId = "sub_algebra",
                    totalQuestions = 5,
                    answeredQuestions = 5,
                    correctAnswers = 5,
                    incorrectAnswers = 0,
                    percentageScore = 100.0,
                    startedAt = 2100L,
                    completedAt = 3000L
                )
            )
        )

        viewModel = PerformanceViewModel(
            repository = fakeRepository,
            calculatePerformance = calculateUseCase
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Success state, but was $state", state is PerformanceUiState.Success)
        val success = state as PerformanceUiState.Success
        assertEquals(2, success.performance.totalCompletedAttempts)
        assertEquals(10, success.performance.totalQuestionsAttempted)
        assertEquals(9, success.performance.totalCorrectAnswers)
        assertEquals(1, success.performance.totalIncorrectAnswers)
        assertEquals(90.0, success.performance.overallAccuracyPercentage, 0.001)
        assertEquals(1, success.performance.subtopicPerformances.size)
        assertEquals("sub_algebra", success.performance.subtopicPerformances[0].subtopicId)
    }

    @Test
    fun `repository failure emits Error UI state safely without crashing`() = runTest(testDispatcher) {
        fakeRepository.shouldThrowOnAttempt = true

        viewModel = PerformanceViewModel(
            repository = fakeRepository,
            calculatePerformance = calculateUseCase
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Error state, but was $state", state is PerformanceUiState.Error)
        val error = state as PerformanceUiState.Error
        assertTrue(error.message.contains("Simulated attempt database failure"))
    }

    @Test
    fun `retry recovers from Error state to Success state when repository becomes healthy`() = runTest(testDispatcher) {
        fakeRepository.shouldThrowOnAttempt = true

        viewModel = PerformanceViewModel(
            repository = fakeRepository,
            calculatePerformance = calculateUseCase
        )

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is PerformanceUiState.Error)

        // Fix failure and add attempts
        fakeRepository.shouldThrowOnAttempt = false
        fakeRepository.practiceAttempts.add(
            PracticeAttempt(
                id = "att_recovery",
                subtopicId = "sub_geom",
                totalQuestions = 4,
                answeredQuestions = 4,
                correctAnswers = 3,
                incorrectAnswers = 1,
                percentageScore = 75.0,
                startedAt = 500L,
                completedAt = 1500L
            )
        )

        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Success after retry, but was $state", state is PerformanceUiState.Success)
        val success = state as PerformanceUiState.Success
        assertEquals(1, success.performance.totalCompletedAttempts)
        assertEquals(4, success.performance.totalQuestionsAttempted)
        assertEquals(75.0, success.performance.overallAccuracyPercentage, 0.001)
    }
}
