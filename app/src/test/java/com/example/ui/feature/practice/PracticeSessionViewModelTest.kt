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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PracticeSessionViewModelTest {

    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: TestEducationalRepository
    private lateinit var evaluateAnswerUseCase: EvaluatePracticeAnswerUseCase
    private lateinit var viewModel: PracticeSessionViewModel

    private val sampleQuestion1 = Question(
        id = "q_1",
        subtopicId = "sub_math",
        questionText = "What is 2 + 2?",
        explanation = "Basic addition: 2 + 2 = 4.",
        difficulty = QuestionDifficulty.EASY,
        sortOrder = 1,
        isActive = true,
        options = listOf(
            QuestionOption(id = "opt_1_a", questionId = "q_1", optionText = "3", sortOrder = 1, isCorrect = false),
            QuestionOption(id = "opt_1_b", questionId = "q_1", optionText = "4", sortOrder = 2, isCorrect = true),
            QuestionOption(id = "opt_1_c", questionId = "q_1", optionText = "5", sortOrder = 3, isCorrect = false)
        )
    )

    private val sampleQuestion2 = Question(
        id = "q_2",
        subtopicId = "sub_math",
        questionText = "What is 5 * 3?",
        explanation = "Multiplication: 5 * 3 = 15.",
        difficulty = QuestionDifficulty.MEDIUM,
        sortOrder = 2,
        isActive = true,
        options = listOf(
            QuestionOption(id = "opt_2_a", questionId = "q_2", optionText = "15", sortOrder = 1, isCorrect = true),
            QuestionOption(id = "opt_2_b", questionId = "q_2", optionText = "20", sortOrder = 2, isCorrect = false)
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = TestEducationalRepository()
        evaluateAnswerUseCase = EvaluatePracticeAnswerUseCase(fakeRepository)
        viewModel = PracticeSessionViewModel(fakeRepository, evaluateAnswerUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoading() {
        assertTrue(viewModel.sessionState.value is PracticeSessionUiState.Loading)
    }

    @Test
    fun startSession_blankSubtopicId_emitsError() = runTest {
        viewModel.startSession("")
        advanceUntilIdle()

        assertTrue(viewModel.sessionState.value is PracticeSessionUiState.Error)
    }

    @Test
    fun startSession_emptyQuestions_emitsEmptyState() = runTest {
        viewModel.startSession("empty_subtopic")
        advanceUntilIdle()

        assertTrue(viewModel.sessionState.value is PracticeSessionUiState.Empty)
    }

    @Test
    fun startSession_withQuestions_loadsFirstQuestionInActiveState() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion1
        fakeRepository.questionsMap["q_2"] = sampleQuestion2

        viewModel.startSession("sub_math")
        advanceUntilIdle()

        val state = viewModel.sessionState.value
        assertTrue("Expected ActiveQuestion state, but got $state", state is PracticeSessionUiState.ActiveQuestion)
        val active = state as PracticeSessionUiState.ActiveQuestion
        assertEquals("q_1", active.question.id)
        assertEquals(1, active.currentQuestionIndex)
        assertEquals(2, active.totalQuestions)
        assertFalse(active.isLastQuestion)
        assertFalse(active.isSubmitted)
        assertFalse(active.canSubmit)
        assertNull(active.selectedOptionId)
        assertNull(active.result)
    }

    @Test
    fun selectOption_updatesSelectionAndEnablesSubmit() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion1
        fakeRepository.questionsMap["q_2"] = sampleQuestion2

        viewModel.startSession("sub_math")
        advanceUntilIdle()

        viewModel.selectOption("opt_1_b")

        val state = viewModel.sessionState.value as PracticeSessionUiState.ActiveQuestion
        assertEquals("opt_1_b", state.selectedOptionId)
        assertTrue(state.canSubmit)
    }

    @Test
    fun submitAnswer_correctOption_evaluatesCorrectlyAndRevealsFeedback() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion1
        fakeRepository.questionsMap["q_2"] = sampleQuestion2

        viewModel.startSession("sub_math")
        advanceUntilIdle()

        viewModel.selectOption("opt_1_b")
        viewModel.submitAnswer()
        advanceUntilIdle()

        val state = viewModel.sessionState.value as PracticeSessionUiState.ActiveQuestion
        assertTrue("State must be marked submitted", state.isSubmitted)
        assertFalse("canSubmit must be false after submission", state.canSubmit)
        assertNotNull(state.result)
        assertEquals(PracticeEvaluationStatus.CORRECT, state.result?.status)
        assertEquals("opt_1_b", state.result?.correctOptionId)
        assertEquals(1, state.session.correctCount)
        assertEquals(1, state.session.answeredCount)
    }

    @Test
    fun submitAnswer_incorrectOption_evaluatesIncorrectlyAndRevealsFeedback() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion1
        fakeRepository.questionsMap["q_2"] = sampleQuestion2

        viewModel.startSession("sub_math")
        advanceUntilIdle()

        viewModel.selectOption("opt_1_a")
        viewModel.submitAnswer()
        advanceUntilIdle()

        val state = viewModel.sessionState.value as PracticeSessionUiState.ActiveQuestion
        assertTrue(state.isSubmitted)
        assertEquals(PracticeEvaluationStatus.INCORRECT, state.result?.status)
        assertEquals("opt_1_b", state.result?.correctOptionId)
        assertEquals(0, state.session.correctCount)
        assertEquals(1, state.session.answeredCount)
    }

    @Test
    fun selectOption_afterSubmission_isIgnored() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion1

        viewModel.startSession("sub_math")
        advanceUntilIdle()

        viewModel.selectOption("opt_1_b")
        viewModel.submitAnswer()
        advanceUntilIdle()

        // Attempt to change selection after submitting
        viewModel.selectOption("opt_1_a")

        val state = viewModel.sessionState.value as PracticeSessionUiState.ActiveQuestion
        assertEquals("opt_1_b", state.selectedOptionId)
    }

    @Test
    fun nextQuestion_advancesToSecondQuestionAndResetsInput() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion1
        fakeRepository.questionsMap["q_2"] = sampleQuestion2

        viewModel.startSession("sub_math")
        advanceUntilIdle()

        viewModel.selectOption("opt_1_b")
        viewModel.submitAnswer()
        advanceUntilIdle()

        viewModel.nextQuestion()

        val state = viewModel.sessionState.value as PracticeSessionUiState.ActiveQuestion
        assertEquals("q_2", state.question.id)
        assertEquals(2, state.currentQuestionIndex)
        assertTrue(state.isLastQuestion)
        assertFalse(state.isSubmitted)
        assertNull(state.selectedOptionId)
        assertNull(state.result)
        assertFalse(state.canSubmit)
    }

    @Test
    fun finishSession_transitionsToCompletedWithAccurateScore() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion1
        fakeRepository.questionsMap["q_2"] = sampleQuestion2

        viewModel.startSession("sub_math")
        advanceUntilIdle()

        // Answer Q1 correctly
        viewModel.selectOption("opt_1_b")
        viewModel.submitAnswer()
        advanceUntilIdle()

        // Advance to Q2
        viewModel.nextQuestion()

        // Answer Q2 incorrectly
        viewModel.selectOption("opt_2_b")
        viewModel.submitAnswer()
        advanceUntilIdle()

        // Finish session
        viewModel.finishSession()

        val state = viewModel.sessionState.value
        assertTrue("Expected Completed state, got $state", state is PracticeSessionUiState.Completed)
        val completed = state as PracticeSessionUiState.Completed
        val result = completed.result
        assertEquals(2, result.totalQuestions)
        assertEquals(2, result.answeredQuestions)
        assertEquals(1, result.correctAnswers)
        assertEquals(1, result.incorrectAnswers)
        assertEquals(50.0, result.scorePercentage, 0.01)
    }

    @Test
    fun resetSession_clearsSessionStateBackToLoading() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion1

        viewModel.startSession("sub_math")
        advanceUntilIdle()

        viewModel.resetSession()

        assertTrue(viewModel.sessionState.value is PracticeSessionUiState.Loading)
    }

    @Test
    fun repositoryError_transitionsToErrorState() = runTest {
        fakeRepository.shouldThrowOnQuestion = true

        viewModel.startSession("sub_math")
        advanceUntilIdle()

        assertTrue(viewModel.sessionState.value is PracticeSessionUiState.Error)
    }

    @Test
    fun finishSession_persistsCompletedAttemptLocally() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion1
        fakeRepository.questionsMap["q_2"] = sampleQuestion2

        viewModel.startSession("sub_math")
        advanceUntilIdle()

        // Answer Q1 correctly
        viewModel.selectOption("opt_1_b")
        viewModel.submitAnswer()
        advanceUntilIdle()

        // Advance to Q2 and answer incorrectly
        viewModel.nextQuestion()
        viewModel.selectOption("opt_2_b")
        viewModel.submitAnswer()
        advanceUntilIdle()

        // Finish session
        viewModel.finishSession()
        advanceUntilIdle()

        // Verify state is completed
        val state = viewModel.sessionState.value
        assertTrue(state is PracticeSessionUiState.Completed)

        // Verify attempt persisted in repository
        assertEquals(1, fakeRepository.practiceAttempts.size)
        val attempt = fakeRepository.practiceAttempts.first()
        assertEquals("sub_math", attempt.subtopicId)
        assertEquals(2, attempt.totalQuestions)
        assertEquals(2, attempt.answeredQuestions)
        assertEquals(1, attempt.correctAnswers)
        assertEquals(1, attempt.incorrectAnswers)
        assertEquals(50.0, attempt.percentageScore, 0.01)
        assertTrue(attempt.startedAt > 0L)
        assertTrue(attempt.completedAt >= attempt.startedAt)
    }

    @Test
    fun finishSession_isIdempotent_doesNotDuplicateAttempts() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion1

        viewModel.startSession("sub_math")
        advanceUntilIdle()

        viewModel.selectOption("opt_1_b")
        viewModel.submitAnswer()
        advanceUntilIdle()

        viewModel.finishSession()
        advanceUntilIdle()

        // Attempting to finish again on already-finished session
        viewModel.finishSession()
        advanceUntilIdle()

        assertEquals(1, fakeRepository.practiceAttempts.size)
    }

    @Test
    fun finishSession_whenRepositoryFailsToSave_sessionResultStillSucceedsGracefully() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion1
        fakeRepository.shouldThrowOnAttempt = true

        viewModel.startSession("sub_math")
        advanceUntilIdle()

        viewModel.selectOption("opt_1_b")
        viewModel.submitAnswer()
        advanceUntilIdle()

        viewModel.finishSession()
        advanceUntilIdle()

        val state = viewModel.sessionState.value
        assertTrue("Session must complete cleanly despite persistence error", state is PracticeSessionUiState.Completed)
        val completed = state as PracticeSessionUiState.Completed
        assertEquals(1, completed.result.correctAnswers)
    }
}
