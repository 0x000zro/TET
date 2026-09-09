package com.example.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Registry of Room schema migrations.
 *
 * NOTE: Destructive migrations (fallbackToDestructiveMigration) are strictly avoided
 * to prevent loss of local student data, cache, and preferences across updates.
 * Explicit SQLite migration scripts are defined and tested here.
 */
object DatabaseMigrations {

    /**
     * Migration from Version 1 (foundation schema) to Version 2 (educational hierarchy schema).
     *
     * Adds:
     * - exams: Top level exam table
     * - papers: Papers under exams with foreign key (exam_id -> exams.id) and index
     * - subjects: Subjects under papers with foreign key (paper_id -> papers.id) and index
     * - topics: Topics under subjects with foreign key (subject_id -> subjects.id) and index
     * - subtopics: Subtopics under topics with foreign key (topic_id -> topics.id) and index
     *
     * Preserves:
     * - app_state
     * - local_preferences
     * - content_sync_state
     */
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Create exams table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `exams` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `short_name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `is_active` INTEGER NOT NULL,
                    `sort_order` INTEGER NOT NULL,
                    `updated_at_timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )

            // 2. Create papers table with Foreign Key to exams
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `papers` (
                    `id` TEXT NOT NULL,
                    `exam_id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `short_name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `is_active` INTEGER NOT NULL,
                    `sort_order` INTEGER NOT NULL,
                    `updated_at_timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`exam_id`) REFERENCES `exams`(`id`) ON UPDATE CASCADE ON DELETE RESTRICT
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_papers_exam_id` ON `papers` (`exam_id`)")

            // 3. Create subjects table with Foreign Key to papers
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `subjects` (
                    `id` TEXT NOT NULL,
                    `paper_id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `short_name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `is_active` INTEGER NOT NULL,
                    `sort_order` INTEGER NOT NULL,
                    `updated_at_timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`paper_id`) REFERENCES `papers`(`id`) ON UPDATE CASCADE ON DELETE RESTRICT
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_subjects_paper_id` ON `subjects` (`paper_id`)")

            // 4. Create topics table with Foreign Key to subjects
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `topics` (
                    `id` TEXT NOT NULL,
                    `subject_id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `is_active` INTEGER NOT NULL,
                    `sort_order` INTEGER NOT NULL,
                    `updated_at_timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`subject_id`) REFERENCES `subjects`(`id`) ON UPDATE CASCADE ON DELETE RESTRICT
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_topics_subject_id` ON `topics` (`subject_id`)")

            // 5. Create subtopics table with Foreign Key to topics
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `subtopics` (
                    `id` TEXT NOT NULL,
                    `topic_id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `is_active` INTEGER NOT NULL,
                    `sort_order` INTEGER NOT NULL,
                    `updated_at_timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`topic_id`) REFERENCES `topics`(`id`) ON UPDATE CASCADE ON DELETE RESTRICT
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_subtopics_topic_id` ON `subtopics` (`topic_id`)")
        }
    }

    /**
     * Migration from Version 2 (educational hierarchy schema) to Version 3 (syllabus metadata schema).
     *
     * Adds:
     * - syllabus_metadata: Normalized metadata table for learning objectives, short notes,
     *   and estimated study durations for any syllabus node (node_id PK).
     *
     * Preserves:
     * - app_state
     * - local_preferences
     * - content_sync_state
     * - exams
     * - papers
     * - subjects
     * - topics
     * - subtopics
     */
    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `syllabus_metadata` (
                    `node_id` TEXT NOT NULL,
                    `learning_objective` TEXT NOT NULL,
                    `short_note` TEXT NOT NULL,
                    `estimated_minutes` INTEGER NOT NULL,
                    `updated_at_timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`node_id`)
                )
                """.trimIndent()
            )
        }
    }

    /**
     * Array of all defined database migrations.
     */
    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3
    )
}
