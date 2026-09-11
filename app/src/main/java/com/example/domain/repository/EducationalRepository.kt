package com.example.domain.repository

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
import com.example.domain.model.bookmark.BookmarkedQuestion
import com.example.domain.model.practice.PracticeAttempt
import com.example.domain.model.wrongquestion.WrongQuestion
import kotlinx.coroutines.flow.Flow

/**
 * Domain boundary contract for educational application services.
 * Implemented by the data layer (offline-first repository).
 * Pure domain contract completely independent of Room and SQLite.
 */
interface EducationalRepository {
    fun getPlannedModules(): Flow<List<EducationalModule>>
    fun getFoundationInfo(): Flow<AppFoundationInfo>
    fun getOfflineSyncStatus(): Flow<String>

    // Local Data Foundation operations
    fun observeAppState(): Flow<AppState>
    suspend fun getAppState(): AppState?
    suspend fun saveAppState(appState: AppState): Result<Unit>

    fun observePreference(key: String): Flow<LocalPreference?>
    suspend fun getPreferenceValue(key: String): String?
    suspend fun savePreference(key: String, value: String): Result<Unit>
    suspend fun deletePreference(key: String): Result<Unit>

    fun observeAllSyncStates(): Flow<List<ContentSyncState>>
    fun observeSyncState(contentSource: String): Flow<ContentSyncState?>
    suspend fun saveSyncState(syncState: ContentSyncState): Result<Unit>

    // Educational Hierarchy: Exam operations
    fun observeAllExams(): Flow<List<Exam>>
    fun observeActiveExams(): Flow<List<Exam>>
    fun observeExamById(id: String): Flow<Exam?>
    suspend fun getExamById(id: String): Exam?
    suspend fun saveExam(exam: Exam): Result<Unit>
    suspend fun saveExams(exams: List<Exam>): Result<Unit>
    suspend fun deleteExamById(id: String): Result<Unit>

    // Educational Hierarchy: Paper operations
    fun observePapersByExamId(examId: String): Flow<List<Paper>>
    fun observeActivePapersByExamId(examId: String): Flow<List<Paper>>
    fun observePaperById(id: String): Flow<Paper?>
    suspend fun getPaperById(id: String): Paper?
    suspend fun savePaper(paper: Paper): Result<Unit>
    suspend fun savePapers(papers: List<Paper>): Result<Unit>
    suspend fun deletePaperById(id: String): Result<Unit>

    // Educational Hierarchy: Subject operations
    fun observeSubjectsByPaperId(paperId: String): Flow<List<Subject>>
    fun observeActiveSubjectsByPaperId(paperId: String): Flow<List<Subject>>
    fun observeSubjectById(id: String): Flow<Subject?>
    suspend fun getSubjectById(id: String): Subject?
    suspend fun saveSubject(subject: Subject): Result<Unit>
    suspend fun saveSubjects(subjects: List<Subject>): Result<Unit>
    suspend fun deleteSubjectById(id: String): Result<Unit>

    // Educational Hierarchy: Topic operations
    fun observeTopicsBySubjectId(subjectId: String): Flow<List<Topic>>
    fun observeActiveTopicsBySubjectId(subjectId: String): Flow<List<Topic>>
    fun observeTopicById(id: String): Flow<Topic?>
    suspend fun getTopicById(id: String): Topic?
    suspend fun saveTopic(topic: Topic): Result<Unit>
    suspend fun saveTopics(topics: List<Topic>): Result<Unit>
    suspend fun deleteTopicById(id: String): Result<Unit>

    // Educational Hierarchy: Subtopic operations
    fun observeSubtopicsByTopicId(topicId: String): Flow<List<Subtopic>>
    fun observeActiveSubtopicsByTopicId(topicId: String): Flow<List<Subtopic>>
    fun observeSubtopicById(id: String): Flow<Subtopic?>
    suspend fun getSubtopicById(id: String): Subtopic?
    suspend fun saveSubtopic(subtopic: Subtopic): Result<Unit>
    suspend fun saveSubtopics(subtopics: List<Subtopic>): Result<Unit>
    suspend fun deleteSubtopicById(id: String): Result<Unit>

