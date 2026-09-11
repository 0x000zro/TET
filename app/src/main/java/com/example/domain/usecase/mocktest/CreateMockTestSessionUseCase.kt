package com.example.domain.usecase.mocktest

import com.example.domain.model.Question
import com.example.domain.model.mocktest.MockTestConfiguration
import com.example.domain.model.mocktest.MockTestScope
import com.example.domain.model.mocktest.MockTestSession
import com.example.domain.repository.EducationalRepository
import kotlinx.coroutines.flow.first
import java.util.Random

/**
 * Controlled outcome when creating a mock test session.
 */
sealed interface CreateMockTestOutcome {
    data class Success(val session: MockTestSession) : CreateMockTestOutcome

    sealed interface Failure : CreateMockTestOutcome {
        val errorMessage: String

        data class InvalidConfiguration(override val errorMessage: String) : Failure
        data class HierarchyNotFound(override val errorMessage: String) : Failure
        data class InsufficientQuestions(
            override val errorMessage: String,
            val availableCount: Int,
            val requestedCount: Int
        ) : Failure
    }
}

/**
 * Pure domain use case responsible for configuring and creating a deterministic [MockTestSession].
 *
 * Rules:
 * 1. Validates [MockTestConfiguration] strictly (positive counts, positive duration, non-blank IDs).
 * 2. Gathers available canonical questions matching the requested [MockTestScope].
 * 3. Applies deterministic selection and ordering:
 *    - If [configuration.seed] is provided: uses deterministic pseudo-random shuffling with java.util.Random(seed).
 *    - If [configuration.seed] is null: orders deterministically by sortOrder ASC, id ASC.
 * 4. Limits the selection to [configuration.questionCount].
 * 5. Returns a ready-to-start [MockTestSession].
 */
class CreateMockTestSessionUseCase(
    private val repository: EducationalRepository
) {

    suspend fun execute(configuration: MockTestConfiguration): CreateMockTestOutcome {
        val validation = configuration.validate()
        if (validation.isFailure) {
            return CreateMockTestOutcome.Failure.InvalidConfiguration(
                validation.exceptionOrNull()?.message ?: "Invalid mock test configuration"
            )
        }

        // Verify Exam exists
        val exam = repository.getExamById(configuration.examId)
            ?: return CreateMockTestOutcome.Failure.HierarchyNotFound("Exam not found: ${configuration.examId}")

        // Verify Paper exists and belongs to Exam
        val paper = repository.getPaperById(configuration.paperId)
            ?: return CreateMockTestOutcome.Failure.HierarchyNotFound("Paper not found: ${configuration.paperId}")

        if (paper.examId != exam.id) {
            return CreateMockTestOutcome.Failure.HierarchyNotFound("Paper ${paper.id} does not belong to Exam ${exam.id}")
        }

        // Resolve candidate subtopic IDs based on scope
        val subtopicIds = resolveSubtopicIds(configuration.paperId, configuration.scope)
        if (subtopicIds.isEmpty()) {
            return CreateMockTestOutcome.Failure.InsufficientQuestions(
                errorMessage = "No matching subtopics found for the specified scope.",
                availableCount = 0,
                requestedCount = configuration.questionCount
            )
        }

        // Gather all active questions under resolved subtopics
        val allQuestions = mutableListOf<Question>()
        for (subtopicId in subtopicIds) {
            val subtopic = repository.getSubtopicById(subtopicId)
            if (subtopic != null && subtopic.isActive) {
                try {
                    val questions: List<Question> = repository.observeQuestionsForSubtopic(subtopicId, activeOnly = true).first()
                    allQuestions.addAll(questions)
                } catch (_: Exception) {
                    // Ignored or handled gracefully
                }
            }
        }

        return createFromQuestions(configuration, allQuestions)
    }

    /**
     * Resolves subtopic IDs strictly matching the scope.
     */
    suspend fun resolveSubtopicIds(paperId: String, scope: MockTestScope): List<String> {
        return when (scope) {
            is MockTestScope.SubtopicScope -> {
                val subtopic = repository.getSubtopicById(scope.subtopicId)
                if (subtopic != null && subtopic.isActive) listOf(scope.subtopicId) else emptyList()
            }
            is MockTestScope.TopicScope -> {
                try {
                    val subtopics = repository.observeActiveSubtopicsByTopicId(scope.topicId).first()
                    subtopics.map { it.id }
                } catch (_: Exception) {
                    emptyList()
                }
            }
            is MockTestScope.SubjectScope -> {
                try {
                    val topics = repository.observeActiveTopicsBySubjectId(scope.subjectId).first()
                    val result = mutableListOf<String>()
                    for (t in topics) {
                        val subtopics = repository.observeActiveSubtopicsByTopicId(t.id).first()
                        result.addAll(subtopics.map { it.id })
                    }
                    result
                } catch (_: Exception) {
                    emptyList()
                }
            }
            MockTestScope.FullPaper -> {
                try {
                    val subjects = repository.observeActiveSubjectsByPaperId(paperId).first()
                    val result = mutableListOf<String>()
                    for (s in subjects) {
                        val topics = repository.observeActiveTopicsBySubjectId(s.id).first()
                        for (t in topics) {
                            val subtopics = repository.observeActiveSubtopicsByTopicId(t.id).first()
                            result.addAll(subtopics.map { it.id })
                        }
                    }
                    result
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }
    }

    /**
     * Pure in-memory factory for creating a session from candidate questions.
     * Ensures deterministic ordering and supports unit testing without IO.
     */
    fun createFromQuestions(
        configuration: MockTestConfiguration,
        candidateQuestions: List<Question>
    ): CreateMockTestOutcome {
        val validation = configuration.validate()
        if (validation.isFailure) {
            return CreateMockTestOutcome.Failure.InvalidConfiguration(
                validation.exceptionOrNull()?.message ?: "Invalid mock test configuration"
            )
        }

        val activeQuestions = candidateQuestions.filter { it.isActive }
        if (activeQuestions.size < configuration.questionCount) {
            return CreateMockTestOutcome.Failure.InsufficientQuestions(
                errorMessage = "Insufficient active questions available: required ${configuration.questionCount}, found ${activeQuestions.size}",
                availableCount = activeQuestions.size,
                requestedCount = configuration.questionCount
            )
        }

        val orderedQuestions = orderQuestions(activeQuestions, configuration.seed)
            .take(configuration.questionCount)

        val session = MockTestSession(
            configuration = configuration,
            questionIds = orderedQuestions.map { it.id }
        )

        return CreateMockTestOutcome.Success(session)
    }

    /**
     * Orders questions deterministically.
     * If seed is non-null, uses a deterministic seeded shuffle.
     * If seed is null, uses deterministic natural sort order (sortOrder ASC, id ASC).
     */
    fun orderQuestions(questions: List<Question>, seed: Long?): List<Question> {
        return if (seed != null) {
            val list = questions.sortedWith(compareBy({ it.sortOrder }, { it.id })).toMutableList()
            val rnd = Random(seed)
            for (i in list.size - 1 downTo 1) {
                val j = rnd.nextInt(i + 1)
                val temp = list[i]
                list[i] = list[j]
                list[j] = temp
            }
            list
        } else {
            questions.sortedWith(compareBy({ it.sortOrder }, { it.id }))
        }
    }
}
