package com.example.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.datasource.DefaultLocalEducationalDataSource
import com.example.data.local.db.entity.ExamEntity
import com.example.data.local.db.entity.PaperEntity
import com.example.data.local.db.entity.SubjectEntity
import com.example.data.local.db.entity.SubtopicEntity
import com.example.data.local.db.entity.TopicEntity
import com.example.data.local.seed.DemoHierarchySeedData
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.Exam
import com.example.domain.model.Paper
import com.example.domain.model.Subject
import com.example.domain.model.Subtopic
import com.example.domain.model.Topic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EducationalHierarchyDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        DatabaseProvider.setTestInstance(database)
    }

    @After
    fun tearDown() {
        database.close()
        DatabaseProvider.setTestInstance(null)
    }

    @Test
    fun hierarchyDaos_areAvailableAndNotNull() {
        database.openHelper.writableDatabase
        assertTrue(database.isOpen)
        assertNotNull(database.examDao())
        assertNotNull(database.paperDao())
        assertNotNull(database.subjectDao())
        assertNotNull(database.topicDao())
        assertNotNull(database.subtopicDao())
    }

    @Test
    fun fullHierarchy_crudAndFlowOperations() = runBlocking {
        val examDao = database.examDao()
        val paperDao = database.paperDao()
        val subjectDao = database.subjectDao()
        val topicDao = database.topicDao()
        val subtopicDao = database.subtopicDao()

        // 1. Insert Exam
        val exam = ExamEntity(
            id = "exam_1",
            name = "National Eligibility Test",
            shortName = "NET",
            description = "National level eligibility test",
            isActive = true,
            sortOrder = 1
        )
        examDao.insertOrUpdateExam(exam)
        assertEquals(1, examDao.getExamCount())
        val retrievedExam = examDao.getExamById("exam_1")
        assertNotNull(retrievedExam)
        assertEquals("NET", retrievedExam?.shortName)

        // 2. Insert Paper
        val paper = PaperEntity(
            id = "paper_1",
            examId = "exam_1",
            name = "General Paper 1",
            shortName = "Paper 1",
            description = "Teaching & Research Aptitude",
            isActive = true,
            sortOrder = 1
        )
        paperDao.insertOrUpdatePaper(paper)
        assertEquals(1, paperDao.getPaperCountByExamId("exam_1"))

        // 3. Insert Subject
        val subject = SubjectEntity(
            id = "subject_1",
            paperId = "paper_1",
            name = "Teaching Aptitude",
            shortName = "TA",
            description = "Foundations of Teaching",
            isActive = true,
            sortOrder = 1
        )
        subjectDao.insertOrUpdateSubject(subject)
        assertEquals(1, subjectDao.getSubjectCountByPaperId("paper_1"))

        // 4. Insert Topic
        val topic = TopicEntity(
            id = "topic_1",
            subjectId = "subject_1",
            name = "Levels of Teaching",
            description = "Memory, Understanding and Reflective levels",
            isActive = true,
            sortOrder = 1
        )
        topicDao.insertOrUpdateTopic(topic)
        assertEquals(1, topicDao.getTopicCountBySubjectId("subject_1"))

        // 5. Insert Subtopics
        val subtopic1 = SubtopicEntity(
            id = "subtopic_1",
            topicId = "topic_1",
            name = "Memory Level of Teaching",
            description = "Herbartian model and rote recall",
            isActive = true,
            sortOrder = 1
        )
        val subtopic2 = SubtopicEntity(
            id = "subtopic_2",
            topicId = "topic_1",
            name = "Reflective Level of Teaching",
            description = "Hunt model and critical problem solving",
            isActive = false, // Inactive
            sortOrder = 2
        )
        subtopicDao.insertOrUpdateSubtopics(listOf(subtopic1, subtopic2))
        assertEquals(2, subtopicDao.getSubtopicCountByTopicId("topic_1"))

        // 6. Test Active filtering
        val allSubtopics = subtopicDao.getSubtopicsByTopicIdFlow("topic_1").first()
        val activeSubtopics = subtopicDao.getActiveSubtopicsByTopicIdFlow("topic_1").first()
        assertEquals(2, allSubtopics.size)
        assertEquals(1, activeSubtopics.size)
        assertEquals("subtopic_1", activeSubtopics.first().id)

        // 7. Test Sorting order
        val subtopic3 = SubtopicEntity(
            id = "subtopic_0",
            topicId = "topic_1",
            name = "Preliminary Overview",
            description = "Overview concepts",
            isActive = true,
            sortOrder = 0 // Lowest sort order
        )
        subtopicDao.insertOrUpdateSubtopic(subtopic3)
        val sortedSubtopics = subtopicDao.getSubtopicsByTopicIdFlow("topic_1").first()
        assertEquals("subtopic_0", sortedSubtopics.first().id)
        assertEquals(0, sortedSubtopics.first().sortOrder)

        // 8. Test Safe Deletion
        subtopicDao.deleteSubtopicById("subtopic_0")
        assertNull(subtopicDao.getSubtopicById("subtopic_0"))
    }

    @Test
    fun repositoryIntegration_managesEducationalHierarchyAcrossDomain() = runBlocking {
        val dataSource = DefaultLocalEducationalDataSource(databaseProvider = { database })
        val repository = EducationalRepositoryImpl(
            localDataSource = dataSource,
            ioDispatcher = Dispatchers.Unconfined
        )

        // 1. Initial exams list is empty
        val initialExams = repository.observeAllExams().first()
        assertTrue(initialExams.isEmpty())

        // 2. Save Exam via Repository
        val exam = Exam(
            id = "repo_exam_1",
            name = "Civil Services Examination",
            shortName = "CSE",
            description = "National civil services recruitment test",
            isActive = true,
            sortOrder = 1
        )
        val examSaveResult = repository.saveExam(exam)
        assertTrue(examSaveResult.isSuccess)

        val retrievedExam = repository.getExamById("repo_exam_1")
        assertNotNull(retrievedExam)
        assertEquals("CSE", retrievedExam?.shortName)

        // 3. Save Paper
        val paper = Paper(
            id = "repo_paper_1",
            examId = "repo_exam_1",
            name = "General Studies I",
            shortName = "GS-1",
            description = "History, Geography, and Society",
            isActive = true,
            sortOrder = 1
        )
        val paperSaveResult = repository.savePaper(paper)
        assertTrue(paperSaveResult.isSuccess)

        val retrievedPaper = repository.getPaperById("repo_paper_1")
        assertNotNull(retrievedPaper)
        assertEquals("repo_exam_1", retrievedPaper?.examId)

        // 4. Save Subject
        val subject = Subject(
            id = "repo_subject_1",
            paperId = "repo_paper_1",
            name = "Modern History",
            shortName = "ModHist",
            description = "Modern history from mid-18th century",
            isActive = true,
            sortOrder = 1
        )
        assertTrue(repository.saveSubject(subject).isSuccess)

        // 5. Save Topic
        val topic = Topic(
            id = "repo_topic_1",
            subjectId = "repo_subject_1",
            name = "Freedom Struggle",
            description = "Various stages and important contributors",
            isActive = true,
            sortOrder = 1
        )
        assertTrue(repository.saveTopic(topic).isSuccess)

        // 6. Save Subtopics
        val subtopic = Subtopic(
            id = "repo_subtopic_1",
            topicId = "repo_topic_1",
            name = "Non-Cooperation Movement",
            description = "Events, causes, and consequences",
            isActive = true,
            sortOrder = 1
        )
        assertTrue(repository.saveSubtopic(subtopic).isSuccess)

        // 7. Verify Flow observation through Repository
        val observedSubtopics = repository.observeSubtopicsByTopicId("repo_topic_1").first()
        assertEquals(1, observedSubtopics.size)
        assertEquals("repo_subtopic_1", observedSubtopics.first().id)

        // 8. Delete subtopic and verify
        assertTrue(repository.deleteSubtopicById("repo_subtopic_1").isSuccess)
        assertNull(repository.getSubtopicById("repo_subtopic_1"))
    }

    @Test
    fun demoSeedData_canBePersistedAndRead() = runBlocking {
        val dataSource = DefaultLocalEducationalDataSource(databaseProvider = { database })
        val repository = EducationalRepositoryImpl(
            localDataSource = dataSource,
            ioDispatcher = Dispatchers.Unconfined
        )

        // Persist entire demo seed hierarchy
        assertTrue(repository.saveExam(DemoHierarchySeedData.demoExam).isSuccess)
        assertTrue(repository.savePapers(DemoHierarchySeedData.demoPapers).isSuccess)
        assertTrue(repository.saveSubjects(DemoHierarchySeedData.demoSubjects).isSuccess)
        assertTrue(repository.saveTopics(DemoHierarchySeedData.demoTopics).isSuccess)
        assertTrue(repository.saveSubtopics(DemoHierarchySeedData.demoSubtopics).isSuccess)

        // Verify counts and nodes
        val exams = repository.observeAllExams().first()
        assertEquals(1, exams.size)
        assertEquals(DemoHierarchySeedData.demoExam.id, exams.first().id)

        val papers = repository.observePapersByExamId(DemoHierarchySeedData.demoExam.id).first()
        assertEquals(2, papers.size)

        val subjects = repository.observeSubjectsByPaperId(papers.first().id).first()
        assertEquals(2, subjects.size)

        val topics = repository.observeTopicsBySubjectId(subjects.first().id).first()
        assertEquals(2, topics.size)

        val subtopics = repository.observeSubtopicsByTopicId(topics.first().id).first()
        assertEquals(2, subtopics.size)
    }

    @Test
    fun migration_1_to_2_preservesExistingDataAndCreatesHierarchyTables() {
        val dbName = "migration_test.db"
        context.deleteDatabase(dbName)

        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    // Create v1 schema
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `app_state` (
                            `id` TEXT NOT NULL,
                            `is_first_launch` INTEGER NOT NULL,
                            `is_content_initialized` INTEGER NOT NULL,
                            `last_known_content_version` INTEGER NOT NULL,
                            `schema_version` INTEGER NOT NULL,
                            `is_compatible` INTEGER NOT NULL,
                            `last_launch_timestamp` INTEGER NOT NULL,
                            `updated_at_timestamp` INTEGER NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `local_preferences` (
                            `preference_key` TEXT NOT NULL,
                            `preference_value` TEXT NOT NULL,
                            `updated_at_timestamp` INTEGER NOT NULL,
                            PRIMARY KEY(`preference_key`)
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `content_sync_state` (
                            `content_source` TEXT NOT NULL,
                            `content_version` INTEGER NOT NULL,
                            `last_successful_sync_timestamp` INTEGER,
                            `sync_status` TEXT NOT NULL,
                            `error_message` TEXT,
                            `updated_at_timestamp` INTEGER NOT NULL,
                            PRIMARY KEY(`content_source`)
                        )
                        """.trimIndent()
                    )

                    // Seed v1 data
                    db.execSQL(
                        """
                        INSERT INTO app_state (id, is_first_launch, is_content_initialized, last_known_content_version, schema_version, is_compatible, last_launch_timestamp, updated_at_timestamp)
                        VALUES ('app_state_singleton', 0, 1, 1, 1, 1, 12345, 12345)
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        INSERT INTO local_preferences (preference_key, preference_value, updated_at_timestamp)
                        VALUES ('user_theme', 'DARK_MODE', 12345)
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val openHelper = FrameworkSQLiteOpenHelperFactory().create(config)
        val dbV1 = openHelper.writableDatabase
        dbV1.close()

        // Apply MIGRATION_1_2 directly on the SQLite database
        val openHelperV2 = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : SupportSQLiteOpenHelper.Callback(2) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {}
                    override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                        if (oldVersion == 1 && newVersion == 2) {
                            DatabaseMigrations.MIGRATION_1_2.migrate(db)
                        }
                    }
                })
                .build()
        )
        val dbV2 = openHelperV2.writableDatabase

        // Verify old data is completely preserved
        val stateCursor = dbV2.query("SELECT is_content_initialized, last_launch_timestamp FROM app_state WHERE id = 'app_state_singleton'")
        assertTrue(stateCursor.moveToFirst())
        assertEquals(1, stateCursor.getInt(0))
        assertEquals(12345L, stateCursor.getLong(1))
        stateCursor.close()

        val prefCursor = dbV2.query("SELECT preference_value FROM local_preferences WHERE preference_key = 'user_theme'")
        assertTrue(prefCursor.moveToFirst())
        assertEquals("DARK_MODE", prefCursor.getString(0))
        prefCursor.close()

        // Verify new hierarchy tables exist and accept records
        dbV2.execSQL(
            """
            INSERT INTO exams (id, name, short_name, description, is_active, sort_order, updated_at_timestamp)
            VALUES ('migrated_exam', 'Migrated Exam Name', 'MEX', 'Description', 1, 1, 99999)
            """.trimIndent()
        )
        val examCursor = dbV2.query("SELECT name, short_name FROM exams WHERE id = 'migrated_exam'")
        assertTrue(examCursor.moveToFirst())
        assertEquals("Migrated Exam Name", examCursor.getString(0))
        assertEquals("MEX", examCursor.getString(1))
        examCursor.close()

        dbV2.close()
        context.deleteDatabase(dbName)
    }
}
