package com.example.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.db.dao.AppStateDao
import com.example.data.local.db.dao.ContentSyncStateDao
import com.example.data.local.db.dao.ExamDao
import com.example.data.local.db.dao.LocalPreferenceDao
import com.example.data.local.db.dao.PaperDao
import com.example.data.local.db.dao.PracticeAttemptDao
import com.example.data.local.db.dao.QuestionDao
import com.example.data.local.db.dao.QuestionOptionDao
import com.example.data.local.db.dao.SubjectDao
import com.example.data.local.db.dao.SubtopicDao
import com.example.data.local.db.dao.SyllabusMetadataDao
import com.example.data.local.db.dao.TopicDao
import com.example.data.local.db.entity.AppStateEntity
import com.example.data.local.db.entity.ContentSyncStateEntity
import com.example.data.local.db.entity.ExamEntity
import com.example.data.local.db.entity.LocalPreferenceEntity
import com.example.data.local.db.entity.PaperEntity
import com.example.data.local.db.entity.PracticeAttemptEntity
import com.example.data.local.db.entity.QuestionEntity
import com.example.data.local.db.entity.QuestionOptionEntity
import com.example.data.local.db.entity.SubjectEntity
import com.example.data.local.db.entity.SubtopicEntity
import com.example.data.local.db.entity.SyllabusMetadataEntity
import com.example.data.local.db.entity.TopicEntity

/**
 * The central Room database for the application.
 *
 * Designed to be:
 * - Singleton: Only one persistent database instance per application lifecycle.
 * - Thread-safe: Double-checked locking pattern with `@Volatile`.
 * - Extensible: Ready to include future educational entity modules in later steps.
 * - Non-destructive: Explicit migrations are enforced without fallbackToDestructiveMigration.
 */
@Database(
    entities = [
        // Foundation Entities (v1)
        AppStateEntity::class,
        LocalPreferenceEntity::class,
        ContentSyncStateEntity::class,
        // Educational Hierarchy Entities (v2)
        ExamEntity::class,
        PaperEntity::class,
        SubjectEntity::class,
        TopicEntity::class,
        SubtopicEntity::class,
        // Syllabus Metadata Entity (v3)
        SyllabusMetadataEntity::class,
        // Question Foundation Entities (v4)
        QuestionEntity::class,
        QuestionOptionEntity::class,
        // Practice Attempts Entity (v5)
        PracticeAttemptEntity::class
    ],
    version = DatabaseContract.DATABASE_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Foundation DAOs
    abstract fun appStateDao(): AppStateDao
    abstract fun localPreferenceDao(): LocalPreferenceDao
    abstract fun contentSyncStateDao(): ContentSyncStateDao

    // Educational Hierarchy DAOs
    abstract fun examDao(): ExamDao
    abstract fun paperDao(): PaperDao
    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao
    abstract fun subtopicDao(): SubtopicDao

    // Syllabus Metadata DAO
    abstract fun syllabusMetadataDao(): SyllabusMetadataDao

    // Question Foundation DAOs
    abstract fun questionDao(): QuestionDao
    abstract fun questionOptionDao(): QuestionOptionDao

    // Practice Attempts DAO (Step 11)
    abstract fun practiceAttemptDao(): PracticeAttemptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(appContext: Context): AppDatabase {
            return Room.databaseBuilder(
                appContext,
                AppDatabase::class.java,
                DatabaseContract.DATABASE_NAME
            )
                .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
                .build()
        }

        /**
         * Facilitates local JVM unit and integration tests with in-memory database instances.
         */
        internal fun setTestInstance(testDatabase: AppDatabase?) {
            INSTANCE = testDatabase
        }
    }
}
