package com.example.data.local.db.mapper

import com.example.data.local.db.entity.AppStateEntity
import com.example.data.local.db.entity.BookmarkedQuestionEntity
import com.example.data.local.db.entity.ContentSyncStateEntity
import com.example.data.local.db.entity.ExamEntity
import com.example.data.local.db.entity.LocalPreferenceEntity
import com.example.data.local.db.entity.PaperEntity
import com.example.data.local.db.entity.PracticeAttemptEntity
import com.example.data.local.db.entity.QuestionEntity
import com.example.data.local.db.entity.QuestionOptionEntity
import com.example.data.local.db.entity.QuestionWithOptionsEntity
import com.example.data.local.db.entity.SubjectEntity
import com.example.data.local.db.entity.SubtopicEntity
import com.example.data.local.db.entity.TopicEntity
import com.example.data.local.db.entity.WrongQuestionEntity
import com.example.domain.model.AppState
import com.example.domain.model.ContentSyncState
import com.example.domain.model.Exam
import com.example.domain.model.LocalPreference
import com.example.domain.model.Paper
import com.example.domain.model.Question
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionOption
import com.example.domain.model.QuestionType
import com.example.domain.model.Subject
import com.example.domain.model.Subtopic
import com.example.domain.model.SyncStatus
import com.example.domain.model.Topic
import com.example.domain.model.bookmark.BookmarkedQuestion
import com.example.domain.model.practice.PracticeAttempt
import com.example.domain.model.wrongquestion.WrongQuestion

/**
 * Clean Architecture mappers converting between Room database entities
 * and framework-independent domain models.
 */
object DatabaseMappers {

    fun AppStateEntity.toDomain(): AppState {
        return AppState(
            id = this.id,
            isFirstLaunch = this.isFirstLaunch,
            isContentInitialized = this.isContentInitialized,
            lastKnownContentVersion = this.lastKnownContentVersion,
            schemaVersion = this.schemaVersion,
            isCompatible = this.isCompatible,
            lastLaunchTimestamp = this.lastLaunchTimestamp,
            updatedAt = this.updatedAtTimestamp
        )
    }

    fun AppState.toEntity(): AppStateEntity {
        return AppStateEntity(
            id = this.id,
            isFirstLaunch = this.isFirstLaunch,
            isContentInitialized = this.isContentInitialized,
            lastKnownContentVersion = this.lastKnownContentVersion,
            schemaVersion = this.schemaVersion,
            isCompatible = this.isCompatible,
            lastLaunchTimestamp = this.lastLaunchTimestamp,
            updatedAtTimestamp = this.updatedAt
        )
    }

    fun LocalPreferenceEntity.toDomain(): LocalPreference {
        return LocalPreference(
            key = this.preferenceKey,
            value = this.preferenceValue,
            updatedAt = this.updatedAtTimestamp
        )
    }

    fun LocalPreference.toEntity(): LocalPreferenceEntity {
        return LocalPreferenceEntity(
            preferenceKey = this.key,
            preferenceValue = this.value,
            updatedAtTimestamp = this.updatedAt
        )
    }

    fun ContentSyncStateEntity.toDomain(): ContentSyncState {
        val status = try {
            SyncStatus.valueOf(this.syncStatus)
        } catch (_: IllegalArgumentException) {
            SyncStatus.IDLE
        }

        return ContentSyncState(
            contentSource = this.contentSource,
            contentVersion = this.contentVersion,
            lastSuccessfulSyncTimestamp = this.lastSuccessfulSyncTimestamp,
            syncStatus = status,
            errorMessage = this.errorMessage,
            updatedAt = this.updatedAtTimestamp
        )
    }

    fun ContentSyncState.toEntity(): ContentSyncStateEntity {
        return ContentSyncStateEntity(
            contentSource = this.contentSource,
            contentVersion = this.contentVersion,
            lastSuccessfulSyncTimestamp = this.lastSuccessfulSyncTimestamp,
            syncStatus = this.syncStatus.name,
            errorMessage = this.errorMessage,
            updatedAtTimestamp = this.updatedAt
        )
    }

