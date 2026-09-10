package com.example.testutil

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

open class TestEducationalRepository : EducationalRepository {
    val questionsMap = mutableMapOf<String, Question>()
    var shouldThrowOnQuestion = false
    val practiceAttempts = mutableListOf<PracticeAttempt>()
    var shouldThrowOnAttempt = false

    override fun getFoundationInfo(): Flow<AppFoundationInfo> = flow {
        emit(AppFoundationInfo("Test", "1.0", 35, 26, "Clean", true, true, emptyList()))
    }
    override fun getOfflineSyncStatus(): Flow<String> = flow { emit("idle") }
    override fun getPlannedModules(): Flow<List<EducationalModule>> = flow { emit(emptyList()) }
    override fun observeAppState(): Flow<AppState> = flow { emit(AppState()) }
    override suspend fun getAppState(): AppState? = null
    override suspend fun saveAppState(appState: AppState): Result<Unit> = Result.success(Unit)
    override fun observePreference(key: String): Flow<LocalPreference?> = flow { emit(null) }
    override suspend fun getPreferenceValue(key: String): String? = null
    override suspend fun savePreference(key: String, value: String): Result<Unit> = Result.success(Unit)
    override suspend fun deletePreference(key: String): Result<Unit> = Result.success(Unit)
    override fun observeAllSyncStates(): Flow<List<ContentSyncState>> = flow { emit(emptyList()) }
    override fun observeSyncState(contentSource: String): Flow<ContentSyncState?> = flow { emit(null) }
    override suspend fun saveSyncState(syncState: ContentSyncState): Result<Unit> = Result.success(Unit)
    override fun observeAllExams(): Flow<List<Exam>> = flow { emit(emptyList()) }
    override fun observeActiveExams(): Flow<List<Exam>> = flow { emit(emptyList()) }
    override fun observeExamById(id: String): Flow<Exam?> = flow { emit(null) }
    override suspend fun getExamById(id: String): Exam? = null
    override suspend fun saveExam(exam: Exam): Result<Unit> = Result.success(Unit)
    override suspend fun saveExams(exams: List<Exam>): Result<Unit> = Result.success(Unit)
    override suspend fun deleteExamById(id: String): Result<Unit> = Result.success(Unit)
    override fun observePapersByExamId(examId: String): Flow<List<Paper>> = flow { emit(emptyList()) }
    override fun observeActivePapersByExamId(examId: String): Flow<List<Paper>> = flow { emit(emptyList()) }
    override fun observePaperById(id: String): Flow<Paper?> = flow { emit(null) }
    override suspend fun getPaperById(id: String): Paper? = null
    override suspend fun savePaper(paper: Paper): Result<Unit> = Result.success(Unit)
    override suspend fun savePapers(papers: List<Paper>): Result<Unit> = Result.success(Unit)
    override suspend fun deletePaperById(id: String): Result<Unit> = Result.success(Unit)
    override fun observeSubjectsByPaperId(paperId: String): Flow<List<Subject>> = flow { emit(emptyList()) }
    override fun observeActiveSubjectsByPaperId(paperId: String): Flow<List<Subject>> = flow { emit(emptyList()) }
    override fun observeSubjectById(id: String): Flow<Subject?> = flow { emit(null) }
    override suspend fun getSubjectById(id: String): Subject? = null
    override suspend fun saveSubject(subject: Subject): Result<Unit> = Result.success(Unit)
    override suspend fun saveSubjects(subjects: List<Subject>): Result<Unit> = Result.success(Unit)
    override suspend fun deleteSubjectById(id: String): Result<Unit> = Result.success(Unit)
    override fun observeTopicsBySubjectId(subjectId: String): Flow<List<Topic>> = flow { emit(emptyList()) }
    override fun observeActiveTopicsBySubjectId(subjectId: String): Flow<List<Topic>> = flow { emit(emptyList()) }
    override fun observeTopicById(id: String): Flow<Topic?> = flow { emit(null) }
    override suspend fun getTopicById(id: String): Topic? = null
    override suspend fun saveTopic(topic: Topic): Result<Unit> = Result.success(Unit)
    override suspend fun saveTopics(topics: List<Topic>): Result<Unit> = Result.success(Unit)
    override suspend fun deleteTopicById(id: String): Result<Unit> = Result.success(Unit)
    override fun observeSubtopicsByTopicId(topicId: String): Flow<List<Subtopic>> = flow { emit(emptyList()) }
    override fun observeActiveSubtopicsByTopicId(topicId: String): Flow<List<Subtopic>> = flow { emit(emptyList()) }
    override fun observeSubtopicById(id: String): Flow<Subtopic?> = flow { emit(null) }
    override suspend fun getSubtopicById(id: String): Subtopic? = null
    override suspend fun saveSubtopic(subtopic: Subtopic): Result<Unit> = Result.success(Unit)
    override suspend fun saveSubtopics(subtopics: List<Subtopic>): Result<Unit> = Result.success(Unit)
    override suspend fun deleteSubtopicById(id: String): Result<Unit> = Result.success(Unit)
    override fun observeChildrenOfNode(parentId: String?, parentType: SyllabusNodeType?, activeOnly: Boolean): Flow<List<SyllabusNode>> = flow { emit(emptyList()) }
    override fun observeSyllabusNode(id: String, nodeType: SyllabusNodeType): Flow<SyllabusNode?> = flow { emit(null) }
    override suspend fun getSyllabusNode(id: String, nodeType: SyllabusNodeType): SyllabusNode? = null
    override suspend fun getSyllabusBreadcrumb(id: String, nodeType: SyllabusNodeType): SyllabusBreadcrumb = SyllabusBreadcrumb()
    override suspend fun getSyllabusTreeForExam(examId: String, activeOnly: Boolean): SyllabusTreeNode? = null
    override fun observeSyllabusMetadata(nodeId: String): Flow<SyllabusMetadata?> = flow { emit(null) }
    override suspend fun getSyllabusMetadata(nodeId: String): SyllabusMetadata? = null
    override suspend fun saveSyllabusMetadata(metadata: SyllabusMetadata): Result<Unit> = Result.success(Unit)
    override suspend fun deleteSyllabusMetadata(nodeId: String): Result<Unit> = Result.success(Unit)
    override fun observeActiveSubtopicsWithDetailsByTopicId(topicId: String): Flow<List<SubtopicWithDetails>> = flow { emit(emptyList()) }

