package com.example.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.datasource.DefaultLocalEducationalDataSource
import com.example.data.local.db.entity.ExamEntity
import com.example.data.local.db.entity.PaperEntity
import com.example.data.local.db.entity.QuestionEntity
import com.example.data.local.db.entity.SubjectEntity
import com.example.data.local.db.entity.SubtopicEntity
import com.example.data.local.db.entity.TopicEntity
import com.example.data.repository.EducationalRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * End-to-end integration test for Bookmark / Important Questions Foundation (Step 14).
 *
 * Verifies with actual Room SQLite database:
 * 1. Dedicated bookmarked_questions Room table schema and operations.
 * 2. Atomic toggleBookmark: inserts if missing, removes if present.
 * 3. Sorting bookmarks by bookmarked_at DESC.
 * 4. Filtering bookmarks by subtopic_id.
 * 5. Checking isBookmarked status.
 * 6. Removing a bookmark preserves Question and Subtopic data.
 * 7. Deleting a Question cascades and removes its bookmark.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkedQuestionDatabaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var repository: EducationalRepositoryImpl

    private val examId = "exam_gate"
    private val paperId = "paper_cs"
    private val subjectId = "subj_algorithms"
    private val topicId = "topic_sorting"
    private val subtopicId1 = "sub_quicksort"
    private val subtopicId2 = "sub_mergesort"
    private val questionId1 = "q_quick_1"
    private val questionId2 = "q_merge_1"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context: Context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val localDataSource = DefaultLocalEducationalDataSource { database }
        repository = EducationalRepositoryImpl(
            localDataSource = localDataSource,
            ioDispatcher = testDispatcher
        )

        runBlocking {
            seedHierarchy()
        }
    }

    private suspend fun seedHierarchy() {
        database.examDao().insertOrUpdateExam(
            ExamEntity(id = examId, name = "GATE CS", shortName = "GATE", isActive = true)
        )
        database.paperDao().insertOrUpdatePaper(
            PaperEntity(id = paperId, examId = examId, name = "Computer Science", shortName = "CS", isActive = true)
        )
        database.subjectDao().insertOrUpdateSubject(
            SubjectEntity(id = subjectId, paperId = paperId, name = "Algorithms", shortName = "Algo", isActive = true)
        )
        database.topicDao().insertOrUpdateTopic(
            TopicEntity(id = topicId, subjectId = subjectId, name = "Sorting Algorithms", isActive = true)
        )
        database.subtopicDao().insertOrUpdateSubtopic(
            SubtopicEntity(id = subtopicId1, topicId = topicId, name = "Quicksort", isActive = true)
        )
        database.subtopicDao().insertOrUpdateSubtopic(
            SubtopicEntity(id = subtopicId2, topicId = topicId, name = "Mergesort", isActive = true)
        )
        database.questionDao().insertOrUpdateQuestion(
            QuestionEntity(
                id = questionId1,
                subtopicId = subtopicId1,
                questionText = "What is the worst case time complexity of Quicksort?",
                questionType = "MCQ_SINGLE",
                explanation = "O(n^2) when partition is unbalanced.",
                difficulty = "MEDIUM",
                isActive = true
            )
        )
        database.questionDao().insertOrUpdateQuestion(
            QuestionEntity(
                id = questionId2,
                subtopicId = subtopicId2,
                questionText = "What is the space complexity of standard Mergesort?",
                questionType = "MCQ_SINGLE",
                explanation = "O(n) auxiliary space.",
                difficulty = "EASY",
                isActive = true
            )
        )
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `toggleBookmark adds bookmark when not present and removes when present`() = runTest(testDispatcher) {
        // Initially not bookmarked
        val initialIsBookmarked = repository.observeIsBookmarked(questionId1).first()
        assertFalse(initialIsBookmarked)

        // Toggle on
        val result1 = repository.toggleBookmark(questionId1, subtopicId1)
        assertTrue(result1.isSuccess)
        assertTrue(result1.getOrThrow())

        val bookmarkedAfterToggle1 = repository.observeIsBookmarked(questionId1).first()
        assertTrue(bookmarkedAfterToggle1)

        val bookmark = repository.getBookmarkedQuestionByQuestionId(questionId1)
        assertNotNull(bookmark)
        assertEquals(questionId1, bookmark?.questionId)
        assertEquals(subtopicId1, bookmark?.subtopicId)

        // Toggle off
        val result2 = repository.toggleBookmark(questionId1, subtopicId1)
        assertTrue(result2.isSuccess)
        assertFalse(result2.getOrThrow())

        val bookmarkedAfterToggle2 = repository.observeIsBookmarked(questionId1).first()
        assertFalse(bookmarkedAfterToggle2)

        val bookmarkAfterRemove = repository.getBookmarkedQuestionByQuestionId(questionId1)
        assertNull(bookmarkAfterRemove)
    }

    @Test
    fun `observeAllBookmarkedQuestions returns bookmarks ordered by timestamp descending`() = runTest(testDispatcher) {
        repository.toggleBookmark(questionId1, subtopicId1)
        // Ensure distinct timestamp
        repository.toggleBookmark(questionId2, subtopicId2)

        val all = repository.observeAllBookmarkedQuestions().first()
        assertEquals(2, all.size)
        // Latest first
        assertTrue(all[0].bookmarkedAt >= all[1].bookmarkedAt)
    }

    @Test
    fun `observeBookmarkedQuestionsBySubtopicId filters correctly`() = runTest(testDispatcher) {
        repository.toggleBookmark(questionId1, subtopicId1)
        repository.toggleBookmark(questionId2, subtopicId2)

        val sub1Bookmarks = repository.observeBookmarkedQuestionsBySubtopicId(subtopicId1).first()
        assertEquals(1, sub1Bookmarks.size)
        assertEquals(questionId1, sub1Bookmarks.first().questionId)

        val sub2Bookmarks = repository.observeBookmarkedQuestionsBySubtopicId(subtopicId2).first()
        assertEquals(1, sub2Bookmarks.size)
        assertEquals(questionId2, sub2Bookmarks.first().questionId)
    }

    @Test
    fun `deleteBookmark removes bookmark without deleting question or subtopic`() = runTest(testDispatcher) {
        repository.toggleBookmark(questionId1, subtopicId1)
        val isBookmarked = repository.observeIsBookmarked(questionId1).first()
        assertTrue(isBookmarked)

        // Delete bookmark
        val deleteResult = repository.deleteBookmark(questionId1)
        assertTrue(deleteResult.isSuccess)

        // Bookmark is gone
        assertFalse(repository.observeIsBookmarked(questionId1).first())

        // Question still exists
        val question = repository.getQuestionById(questionId1)
        assertNotNull("Question must not be deleted when bookmark is removed", question)

        // Subtopic still exists
        val subtopic = repository.getSubtopicById(subtopicId1)
        assertNotNull("Subtopic must not be deleted when bookmark is removed", subtopic)
    }

    @Test
    fun `deleting a question cascades and deletes its bookmark`() = runTest(testDispatcher) {
        repository.toggleBookmark(questionId1, subtopicId1)
        assertTrue(repository.observeIsBookmarked(questionId1).first())

        // Delete the question
        repository.deleteQuestionById(questionId1)

        // Bookmark should be cascaded and gone
        val bookmark = repository.getBookmarkedQuestionByQuestionId(questionId1)
        assertNull(bookmark)
    }
}