    // --- Educational Hierarchy Mappers ---

    fun ExamEntity.toDomain(): Exam {
        return Exam(
            id = this.id,
            name = this.name,
            shortName = this.shortName,
            description = this.description,
            isActive = this.isActive,
            sortOrder = this.sortOrder
        )
    }

    fun Exam.toEntity(updatedAtTimestamp: Long = System.currentTimeMillis()): ExamEntity {
        return ExamEntity(
            id = this.id,
            name = this.name,
            shortName = this.shortName,
            description = this.description,
            isActive = this.isActive,
            sortOrder = this.sortOrder,
            updatedAtTimestamp = updatedAtTimestamp
        )
    }

    fun PaperEntity.toDomain(): Paper {
        return Paper(
            id = this.id,
            examId = this.examId,
            name = this.name,
            shortName = this.shortName,
            description = this.description,
            isActive = this.isActive,
            sortOrder = this.sortOrder
        )
    }

    fun Paper.toEntity(updatedAtTimestamp: Long = System.currentTimeMillis()): PaperEntity {
        return PaperEntity(
            id = this.id,
            examId = this.examId,
            name = this.name,
            shortName = this.shortName,
            description = this.description,
            isActive = this.isActive,
            sortOrder = this.sortOrder,
            updatedAtTimestamp = updatedAtTimestamp
        )
    }

    fun SubjectEntity.toDomain(): Subject {
        return Subject(
            id = this.id,
            paperId = this.paperId,
            name = this.name,
            shortName = this.shortName,
            description = this.description,
            isActive = this.isActive,
            sortOrder = this.sortOrder
        )
    }

    fun Subject.toEntity(updatedAtTimestamp: Long = System.currentTimeMillis()): SubjectEntity {
        return SubjectEntity(
            id = this.id,
            paperId = this.paperId,
            name = this.name,
            shortName = this.shortName,
            description = this.description,
            isActive = this.isActive,
            sortOrder = this.sortOrder,
            updatedAtTimestamp = updatedAtTimestamp
        )
    }

    fun TopicEntity.toDomain(): Topic {
        return Topic(
            id = this.id,
            subjectId = this.subjectId,
            name = this.name,
            description = this.description,
            isActive = this.isActive,
            sortOrder = this.sortOrder
        )
    }

    fun Topic.toEntity(updatedAtTimestamp: Long = System.currentTimeMillis()): TopicEntity {
        return TopicEntity(
            id = this.id,
            subjectId = this.subjectId,
            name = this.name,
            description = this.description,
            isActive = this.isActive,
            sortOrder = this.sortOrder,
            updatedAtTimestamp = updatedAtTimestamp
        )
    }

    fun SubtopicEntity.toDomain(): Subtopic {
        return Subtopic(
            id = this.id,
            topicId = this.topicId,
            name = this.name,
            description = this.description,
            isActive = this.isActive,
            sortOrder = this.sortOrder
        )
    }

    fun Subtopic.toEntity(updatedAtTimestamp: Long = System.currentTimeMillis()): SubtopicEntity {
        return SubtopicEntity(
            id = this.id,
            topicId = this.topicId,
            name = this.name,
            description = this.description,
            isActive = this.isActive,
            sortOrder = this.sortOrder,
            updatedAtTimestamp = updatedAtTimestamp
        )
    }

    // --- Syllabus Metadata & Node Mappers ---

    fun com.example.data.local.db.entity.SyllabusMetadataEntity.toDomain(): com.example.domain.model.SyllabusMetadata {
        return com.example.domain.model.SyllabusMetadata(
            nodeId = this.nodeId,
            learningObjective = this.learningObjective,
            shortNote = this.shortNote,
            estimatedMinutes = this.estimatedMinutes
        )
    }

