package com.example.data.repository

import com.example.data.local.datasource.DefaultLocalEducationalDataSource
import com.example.data.local.datasource.LocalEducationalDataSource
import com.example.data.remote.datasource.RemoteEducationalDataSource
import com.example.data.local.db.mapper.DatabaseMappers.toSyllabusNode
import com.example.domain.model.AppFoundationInfo
import com.example.domain.model.AppState
import com.example.domain.model.ContentSyncState
import com.example.domain.model.EducationalModule
import com.example.domain.model.Exam
import com.example.domain.model.LocalPreference
import com.example.domain.model.Paper
import com.example.domain.model.Question
import com.example.domain.model.QuestionOption
import com.example.domain.model.Subject
import com.example.domain.model.Subtopic
import com.example.domain.model.SubtopicWithDetails
import com.example.domain.model.SyllabusBreadcrumb
import com.example.domain.model.SyllabusMetadata
import com.example.domain.model.SyllabusNode
import com.example.domain.model.SyllabusNodeType
import com.example.domain.model.SyllabusTreeNode
import com.example.domain.model.Topic
import com.example.domain.model.practice.PracticeAttempt
import com.example.domain.repository.EducationalRepository
import com.example.domain.validation.QuestionValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Clean offline-first repository implementation.
 * Connects the Domain layer to the Local Data Source and maintains architecture readiness
 * for future Remote Network synchronization.
 *
 * Enforces:
 * - Thread-safety by executing non-Flow database operations on `Dispatchers.IO`.
 * - Robust error handling using `Result<T>` and Flow exception catching to prevent crashes.
 * - Framework isolation: no Android or Room types exposed to consumers.
 */