    // --- Syllabus Foundation Operations ---

    /**
     * Retrieves direct children of a syllabus node given its ID and type.
     * When parentId is null and parentType is null, returns top-level syllabus nodes (Exams).
     * Results are ordered deterministically by sortOrder ASC, title/name ASC.
     */
    fun observeChildrenOfNode(
        parentId: String?,
        parentType: SyllabusNodeType?,
        activeOnly: Boolean = true
    ): Flow<List<SyllabusNode>>

    /**
     * Resolves a single syllabus node by its ID and expected node type,
     * populated with any associated metadata.
     */
    fun observeSyllabusNode(
        id: String,
        nodeType: SyllabusNodeType
    ): Flow<SyllabusNode?>

    suspend fun getSyllabusNode(
        id: String,
        nodeType: SyllabusNodeType
    ): SyllabusNode?

    /**
     * Resolves the complete breadcrumb path from the root Exam down to the specified node.
     */
    suspend fun getSyllabusBreadcrumb(
        id: String,
        nodeType: SyllabusNodeType
    ): SyllabusBreadcrumb

    /**
     * Builds and retrieves the complete hierarchical tree rooted at an Exam.
     */
    suspend fun getSyllabusTreeForExam(
        examId: String,
        activeOnly: Boolean = true
    ): SyllabusTreeNode?

    // Syllabus Metadata CRUD
    fun observeSyllabusMetadata(nodeId: String): Flow<SyllabusMetadata?>
    suspend fun getSyllabusMetadata(nodeId: String): SyllabusMetadata?
    suspend fun saveSyllabusMetadata(metadata: SyllabusMetadata): Result<Unit>
    suspend fun deleteSyllabusMetadata(nodeId: String): Result<Unit>

    /**
     * Observes active subtopics under a topic enriched with metadata and question counts.
     * Orders deterministically by sortOrder ASC, name ASC.
     */
    fun observeActiveSubtopicsWithDetailsByTopicId(topicId: String): Flow<List<SubtopicWithDetails>>

    // Question Foundation Operations
    fun observeQuestionsForSubtopic(subtopicId: String, activeOnly: Boolean = true): Flow<List<Question>>
    fun observeQuestionById(id: String): Flow<Question?>
    fun observeQuestion(questionId: String): Flow<Question?> = observeQuestionById(questionId)
    suspend fun getQuestionById(id: String): Question?
    suspend fun getQuestionCountBySubtopicId(subtopicId: String): Int
    suspend fun getActiveQuestionCountBySubtopicId(subtopicId: String): Int
    suspend fun saveQuestion(question: Question): Result<Unit>
    suspend fun saveQuestions(questions: List<Question>): Result<Unit>
    suspend fun deleteQuestionById(id: String): Result<Unit>
    suspend fun deleteQuestionsBySubtopicId(subtopicId: String): Result<Unit>

    fun observeOptionsForQuestion(questionId: String): Flow<List<QuestionOption>>
    suspend fun getOptionsForQuestion(questionId: String): List<QuestionOption>
    suspend fun saveOption(option: QuestionOption): Result<Unit>
    suspend fun saveOptions(options: List<QuestionOption>): Result<Unit>
    suspend fun deleteOptionById(id: String): Result<Unit>
    suspend fun deleteOptionsForQuestion(questionId: String): Result<Unit>

    // Practice Attempt Operations (Step 11 & 12)
    fun observeAttemptsBySubtopicId(subtopicId: String): Flow<List<PracticeAttempt>>
    fun observeRecentAttempts(limit: Int = 20): Flow<List<PracticeAttempt>>
    fun observeAllPracticeAttempts(): Flow<List<PracticeAttempt>>
    suspend fun getAllPracticeAttempts(): List<PracticeAttempt>
    suspend fun getAttemptById(id: String): PracticeAttempt?
    suspend fun savePracticeAttempt(attempt: PracticeAttempt): Result<Unit>
    suspend fun deleteAttemptById(id: String): Result<Unit>

