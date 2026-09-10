package com.example.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.datasource.DefaultLocalEducationalDataSource
import com.example.data.local.db.entity.ExamEntity
import com.example.data.local.db.entity.PaperEntity
import com.example.data.local.db.entity.PracticeAttemptEntity
import com.example.data.local.db.entity.SubjectEntity
import com.example.data.local.db.entity.SubtopicEntity
import com.example.data.local.db.entity.TopicEntity
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.practice.PracticeAttempt
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * End-to-end integration test for Practice Attempts & Local Progress Foundation (Step 11).
 *
 * Verifies with actual Room SQLite database:
 * 1. Dedicated practice_attempts Room table schema and operations.
 * 2. Stable primary key persistence (UUID).
 * 3. Deterministic query ordering (completed_at DESC, id DESC).
 * 4. Filtering by subtopicId and limiting recent attempts.
 * 5. Score percentage precision and data integrity.
 * 6. Repository layer integration: save, observe, and retrieve attempts.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class PracticeAttemptDatabaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var database: AppDatabase
    private lateinit var repository: EducationalRepositoryImpl

    private val examId = "exam_gate"
    private val paperId = "paper_cs"
    private val subjectId = "subj_algorithms"
    private val topicId = "topic_sorting"
    private val subtopicId1 = "sub_quicksort"
    private val subtopicId2 = "sub_mergesort"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context: Context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val localDataSource = DefaultLocalEducationalDataSource(database, testDispatcher)
        repository = EducationalRepositoryImpl(
            localDataSource = localDataSource,
            ioDispatcher = testDispatcher
        )

        // Seed educational hierarchy to satisfy foreign keys
        runBlocking {
            database.examDao().insertExam(
                ExamEntity(id = examId, code = "GATE", name = "GATE CS", isActive = true)
            )
            database.paperDao().insertPaper(
                PaperEntity(id = paperId, examId = examId, name = "Paper 1", isActive = true)
            )
            database.subjectDao().insertSubject(
                SubjectEntity(id = subjectId, paperId = paperId, name = "Algorithms", isActive = true)
            )
            database.topicDao().insertTopic(
                TopicEntity(id = topicId, subjectId = subjectId, name = "Sorting", isActive = true)
            )
            database.subtopicDao().insertSubtopic(
                SubtopicEntity(id = subtopicId1, topicId = topicId, name = "Quicksort", isActive = true)
            )
            database.subtopicDao().insertSubtopic(
                SubtopicEntity(id = subtopicId2, topicId = topicId, name = "Mergesort", isActive = true)
            )
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        database.close()
    }

    @Test
    fun saveAndRetrieveAttempt_returnsExactData() = runTest {
        val attempt = PracticeAttempt(
            id = "att_101",
            subtopicId = subtopicId1,
            totalQuestions = 5,
            answeredQuestions = 5,
            correctAnswers = 4,
            incorrectAnswers = 1,
            percentageScore = 80.0,
            startedAt = 1000L,
            completedAt = 2000L
        )

        val result = repository.savePracticeAttempt(attempt)
        assertTrue("Expected save to succeed, got $result", result.isSuccess)

        val retrieved = repository.getAttemptById("att_101")
        assertNotNull(retrieved)
        assertEquals("att_101", retrieved?.id)
        assertEquals(subtopicId1, retrieved?.subtopicId)
        assertEquals(5, retrieved?.totalQuestions)
        assertEquals(5, retrieved?.answeredQuestions)
        assertEquals(4, retrieved?.correctAnswers)
        assertEquals(1, retrieved?.incorrectAnswers)
        assertEquals(80.0, retrieved?.percentageScore ?: 0.0, 0.001)
        assertEquals(1000L, retrieved?.startedAt)
        assertEquals(2000L, retrieved?.completedAt)
    }

    @Test
    fun observeAttemptsBySubtopicId_ordersByCompletedAtDescending() = runTest {
        val attempt1 = PracticeAttempt(
            id = "att_1",
            subtopicId = subtopicId1,
            totalQuestions = 3,
            answeredQuestions = 3,
            correctAnswers = 2,
            incorrectAnswers = 1,
            percentageScore = 66.7,
            startedAt = 1000L,
            completedAt = 2000L
        )
        val attempt2 = PracticeAttempt(
            id = "att_2",
            subtopicId = subtopicId1,
            totalQuestions = 3,
            answeredQuestions = 3,
            correctAnswers = 3,
            incorrectAnswers = 0,
            percentageScore = 100.0,
            startedAt = 3000L,
            completedAt = 5000L
        )
        val attemptOtherSubtopic = PracticeAttempt(
            id = "att_3",
            subtopicId = subtopicId2,
            totalQuestions = 4,
            answeredQuestions = 4,
            correctAnswers = 4,
            incorrectAnswers = 0,
            percentageScore = 100.0,
            startedAt = 6000L,
            completedAt = 7000L
        )

        repository.savePracticeAttempt(attempt1)
        repository.savePracticeAttempt(attempt2)
        repository.savePracticeAttempt(attemptOtherSubtopic)

        val subtopic1Attempts = repository.observeAttemptsBySubtopicId(subtopicId1).first()
        assertEquals(2, subtopic1Attempts.size)
        // Most recent first
        assertEquals("att_2", subtopic1Attempts[0].id)
        assertEquals("att_1", subtopic1Attempts[1].id)
    }

    @Test
    fun observeRecentAttempts_respectsLimitAndDescendingOrder() = runTest {
        for (i in 1..5) {
            repository.savePracticeAttempt(
                PracticeAttempt(
                    id = "att_$i",
                    subtopicId = subtopicId1,
                    totalQuestions = 10,
                    answeredQuestions = 10,
                    correctAnswers = i,
                    incorrectAnswers = 10 - i,
                    percentageScore = (i * 10).toDouble(),
                    startedAt = i * 1000L,
                    completedAt = i * 1000L + 500L
                )
            )
        }

        val recentAttempts = repository.observeRecentAttempts(limit = 3).first()
        assertEquals(3, recentAttempts.size)
        assertEquals("att_5", recentAttempts[0].id)
        assertEquals("att_4", recentAttempts[1].id)
        assertEquals("att_3", recentAttempts[2].id)
    }

    @Test
    fun deleteAttemptById_removesAttemptSuccessfully() = runTest {
        val attempt = PracticeAttempt(
            id = "att_delete_test",
            subtopicId = subtopicId1,
            totalQuestions = 2,
            answeredQuestions = 2,
            correctAnswers = 2,
            incorrectAnswers = 0,
            percentageScore = 100.0,
            startedAt = 1000L,
            completedAt = 2000L
        )
        repository.savePracticeAttempt(attempt)
        assertNotNull(repository.getAttemptById("att_delete_test"))

        val deleteResult = repository.deleteAttemptById("att_delete_test")
        assertTrue(deleteResult.isSuccess)
        assertNull(repository.getAttemptById("att_delete_test"))
    }

    @Test
    fun migration4To5_createsPracticeAttemptsTableAndPreservesExistingData() {
        val dbName = "migration_test_4_5.db"
        val context: Context = ApplicationProvider.getApplicationContext()
        context.deleteDatabase(dbName)

        // 1. Create a version 4 database with an existing subtopic
        val configV4 = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(4) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS subtopics (
                            id TEXT PRIMARY KEY NOT NULL,
                            topic_id TEXT NOT NULL,
                            name TEXT NOT NULL,
                            is_active INTEGER NOT NULL DEFAULT 1,
                            sort_order INTEGER NOT NULL DEFAULT 0,
                            updated_at_timestamp INTEGER NOT NULL DEFAULT 0
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        INSERT INTO subtopics (id, topic_id, name, is_active, sort_order, updated_at_timestamp)
                        VALUES ('sub_test_mig', 'top_1', 'Binary Search', 1, 1, 1000)
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helperV4 = FrameworkSQLiteOpenHelperFactory().create(configV4)
        val dbV4 = helperV4.writableDatabase
        dbV4.close()

        // 2. Upgrade to version 5 using MIGRATION_4_5
        val configV5 = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {}
                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                    if (oldVersion == 4 && newVersion == 5) {
                        DatabaseMigrations.MIGRATION_4_5.migrate(db)
                    }
                }
            })
            .build()

        val helperV5 = FrameworkSQLiteOpenHelperFactory().create(configV5)
        val dbV5 = helperV5.writableDatabase

        // 3. Verify subtopics table and data preserved
        val subtopicCursor = dbV5.query("SELECT name FROM subtopics WHERE id = 'sub_test_mig'")
        assertTrue(subtopicCursor.moveToFirst())
        assertEquals("Binary Search", subtopicCursor.getString(0))
        subtopicCursor.close()

        // 4. Verify practice_attempts table exists and functions
        dbV5.execSQL(
            """
            INSERT INTO practice_attempts (
                id, subtopic_id, total_questions, answered_questions, correct_answers,
                incorrect_answers, percentage_score, started_at, completed_at, updated_at_timestamp
            ) VALUES ('att_mig_1', 'sub_test_mig', 10, 10, 8, 2, 80.0, 1000, 2000, 2000)
            """.trimIndent()
        )

        val attemptCursor = dbV5.query("SELECT subtopic_id, percentage_score FROM practice_attempts WHERE id = 'att_mig_1'")
        assertTrue(attemptCursor.moveToFirst())
        assertEquals("sub_test_mig", attemptCursor.getString(0))
        assertEquals(80.0, attemptCursor.getDouble(1), 0.01)
        attemptCursor.close()

        dbV5.close()
        context.deleteDatabase(dbName)
    }
}
