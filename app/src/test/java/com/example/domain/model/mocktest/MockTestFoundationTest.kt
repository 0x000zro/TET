package com.example.domain.model.mocktest

import com.example.domain.model.Question
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionOption
import com.example.domain.model.QuestionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MockTestFoundationTest {

    private fun createSampleQuestion(
        id: String,
        correctOptionIndex: Int = 0,
        isActive: Boolean = true,
        sortOrder: Int = 0
    ): Question {
        val options = (0..3).map { i ->
            QuestionOption(
                id = "opt_${id}_$i",
                questionId = id,
                optionText = "Option $i for $id",
                sortOrder = i,
                isCorrect = (i == correctOptionIndex)
            )
        }
        return Question(
            id = id,
            subtopicId = "sub_1",
            questionText = "Question text for $id",
            questionType = QuestionType.MCQ_SINGLE,
            difficulty = QuestionDifficulty.MEDIUM,
            explanation = "Explanation for $id",
            isActive = isActive,
            sortOrder = sortOrder,
            options = options
        )
    }

    private fun createValidConfig(
        id: String = "cfg_1",
        questionCount: Int = 5,
        durationMinutes: Int = 30,
        seed: Long? = 12345L
    ): MockTestConfiguration {
        return MockTestConfiguration(
            id = id,
            title = "CTET Paper 1 Mock Test",
            examId = "exam_ctet",
            paperId = "paper_1",
            scope = MockTestScope.FullPaper,
            questionCount = questionCount,
            durationMinutes = durationMinutes,
            seed = seed
        )
    }

    // --- 1. Configuration Validation Tests ---

    @Test
    fun validConfiguration_passesValidation() {
        val config = createValidConfig()
        val result = config.validate()
        assertTrue(result.isSuccess)
    }

    @Test
    fun invalidConfiguration_blankId_fails() {
        val config = createValidConfig(id = "  ")
        val result = config.validate()
        assertTrue(result.isFailure)
        assertEquals("Mock test configuration ID cannot be blank", result.exceptionOrNull()?.message)
    }

    @Test
    fun invalidConfiguration_blankTitle_fails() {
        val config = createValidConfig().copy(title = "")
        val result = config.validate()
        assertTrue(result.isFailure)
        assertEquals("Mock test title cannot be blank", result.exceptionOrNull()?.message)
    }

    @Test
    fun invalidConfiguration_blankExamId_fails() {
        val config = createValidConfig().copy(examId = "")
        val result = config.validate()
        assertTrue(result.isFailure)
        assertEquals("Exam ID cannot be blank", result.exceptionOrNull()?.message)
    }

    @Test
    fun invalidConfiguration_blankPaperId_fails() {
        val config = createValidConfig().copy(paperId = "")
        val result = config.validate()
        assertTrue(result.isFailure)
        assertEquals("Paper ID cannot be blank", result.exceptionOrNull()?.message)
    }

    @Test
    fun invalidConfiguration_zeroOrNegativeQuestionCount_fails() {
        val configZero = createValidConfig(questionCount = 0)
        assertTrue(configZero.validate().isFailure)

        val configNegative = createValidConfig(questionCount = -5)
        assertTrue(configNegative.validate().isFailure)
    }

    @Test
    fun invalidConfiguration_zeroOrNegativeDuration_fails() {
        val configZero = createValidConfig(durationMinutes = 0)
        assertTrue(configZero.validate().isFailure)

        val configNegative = createValidConfig(durationMinutes = -10)
        assertTrue(configNegative.validate().isFailure)
    }

    @Test
    fun invalidConfiguration_blankScopeIds_fails() {
        val subtopicScope = createValidConfig().copy(scope = MockTestScope.SubtopicScope(" "))
        assertTrue(subtopicScope.validate().isFailure)

        val topicScope = createValidConfig().copy(scope = MockTestScope.TopicScope(""))
        assertTrue(topicScope.validate().isFailure)

        val subjectScope = createValidConfig().copy(scope = MockTestScope.SubjectScope(" "))
        assertTrue(subjectScope.validate().isFailure)
    }

    // --- 2. Deterministic Question Ordering & Session Creation ---

    @Test
    fun sessionCreation_preservesOrderedQuestionIdsWithoutDuplicatingQuestionContent() {
        val config = createValidConfig(questionCount = 3)
        val questionIds = listOf("q_10", "q_20", "q_30")

        val session = MockTestSession(
            configuration = config,
            questionIds = questionIds
        )

        assertEquals(3, session.totalQuestions)
        assertEquals(0, session.currentIndex)
        assertEquals(1, session.displayQuestionNumber)
        assertEquals("q_10", session.currentQuestionId)
        assertFalse(session.isStarted)
        assertFalse(session.isCompleted)
        assertEquals(0, session.answeredCount)
        assertEquals(3, session.unansweredCount)
    }

    // --- 3. Session Start & Navigation ---

    @Test
    fun sessionStart_updatesStartedStateAndTimestamp() {
        val session = MockTestSession(
            configuration = createValidConfig(),
            questionIds = listOf("q_1", "q_2", "q_3")
        )

        assertFalse(session.isStarted)
        val startedSession = session.start(timestamp = 1000L)
        assertTrue(startedSession.isStarted)
        assertEquals(1000L, startedSession.startedAt)

        // Idempotent start does not overwrite timestamp
        val repeatedStart = startedSession.start(timestamp = 2000L)
        assertEquals(1000L, repeatedStart.startedAt)
    }

    @Test
    fun sessionNavigation_nextPreviousAndDirectIndex() {
        val session = MockTestSession(
            configuration = createValidConfig(),
            questionIds = listOf("q_1", "q_2", "q_3")
        ).start(1000L)

        assertEquals(0, session.currentIndex)
        assertEquals("q_1", session.currentQuestionId)
        assertTrue(session.hasNextQuestion)
        assertFalse(session.hasPreviousQuestion)

        // Next
        val q2Session = session.nextQuestion()
        assertEquals(1, q2Session.currentIndex)
        assertEquals("q_2", q2Session.currentQuestionId)
        assertTrue(q2Session.hasNextQuestion)
        assertTrue(q2Session.hasPreviousQuestion)

        // Next to last
        val q3Session = q2Session.nextQuestion()
        assertEquals(2, q3Session.currentIndex)
        assertEquals("q_3", q3Session.currentQuestionId)
        assertFalse(q3Session.hasNextQuestion)
        assertTrue(q3Session.hasPreviousQuestion)

        // Next when on last question does not overrun
        val beyondEnd = q3Session.nextQuestion()
        assertEquals(2, beyondEnd.currentIndex)

        // Previous
        val backToQ2 = q3Session.previousQuestion()
        assertEquals(1, backToQ2.currentIndex)

        // Direct navigation to valid index
        val directNav = session.navigateToIndex(2)
        assertEquals(2, directNav.currentIndex)

        // Direct navigation to invalid index ignored
        val invalidNav = session.navigateToIndex(10)
        assertEquals(0, invalidNav.currentIndex)
    }

    // --- 4. Answer Recording, Updating, and Clearing ---

    @Test
    fun answerRecording_selectClearAndCountTracking() {
        val session = MockTestSession(
            configuration = createValidConfig(),
            questionIds = listOf("q_1", "q_2", "q_3")
        ).start(1000L)

        // Cannot answer unstarted session
        val unstarted = MockTestSession(
            configuration = createValidConfig(),
            questionIds = listOf("q_1")
        )
        assertEquals(unstarted, unstarted.selectAnswer("opt_1"))

        // Select answer for q_1
        val answeredQ1 = session.selectAnswer("opt_q_1_0")
        assertEquals("opt_q_1_0", answeredQ1.currentSelectedOptionId)
        assertEquals(1, answeredQ1.answeredCount)
        assertEquals(2, answeredQ1.unansweredCount)

        // Update answer for q_1
        val updatedQ1 = answeredQ1.selectAnswer("opt_q_1_2")
        assertEquals("opt_q_1_2", updatedQ1.currentSelectedOptionId)
        assertEquals(1, answeredQ1.answeredCount)

        // Advance to q_2 and select answer
        val q2Answered = updatedQ1.nextQuestion().selectAnswer("opt_q_2_1")
        assertEquals(2, q2Answered.answeredCount)
        assertEquals(1, q2Answered.unansweredCount)

        // Clear answer for q_2
        val q2Cleared = q2Answered.clearAnswer()
        assertNull(q2Cleared.currentSelectedOptionId)
        assertEquals(1, q2Cleared.answeredCount)
        assertEquals(2, q2Cleared.unansweredCount)
    }

    // --- 5. Finish Session & Immutable State Protection ---

    @Test
    fun finishSession_marksCompletedAndLocksTransitions() {
        val session = MockTestSession(
            configuration = createValidConfig(),
            questionIds = listOf("q_1", "q_2")
        ).start(1000L).selectAnswer("opt_q_1_0")

        val finished = session.finish(timestamp = 5000L)
        assertTrue(finished.isCompleted)
        assertEquals(5000L, finished.finishedAt)

        // Subsequent mutations on finished session are no-ops
        val trySelect = finished.selectAnswer("opt_q_1_1")
        assertEquals(finished, trySelect)

        val tryClear = finished.clearAnswer()
        assertEquals(finished, tryClear)

        val tryNext = finished.nextQuestion()
        assertEquals(finished, tryNext)

        val tryPrev = finished.previousQuestion()
        assertEquals(finished, tryPrev)

        val tryNav = finished.navigateToIndex(1)
        assertEquals(finished, tryNav)
    }

    // --- 6. Deterministic Result Calculation ---

    @Test
    fun calculateResult_allCorrect() {
        val q1 = createSampleQuestion("q_1", correctOptionIndex = 0)
        val q2 = createSampleQuestion("q_2", correctOptionIndex = 1)
        val q3 = createSampleQuestion("q_3", correctOptionIndex = 2)

        val session = MockTestSession(
            configuration = createValidConfig(questionCount = 3),
            questionIds = listOf("q_1", "q_2", "q_3")
        ).start(1000L)
            .selectAnswer("opt_q_1_0") // correct
            .nextQuestion()
            .selectAnswer("opt_q_2_1") // correct
            .nextQuestion()
            .selectAnswer("opt_q_3_2") // correct
            .finish(2000L)

        val result = session.calculateResult(listOf(q1, q2, q3))

        assertEquals("cfg_1", result.configurationId)
        assertEquals(3, result.totalQuestions)
        assertEquals(3, result.answeredQuestions)
        assertEquals(3, result.correctAnswers)
        assertEquals(0, result.incorrectAnswers)
        assertEquals(0, result.unansweredQuestions)
        assertEquals(100.0, result.scorePercentage, 0.001)
        assertEquals(1000L, result.startedAt)
        assertEquals(2000L, result.finishedAt)
    }

    @Test
    fun calculateResult_partialAndUnanswered() {
        val q1 = createSampleQuestion("q_1", correctOptionIndex = 0)
        val q2 = createSampleQuestion("q_2", correctOptionIndex = 1)
        val q3 = createSampleQuestion("q_3", correctOptionIndex = 2)
        val q4 = createSampleQuestion("q_4", correctOptionIndex = 3)

        val session = MockTestSession(
            configuration = createValidConfig(questionCount = 4),
            questionIds = listOf("q_1", "q_2", "q_3", "q_4")
        ).start(1000L)
            .selectAnswer("opt_q_1_0") // correct
            .nextQuestion()
            .selectAnswer("opt_q_2_0") // incorrect (opt 1 is correct)
            .nextQuestion()
            // q_3 left unanswered
            .nextQuestion()
            .selectAnswer("opt_q_4_3") // correct
            .finish(3000L)

        val result = session.calculateResult(listOf(q1, q2, q3, q4))

        assertEquals(4, result.totalQuestions)
        assertEquals(3, result.answeredQuestions)
        assertEquals(2, result.correctAnswers)
        assertEquals(1, result.incorrectAnswers)
        assertEquals(1, result.unansweredQuestions)
        assertEquals(50.0, result.scorePercentage, 0.001)
    }

    @Test
    fun mockTestResultFrom_handlesZeroQuestionsSafely() {
        val result = MockTestResult.from(
            configurationId = "cfg_empty",
            totalQuestions = 0,
            answeredQuestions = 0,
            correctAnswers = 0
        )

        assertEquals(0, result.totalQuestions)
        assertEquals(0, result.answeredQuestions)
        assertEquals(0, result.correctAnswers)
        assertEquals(0, result.incorrectAnswers)
        assertEquals(0, result.unansweredQuestions)
        assertEquals(0.0, result.scorePercentage, 0.001)
    }
}
