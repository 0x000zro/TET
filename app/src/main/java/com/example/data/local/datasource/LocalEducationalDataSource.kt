package com.example.data.local.datasource

import com.example.data.local.db.AppDatabase
import com.example.data.local.db.DatabaseProvider
import com.example.data.local.db.mapper.DatabaseMappers.toDomain
import com.example.data.local.db.mapper.DatabaseMappers.toEntity
import com.example.domain.model.AppFoundationInfo
import com.example.domain.model.AppState
import com.example.domain.model.ArchitecturalLayerInfo
import com.example.domain.model.ContentSyncState
import com.example.domain.model.EducationalModule
import com.example.domain.model.EducationalModuleId
import com.example.domain.model.Exam
import com.example.domain.model.LocalPreference
import com.example.domain.model.Paper
import com.example.domain.model.Subject
import com.example.domain.model.Subtopic
import com.example.domain.model.SyllabusMetadata
import com.example.domain.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Local offline data source interface.
 * Implements the offline-first data provision foundation and coordinates Room persistence.
 */
interface LocalEducationalDataSource {
    fun getModules(): Flow<List<EducationalModule>>
    fun getFoundationSpecification(): Flow<AppFoundationInfo>
    fun getOfflineStatus(): Flow<String>

    // Room Database Foundation operations
    fun observeAppState(): Flow<AppState>
    suspend fun getAppState(): AppState?
    suspend fun saveAppState(appState: AppState)

    fun observePreference(key: String): Flow<LocalPreference?>
    suspend fun getPreferenceValue(key: String): String?
    suspend fun savePreference(key: String, value: String)
    suspend fun deletePreference(key: String)

    fun observeAllSyncStates(): Flow<List<ContentSyncState>>
    fun observeSyncState(contentSource: String): Flow<ContentSyncState?>
    suspend fun saveSyncState(syncState: ContentSyncState)

    // Educational Hierarchy operations: Exam
    fun observeAllExams(): Flow<List<Exam>>
    fun observeActiveExams(): Flow<List<Exam>>
    fun observeExamById(id: String): Flow<Exam?>
    suspend fun getExamById(id: String): Exam?
    suspend fun saveExam(exam: Exam)
    suspend fun saveExams(exams: List<Exam>)
    suspend fun deleteExamById(id: String)

    // Educational Hierarchy operations: Paper
    fun observePapersByExamId(examId: String): Flow<List<Paper>>
    fun observeActivePapersByExamId(examId: String): Flow<List<Paper>>
    fun observePaperById(id: String): Flow<Paper?>
    suspend fun getPaperById(id: String): Paper?
    suspend fun savePaper(paper: Paper)
    suspend fun savePapers(papers: List<Paper>)
    suspend fun deletePaperById(id: String)

    // Educational Hierarchy operations: Subject
    fun observeSubjectsByPaperId(paperId: String): Flow<List<Subject>>
    fun observeActiveSubjectsByPaperId(paperId: String): Flow<List<Subject>>
    fun observeSubjectById(id: String): Flow<Subject?>
    suspend fun getSubjectById(id: String): Subject?
    suspend fun saveSubject(subject: Subject)
    suspend fun saveSubjects(subjects: List<Subject>)
    suspend fun deleteSubjectById(id: String)

    // Educational Hierarchy operations: Topic
    fun observeTopicsBySubjectId(subjectId: String): Flow<List<Topic>>
    fun observeActiveTopicsBySubjectId(subjectId: String): Flow<List<Topic>>
    fun observeTopicById(id: String): Flow<Topic?>
    suspend fun getTopicById(id: String): Topic?
    suspend fun saveTopic(topic: Topic)
    suspend fun saveTopics(topics: List<Topic>)
    suspend fun deleteTopicById(id: String)

    // Educational Hierarchy operations: Subtopic
    fun observeSubtopicsByTopicId(topicId: String): Flow<List<Subtopic>>
    fun observeActiveSubtopicsByTopicId(topicId: String): Flow<List<Subtopic>>
    fun observeSubtopicById(id: String): Flow<Subtopic?>
    suspend fun getSubtopicById(id: String): Subtopic?
    suspend fun saveSubtopic(subtopic: Subtopic)
    suspend fun saveSubtopics(subtopics: List<Subtopic>)
    suspend fun deleteSubtopicById(id: String)

