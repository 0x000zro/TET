package com.example.domain.usecase.performance

import com.example.domain.model.practice.PracticeAttempt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateStudentPerformanceUseCaseTest {

    private lateinit var useCase: CalculateStudentPerformanceUseCase

    @Before
    fun setUp() {
        useCase = CalculateStudentPerformanceUseCase()
    }

    @Test
    fun `zero-attempt handling returns zero metrics and safe 0_0 accuracy without crashing`() {
        val result = useCase(emptyList())

        assertEquals(0, result.totalCompletedAttempts)
        assertEquals(0, result.totalQuestionsAttempted)
        assertEquals(0, result.totalCorrectAnswers)
        assertEquals(0, result.totalIncorrectAnswers)
        assertEquals(0.0, result.overallAccuracyPercentage, 0.001)
        assertTrue(result.recentAttempts.isEmpty())
        assertTrue(result.subtopicPerformances.isEmpty())
    }

    @Test
    fun `zero-answered handling returns 0_0 accuracy safely without division by zero`() {
        val attempts = listOf(
            PracticeAttempt(
                id = "att_0",
                subtopicId = "sub_empty",
                totalQuestions = 5,
                answeredQuestions = 0,
                correctAnswers = 0,
                incorrectAnswers = 0,
                percentageScore = 0.0,
                startedAt = 1000L,
                completedAt = 2000L
            )
        )

        val result = useCase(attempts)

        assertEquals(1, result.totalCompletedAttempts)
        assertEquals(0, result.totalQuestionsAttempted)
        assertEquals(0, result.totalCorrectAnswers)
        assertEquals(0, result.totalIncorrectAnswers)
        assertEquals(0.0, result.overallAccuracyPercentage, 0.001)
        assertEquals(1, result.subtopicPerformances.size)
        assertEquals(0.0, result.subtopicPerformances[0].accuracyPercentage, 0.001)
    }

    @Test
    fun `aggregation of multiple attempts calculates correct and incorrect totals accurately`() {
        val attempts = listOf(
            PracticeAttempt(
                id = "att_1",
                subtopicId = "sub_math_algebra",
                totalQuestions = 10,
                answeredQuestions = 10,
                correctAnswers = 8,
                incorrectAnswers = 2,
                percentageScore = 80.0,
                startedAt = 1000L,
                completedAt = 1100L
            ),
            PracticeAttempt(
                id = "att_2",
                subtopicId = "sub_math_algebra",
                totalQuestions = 10,
                answeredQuestions = 8,
                correctAnswers = 6,
                incorrectAnswers = 2,
                percentageScore = 75.0,
                startedAt = 2000L,
                completedAt = 2100L
            ),
            PracticeAttempt(
                id = "att_3",
                subtopicId = "sub_physics_optics",
                totalQuestions = 5,
                answeredQuestions = 5,
                correctAnswers = 4,
                incorrectAnswers = 1,
                percentageScore = 80.0,
                startedAt = 3000L,
                completedAt = 3100L
            )
        )

        val result = useCase(attempts)

        assertEquals(3, result.totalCompletedAttempts)
        // 10 + 8 + 5 = 23 answered
        assertEquals(23, result.totalQuestionsAttempted)
        // 8 + 6 + 4 = 18 correct
        assertEquals(18, result.totalCorrectAnswers)
        // 2 + 2 + 1 = 5 incorrect
        assertEquals(5, result.totalIncorrectAnswers)
        // Accuracy: 18 / 23 * 100 = 78.260869%
        val expectedAccuracy = (18.0 / 23.0) * 100.0
        assertEquals(expectedAccuracy, result.overallAccuracyPercentage, 0.001)
    }

    @Test
    fun `subtopic aggregation groups attempts by subtopicId with correct metrics`() {
        val attempts = listOf(
            PracticeAttempt(
                id = "att_1",
                subtopicId = "sub_1",
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
                subtopicId = "sub_1",
                totalQuestions = 5,
                answeredQuestions = 5,
                correctAnswers = 3,
                incorrectAnswers = 2,
                percentageScore = 60.0,
                startedAt = 2000L,
                completedAt = 3000L
            ),
            PracticeAttempt(
                id = "att_3",
                subtopicId = "sub_2",
                totalQuestions = 10,
                answeredQuestions = 10,
                correctAnswers = 10,
                incorrectAnswers = 0,
                percentageScore = 100.0,
                startedAt = 3000L,
                completedAt = 4000L
            )
        )

        val subtopicTitles = mapOf("sub_1" to "Algebra", "sub_2" to "Geometry")
        val result = useCase(attempts, subtopicTitles)

        assertEquals(2, result.subtopicPerformances.size)

        // sub_1 had 2 attempts, sub_2 had 1 attempt. Ordered by attemptsCount DESC.
        val sub1 = result.subtopicPerformances[0]
        assertEquals("sub_1", sub1.subtopicId)
        assertEquals("Algebra", sub1.subtopicTitle)
        assertEquals(2, sub1.attemptsCount)
        assertEquals(10, sub1.questionsAnswered)
        assertEquals(7, sub1.correctAnswers)
        assertEquals(3, sub1.incorrectAnswers)
        assertEquals(70.0, sub1.accuracyPercentage, 0.001)

        val sub2 = result.subtopicPerformances[1]
        assertEquals("sub_2", sub2.subtopicId)
        assertEquals("Geometry", sub2.subtopicTitle)
        assertEquals(1, sub2.attemptsCount)
        assertEquals(10, sub2.questionsAnswered)
        assertEquals(10, sub2.correctAnswers)
        assertEquals(0, sub2.incorrectAnswers)
        assertEquals(100.0, sub2.accuracyPercentage, 0.001)
    }

    @Test
    fun `deterministic ordering of recent attempts orders by completedAt DESC then id DESC`() {
        val attempts = listOf(
            PracticeAttempt("id_a", "sub_1", 5, 5, 4, 1, 80.0, 100L, 500L),
            PracticeAttempt("id_c", "sub_1", 5, 5, 5, 0, 100.0, 300L, 700L),
            PracticeAttempt("id_b", "sub_1", 5, 5, 3, 2, 60.0, 200L, 500L),
            PracticeAttempt("id_d", "sub_1", 5, 5, 2, 3, 40.0, 400L, 800L)
        )

        val result = useCase(attempts)

        val recentIds = result.recentAttempts.map { it.id }
        // 800L (id_d) -> 700L (id_c) -> 500L tie broken by id DESC: "id_b" > "id_a"
        assertEquals(listOf("id_d", "id_c", "id_b", "id_a"), recentIds)
    }
}
