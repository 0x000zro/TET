package com.example.data.local.db

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.datasource.DefaultLocalEducationalDataSource
import com.example.data.local.db.entity.ExamEntity
import com.example.data.local.db.entity.PaperEntity
import com.example.data.local.db.entity.QuestionEntity
import com.example.data.local.db.entity.QuestionOptionEntity
import com.example.data.local.db.entity.SubjectEntity
import com.example.data.local.db.entity.SubtopicEntity
import com.example.data.local.db.entity.TopicEntity
import com.example.data.local.seed.GenericQuestionFixture
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.Question
import com.example.domain.model.QuestionDifficulty
import com.example.domain.model.QuestionOption
import com.example.domain.model.QuestionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class QuestionFoundationIntegrationTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        AppDatabase.setTestInstance(database)
    }

    @After
    fun tearDown() {
        AppDatabase.setTestInstance(null)
        database.close()
    }

    private fun insertHierarchyFixtures(
        subtopicId: String = "subtopic_test_01",
        topicId: String = "topic_test_01",
        subjectId: String = "subject_test_01",
        paperId: String = "paper_test_01",
        examId: String = "exam_test_01"
    ) = runBlocking {
        database.examDao().insertOrUpdateExam(
            ExamEntity(examId, "CTET", "CTET", "Central Teacher Eligibility Test", true, 1, 1000)
        )
        database.paperDao().insertOrUpdatePaper(
            PaperEntity(paperId, examId, "Paper 1", "P1", "Primary Stage", true, 1, 1000)
        )
        database.subjectDao().insertOrUpdateSubject(
            SubjectEntity(subjectId, paperId, "Child Development", "CDP", "Pedagogy", true, 1, 1000)
        )
        database.topicDao().insertOrUpdateTopic(
            TopicEntity(topicId, subjectId, "Development Concept", "Piaget and Vygotsky", true, 1, 1000)
        )
        database.subtopicDao().insertOrUpdateSubtopic(
            SubtopicEntity(subtopicId, topicId, "Piaget Stages", "Cognitive stages", true, 1, 1000)
        )
    }

    @Test
    fun databaseVersion_isVersionCurrent() {
        val version = database.openHelper.readableDatabase.version
        assertEquals(DatabaseContract.DATABASE_VERSION, version)
    }

    @Test
    fun insertAndQueryQuestionWithOptions_returnsNormalizedStructure() = runBlocking {
        insertHierarchyFixtures()

        val question = QuestionEntity(
            id = "q_01",
            subtopicId = "subtopic_test_01",
            questionText = "Which stage represents sensorimotor intelligence?",
            questionType = QuestionType.MCQ_SINGLE.name,
            difficulty = QuestionDifficulty.EASY.name,
            explanation = "Sensorimotor lasts from 0-2 years.",
            isActive = true,
            sortOrder = 1
        )
        database.questionDao().insertOrUpdateQuestion(question)

        val options = listOf(
            QuestionOptionEntity("opt_1", "q_01", "Stage 1", 1, true),
            QuestionOptionEntity("opt_2", "q_01", "Stage 2", 2, false),
            QuestionOptionEntity("opt_3", "q_01", "Stage 3", 3, false),
            QuestionOptionEntity("opt_4", "q_01", "Stage 4", 4, false)
        )
        database.questionOptionDao().insertOrUpdateOptions(options)

        val retrieved = database.questionDao().getQuestionWithOptionsById("q_01")
        assertNotNull(retrieved)
        assertEquals("q_01", retrieved!!.question.id)
        assertEquals("subtopic_test_01", retrieved.question.subtopicId)
        assertEquals(4, retrieved.options.size)
        assertEquals("opt_1", retrieved.options[0].id)
        assertTrue(retrieved.options[0].isCorrect)
    }

    @Test
    fun observeQuestionsBySubtopic_returnsSortedDeterministicList() = runBlocking {
        insertHierarchyFixtures()

        val q2 = QuestionEntity("q_02", "subtopic_test_01", "Second question", "MCQ_SINGLE", "MEDIUM", "", true, 2)
        val q1 = QuestionEntity("q_01", "subtopic_test_01", "First question", "MCQ_SINGLE", "EASY", "", true, 1)
        val qInactive = QuestionEntity("q_inact", "subtopic_test_01", "Inactive question", "MCQ_SINGLE", "HARD", "", false, 0)

        database.questionDao().insertOrUpdateQuestions(listOf(q2, q1, qInactive))

        val allQuestions = database.questionDao().getQuestionsBySubtopicIdFlow("subtopic_test_01").first()
        assertEquals(3, allQuestions.size)
        assertEquals("q_inact", allQuestions[0].id) // sortOrder 0
        assertEquals("q_01", allQuestions[1].id)    // sortOrder 1
        assertEquals("q_02", allQuestions[2].id)    // sortOrder 2

        val activeQuestions = database.questionDao().getActiveQuestionsBySubtopicIdFlow("subtopic_test_01").first()
        assertEquals(2, activeQuestions.size)
        assertEquals("q_01", activeQuestions[0].id)
        assertEquals("q_02", activeQuestions[1].id)
    }

    @Test
    fun cascadeDeleteOnQuestion_deletesChildOptions() = runBlocking {
        insertHierarchyFixtures()

        val question = QuestionEntity("q_delete_me", "subtopic_test_01", "Q to delete", "MCQ_SINGLE", "EASY", "", true, 1)
        database.questionDao().insertOrUpdateQuestion(question)

        val options = listOf(
            QuestionOptionEntity("opt_del_1", "q_delete_me", "Opt 1", 1, true),
            QuestionOptionEntity("opt_del_2", "q_delete_me", "Opt 2", 2, false)
        )
        database.questionOptionDao().insertOrUpdateOptions(options)

        assertEquals(2, database.questionOptionDao().getOptionsForQuestion("q_delete_me").size)

        // Delete question
        database.questionDao().deleteQuestionById("q_delete_me")

        assertNull(database.questionDao().getQuestionById("q_delete_me"))
        assertTrue(database.questionOptionDao().getOptionsForQuestion("q_delete_me").isEmpty())
    }

    @Test
    fun foreignKeyRestrict_preventsDeletingSubtopicWithQuestions() = runBlocking {
        insertHierarchyFixtures()

        val question = QuestionEntity("q_fk_test", "subtopic_test_01", "Q text", "MCQ_SINGLE", "EASY", "", true, 1)
        database.questionDao().insertOrUpdateQuestion(question)

        try {
            // Attempt deleting parent subtopic while child question exists
            database.subtopicDao().deleteSubtopicById("subtopic_test_01")
            fail("Expected SQLiteConstraintException due to ForeignKey.RESTRICT on subtopic deletion")
        } catch (expected: SQLiteConstraintException) {
            assertTrue(expected.message?.contains("FOREIGN KEY") == true || expected.message?.contains("constraint failed") == true)
        }
    }

    @Test
    fun repository_saveAndObserveQuestions_validatesAndPersists() = runBlocking {
        insertHierarchyFixtures()

        val repository = EducationalRepositoryImpl(
            localDataSource = DefaultLocalEducationalDataSource(databaseProvider = { database }),
            ioDispatcher = Dispatchers.Unconfined
        )

        val sampleQuestion = GenericQuestionFixture.createSampleQuestion(
            id = "q_repo_01",
            subtopicId = "subtopic_test_01"
        )

        val saveResult = repository.saveQuestion(sampleQuestion)
        assertTrue(saveResult.isSuccess)

        val observed = repository.observeQuestionsForSubtopic("subtopic_test_01").first()
        assertEquals(1, observed.size)
        assertEquals("q_repo_01", observed[0].id)
        assertEquals(4, observed[0].options.size)
        assertEquals("Sensorimotor stage", observed[0].options.first { it.isCorrect }.optionText)

        // Count check
        val count = repository.getQuestionCountBySubtopicId("subtopic_test_01")
        assertEquals(1, count)
    }

    @Test
    fun repository_saveInvalidQuestion_rejectsWithFailure() = runBlocking {
        insertHierarchyFixtures()

        val repository = EducationalRepositoryImpl(
            localDataSource = DefaultLocalEducationalDataSource(databaseProvider = { database }),
            ioDispatcher = Dispatchers.Unconfined
        )

        // Invalid: No correct option
        val invalidQuestion = Question(
            id = "q_invalid_01",
            subtopicId = "subtopic_test_01",
            questionText = "Invalid question with no answer",
            options = listOf(
                QuestionOption("o1", "q_invalid_01", "A", 1, false),
                QuestionOption("o2", "q_invalid_01", "B", 2, false)
            )
        )

        val result = repository.saveQuestion(invalidQuestion)
        assertTrue(result.isFailure)
        assertNull(repository.getQuestionById("q_invalid_01"))
    }

    @Test
    fun migrationFromV3toV4_preservesV3TablesAndCreatesQuestionsTables() {
        val dbName = "migration_test_v3_to_v4.db"
        context.deleteDatabase(dbName)

        // 1. Create v3 database using SQLite OpenHelper
        val configV3 = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(3) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Foundation tables (v1)
                    db.execSQL("CREATE TABLE IF NOT EXISTS `app_state` (`id` TEXT NOT NULL, `is_first_launch` INTEGER NOT NULL, `is_content_initialized` INTEGER NOT NULL, `last_known_content_version` INTEGER NOT NULL, `schema_version` INTEGER NOT NULL, `is_compatible` INTEGER NOT NULL, `last_launch_timestamp` INTEGER NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `local_preferences` (`preference_key` TEXT NOT NULL, `preference_value` TEXT NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`preference_key`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `content_sync_state` (`content_source` TEXT NOT NULL, `content_version` INTEGER NOT NULL, `last_successful_sync_timestamp` INTEGER, `sync_status` TEXT NOT NULL, `error_message` TEXT, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`content_source`))")

                    // Hierarchy tables (v2)
                    db.execSQL("CREATE TABLE IF NOT EXISTS `exams` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `short_name` TEXT NOT NULL, `description` TEXT NOT NULL, `is_active` INTEGER NOT NULL, `sort_order` INTEGER NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `papers` (`id` TEXT NOT NULL, `exam_id` TEXT NOT NULL, `name` TEXT NOT NULL, `short_name` TEXT NOT NULL, `description` TEXT NOT NULL, `is_active` INTEGER NOT NULL, `sort_order` INTEGER NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `subjects` (`id` TEXT NOT NULL, `paper_id` TEXT NOT NULL, `name` TEXT NOT NULL, `short_name` TEXT NOT NULL, `description` TEXT NOT NULL, `is_active` INTEGER NOT NULL, `sort_order` INTEGER NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `topics` (`id` TEXT NOT NULL, `subject_id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `is_active` INTEGER NOT NULL, `sort_order` INTEGER NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                    db.execSQL("CREATE TABLE IF NOT EXISTS `subtopics` (`id` TEXT NOT NULL, `topic_id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `is_active` INTEGER NOT NULL, `sort_order` INTEGER NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")

                    // Syllabus metadata (v3)
                    db.execSQL("CREATE TABLE IF NOT EXISTS `syllabus_metadata` (`node_id` TEXT NOT NULL, `learning_objective` TEXT NOT NULL, `short_note` TEXT NOT NULL, `estimated_minutes` INTEGER NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`node_id`))")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helperV3 = FrameworkSQLiteOpenHelperFactory().create(configV3)
        val v3Db = helperV3.writableDatabase
        v3Db.execSQL("INSERT INTO exams VALUES ('exam_v3', 'Exam v3', 'E3', 'Desc', 1, 1, 1000)")
        v3Db.execSQL("INSERT INTO papers VALUES ('paper_v3', 'exam_v3', 'Paper v3', 'P3', 'Desc', 1, 1, 1000)")
        v3Db.execSQL("INSERT INTO subjects VALUES ('subject_v3', 'paper_v3', 'Subject v3', 'S3', 'Desc', 1, 1, 1000)")
        v3Db.execSQL("INSERT INTO topics VALUES ('topic_v3', 'subject_v3', 'Topic v3', 'Desc', 1, 1, 1000)")
        v3Db.execSQL("INSERT INTO subtopics VALUES ('subtopic_v3', 'topic_v3', 'Subtopic v3', 'Desc', 1, 1, 1000)")
        v3Db.execSQL("INSERT INTO syllabus_metadata VALUES ('subtopic_v3', 'Learn Subtopic v3', 'Notes', 20, 1000)")
        v3Db.close()

        // 2. Open helper with target Version 4 and run MIGRATION_3_4
        val helperV4 = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(4) {
                    override fun onCreate(db: SupportSQLiteDatabase) {}
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                        if (oldVersion == 3 && newVersion == 4) {
                            DatabaseMigrations.MIGRATION_3_4.migrate(db)
                        }
                    }
                })
                .build()
        )

        val v4Db = helperV4.writableDatabase
        assertEquals(4, v4Db.version)

        // 3. Verify existing data preserved across all prior tables
        val examCursor = v4Db.query("SELECT name FROM exams WHERE id = 'exam_v3'")
        assertTrue(examCursor.moveToFirst())
        assertEquals("Exam v3", examCursor.getString(0))
        examCursor.close()

        val subtopicCursor = v4Db.query("SELECT name FROM subtopics WHERE id = 'subtopic_v3'")
        assertTrue(subtopicCursor.moveToFirst())
        assertEquals("Subtopic v3", subtopicCursor.getString(0))
        subtopicCursor.close()

        val metaCursor = v4Db.query("SELECT learning_objective FROM syllabus_metadata WHERE node_id = 'subtopic_v3'")
        assertTrue(metaCursor.moveToFirst())
        assertEquals("Learn Subtopic v3", metaCursor.getString(0))
        metaCursor.close()

        // 4. Verify new questions and question_options tables are fully functional
        v4Db.execSQL(
            "INSERT INTO questions VALUES ('q_migrated', 'subtopic_v3', 'Migrated Question?', 'MCQ_SINGLE', 'MEDIUM', 'Explanation', 1, 1, 2000)"
        )
        v4Db.execSQL(
            "INSERT INTO question_options VALUES ('opt_migrated_1', 'q_migrated', 'Option 1', 1, 1, 2000)"
        )

        val qCursor = v4Db.query("SELECT question_text, difficulty FROM questions WHERE id = 'q_migrated'")
        assertTrue(qCursor.moveToFirst())
        assertEquals("Migrated Question?", qCursor.getString(0))
        assertEquals("MEDIUM", qCursor.getString(1))
        qCursor.close()

        val optCursor = v4Db.query("SELECT option_text, is_correct FROM question_options WHERE id = 'opt_migrated_1'")
        assertTrue(optCursor.moveToFirst())
        assertEquals("Option 1", optCursor.getString(0))
        assertEquals(1, optCursor.getInt(1))
        optCursor.close()

        v4Db.close()
    }
}
