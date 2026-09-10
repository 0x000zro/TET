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
     * Migration from Version 3 (syllabus metadata) to Version 4 (question foundation).
     *
     * Adds:
     * - questions: Canonical question entities linked directly to subtopics with RESTRICT on delete.
     * - question_options: Normalized answer options linked directly to questions with CASCADE on delete.
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
     * - syllabus_metadata
     */
    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // 1. Create questions table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `questions` (
                    `id` TEXT NOT NULL,
                    `subtopic_id` TEXT NOT NULL,
                    `question_text` TEXT NOT NULL,
                    `question_type` TEXT NOT NULL,
                    `difficulty` TEXT NOT NULL,
                    `explanation` TEXT NOT NULL,
                    `is_active` INTEGER NOT NULL,
                    `sort_order` INTEGER NOT NULL,
                    `updated_at_timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`subtopic_id`) REFERENCES `subtopics`(`id`) ON UPDATE CASCADE ON DELETE RESTRICT
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_questions_subtopic_id` ON `questions` (`subtopic_id`)")

            // 2. Create question_options table
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `question_options` (
                    `id` TEXT NOT NULL,
                    `question_id` TEXT NOT NULL,
                    `option_text` TEXT NOT NULL,
                    `sort_order` INTEGER NOT NULL,
                    `is_correct` INTEGER NOT NULL,
                    `updated_at_timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`question_id`) REFERENCES `questions`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_question_options_question_id` ON `question_options` (`question_id`)")
        }
    }

    /**
     * Migration from Version 4 (question foundation) to Version 5 (practice attempts & local progress).
     *
     * Adds:
     * - practice_attempts: Dedicated table recording student practice completions.
     *   Indexes subtopic_id for fast subtopic history lookups and completed_at for time-ordered sorting.
     *
     * Preserves:
     * - All existing foundation, educational hierarchy, syllabus metadata, and question tables.
     */
    val MIGRATION_4_5: Migration = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `practice_attempts` (
                    `id` TEXT NOT NULL,
                    `subtopic_id` TEXT NOT NULL,
                    `total_questions` INTEGER NOT NULL,
                    `answered_questions` INTEGER NOT NULL,
                    `correct_answers` INTEGER NOT NULL,
                    `incorrect_answers` INTEGER NOT NULL,
                    `percentage_score` REAL NOT NULL,
                    `started_at` INTEGER NOT NULL,
                    `completed_at` INTEGER NOT NULL,
                    `updated_at_timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_practice_attempts_subtopic_id` ON `practice_attempts` (`subtopic_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_practice_attempts_completed_at` ON `practice_attempts` (`completed_at`)")
        }
    }

    /**
     * Array of all defined database migrations.
     */
    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5
    )
}
