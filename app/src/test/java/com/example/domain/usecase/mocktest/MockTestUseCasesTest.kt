package com.example.domain.usecase.mocktest

import com.example.domain.model.Question
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionOption
import com.example.domain.model.QuestionType
import com.example.domain.model.mocktest.MockTestConfiguration
import com.example.domain.model.mocktest.MockTestScope
import com.example.domain.model.mocktest.MockTestSession
import com.example.testutil.TestEducationalRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockTestUseCasesTest {

    private lateinit var repository: TestEducationalRepository
    private lateinit var createUseCase: CreateMockTestSessionUseCase
    private lateinit var finishUseCase: FinishMockTestSessionUseCase

    @Before
    fun setUp() {
        repository = TestEducationalRepository()
        createUseCase = CreateMockTestSessionUseCase(repository)
        finishUseCase = FinishMockTestSessionUseCase(repository)
    }

    private fun createQuestion(id: String, sortOrder: Int = 0, isActive: Boolean = true, correctIndex: Int = 0): Question {
        return Question(
            id = id,
            subtopicId = "sub_1",
            questionText = "Question text $id",
            questionType = QuestionType.MCQ_SINGLE,
            difficulty = QuestionDifficulty.MEDIUM,
            explanation = "Explanation $id",
            isActive = isActive,
            sortOrder = sortOrder,
            options = (0..3).map { i ->
                QuestionOption(
                    id = "opt_${id}_$i",
                    questionId = id,
                    optionText = "Option $i",
                    sortOrder = i,
                    isCorrect = (i == correctIndex)
                )
            }
        )
    }

    @Test
    fun createFromQuestions_deterministicOrdering_withoutSeedUsesSortOrderThenId() {
        val q1 = createQuestion("q_c", sortOrder = 2)
        val q2 = createQuestion("q_a", sortOrder = 1)
        val q3 = createQuestion("q_b", sortOrder = 1)
        val q4 = createQuestion("q_d", sortOrder = 3)

        val config = MockTestConfiguration(
            id = "cfg_natural",
            title = "Natural Sort Test",
            examId = "exam_1",
            paperId = "paper_1",
            scope = MockTestScope.FullPaper,
            questionCount = 3,
            durationMinutes = 30,
            seed = null
        )

        val outcome = createUseCase.createFromQuestions(config, listOf(q1, q2, q3, q4))
        assertTrue(outcome is CreateMockTestOutcome.Success)
        val session = (outcome as CreateMockTestOutcome.Success).session

        // Ordered by sortOrder ASC, then id ASC: q_a (1), q_b (1), q_c (2)
        assertEquals(listOf("q_a", "q_b", "q_c"), session.questionIds)
    }

    @Test
    fun createFromQuestions_deterministicOrdering_withSeedIsRepeatable() {
        val questions = (1..10).map { i -> createQuestion("q_$i", sortOrder = i) }

        val config1 = MockTestConfiguration(
            id = "cfg_seed_1",
            title = "Seed Test 1",
            examId = "exam_1",
            paperId = "paper_1",
            scope = MockTestScope.FullPaper,
            questionCount = 5,
            durationMinutes = 30,
            seed = 987654321L
        )

        val config2 = MockTestConfiguration(
            id = "cfg_seed_2",
            title = "Seed Test 2",
            examId = "exam_1",
            paperId = "paper_1",
            scope = MockTestScope.FullPaper,
            questionCount = 5,
            durationMinutes = 30,
            seed = 987654321L
        )

        val outcome1 = createUseCase.createFromQuestions(config1, questions)
        val outcome2 = createUseCase.createFromQuestions(config2, questions)

        assertTrue(outcome1 is CreateMockTestOutcome.Success)
        assertTrue(outcome2 is CreateMockTestOutcome.Success)

        val ids1 = (outcome1 as CreateMockTestOutcome.Success).session.questionIds
        val ids2 = (outcome2 as CreateMockTestOutcome.Success).session.questionIds

        // Exactly repeatable across identical seeds
        assertEquals(ids1, ids2)
        assertEquals(5, ids1.size)
    }

    @Test
    fun createFromQuestions_insufficientActiveQuestions_fails() {
        val q1 = createQuestion("q_1", isActive = true)
        val q2 = createQuestion("q_2", isActive = false) // inactive

        val config = MockTestConfiguration(
            id = "cfg_insufficient",
            title = "Insufficient Test",
            examId = "exam_1",
            paperId = "paper_1",
            scope = MockTestScope.FullPaper,
            questionCount = 2, // requires 2, only 1 active
            durationMinutes = 20
        )

        val outcome = createUseCase.createFromQuestions(config, listOf(q1, q2))
        assertTrue(outcome is CreateMockTestOutcome.Failure.InsufficientQuestions)
        val failure = outcome as CreateMockTestOutcome.Failure.InsufficientQuestions
        assertEquals(1, failure.availableCount)
        assertEquals(2, failure.requestedCount)
    }

    @Test
    fun finishSession_unstartedSession_fails() = runTest {
        val session = MockTestSession(
            configuration = MockTestConfiguration(
                id = "cfg_1",
                title = "Test",
                examId = "e1",
                paperId = "p1",
                questionCount = 1,
                durationMinutes = 10
            ),
            questionIds = listOf("q_1")
        )

        val outcome = finishUseCase.execute(session)
        assertTrue(outcome is FinishMockTestOutcome.Failure.SessionNotStarted)
    }

    @Test
    fun finishSession_evaluatesAgainstCanonicalQuestionsFromRepository() = runTest {
        val q1 = createQuestion("q_1", correctIndex = 0)
        val q2 = createQuestion("q_2", correctIndex = 1)
        repository.questionsMap["q_1"] = q1
        repository.questionsMap["q_2"] = q2

        val session = MockTestSession(
            configuration = MockTestConfiguration(
                id = "cfg_1",
                title = "Test",
                examId = "e1",
                paperId = "p1",
                questionCount = 2,
                durationMinutes = 10
            ),
            questionIds = listOf("q_1", "q_2")
        ).start(1000L)
            .selectAnswer("opt_q_1_0") // Correct
            .nextQuestion()
            .selectAnswer("opt_q_2_3") // Incorrect

        val outcome = finishUseCase.execute(session, finishedAt = 2500L)
        assertTrue(outcome is FinishMockTestOutcome.Success)

        val success = outcome as FinishMockTestOutcome.Success
        assertTrue(success.finalSession.isCompleted)
        assertEquals(2500L, success.finalSession.finishedAt)

        val result = success.result
        assertEquals("cfg_1", result.configurationId)
        assertEquals(2, result.totalQuestions)
        assertEquals(2, result.answeredQuestions)
        assertEquals(1, result.correctAnswers)
        assertEquals(1, result.incorrectAnswers)
        assertEquals(0, result.unansweredQuestions)
        assertEquals(50.0, result.scorePercentage, 0.001)
        assertEquals(1000L, result.startedAt)
        assertEquals(2500L, result.finishedAt)
    }
}
