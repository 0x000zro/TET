package com.example.domain.model.mocktest

/**
 * Defines the educational boundary/scope for a Mock Test.
 */
sealed interface MockTestScope {
    /**
     * Scope covering all subjects, topics, and subtopics within the given Paper.
     */
    data object FullPaper : MockTestScope

    /**
     * Scope restricted to a specific Subject ID within the Paper.
     */
    data class SubjectScope(val subjectId: String) : MockTestScope

    /**
     * Scope restricted to a specific Topic ID within the Paper.
     */
    data class TopicScope(val topicId: String) : MockTestScope

    /**
     * Scope restricted to a specific Subtopic ID within the Paper.
     */
    data class SubtopicScope(val subtopicId: String) : MockTestScope
}

/**
 * Pure Kotlin domain model representing the configuration parameters for launching a Mock Test.
 *
 * Requirements:
 * - [id]: Unique stable identifier for this test run / configuration
 * - [title]: Descriptive title for the mock test session
 * - [examId]: Mandatory Exam ID
 * - [paperId]: Mandatory Paper ID
 * - [scope]: Granular scope (Full paper, Subject, Topic, or Subtopic)
 * - [questionCount]: Configured target count of questions (must be > 0)
 * - [durationMinutes]: Time allotment in minutes (must be > 0)
 * - [seed]: Optional deterministic random seed to support repeatable question order when shuffling
 *
 * Immutability and pure domain validation rules are enforced.
 */
data class MockTestConfiguration(
    val id: String,
    val title: String,
    val examId: String,
    val paperId: String,
    val scope: MockTestScope = MockTestScope.FullPaper,
    val questionCount: Int,
    val durationMinutes: Int,
    val seed: Long? = null
) {
    /**
     * Validates whether this configuration is logically well-formed.
     */
    fun validate(): Result<Unit> {
        if (id.isBlank()) {
            return Result.failure(IllegalArgumentException("Mock test configuration ID cannot be blank"))
        }
        if (title.isBlank()) {
            return Result.failure(IllegalArgumentException("Mock test title cannot be blank"))
        }
        if (examId.isBlank()) {
            return Result.failure(IllegalArgumentException("Exam ID cannot be blank"))
        }
        if (paperId.isBlank()) {
            return Result.failure(IllegalArgumentException("Paper ID cannot be blank"))
        }
        if (questionCount <= 0) {
            return Result.failure(IllegalArgumentException("Question count must be greater than 0"))
        }
        if (durationMinutes <= 0) {
            return Result.failure(IllegalArgumentException("Duration must be greater than 0 minutes"))
        }
        when (val s = scope) {
            is MockTestScope.SubjectScope -> if (s.subjectId.isBlank()) {
                return Result.failure(IllegalArgumentException("Subject ID cannot be blank for SubjectScope"))
            }
            is MockTestScope.TopicScope -> if (s.topicId.isBlank()) {
                return Result.failure(IllegalArgumentException("Topic ID cannot be blank for TopicScope"))
            }
            is MockTestScope.SubtopicScope -> if (s.subtopicId.isBlank()) {
                return Result.failure(IllegalArgumentException("Subtopic ID cannot be blank for SubtopicScope"))
            }
            MockTestScope.FullPaper -> Unit
        }
        return Result.success(Unit)
    }
}