    // Wrong Question Operations (Step 13)
    fun observeAllWrongQuestions(): Flow<List<WrongQuestion>>
    fun observeWrongQuestionsBySubtopicId(subtopicId: String): Flow<List<WrongQuestion>>
    suspend fun getWrongQuestionsBySubtopicId(subtopicId: String): List<WrongQuestion>
    suspend fun getWrongQuestionByQuestionId(questionId: String): WrongQuestion?
    suspend fun recordMistake(questionId: String, subtopicId: String, attemptId: String?, timestamp: Long): Result<Unit>
    suspend fun deleteWrongQuestion(questionId: String): Result<Unit>

    // Bookmarked Question Operations (Step 14)
    fun observeAllBookmarkedQuestions(): Flow<List<BookmarkedQuestion>>
    fun observeBookmarkedQuestionsBySubtopicId(subtopicId: String): Flow<List<BookmarkedQuestion>>
    suspend fun getBookmarkedQuestionsBySubtopicId(subtopicId: String): List<BookmarkedQuestion>
    suspend fun getBookmarkedQuestionByQuestionId(questionId: String): BookmarkedQuestion?
    fun observeIsBookmarked(questionId: String): Flow<Boolean>
    suspend fun isBookmarked(questionId: String): Boolean
    suspend fun saveBookmark(questionId: String, subtopicId: String, timestamp: Long = System.currentTimeMillis()): Result<Unit>
    suspend fun deleteBookmark(questionId: String): Result<Unit>
    suspend fun toggleBookmark(questionId: String, subtopicId: String, timestamp: Long = System.currentTimeMillis()): Result<Boolean>
    fun observeBookmarkCount(): Flow<Int>
    suspend fun getBookmarkCount(): Int

    // Previous Year Question (PYQ) Operations (Step 15)
    fun observeAllPreviousYearQuestions(): Flow<List<com.example.domain.model.pyq.PreviousYearQuestion>>
    fun observePreviousYearQuestionsByExamId(examId: String): Flow<List<com.example.domain.model.pyq.PreviousYearQuestion>>
    fun observePreviousYearQuestionsByPaperId(paperId: String): Flow<List<com.example.domain.model.pyq.PreviousYearQuestion>>
    fun observePreviousYearQuestionsByPaperIdAndYear(paperId: String, year: Int): Flow<List<com.example.domain.model.pyq.PreviousYearQuestion>>
    fun observePreviousYearQuestionsBySubtopicId(subtopicId: String): Flow<List<com.example.domain.model.pyq.PreviousYearQuestion>>
    fun observePreviousYearQuestionByQuestionId(questionId: String): Flow<com.example.domain.model.pyq.PreviousYearQuestion?>
    suspend fun getPreviousYearQuestionByQuestionId(questionId: String): com.example.domain.model.pyq.PreviousYearQuestion?
    suspend fun getPreviousYearQuestionById(id: String): com.example.domain.model.pyq.PreviousYearQuestion?
    fun observeDistinctYearsForPaper(paperId: String): Flow<List<Int>>
    suspend fun getDistinctYearsForPaper(paperId: String): List<Int>
    fun observeAllDistinctYears(): Flow<List<Int>>
    suspend fun savePreviousYearQuestion(pyq: com.example.domain.model.pyq.PreviousYearQuestion): Result<Unit>
    suspend fun deletePreviousYearQuestion(id: String): Result<Unit>
    suspend fun deletePreviousYearQuestionByQuestionId(questionId: String): Result<Unit>
    fun observePreviousYearQuestionCount(): Flow<Int>
    suspend fun getPreviousYearQuestionCount(): Int
}