    override fun observeQuestionsForSubtopic(subtopicId: String, activeOnly: Boolean): Flow<List<Question>> = flow {
        if (shouldThrowOnQuestion) throw RuntimeException("Simulated database failure")
        val filtered = questionsMap.values.filter { it.subtopicId == subtopicId && (!activeOnly || it.isActive) }
        emit(filtered)
    }

    override fun observeQuestionById(id: String): Flow<Question?> = flow {
        if (shouldThrowOnQuestion) throw RuntimeException("Simulated database failure")
        emit(questionsMap[id])
    }

    override fun observeQuestion(questionId: String): Flow<Question?> = observeQuestionById(questionId)

    override suspend fun getQuestionById(id: String): Question? {
        if (shouldThrowOnQuestion) throw RuntimeException("Simulated database failure")
        return questionsMap[id]
    }

    override suspend fun getQuestionCountBySubtopicId(subtopicId: String): Int =
        questionsMap.values.count { it.subtopicId == subtopicId }

    override suspend fun getActiveQuestionCountBySubtopicId(subtopicId: String): Int =
        questionsMap.values.count { it.subtopicId == subtopicId && it.isActive }

    override suspend fun saveQuestion(question: Question): Result<Unit> {
        questionsMap[question.id] = question
        return Result.success(Unit)
    }

    override suspend fun saveQuestions(questions: List<Question>): Result<Unit> {
        questions.forEach { questionsMap[it.id] = it }
        return Result.success(Unit)
    }

    override suspend fun deleteQuestionById(id: String): Result<Unit> {
        questionsMap.remove(id)
        return Result.success(Unit)
    }

    override suspend fun deleteQuestionsBySubtopicId(subtopicId: String): Result<Unit> {
        questionsMap.entries.removeIf { it.value.subtopicId == subtopicId }
        return Result.success(Unit)
    }

    override fun observeOptionsForQuestion(questionId: String): Flow<List<QuestionOption>> = flow {
        emit(questionsMap[questionId]?.options ?: emptyList())
    }

    override suspend fun getOptionsForQuestion(questionId: String): List<QuestionOption> =
        questionsMap[questionId]?.options ?: emptyList()

    override suspend fun saveOption(option: QuestionOption): Result<Unit> = Result.success(Unit)
    override suspend fun saveOptions(options: List<QuestionOption>): Result<Unit> = Result.success(Unit)
    override suspend fun deleteOptionById(id: String): Result<Unit> = Result.success(Unit)
    override suspend fun deleteOptionsForQuestion(questionId: String): Result<Unit> = Result.success(Unit)

    override fun observeAttemptsBySubtopicId(subtopicId: String): Flow<List<PracticeAttempt>> = flow {
        emit(practiceAttempts.filter { it.subtopicId == subtopicId }.sortedWith(compareByDescending<PracticeAttempt> { it.completedAt }.thenByDescending { it.id }))
    }

    override fun observeRecentAttempts(limit: Int): Flow<List<PracticeAttempt>> = flow {
        if (shouldThrowOnAttempt) throw RuntimeException("Simulated attempt database failure")
        emit(practiceAttempts.sortedWith(compareByDescending<PracticeAttempt> { it.completedAt }.thenByDescending { it.id }).take(limit))
    }

    override fun observeAllPracticeAttempts(): Flow<List<PracticeAttempt>> = flow {
        if (shouldThrowOnAttempt) throw RuntimeException("Simulated attempt database failure")
        emit(practiceAttempts.sortedWith(compareByDescending<PracticeAttempt> { it.completedAt }.thenByDescending { it.id }))
    }

    override suspend fun getAllPracticeAttempts(): List<PracticeAttempt> {
        if (shouldThrowOnAttempt) throw RuntimeException("Simulated attempt database failure")
        return practiceAttempts.sortedWith(compareByDescending<PracticeAttempt> { it.completedAt }.thenByDescending { it.id })
    }

    override suspend fun getAttemptById(id: String): PracticeAttempt? {
        if (shouldThrowOnAttempt) throw RuntimeException("Simulated attempt database failure")
        return practiceAttempts.find { it.id == id }
    }

    override suspend fun savePracticeAttempt(attempt: PracticeAttempt): Result<Unit> {
        if (shouldThrowOnAttempt) return Result.failure(RuntimeException("Simulated attempt database failure"))
        val existingIndex = practiceAttempts.indexOfFirst { it.id == attempt.id }
        if (existingIndex >= 0) {
            practiceAttempts[existingIndex] = attempt
        } else {
            practiceAttempts.add(attempt)
        }
        return Result.success(Unit)
    }

    override suspend fun deleteAttemptById(id: String): Result<Unit> {
        practiceAttempts.removeIf { it.id == id }
        return Result.success(Unit)
    }
}
