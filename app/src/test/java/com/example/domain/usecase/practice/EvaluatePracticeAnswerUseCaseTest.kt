package com.example.domain.usecase.practice

import com.example.domain.model.Question
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionOption
import com.example.domain.model.practice.PracticeEvaluationStatus
import com.example.testutil.TestEducationalRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EvaluatePracticeAnswerUseCaseTest {

    private lateinit var fakeRepository: TestEducationalRepository
    private lateinit var useCase: EvaluatePracticeAnswerUseCase

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
    fun setUp() {
        fakeRepository = TestEducationalRepository()
        useCase = EvaluatePracticeAnswerUseCase(fakeRepository)
    }

    @Test
    fun execute_correctAnswer_returnsSuccessWithCorrectStatus() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion

        val outcome = useCase.execute(questionId = "q_1", selectedOptionId = "opt_2")

        assertTrue("Expected Success outcome", outcome is PracticeEvaluationOutcome.Success)
        val result = (outcome as PracticeEvaluationOutcome.Success).result
        assertEquals("q_1", result.questionId)
        assertEquals("opt_2", result.selectedOptionId)
        assertEquals("opt_2", result.correctOptionId)
        assertEquals(PracticeEvaluationStatus.CORRECT, result.status)
        assertEquals("Basic arithmetic: 2 + 2 equals 4.", result.explanation)
    }

    @Test
    fun execute_incorrectAnswer_returnsSuccessWithIncorrectStatusAndRevealsCorrectOption() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion

        val outcome = useCase.execute(questionId = "q_1", selectedOptionId = "opt_1")

        assertTrue("Expected Success outcome", outcome is PracticeEvaluationOutcome.Success)
        val result = (outcome as PracticeEvaluationOutcome.Success).result
        assertEquals("q_1", result.questionId)
        assertEquals("opt_1", result.selectedOptionId)
        assertEquals("opt_2", result.correctOptionId)
        assertEquals(PracticeEvaluationStatus.INCORRECT, result.status)
        assertEquals("Basic arithmetic: 2 + 2 equals 4.", result.explanation)
    }

    @Test
    fun execute_questionNotFound_returnsQuestionNotFoundFailure() = runTest {
        val outcome = useCase.execute(questionId = "non_existent_id", selectedOptionId = "opt_1")

        assertTrue(outcome is PracticeEvaluationOutcome.Failure.QuestionNotFound)
    }

    @Test
    fun execute_inactiveQuestion_returnsQuestionInactiveFailure() = runTest {
        val inactiveQuestion = sampleQuestion.copy(isActive = false)
        fakeRepository.questionsMap["q_1"] = inactiveQuestion

        val outcome = useCase.execute(questionId = "q_1", selectedOptionId = "opt_2")

        assertTrue(outcome is PracticeEvaluationOutcome.Failure.QuestionInactive)
    }

    @Test
    fun execute_optionNotBelongingToQuestion_returnsInvalidOptionFailure() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion

        val outcome = useCase.execute(questionId = "q_1", selectedOptionId = "foreign_option_id")

        assertTrue(outcome is PracticeEvaluationOutcome.Failure.InvalidOption)
    }

    @Test
    fun execute_blankOptionId_returnsInvalidOptionFailure() = runTest {
        fakeRepository.questionsMap["q_1"] = sampleQuestion

        val outcome = useCase.execute(questionId = "q_1", selectedOptionId = "   ")

        assertTrue(outcome is PracticeEvaluationOutcome.Failure.InvalidOption)
    }

    @Test
    fun evaluate_zeroCorrectOptions_returnsMalformedQuestionFailure() {
        val allFalseOptions = sampleOptions.map { it.copy(isCorrect = false) }
        val question = sampleQuestion.copy(options = allFalseOptions)

        val outcome = useCase.evaluate(question = question, selectedOptionId = "opt_1")

        assertTrue(outcome is PracticeEvaluationOutcome.Failure.MalformedQuestion)
    }

    @Test
    fun evaluate_multipleCorrectOptions_returnsMalformedQuestionFailure() {
        val multipleCorrectOptions = sampleOptions.map { it.copy(isCorrect = true) }
        val question = sampleQuestion.copy(options = multipleCorrectOptions)

        val outcome = useCase.evaluate(question = question, selectedOptionId = "opt_1")

        assertTrue(outcome is PracticeEvaluationOutcome.Failure.MalformedQuestion)
    }
}
