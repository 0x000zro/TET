package com.example.domain.usecase.mocktest

import com.example.domain.model.Question
import com.example.domain.model.mocktest.MockTestResult
import com.example.domain.model.mocktest.MockTestSession
import com.example.domain.repository.EducationalRepository

/**
 * Controlled outcome of calculating and finalizing a mock test session.
 */
sealed interface FinishMockTestOutcome {
    data class Success(
        val finalSession: MockTestSession,
        val result: MockTestResult
    ) : FinishMockTestOutcome

    sealed interface Failure : FinishMockTestOutcome {
        val errorMessage: String

        data class SessionNotStarted(override val errorMessage: String = "Cannot finish a session that has not started") : Failure
        data class QuestionDataMissing(override val errorMessage: String) : Failure
    }
}

/**
 * Pure domain use case for completing a [MockTestSession] and producing an immutable [MockTestResult].
 *
 * Requirements:
 * 1. Verifies that the session has been started.
 * 2. Fetches canonical questions and options from [EducationalRepository] to ensure the evaluation
 *    is performed against the single source of truth (answer keys are never kept in session).
 * 3. Evaluates selected answers against canonical options (isCorrect).
 * 4. Produces deterministic [MockTestResult] containing:
 *    - totalQuestions
 *    - answeredQuestions
 *    - correctAnswers
 *    - incorrectAnswers
 *    - unansweredQuestions
 *    - scorePercentage
 * 5. Returns completed session and result.
 */
class FinishMockTestSessionUseCase(
    private val repository: EducationalRepository
) {

    suspend fun execute(
        session: MockTestSession,
        finishedAt: Long = System.currentTimeMillis()
    ): FinishMockTestOutcome {
        if (!session.isStarted) {
            return FinishMockTestOutcome.Failure.SessionNotStarted()
        }

        // Gather canonical questions for all question IDs in the session
        val canonicalQuestions = mutableListOf<Question>()
        for (qId in session.questionIds) {
            val q = repository.getQuestionById(qId)
            if (q != null) {
                canonicalQuestions.add(q)
            }
        }

        return evaluateAndFinish(session, canonicalQuestions, finishedAt)
    }

    /**
     * Pure in-memory evaluation for testing or direct invocation.
     */
    fun evaluateAndFinish(
        session: MockTestSession,
        canonicalQuestions: List<Question>,
        finishedAt: Long = System.currentTimeMillis()
    ): FinishMockTestOutcome {
        if (!session.isStarted) {
            return FinishMockTestOutcome.Failure.SessionNotStarted()
        }

        val completedSession = session.finish(timestamp = finishedAt)
        val result = completedSession.calculateResult(canonicalQuestions)

        return FinishMockTestOutcome.Success(
            finalSession = completedSession,
            result = result
        )
    }
}
