package com.example.domain.usecase.mocktest

import com.example.domain.model.mocktest.MockTestPerformance
import com.example.domain.model.mocktest.MockTestResult
import com.example.domain.model.mocktest.MockTestScope
import com.example.domain.model.mocktest.MockTestSession
import com.example.domain.repository.MockTestPerformanceRepository

/**
 * Use case to record the completed performance outcome of a Mock Test session (Step 21).
 *
 * Guarantees:
 * 1. Only completed sessions with valid results are recorded.
 * 2. Abandoned tests are never recorded.
 * 3. Calculates deterministic time used based on session timing.
 * 4. Stored cleanly in [MockTestPerformanceRepository] without touching PracticeAttempt.
 */
class RecordMockTestPerformanceUseCase(
    private val performanceRepository: MockTestPerformanceRepository
) {
    suspend fun execute(
        session: MockTestSession,
        result: MockTestResult
    ): MockTestPerformance? {
        if (!session.isCompleted) {
            return null
        }

        val timeUsedSeconds = if (result.finishedAt > result.startedAt && result.startedAt > 0L) {
            ((result.finishedAt - result.startedAt) / 1000L).coerceAtLeast(0L)
        } else {
            0L
        }

        val scopeDescription = when (val s = session.configuration.scope) {
            is MockTestScope.FullPaper -> "Full Paper"
            is MockTestScope.SubjectScope -> "Subject: ${s.subjectId}"
            is MockTestScope.TopicScope -> "Topic: ${s.topicId}"
            is MockTestScope.SubtopicScope -> "Subtopic: ${s.subtopicId}"
        }

        val performance = MockTestPerformance(
            sessionId = "session_${session.startedAt}_${session.configuration.id}",
            configurationId = session.configuration.id,
            examId = session.configuration.examId,
            paperId = session.configuration.paperId,
            scopeDescription = scopeDescription,
            totalQuestions = result.totalQuestions,
            answered = result.answeredQuestions,
            correct = result.correctAnswers,
            incorrect = result.incorrectAnswers,
            unanswered = result.unansweredQuestions,
            percentage = result.scorePercentage,
            startedAt = result.startedAt,
            completedAt = result.finishedAt,
            timeUsedSeconds = timeUsedSeconds
        )

        performanceRepository.recordPerformance(performance)
        return performance
    }
}