class EducationalRepositoryImpl(
    private val localDataSource: LocalEducationalDataSource = DefaultLocalEducationalDataSource(),
    private val remoteDataSource: RemoteEducationalDataSource? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : EducationalRepository {

    override fun getPlannedModules(): Flow<List<EducationalModule>> {
        return localDataSource.getModules()
    }

    override fun getFoundationInfo(): Flow<AppFoundationInfo> {
        return localDataSource.getFoundationSpecification()
    }

    override fun getOfflineSyncStatus(): Flow<String> {
        return localDataSource.getOfflineStatus()
    }

    // --- Local Data Foundation Operations ---

    override fun observeAppState(): Flow<AppState> {
        return localDataSource.observeAppState()
            .catch { emit(AppState()) }
    }

    override suspend fun getAppState(): AppState? = withContext(ioDispatcher) {
        runCatching { localDataSource.getAppState() }.getOrNull()
    }

    override suspend fun saveAppState(appState: AppState): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            localDataSource.saveAppState(appState)
        }
    }

    override fun observePreference(key: String): Flow<LocalPreference?> {
        return localDataSource.observePreference(key)
            .catch { emit(null) }
    }

    override suspend fun getPreferenceValue(key: String): String? = withContext(ioDispatcher) {
        runCatching { localDataSource.getPreferenceValue(key) }.getOrNull()
    }

    override suspend fun savePreference(key: String, value: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            localDataSource.savePreference(key, value)
        }
    }

    override suspend fun deletePreference(key: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            localDataSource.deletePreference(key)
        }
    }

    override fun observeAllSyncStates(): Flow<List<ContentSyncState>> {
        return localDataSource.observeAllSyncStates()
            .catch { emit(emptyList()) }
    }

    override fun observeSyncState(contentSource: String): Flow<ContentSyncState?> {
        return localDataSource.observeSyncState(contentSource)
            .catch { emit(null) }
    }

    override suspend fun saveSyncState(syncState: ContentSyncState): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            localDataSource.saveSyncState(syncState)
        }
    }

    // --- Educational Hierarchy: Exam operations ---

    override fun observeAllExams(): Flow<List<Exam>> {
        return localDataSource.observeAllExams()
            .catch { emit(emptyList()) }
    }

    override fun observeActiveExams(): Flow<List<Exam>> {
        return localDataSource.observeActiveExams()
            .catch { emit(emptyList()) }
    }

    override fun observeExamById(id: String): Flow<Exam?> {
        return localDataSource.observeExamById(id)
            .catch { emit(null) }
    }

    override suspend fun getExamById(id: String): Exam? = withContext(ioDispatcher) {
        runCatching { localDataSource.getExamById(id) }.getOrNull()
    }

    override suspend fun saveExam(exam: Exam): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.saveExam(exam) }
    }

    override suspend fun saveExams(exams: List<Exam>): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.saveExams(exams) }
    }

    override suspend fun deleteExamById(id: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.deleteExamById(id) }
    }

    // --- Educational Hierarchy: Paper operations ---

    override fun observePapersByExamId(examId: String): Flow<List<Paper>> {
        return localDataSource.observePapersByExamId(examId)
            .catch { emit(emptyList()) }
    }

    override fun observeActivePapersByExamId(examId: String): Flow<List<Paper>> {
        return localDataSource.observeActivePapersByExamId(examId)
            .catch { emit(emptyList()) }
    }

    override fun observePaperById(id: String): Flow<Paper?> {
        return localDataSource.observePaperById(id)
            .catch { emit(null) }
    }

    override suspend fun getPaperById(id: String): Paper? = withContext(ioDispatcher) {
        runCatching { localDataSource.getPaperById(id) }.getOrNull()
    }

    override suspend fun savePaper(paper: Paper): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.savePaper(paper) }
    }

    override suspend fun savePapers(papers: List<Paper>): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.savePapers(papers) }
    }

    override suspend fun deletePaperById(id: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.deletePaperById(id) }
    }

    // --- Educational Hierarchy: Subject operations ---

    override fun observeSubjectsByPaperId(paperId: String): Flow<List<Subject>> {
        return localDataSource.observeSubjectsByPaperId(paperId)
            .catch { emit(emptyList()) }
    }

    override fun observeActiveSubjectsByPaperId(paperId: String): Flow<List<Subject>> {
        return localDataSource.observeActiveSubjectsByPaperId(paperId)
            .catch { emit(emptyList()) }
    }

    override fun observeSubjectById(id: String): Flow<Subject?> {
        return localDataSource.observeSubjectById(id)
            .catch { emit(null) }
    }

    override suspend fun getSubjectById(id: String): Subject? = withContext(ioDispatcher) {
        runCatching { localDataSource.getSubjectById(id) }.getOrNull()
    }

    override suspend fun saveSubject(subject: Subject): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.saveSubject(subject) }
    }

    override suspend fun saveSubjects(subjects: List<Subject>): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.saveSubjects(subjects) }
    }

    override suspend fun deleteSubjectById(id: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.deleteSubjectById(id) }
    }

    // --- Educational Hierarchy: Topic operations ---

    override fun observeTopicsBySubjectId(subjectId: String): Flow<List<Topic>> {
        return localDataSource.observeTopicsBySubjectId(subjectId)
            .catch { emit(emptyList()) }
    }

    override fun observeActiveTopicsBySubjectId(subjectId: String): Flow<List<Topic>> {
        return localDataSource.observeActiveTopicsBySubjectId(subjectId)
            .catch { emit(emptyList()) }
    }

    override fun observeTopicById(id: String): Flow<Topic?> {
        return localDataSource.observeTopicById(id)
            .catch { emit(null) }
    }

    override suspend fun getTopicById(id: String): Topic? = withContext(ioDispatcher) {
        runCatching { localDataSource.getTopicById(id) }.getOrNull()
    }

    override suspend fun saveTopic(topic: Topic): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.saveTopic(topic) }
    }

    override suspend fun saveTopics(topics: List<Topic>): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.saveTopics(topics) }
    }

    override suspend fun deleteTopicById(id: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.deleteTopicById(id) }
    }

    // --- Educational Hierarchy: Subtopic operations ---

    override fun observeSubtopicsByTopicId(topicId: String): Flow<List<Subtopic>> {
        return localDataSource.observeSubtopicsByTopicId(topicId)
            .catch { emit(emptyList()) }
    }

    override fun observeActiveSubtopicsByTopicId(topicId: String): Flow<List<Subtopic>> {
        return localDataSource.observeActiveSubtopicsByTopicId(topicId)
            .catch { emit(emptyList()) }
    }

    override fun observeSubtopicById(id: String): Flow<Subtopic?> {
        return localDataSource.observeSubtopicById(id)
            .catch { emit(null) }
    }

    override suspend fun getSubtopicById(id: String): Subtopic? = withContext(ioDispatcher) {
        runCatching { localDataSource.getSubtopicById(id) }.getOrNull()
    }

    override suspend fun saveSubtopic(subtopic: Subtopic): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.saveSubtopic(subtopic) }
    }

    override suspend fun saveSubtopics(subtopics: List<Subtopic>): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.saveSubtopics(subtopics) }
    }

    override suspend fun deleteSubtopicById(id: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.deleteSubtopicById(id) }
    }

    // --- Syllabus Foundation Operations ---

    override fun observeChildrenOfNode(
        parentId: String?,
        parentType: SyllabusNodeType?,
        activeOnly: Boolean
    ): Flow<List<SyllabusNode>> {
        return when {
            // Root request (Exams)
            parentId == null || parentType == null -> {
                val examsFlow = if (activeOnly) localDataSource.observeActiveExams() else localDataSource.observeAllExams()
                examsFlow.map { exams ->
                    exams.sortedWith(compareBy({ it.sortOrder }, { it.name }))
                        .map { it.toSyllabusNode() }
                }
            }
            parentType == SyllabusNodeType.EXAM -> {
                val papersFlow = if (activeOnly) localDataSource.observeActivePapersByExamId(parentId) else localDataSource.observePapersByExamId(parentId)
                papersFlow.map { papers ->
                    papers.sortedWith(compareBy({ it.sortOrder }, { it.name }))
                        .map { it.toSyllabusNode() }
                }
            }
            parentType == SyllabusNodeType.PAPER -> {
                val subjectsFlow = if (activeOnly) localDataSource.observeActiveSubjectsByPaperId(parentId) else localDataSource.observeSubjectsByPaperId(parentId)
                subjectsFlow.map { subjects ->
                    subjects.sortedWith(compareBy({ it.sortOrder }, { it.name }))
                        .map { it.toSyllabusNode() }
                }
            }
            parentType == SyllabusNodeType.SUBJECT -> {
                val topicsFlow = if (activeOnly) localDataSource.observeActiveTopicsBySubjectId(parentId) else localDataSource.observeTopicsBySubjectId(parentId)
                topicsFlow.map { topics ->
                    topics.sortedWith(compareBy({ it.sortOrder }, { it.name }))
                        .map { it.toSyllabusNode() }
                }
            }
            parentType == SyllabusNodeType.TOPIC -> {
                val subtopicsFlow = if (activeOnly) localDataSource.observeActiveSubtopicsByTopicId(parentId) else localDataSource.observeSubtopicsByTopicId(parentId)
                subtopicsFlow.map { subtopics ->
                    subtopics.sortedWith(compareBy({ it.sortOrder }, { it.name }))
                        .map { it.toSyllabusNode() }
                }
            }
            parentType == SyllabusNodeType.SUBTOPIC -> {
                flowOf(emptyList())
            }
            else -> {
                flowOf(emptyList())
            }
        }.catch { emit(emptyList()) }
    }

    override fun observeSyllabusNode(
        id: String,
        nodeType: SyllabusNodeType
    ): Flow<SyllabusNode?> {
        val metadataFlow = localDataSource.observeSyllabusMetadata(id)
        val rawNodeFlow: Flow<SyllabusNode?> = when (nodeType) {
            SyllabusNodeType.EXAM -> localDataSource.observeExamById(id).map { it?.toSyllabusNode() }
            SyllabusNodeType.PAPER -> localDataSource.observePaperById(id).map { it?.toSyllabusNode() }
            SyllabusNodeType.SUBJECT -> localDataSource.observeSubjectById(id).map { it?.toSyllabusNode() }
            SyllabusNodeType.TOPIC -> localDataSource.observeTopicById(id).map { it?.toSyllabusNode() }
            SyllabusNodeType.SUBTOPIC -> localDataSource.observeSubtopicById(id).map { it?.toSyllabusNode() }
        }

        return combine(rawNodeFlow, metadataFlow) { node, meta ->
            node?.copy(
                learningObjective = meta?.learningObjective.orEmpty(),
                shortNote = meta?.shortNote.orEmpty()
            )
        }.catch { emit(null) }
    }

    override suspend fun getSyllabusNode(
        id: String,
        nodeType: SyllabusNodeType
    ): SyllabusNode? = withContext(ioDispatcher) {
        val metadata = runCatching { localDataSource.getSyllabusMetadata(id) }.getOrNull()
        when (nodeType) {
            SyllabusNodeType.EXAM -> localDataSource.getExamById(id)?.toSyllabusNode(metadata)
            SyllabusNodeType.PAPER -> localDataSource.getPaperById(id)?.toSyllabusNode(metadata)
            SyllabusNodeType.SUBJECT -> localDataSource.getSubjectById(id)?.toSyllabusNode(metadata)
            SyllabusNodeType.TOPIC -> localDataSource.getTopicById(id)?.toSyllabusNode(metadata)
            SyllabusNodeType.SUBTOPIC -> localDataSource.getSubtopicById(id)?.toSyllabusNode(metadata)
        }
    }

    override suspend fun getSyllabusBreadcrumb(
        id: String,
        nodeType: SyllabusNodeType
    ): SyllabusBreadcrumb = withContext(ioDispatcher) {
        when (nodeType) {
            SyllabusNodeType.EXAM -> {
                val exam = getSyllabusNode(id, SyllabusNodeType.EXAM)
                SyllabusBreadcrumb(exam = exam)
            }
            SyllabusNodeType.PAPER -> {
                val paper = getSyllabusNode(id, SyllabusNodeType.PAPER)
                val exam = paper?.parentId?.let { getSyllabusNode(it, SyllabusNodeType.EXAM) }
                SyllabusBreadcrumb(exam = exam, paper = paper)
            }
            SyllabusNodeType.SUBJECT -> {
                val subject = getSyllabusNode(id, SyllabusNodeType.SUBJECT)
                val paper = subject?.parentId?.let { getSyllabusNode(it, SyllabusNodeType.PAPER) }
                val exam = paper?.parentId?.let { getSyllabusNode(it, SyllabusNodeType.EXAM) }
                SyllabusBreadcrumb(exam = exam, paper = paper, subject = subject)
            }
            SyllabusNodeType.TOPIC -> {
                val topic = getSyllabusNode(id, SyllabusNodeType.TOPIC)
                val subject = topic?.parentId?.let { getSyllabusNode(it, SyllabusNodeType.SUBJECT) }
                val paper = subject?.parentId?.let { getSyllabusNode(it, SyllabusNodeType.PAPER) }
                val exam = paper?.parentId?.let { getSyllabusNode(it, SyllabusNodeType.EXAM) }
                SyllabusBreadcrumb(exam = exam, paper = paper, subject = subject, topic = topic)
            }
            SyllabusNodeType.SUBTOPIC -> {
                val subtopic = getSyllabusNode(id, SyllabusNodeType.SUBTOPIC)
                val topic = subtopic?.parentId?.let { getSyllabusNode(it, SyllabusNodeType.TOPIC) }
                val subject = topic?.parentId?.let { getSyllabusNode(it, SyllabusNodeType.SUBJECT) }
                val paper = subject?.parentId?.let { getSyllabusNode(it, SyllabusNodeType.PAPER) }
                val exam = paper?.parentId?.let { getSyllabusNode(it, SyllabusNodeType.EXAM) }
                SyllabusBreadcrumb(exam = exam, paper = paper, subject = subject, topic = topic, subtopic = subtopic)
            }
        }
    }

    override suspend fun getSyllabusTreeForExam(
        examId: String,
        activeOnly: Boolean
    ): SyllabusTreeNode? = withContext(ioDispatcher) {
        val rootExam = getSyllabusNode(examId, SyllabusNodeType.EXAM) ?: return@withContext null

        val papersFlow = if (activeOnly) {
            localDataSource.observeActivePapersByExamId(examId)
        } else {
            localDataSource.observePapersByExamId(examId)
        }
        val papers = papersFlow.first().sortedWith(compareBy({ it.sortOrder }, { it.name }))

        val paperTreeNodes = papers.map { paper ->
            val subjectsFlow = if (activeOnly) {
                localDataSource.observeActiveSubjectsByPaperId(paper.id)
            } else {
                localDataSource.observeSubjectsByPaperId(paper.id)
            }
            val subjects = subjectsFlow.first().sortedWith(compareBy({ it.sortOrder }, { it.name }))

            val subjectTreeNodes = subjects.map { subject ->
                val topicsFlow = if (activeOnly) {
                    localDataSource.observeActiveTopicsBySubjectId(subject.id)
                } else {
                    localDataSource.observeTopicsBySubjectId(subject.id)
                }
                val topics = topicsFlow.first().sortedWith(compareBy({ it.sortOrder }, { it.name }))

                val topicTreeNodes = topics.map { topic ->
                    val subtopicsFlow = if (activeOnly) {
                        localDataSource.observeActiveSubtopicsByTopicId(topic.id)
                    } else {
                        localDataSource.observeSubtopicsByTopicId(topic.id)
                    }
                    val subtopics = subtopicsFlow.first().sortedWith(compareBy({ it.sortOrder }, { it.name }))

                    val subtopicTreeNodes = subtopics.map { subtopic ->
                        SyllabusTreeNode(node = subtopic.toSyllabusNode())
                    }
                    SyllabusTreeNode(node = topic.toSyllabusNode(), children = subtopicTreeNodes)
                }
                SyllabusTreeNode(node = subject.toSyllabusNode(), children = topicTreeNodes)
            }
            SyllabusTreeNode(node = paper.toSyllabusNode(), children = subjectTreeNodes)
        }

        SyllabusTreeNode(node = rootExam, children = paperTreeNodes)
    }

    // Syllabus Metadata CRUD
    override fun observeSyllabusMetadata(nodeId: String): Flow<SyllabusMetadata?> {
        return localDataSource.observeSyllabusMetadata(nodeId)
            .catch { emit(null) }
    }

    override suspend fun getSyllabusMetadata(nodeId: String): SyllabusMetadata? = withContext(ioDispatcher) {
        runCatching { localDataSource.getSyllabusMetadata(nodeId) }.getOrNull()
    }

    override suspend fun saveSyllabusMetadata(metadata: SyllabusMetadata): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.saveSyllabusMetadata(metadata) }
    }

    override suspend fun deleteSyllabusMetadata(nodeId: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.deleteSyllabusMetadata(nodeId) }
    }

    override fun observeActiveSubtopicsWithDetailsByTopicId(topicId: String): Flow<List<SubtopicWithDetails>> {
        return localDataSource.observeActiveSubtopicsByTopicId(topicId)
            .map { subtopics ->
                val sortedSubtopics = subtopics.sortedWith(compareBy({ it.sortOrder }, { it.name }))
                sortedSubtopics.map { subtopic ->
                    val metadata = localDataSource.getSyllabusMetadata(subtopic.id)
                    val count = localDataSource.getActiveQuestionCountBySubtopicId(subtopic.id)
                    SubtopicWithDetails(
                        subtopic = subtopic,
                        metadata = metadata,
                        questionCount = count
                    )
                }
            }
            .catch { emit(emptyList()) }
    }

    // --- Question Foundation Operations ---

    override fun observeQuestionsForSubtopic(subtopicId: String, activeOnly: Boolean): Flow<List<Question>> {
        return localDataSource.observeQuestionsBySubtopicId(subtopicId, activeOnly)
            .catch { emit(emptyList()) }
    }

    override fun observeQuestionById(id: String): Flow<Question?> {
        return localDataSource.observeQuestionById(id)
            .catch { emit(null) }
    }

    override fun observeQuestion(questionId: String): Flow<Question?> {
        return observeQuestionById(questionId)
    }

    override suspend fun getQuestionById(id: String): Question? = withContext(ioDispatcher) {
        runCatching { localDataSource.getQuestionById(id) }.getOrNull()
    }

    override suspend fun getQuestionCountBySubtopicId(subtopicId: String): Int = withContext(ioDispatcher) {
        runCatching { localDataSource.getQuestionCountBySubtopicId(subtopicId) }.getOrDefault(0)
    }

    override suspend fun getActiveQuestionCountBySubtopicId(subtopicId: String): Int = withContext(ioDispatcher) {
        runCatching { localDataSource.getActiveQuestionCountBySubtopicId(subtopicId) }.getOrDefault(0)
    }

    override suspend fun saveQuestion(question: Question): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val validation = QuestionValidator.validateQuestion(question)
            require(validation.isValid) {
                "Question validation failed: ${validation.errors.joinToString { it.message }}"
            }
            localDataSource.saveQuestion(question)
        }
    }

    override suspend fun saveQuestions(questions: List<Question>): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val validation = QuestionValidator.validateQuestions(questions)
            require(validation.isValid) {
                "Questions validation failed: ${validation.errors.joinToString { it.message }}"
            }
            localDataSource.saveQuestions(questions)
        }
    }

    override suspend fun deleteQuestionById(id: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.deleteQuestionById(id) }
    }

    override suspend fun deleteQuestionsBySubtopicId(subtopicId: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.deleteQuestionsBySubtopicId(subtopicId) }
    }

    override fun observeOptionsForQuestion(questionId: String): Flow<List<QuestionOption>> {
        return localDataSource.observeOptionsForQuestion(questionId)
            .catch { emit(emptyList()) }
    }

    override suspend fun getOptionsForQuestion(questionId: String): List<QuestionOption> = withContext(ioDispatcher) {
        runCatching { localDataSource.getOptionsForQuestion(questionId) }.getOrDefault(emptyList())
    }

    override suspend fun saveOption(option: QuestionOption): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.saveOption(option) }
    }

    override suspend fun saveOptions(options: List<QuestionOption>): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.saveOptions(options) }
    }

    override suspend fun deleteOptionById(id: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.deleteOptionById(id) }
    }

    override suspend fun deleteOptionsForQuestion(questionId: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.deleteOptionsForQuestion(questionId) }
    }

    override fun observeAttemptsBySubtopicId(subtopicId: String): Flow<List<PracticeAttempt>> {
        return localDataSource.observeAttemptsBySubtopicId(subtopicId)
            .catch { emit(emptyList()) }
    }

    override fun observeRecentAttempts(limit: Int): Flow<List<PracticeAttempt>> {
        return localDataSource.observeRecentAttempts(limit)
            .catch { emit(emptyList()) }
    }

    override suspend fun getAttemptById(id: String): PracticeAttempt? = withContext(ioDispatcher) {
        runCatching { localDataSource.getAttemptById(id) }.getOrNull()
    }

    override suspend fun savePracticeAttempt(attempt: PracticeAttempt): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.savePracticeAttempt(attempt) }
    }

    override suspend fun deleteAttemptById(id: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching { localDataSource.deleteAttemptById(id) }
    }
}