    // Syllabus Metadata operations
    fun observeSyllabusMetadata(nodeId: String): Flow<SyllabusMetadata?>
    suspend fun getSyllabusMetadata(nodeId: String): SyllabusMetadata?
    suspend fun getSyllabusMetadataList(nodeIds: List<String>): List<SyllabusMetadata>
    suspend fun saveSyllabusMetadata(metadata: SyllabusMetadata)
    suspend fun deleteSyllabusMetadata(nodeId: String)
}

class DefaultLocalEducationalDataSource(
    private val databaseProvider: () -> AppDatabase? = { DatabaseProvider.getDatabaseOrNull() }
) : LocalEducationalDataSource {

    private val database: AppDatabase?
        get() = databaseProvider()

    private val plannedModules = listOf(
        EducationalModule(
            id = EducationalModuleId.EXAMS,
            title = "Target Exam Selector",
            category = "Core Framework",
            description = "Multi-exam goal orientation with customized syllabus, pattern, and practice tracks."
        ),
        EducationalModule(
            id = EducationalModuleId.SYLLABUS,
            title = "Structured Syllabus",
            category = "Learning Path",
            description = "Curriculum hierarchy from Exam -> Paper -> Subject -> Topic -> Subtopic."
        ),
        EducationalModule(
            id = EducationalModuleId.PRACTICE,
            title = "Topic Practice Engine",
            category = "Active Recall",
            description = "Interactive question solving with instant feedback, explanations, and difficulty ratings."
        ),
        EducationalModule(
            id = EducationalModuleId.MOCK_TESTS,
            title = "Exam-Style Mock Tests",
            category = "Assessment",
            description = "Timed exam simulations with real question counts, negative marking, and score breakdowns."
        ),
        EducationalModule(
            id = EducationalModuleId.PREVIOUS_YEAR_QUESTIONS,
            title = "Previous Year Papers (PYQ)",
            category = "Assessment",
            description = "Authentic past examination papers with verified solutions and year-wise filtering."
        ),
        EducationalModule(
            id = EducationalModuleId.EBOOKS,
            title = "E-Books & Revision Notes",
            category = "Reference",
            description = "Offline-cached subject compendiums, cheat-sheets, and formula reference books."
        ),
        EducationalModule(
            id = EducationalModuleId.VIDEOS,
            title = "Conceptual Video Lessons",
            category = "Multimedia",
            description = "Curated video lectures mapped directly to syllabus topics and subtopics."
        ),
        EducationalModule(
            id = EducationalModuleId.BOOKMARKS,
            title = "Saved Bookmarks",
            category = "Personalized",
            description = "Quick-access repository for high-yield questions, concepts, and formulas."
        ),
        EducationalModule(
            id = EducationalModuleId.WRONG_QUESTIONS,
            title = "Error Bank (Mistake Tracker)",
            category = "Personalized",
            description = "Automated compilation of practice and mock test mistakes for targeted revision."
        ),
        EducationalModule(
            id = EducationalModuleId.PERFORMANCE,
            title = "Analytics & Readiness",
            category = "Analytics",
            description = "Accuracy percentages, topic strength meters, and speed metrics."
        ),
        EducationalModule(
            id = EducationalModuleId.SETTINGS,
            title = "Preferences & Offline Cache",
            category = "System",
            description = "Local storage manager, dark/light theme switcher, and study notifications."
        )
    )

    private val foundationInfo = AppFoundationInfo(
        appName = "EduPrep",
        version = "2.0.0-hierarchy",
        targetSdk = 36,
        minSdk = 24,
        architecturePattern = "Clean Architecture + MVVM + Offline-First",
        offlineFirstReady = true,
        networkContractReady = true,
        layers = listOf(
            ArchitecturalLayerInfo(
                layerName = "Domain Layer",
                description = "Pure Kotlin business models (Exam, Paper, Subject, Topic, Subtopic) and repository contracts. No Android/Room imports.",
                status = "Active & Enforced"
            ),
            ArchitecturalLayerInfo(
                layerName = "Data Layer",
                description = "Room entities, DAOs, Database migrations (v1->v2), and offline-first repository implementations.",
                status = "Active & Enforced"
            ),
            ArchitecturalLayerInfo(
                layerName = "UI Layer",
                description = "Jetpack Compose screens, MVVM ViewModels, M3 components, and accessible navigation shell.",
                status = "Active & Enforced"
            )
        )
    )

    override fun getModules(): Flow<List<EducationalModule>> = flowOf(plannedModules)

    override fun getFoundationSpecification(): Flow<AppFoundationInfo> = flowOf(foundationInfo)

    override fun getOfflineStatus(): Flow<String> = flowOf("Local Architecture Initialized")

    // --- Local Data Foundation (Room) ---

    override fun observeAppState(): Flow<AppState> {
        val dao = database?.appStateDao() ?: return flowOf(AppState())
        return dao.getAppStateFlow().map { entity ->
            entity?.toDomain() ?: AppState()
        }
    }

    override suspend fun getAppState(): AppState? {
        return database?.appStateDao()?.getAppState()?.toDomain()
    }

    override suspend fun saveAppState(appState: AppState) {
        database?.appStateDao()?.insertOrUpdateAppState(appState.toEntity())
    }

    override fun observePreference(key: String): Flow<LocalPreference?> {
        val dao = database?.localPreferenceDao() ?: return flowOf(null)
        return dao.getPreferenceFlow(key).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun getPreferenceValue(key: String): String? {
        return database?.localPreferenceDao()?.getPreferenceValue(key)
    }

    override suspend fun savePreference(key: String, value: String) {
        val preference = LocalPreference(key = key, value = value)
        database?.localPreferenceDao()?.insertOrUpdatePreference(preference.toEntity())
    }

    override suspend fun deletePreference(key: String) {
        database?.localPreferenceDao()?.deletePreference(key)
    }

    override fun observeAllSyncStates(): Flow<List<ContentSyncState>> {
        val dao = database?.contentSyncStateDao() ?: return flowOf(emptyList())
        return dao.getAllSyncStatesFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeSyncState(contentSource: String): Flow<ContentSyncState?> {
        val dao = database?.contentSyncStateDao() ?: return flowOf(null)
        return dao.getSyncStateFlow(contentSource).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun saveSyncState(syncState: ContentSyncState) {
        database?.contentSyncStateDao()?.insertOrUpdateSyncState(syncState.toEntity())
    }

    // --- Educational Hierarchy: Exam ---

    override fun observeAllExams(): Flow<List<Exam>> {
        val dao = database?.examDao() ?: return flowOf(emptyList())
        return dao.getAllExamsFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun observeActiveExams(): Flow<List<Exam>> {
        val dao = database?.examDao() ?: return flowOf(emptyList())
        return dao.getActiveExamsFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun observeExamById(id: String): Flow<Exam?> {
        val dao = database?.examDao() ?: return flowOf(null)
        return dao.getExamByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun getExamById(id: String): Exam? {
        return database?.examDao()?.getExamById(id)?.toDomain()
    }

    override suspend fun saveExam(exam: Exam) {
        database?.examDao()?.insertOrUpdateExam(exam.toEntity())
    }

    override suspend fun saveExams(exams: List<Exam>) {
        database?.examDao()?.insertOrUpdateExams(exams.map { it.toEntity() })
    }

    override suspend fun deleteExamById(id: String) {
        database?.examDao()?.deleteExamById(id)
    }

    // --- Educational Hierarchy: Paper ---

    override fun observePapersByExamId(examId: String): Flow<List<Paper>> {
        val dao = database?.paperDao() ?: return flowOf(emptyList())
        return dao.getPapersByExamIdFlow(examId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeActivePapersByExamId(examId: String): Flow<List<Paper>> {
        val dao = database?.paperDao() ?: return flowOf(emptyList())
        return dao.getActivePapersByExamIdFlow(examId).map { list -> list.map { it.toDomain() } }
    }

    override fun observePaperById(id: String): Flow<Paper?> {
        val dao = database?.paperDao() ?: return flowOf(null)
        return dao.getPaperByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun getPaperById(id: String): Paper? {
        return database?.paperDao()?.getPaperById(id)?.toDomain()
    }

    override suspend fun savePaper(paper: Paper) {
        database?.paperDao()?.insertOrUpdatePaper(paper.toEntity())
    }

    override suspend fun savePapers(papers: List<Paper>) {
        database?.paperDao()?.insertOrUpdatePapers(papers.map { it.toEntity() })
    }

    override suspend fun deletePaperById(id: String) {
        database?.paperDao()?.deletePaperById(id)
    }

    // --- Educational Hierarchy: Subject ---

    override fun observeSubjectsByPaperId(paperId: String): Flow<List<Subject>> {
        val dao = database?.subjectDao() ?: return flowOf(emptyList())
        return dao.getSubjectsByPaperIdFlow(paperId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeActiveSubjectsByPaperId(paperId: String): Flow<List<Subject>> {
        val dao = database?.subjectDao() ?: return flowOf(emptyList())
        return dao.getActiveSubjectsByPaperIdFlow(paperId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeSubjectById(id: String): Flow<Subject?> {
        val dao = database?.subjectDao() ?: return flowOf(null)
        return dao.getSubjectByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun getSubjectById(id: String): Subject? {
        return database?.subjectDao()?.getSubjectById(id)?.toDomain()
    }

    override suspend fun saveSubject(subject: Subject) {
        database?.subjectDao()?.insertOrUpdateSubject(subject.toEntity())
    }

    override suspend fun saveSubjects(subjects: List<Subject>) {
        database?.subjectDao()?.insertOrUpdateSubjects(subjects.map { it.toEntity() })
    }

    override suspend fun deleteSubjectById(id: String) {
        database?.subjectDao()?.deleteSubjectById(id)
    }

    // --- Educational Hierarchy: Topic ---

    override fun observeTopicsBySubjectId(subjectId: String): Flow<List<Topic>> {
        val dao = database?.topicDao() ?: return flowOf(emptyList())
        return dao.getTopicsBySubjectIdFlow(subjectId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeActiveTopicsBySubjectId(subjectId: String): Flow<List<Topic>> {
        val dao = database?.topicDao() ?: return flowOf(emptyList())
        return dao.getActiveTopicsBySubjectIdFlow(subjectId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeTopicById(id: String): Flow<Topic?> {
        val dao = database?.topicDao() ?: return flowOf(null)
        return dao.getTopicByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun getTopicById(id: String): Topic? {
        return database?.topicDao()?.getTopicById(id)?.toDomain()
    }

    override suspend fun saveTopic(topic: Topic) {
        database?.topicDao()?.insertOrUpdateTopic(topic.toEntity())
    }

    override suspend fun saveTopics(topics: List<Topic>) {
        database?.topicDao()?.insertOrUpdateTopics(topics.map { it.toEntity() })
    }

    override suspend fun deleteTopicById(id: String) {
        database?.topicDao()?.deleteTopicById(id)
    }

    // --- Educational Hierarchy: Subtopic ---

    override fun observeSubtopicsByTopicId(topicId: String): Flow<List<Subtopic>> {
        val dao = database?.subtopicDao() ?: return flowOf(emptyList())
        return dao.getSubtopicsByTopicIdFlow(topicId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeActiveSubtopicsByTopicId(topicId: String): Flow<List<Subtopic>> {
        val dao = database?.subtopicDao() ?: return flowOf(emptyList())
        return dao.getActiveSubtopicsByTopicIdFlow(topicId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeSubtopicById(id: String): Flow<Subtopic?> {
        val dao = database?.subtopicDao() ?: return flowOf(null)
        return dao.getSubtopicByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun getSubtopicById(id: String): Subtopic? {
        return database?.subtopicDao()?.getSubtopicById(id)?.toDomain()
    }

    override suspend fun saveSubtopic(subtopic: Subtopic) {
        database?.subtopicDao()?.insertOrUpdateSubtopic(subtopic.toEntity())
    }

    override suspend fun saveSubtopics(subtopics: List<Subtopic>) {
        database?.subtopicDao()?.insertOrUpdateSubtopics(subtopics.map { it.toEntity() })
    }

    override suspend fun deleteSubtopicById(id: String) {
        database?.subtopicDao()?.deleteSubtopicById(id)
    }

    // --- Syllabus Metadata Operations ---

    override fun observeSyllabusMetadata(nodeId: String): Flow<SyllabusMetadata?> {
        val dao = database?.syllabusMetadataDao() ?: return flowOf(null)
        return dao.getMetadataByNodeIdFlow(nodeId).map { it?.toDomain() }
    }

    override suspend fun getSyllabusMetadata(nodeId: String): SyllabusMetadata? {
        return database?.syllabusMetadataDao()?.getMetadataByNodeId(nodeId)?.toDomain()
    }

    override suspend fun getSyllabusMetadataList(nodeIds: List<String>): List<SyllabusMetadata> {
        if (nodeIds.isEmpty()) return emptyList()
        return database?.syllabusMetadataDao()?.getMetadataByNodeIds(nodeIds)?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun saveSyllabusMetadata(metadata: SyllabusMetadata) {
        database?.syllabusMetadataDao()?.insertOrUpdateMetadata(metadata.toEntity())
    }

    override suspend fun deleteSyllabusMetadata(nodeId: String) {
        database?.syllabusMetadataDao()?.deleteMetadataByNodeId(nodeId)
    }
}