    fun com.example.domain.model.SyllabusMetadata.toEntity(updatedAtTimestamp: Long = System.currentTimeMillis()): com.example.data.local.db.entity.SyllabusMetadataEntity {
        return com.example.data.local.db.entity.SyllabusMetadataEntity(
            nodeId = this.nodeId,
            learningObjective = this.learningObjective,
            shortNote = this.shortNote,
            estimatedMinutes = this.estimatedMinutes,
            updatedAtTimestamp = updatedAtTimestamp
        )
    }

    fun Exam.toSyllabusNode(metadata: com.example.domain.model.SyllabusMetadata? = null): com.example.domain.model.SyllabusNode {
        return com.example.domain.model.SyllabusNode(
            id = this.id,
            parentId = null,
            title = this.name,
            description = this.description,
            nodeType = com.example.domain.model.SyllabusNodeType.EXAM,
            isActive = this.isActive,
            sortOrder = this.sortOrder,
            learningObjective = metadata?.learningObjective.orEmpty(),
            shortNote = metadata?.shortNote.orEmpty()
        )
    }

    fun Paper.toSyllabusNode(metadata: com.example.domain.model.SyllabusMetadata? = null): com.example.domain.model.SyllabusNode {
        return com.example.domain.model.SyllabusNode(
            id = this.id,
            parentId = this.examId,
            title = this.name,
            description = this.description,
            nodeType = com.example.domain.model.SyllabusNodeType.PAPER,
            isActive = this.isActive,
            sortOrder = this.sortOrder,
            learningObjective = metadata?.learningObjective.orEmpty(),
            shortNote = metadata?.shortNote.orEmpty()
        )
    }

    fun Subject.toSyllabusNode(metadata: com.example.domain.model.SyllabusMetadata? = null): com.example.domain.model.SyllabusNode {
        return com.example.domain.model.SyllabusNode(
            id = this.id,
            parentId = this.paperId,
            title = this.name,
            description = this.description,
            nodeType = com.example.domain.model.SyllabusNodeType.SUBJECT,
            isActive = this.isActive,
            sortOrder = this.sortOrder,
            learningObjective = metadata?.learningObjective.orEmpty(),
            shortNote = metadata?.shortNote.orEmpty()
        )
    }

    fun Topic.toSyllabusNode(metadata: com.example.domain.model.SyllabusMetadata? = null): com.example.domain.model.SyllabusNode {
        return com.example.domain.model.SyllabusNode(
            id = this.id,
            parentId = this.subjectId,
            title = this.name,
            description = this.description,
            nodeType = com.example.domain.model.SyllabusNodeType.TOPIC,
            isActive = this.isActive,
            sortOrder = this.sortOrder,
            learningObjective = metadata?.learningObjective.orEmpty(),
            shortNote = metadata?.shortNote.orEmpty()
        )
    }

    fun Subtopic.toSyllabusNode(metadata: com.example.domain.model.SyllabusMetadata? = null): com.example.domain.model.SyllabusNode {
        return com.example.domain.model.SyllabusNode(
            id = this.id,
            parentId = this.topicId,
            title = this.name,
            description = this.description,
            nodeType = com.example.domain.model.SyllabusNodeType.SUBTOPIC,
            isActive = this.isActive,
            sortOrder = this.sortOrder,
            learningObjective = metadata?.learningObjective.orEmpty(),
            shortNote = metadata?.shortNote.orEmpty()
        )
    }

    // --- Question Foundation Mappers ---

    fun QuestionEntity.toDomain(options: List<QuestionOption> = emptyList()): Question {
        return Question(
            id = this.id,
            subtopicId = this.subtopicId,
            questionText = this.questionText,
            questionType = QuestionType.fromString(this.questionType),
            difficulty = QuestionDifficulty.fromString(this.difficulty),
            explanation = this.explanation,
            isActive = this.isActive,
            sortOrder = this.sortOrder,
            options = options.sortedWith(compareBy({ it.sortOrder }, { it.id }))
        )
    }

