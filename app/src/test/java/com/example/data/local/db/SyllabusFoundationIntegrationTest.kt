package com.example.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.datasource.DefaultLocalEducationalDataSource
import com.example.data.local.db.entity.SyllabusMetadataEntity
import com.example.data.local.seed.GenericSyllabusFixture
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.SyllabusMetadata
import com.example.domain.model.SyllabusNodeType
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

/**
 * Integration tests for Syllabus Foundation:
 * - Room database v3 schema and SyllabusMetadataDao CRUD
 * - Migration from v2 to v3 preserving existing hierarchy
 * - Integration with EducationalRepository:
 *   - observeChildrenOfNode (deterministic sort order)
 *   - getSyllabusNode with metadata resolution
 *   - getSyllabusBreadcrumb ancestry path
 *   - getSyllabusTreeForExam complete tree building
 */
@RunWith(RobolectricTestRunner::class)
class SyllabusFoundationIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context
    private lateinit var repository: EducationalRepositoryImpl

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        DatabaseProvider.setTestInstance(database)
        repository = EducationalRepositoryImpl(
            localDataSource = DefaultLocalEducationalDataSource(databaseProvider = { database })
        )
    }

    @After
    fun tearDown() {
        database.close()
        DatabaseProvider.setTestInstance(null)
    }

    @Test
    fun databaseVersion_isVersionThree() {
        val version = database.openHelper.readableDatabase.version
        assertEquals(3, version)
    }

    @Test
    fun syllabusMetadataDao_performsInsertQueryDeleteCorrectly() = runBlocking {
        val dao = database.syllabusMetadataDao()
        val entity = SyllabusMetadataEntity(
            nodeId = "test_subtopic_01",
            learningObjective = "Understand cognitive operational shifts",
            shortNote = "Focus on conservation experiments",
            estimatedMinutes = 45
        )

        dao.insertOrUpdateMetadata(entity)

        val retrieved = dao.getMetadataByNodeId("test_subtopic_01")
        assertNotNull(retrieved)
        assertEquals("Understand cognitive operational shifts", retrieved?.learningObjective)
        assertEquals(45, retrieved?.estimatedMinutes)

        dao.deleteMetadataByNodeId("test_subtopic_01")
        val deleted = dao.getMetadataByNodeId("test_subtopic_01")
        assertNull(deleted)
    }

    @Test
    fun migrationFromV2toV3_preservesHierarchyAndCreatesSyllabusMetadataTable() {
        val dbName = "migration_test_syllabus.db"
        context.deleteDatabase(dbName)

        // 1. Create v2 database and insert existing hierarchy nodes
        val configV2 = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    // Create v1 tables
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `app_state` (`id` TEXT NOT NULL, `is_first_launch` INTEGER NOT NULL, `is_content_initialized` INTEGER NOT NULL, `last_known_content_version` INTEGER NOT NULL, `schema_version` INTEGER NOT NULL, `is_compatible` INTEGER NOT NULL, `last_launch_timestamp` INTEGER NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                    )
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `local_preferences` (`preference_key` TEXT NOT NULL, `preference_value` TEXT NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`preference_key`))"
                    )
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `content_sync_state` (`content_source` TEXT NOT NULL, `content_version` INTEGER NOT NULL, `last_successful_sync_timestamp` INTEGER, `sync_status` TEXT NOT NULL, `error_message` TEXT, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`content_source`))"
                    )
                    // Create v2 tables
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `exams` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `short_name` TEXT NOT NULL, `description` TEXT NOT NULL, `is_active` INTEGER NOT NULL, `sort_order` INTEGER NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                    )
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `papers` (`id` TEXT NOT NULL, `exam_id` TEXT NOT NULL, `name` TEXT NOT NULL, `short_name` TEXT NOT NULL, `description` TEXT NOT NULL, `is_active` INTEGER NOT NULL, `sort_order` INTEGER NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                    )
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `subjects` (`id` TEXT NOT NULL, `paper_id` TEXT NOT NULL, `name` TEXT NOT NULL, `short_name` TEXT NOT NULL, `description` TEXT NOT NULL, `is_active` INTEGER NOT NULL, `sort_order` INTEGER NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                    )
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `topics` (`id` TEXT NOT NULL, `subject_id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `is_active` INTEGER NOT NULL, `sort_order` INTEGER NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                    )
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `subtopics` (`id` TEXT NOT NULL, `topic_id` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `is_active` INTEGER NOT NULL, `sort_order` INTEGER NOT NULL, `updated_at_timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helperV2 = FrameworkSQLiteOpenHelperFactory().create(configV2)
        val v2Db = helperV2.writableDatabase
        v2Db.execSQL("INSERT INTO exams VALUES ('exam_pre_migration', 'Exam Pre', 'EP', 'Desc', 1, 1, 1000)")
        v2Db.execSQL("INSERT INTO papers VALUES ('paper_pre_migration', 'exam_pre_migration', 'Paper Pre', 'PP', 'Desc', 1, 1, 1000)")
        v2Db.close()

        // 2. Open with SQLite specifying MIGRATION_2_3 to version 3
        val helperV3 = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(dbName)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(3) {
                    override fun onCreate(db: SupportSQLiteDatabase) {}
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                        if (oldVersion == 2 && newVersion == 3) {
                            DatabaseMigrations.MIGRATION_2_3.migrate(db)
                        }
                    }
                })
                .build()
        )

        val migratedHelper = helperV3.writableDatabase
        assertEquals(3, migratedHelper.version)

        // 3. Verify pre-migration data intact
        val cursorExams = migratedHelper.query("SELECT name FROM exams WHERE id = 'exam_pre_migration'")
        assertTrue(cursorExams.moveToFirst())
        assertEquals("Exam Pre", cursorExams.getString(0))
        cursorExams.close()

        // 4. Verify new syllabus_metadata table is queryable and functional
        migratedHelper.execSQL(
            "INSERT INTO syllabus_metadata VALUES ('paper_pre_migration', 'Understand Paper Pre', 'Note', 30, 2000)"
        )
        val cursorMeta = migratedHelper.query("SELECT learning_objective FROM syllabus_metadata WHERE node_id = 'paper_pre_migration'")
        assertTrue(cursorMeta.moveToFirst())
        assertEquals("Understand Paper Pre", cursorMeta.getString(0))
        cursorMeta.close()

        migratedHelper.close()
        context.deleteDatabase(dbName)
    }

    @Test
    fun repository_observesChildrenInDeterministicSortOrder() = runBlocking {
        // Populate complete GenericSyllabusFixture
        repository.saveExam(GenericSyllabusFixture.fixtureExam)
        repository.savePaper(GenericSyllabusFixture.fixturePaper)
        repository.saveSubject(GenericSyllabusFixture.fixtureSubject)
        repository.saveTopic(GenericSyllabusFixture.fixtureTopic)
        repository.saveSubtopics(GenericSyllabusFixture.fixtureSubtopics)

        // Observe root children (Exams)
        val rootNodes = repository.observeChildrenOfNode(null, null).first()
        assertEquals(1, rootNodes.size)
        assertEquals(GenericSyllabusFixture.fixtureExam.id, rootNodes[0].id)
        assertEquals(SyllabusNodeType.EXAM, rootNodes[0].nodeType)

        // Observe Topic's children (Subtopics)
        val subtopicNodes = repository.observeChildrenOfNode(
            parentId = GenericSyllabusFixture.fixtureTopic.id,
            parentType = SyllabusNodeType.TOPIC
        ).first()

        assertEquals(2, subtopicNodes.size)
        // Deterministic ascending sort order: sortOrder 1 then 2
        assertEquals(GenericSyllabusFixture.fixtureSubtopics[0].id, subtopicNodes[0].id)
        assertEquals(1, subtopicNodes[0].sortOrder)
        assertEquals(GenericSyllabusFixture.fixtureSubtopics[1].id, subtopicNodes[1].id)
        assertEquals(2, subtopicNodes[1].sortOrder)
    }

    @Test
    fun repository_resolvesSyllabusNodeWithMetadata() = runBlocking {
        repository.saveExam(GenericSyllabusFixture.fixtureExam)
        repository.savePaper(GenericSyllabusFixture.fixturePaper)
        repository.saveSubject(GenericSyllabusFixture.fixtureSubject)
        repository.saveTopic(GenericSyllabusFixture.fixtureTopic)
        repository.saveSubtopic(GenericSyllabusFixture.fixtureSubtopics[0])
        repository.saveSyllabusMetadata(
            SyllabusMetadata(
                nodeId = GenericSyllabusFixture.fixtureSubtopics[0].id,
                learningObjective = "Master cognitive transition stages",
                shortNote = "Focus on conservation"
            )
        )

        val node = repository.getSyllabusNode(
            id = GenericSyllabusFixture.fixtureSubtopics[0].id,
            nodeType = SyllabusNodeType.SUBTOPIC
        )

        assertNotNull(node)
        assertEquals("Master cognitive transition stages", node?.learningObjective)
        assertEquals("Focus on conservation", node?.shortNote)
        assertEquals(GenericSyllabusFixture.fixtureTopic.id, node?.parentId)
    }

    @Test
    fun repository_computesCompleteSyllabusBreadcrumb() = runBlocking {
        repository.saveExam(GenericSyllabusFixture.fixtureExam)
        repository.savePaper(GenericSyllabusFixture.fixturePaper)
        repository.saveSubject(GenericSyllabusFixture.fixtureSubject)
        repository.saveTopic(GenericSyllabusFixture.fixtureTopic)
        repository.saveSubtopic(GenericSyllabusFixture.fixtureSubtopics[0])

        val breadcrumb = repository.getSyllabusBreadcrumb(
            id = GenericSyllabusFixture.fixtureSubtopics[0].id,
            nodeType = SyllabusNodeType.SUBTOPIC
        )

        assertEquals(5, breadcrumb.segments.size)
        assertEquals(GenericSyllabusFixture.fixtureExam.name, breadcrumb.exam?.title)
        assertEquals(GenericSyllabusFixture.fixturePaper.name, breadcrumb.paper?.title)
        assertEquals(GenericSyllabusFixture.fixtureSubject.name, breadcrumb.subject?.title)
        assertEquals(GenericSyllabusFixture.fixtureTopic.name, breadcrumb.topic?.title)
        assertEquals(GenericSyllabusFixture.fixtureSubtopics[0].name, breadcrumb.subtopic?.title)
        assertTrue(breadcrumb.formattedTrail.contains(" > "))
    }

    @Test
    fun repository_buildsCompleteSyllabusTree() = runBlocking {
        repository.saveExam(GenericSyllabusFixture.fixtureExam)
        repository.savePaper(GenericSyllabusFixture.fixturePaper)
        repository.saveSubject(GenericSyllabusFixture.fixtureSubject)
        repository.saveTopic(GenericSyllabusFixture.fixtureTopic)
        repository.saveSubtopics(GenericSyllabusFixture.fixtureSubtopics)

        val tree = repository.getSyllabusTreeForExam(GenericSyllabusFixture.fixtureExam.id)
        assertNotNull(tree)
        assertEquals(GenericSyllabusFixture.fixtureExam.id, tree?.node?.id)
        assertEquals(1, tree?.children?.size) // 1 Paper
        val paperNode = tree?.children?.get(0)
        assertEquals(1, paperNode?.children?.size) // 1 Subject
        val subjectNode = paperNode?.children?.get(0)
        assertEquals(1, subjectNode?.children?.size) // 1 Topic
        val topicNode = subjectNode?.children?.get(0)
        assertEquals(2, topicNode?.children?.size) // 2 Subtopics
        assertEquals(5, tree?.totalDescendantsCount) // 1 + 1 + 1 + 2 = 5 descendants
    }
}
