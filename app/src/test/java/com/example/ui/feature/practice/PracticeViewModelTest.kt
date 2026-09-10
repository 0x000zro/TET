package com.example.ui.feature.practice

import com.example.domain.model.Question
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionOption
import com.example.domain.model.practice.PracticeEvaluationStatus
import com.example.domain.usecase.practice.EvaluatePracticeAnswerUseCase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PracticeViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: TestEducationalRepository
    private lateinit var evaluateAnswerUseCase: EvaluatePracticeAnswerUseCase
    private lateinit var viewModel: PracticeViewModel

    private val sampleOptions = listOf(
        QuestionOption(id = "opt_1", questionId = "q_1", optionText = "2", sortOrder = 1, isCorrect = false),
        QuestionOption(id = "opt_2", questionId = "q_1", optionText = "4", sortOrder = 2, isCorrect = true),
        QuestionOption(id = "opt_3", questionId = "q_1", optionText = "6", sortOrder = 3, isCorrect = false),
        QuestionOption(id = "opt_4", questionId = "q_1", optionText = "8", sortOrder = 4, isCorrect = false)
    )

    private val sampleQuestion = Question(
        id = "q_1",
        subtopicId = "sub_1",
        questionText = "What is 2 + 2?",
        explanation = "Basic arithmetic: 2 + 2 equals 4.",
        difficulty = QuestionDifficulty.EASY,
        sortOrder = 1,
        isActive = true,
        options = sampleOptions
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = TestEducationalRepository()
        evaluateAnswerUseCase = EvaluatePracticeAnswerUseCase(fakeRepository)
        viewModel = PracticeViewModel(fakeRepository, evaluateAnswerUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoading() {
        assertTrue(viewModel.uiState.value is PracticeUiState.Loading)
    }

    @Test
    fun loadQuestion_validQuestion_transitionsToReadyWithNullSelection() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion

        viewModel.loadQuestion("q_1", questionIndex = 1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Ready state, but got: $state", state is PracticeUiState.Ready)
        val readyState = state as PracticeUiState.Ready
        assertEquals("q_1", readyState.question.id)
        assertEquals(1, readyState.question.questionNumber)
        assertNull(readyState.selectedOptionId)
        assertFalse("canSubmit must be false when no option is selected", readyState.canSubmit)
    }

    @Test
    fun selectOption_updatesSelectionAndEnablesSubmit() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion
        viewModel.loadQuestion("q_1")
        advanceUntilIdle()

        viewModel.selectOption("opt_1")

        val state = viewModel.uiState.value as PracticeUiState.Ready
        assertEquals("opt_1", state.selectedOptionId)
        assertTrue("canSubmit must be true when an option is selected", state.canSubmit)
    }

    @Test
    fun selectOption_replacingSelection_updatesToNewSelection() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion
        viewModel.loadQuestion("q_1")
        advanceUntilIdle()

        viewModel.selectOption("opt_1")
        assertEquals("opt_1", (viewModel.uiState.value as PracticeUiState.Ready).selectedOptionId)

        viewModel.selectOption("opt_2")
        assertEquals("opt_2", (viewModel.uiState.value as PracticeUiState.Ready).selectedOptionId)
    }

    @Test
    fun submitAnswer_withoutSelection_isIgnored() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion
        viewModel.loadQuestion("q_1")
        advanceUntilIdle()

        // Submit without selecting an option
        viewModel.submitAnswer()
        advanceUntilIdle()

        // State remains in Ready, not submitted
        assertTrue(viewModel.uiState.value is PracticeUiState.Ready)
    }

    @Test
    fun submitAnswer_correctSelection_transitionsToSubmittedWithCorrectStatus() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion
        viewModel.loadQuestion("q_1")
        advanceUntilIdle()

        viewModel.selectOption("opt_2") // opt_2 is correct
        viewModel.submitAnswer()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Submitted state, but got: $state", state is PracticeUiState.Submitted)
        val submittedState = state as PracticeUiState.Submitted
        assertEquals("opt_2", submittedState.selectedOptionId)
        assertEquals(PracticeEvaluationStatus.CORRECT, submittedState.result.status)
        assertEquals("opt_2", submittedState.result.correctOptionId)
        assertEquals("Basic arithmetic: 2 + 2 equals 4.", submittedState.result.explanation)
    }

    @Test
    fun submitAnswer_incorrectSelection_transitionsToSubmittedWithIncorrectStatusAndRevealsCorrect() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion
        viewModel.loadQuestion("q_1")
        advanceUntilIdle()

        viewModel.selectOption("opt_1") // opt_1 is incorrect
        viewModel.submitAnswer()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Submitted state, but got: $state", state is PracticeUiState.Submitted)
        val submittedState = state as PracticeUiState.Submitted
        assertEquals("opt_1", submittedState.selectedOptionId)
        assertEquals(PracticeEvaluationStatus.INCORRECT, submittedState.result.status)
        assertEquals("opt_2", submittedState.result.correctOptionId)
        assertEquals("Basic arithmetic: 2 + 2 equals 4.", submittedState.result.explanation)
    }

    @Test
    fun duplicateSubmission_isPreventedAndStateRemainsIntact() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion
        viewModel.loadQuestion("q_1")
        advanceUntilIdle()

        viewModel.selectOption("opt_2")
        viewModel.submitAnswer()
        advanceUntilIdle()

        val firstSubmittedState = viewModel.uiState.value
        assertTrue(firstSubmittedState is PracticeUiState.Submitted)

        // Attempt second submission
        viewModel.submitAnswer()
        advanceUntilIdle()

        // State is identical and still submitted
        assertEquals(firstSubmittedState, viewModel.uiState.value)
    }

    @Test
    fun selectOption_afterSubmission_isFrozenAndIgnored() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion
        viewModel.loadQuestion("q_1")
        advanceUntilIdle()

        viewModel.selectOption("opt_1")
        viewModel.submitAnswer()
        advanceUntilIdle()

        val state = viewModel.uiState.value as PracticeUiState.Submitted
        assertEquals("opt_1", state.selectedOptionId)

        // Attempting to select another option after submission
        viewModel.selectOption("opt_2")

        val stateAfter = viewModel.uiState.value as PracticeUiState.Submitted
        assertEquals("opt_1", stateAfter.selectedOptionId)
    }

    @Test
    fun loadQuestion_notFound_transitionsToNotFound() = runTest {
        viewModel.loadQuestion("non_existent")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is PracticeUiState.NotFound)
    }

    @Test
    fun loadQuestion_inactive_transitionsToNotFound() = runTest {
        val inactiveQuestion = sampleQuestion.copy(isActive = false)
        fakeRepository.questionsMap["q_1"] = inactiveQuestion

        viewModel.loadQuestion("q_1")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is PracticeUiState.NotFound)
    }

    @Test
    fun loadQuestion_error_transitionsToError() = runTest {
        fakeRepository.shouldThrowOnQuestion = true

        viewModel.loadQuestion("q_1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Error state, but got: $state", state is PracticeUiState.Error)
        assertEquals("Simulated database failure", (state as PracticeUiState.Error).message)
    }

    @Test
    fun retry_reloadsQuestion() = runTest {
        fakeRepository.shouldThrowOnQuestion = true
        viewModel.loadQuestion("q_1")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is PracticeUiState.Error)

        fakeRepository.shouldThrowOnQuestion = false
        fakeRepository.questionsMap["q_1"] = sampleQuestion
        viewModel.retry()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is PracticeUiState.Ready)
    }

    @Test
    fun loadQuestion_startsObservingBookmark_initiallyFalse() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion
        viewModel.loadQuestion("q_1")
        advanceUntilIdle()

        assertFalse(viewModel.isBookmarked.value)
    }

    @Test
    fun toggleBookmark_togglesBookmarkState() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion
        viewModel.loadQuestion("q_1")
        advanceUntilIdle()

        assertFalse(viewModel.isBookmarked.value)

        viewModel.toggleBookmark("q_1", "sub_1")
        advanceUntilIdle()

        assertTrue(viewModel.isBookmarked.value)
        assertEquals(1, fakeRepository.bookmarkedQuestions.size)

        viewModel.toggleBookmark("q_1", "sub_1")
        advanceUntilIdle()

        assertFalse(viewModel.isBookmarked.value)
        assertEquals(0, fakeRepository.bookmarkedQuestions.size)
    }
}