    fun Question.toEntity(): QuestionEntity {
        return QuestionEntity(
            id = this.id,
            subtopicId = this.subtopicId,
            questionText = this.questionText,
            questionType = this.questionType.name,
            difficulty = this.difficulty.name,
            explanation = this.explanation,
            isActive = this.isActive,
            sortOrder = this.sortOrder,
            updatedAtTimestamp = System.currentTimeMillis()
        )
    }

    fun QuestionOptionEntity.toDomain(): QuestionOption {
        return QuestionOption(
            id = this.id,
            questionId = this.questionId,
            optionText = this.optionText,
            sortOrder = this.sortOrder,
            isCorrect = this.isCorrect
        )
    }

    fun QuestionOption.toEntity(): QuestionOptionEntity {
        return QuestionOptionEntity(
            id = this.id,
            questionId = this.questionId,
            optionText = this.optionText,
            sortOrder = this.sortOrder,
            isCorrect = this.isCorrect,
            updatedAtTimestamp = System.currentTimeMillis()
        )
    }

    fun QuestionWithOptionsEntity.toDomain(): Question {
        return this.question.toDomain(
            options = this.options.map { it.toDomain() }
        )
    }

    fun PracticeAttemptEntity.toDomain(): PracticeAttempt {
        return PracticeAttempt(
            id = this.id,
            subtopicId = this.subtopicId,
            totalQuestions = this.totalQuestions,
            answeredQuestions = this.answeredQuestions,
            correctAnswers = this.correctAnswers,
            incorrectAnswers = this.incorrectAnswers,
            percentageScore = this.percentageScore,
            startedAt = this.startedAt,
            completedAt = this.completedAt
        )
    }

    fun PracticeAttempt.toEntity(): PracticeAttemptEntity {
        return PracticeAttemptEntity(
            id = this.id,
            subtopicId = this.subtopicId,
            totalQuestions = this.totalQuestions,
            answeredQuestions = this.answeredQuestions,
            correctAnswers = this.correctAnswers,
            incorrectAnswers = this.incorrectAnswers,
            percentageScore = this.percentageScore,
            startedAt = this.startedAt,
            completedAt = this.completedAt,
            updatedAtTimestamp = System.currentTimeMillis()
        )
    }

    fun WrongQuestionEntity.toDomain(): WrongQuestion {
        return WrongQuestion(
            questionId = this.questionId,
            subtopicId = this.subtopicId,
            firstWrongAt = this.firstWrongAt,
            lastWrongAt = this.lastWrongAt,
            wrongCount = this.wrongCount,
            lastAttemptId = this.lastAttemptId
        )
    }

    fun WrongQuestion.toEntity(updatedAtTimestamp: Long = System.currentTimeMillis()): WrongQuestionEntity {
        return WrongQuestionEntity(
            questionId = this.questionId,
            subtopicId = this.subtopicId,
            firstWrongAt = this.firstWrongAt,
            lastWrongAt = this.lastWrongAt,
            wrongCount = this.wrongCount,
            lastAttemptId = this.lastAttemptId,
            updatedAtTimestamp = updatedAtTimestamp
        )
    }

    // --- Bookmarked Questions Mappers (Step 14) ---

    fun BookmarkedQuestionEntity.toDomain(): BookmarkedQuestion {
        return BookmarkedQuestion(
            questionId = this.questionId,
            subtopicId = this.subtopicId,
            bookmarkedAt = this.bookmarkedAt
        )
    }

    fun BookmarkedQuestion.toEntity(updatedAtTimestamp: Long = System.currentTimeMillis()): BookmarkedQuestionEntity {
        return BookmarkedQuestionEntity(
            questionId = this.questionId,
            subtopicId = this.subtopicId,
            bookmarkedAt = this.bookmarkedAt,
            updatedAtTimestamp = updatedAtTimestamp
        )
    }
}
