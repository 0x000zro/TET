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
     * Migration from Version 5 (practice attempts & local progress) to Version 6 (wrong questions / mistake tracker).
     *
     * Adds:
     * - wrong_questions: Dedicated table recording student mistakes from completed practice sessions.
     *   Foreign key links directly to questions(id) with CASCADE on delete.
     *   Indexes subtopic_id for fast filtering and (last_wrong_at, question_id) for deterministic ordering.
     *
     * Preserves:
     * - All existing foundation, educational hierarchy, syllabus metadata, questions, and practice attempt records.
     */
    val MIGRATION_5_6: Migration = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `wrong_questions` (
                    `question_id` TEXT NOT NULL,
                    `subtopic_id` TEXT NOT NULL,
                    `first_wrong_at` INTEGER NOT NULL,
                    `last_wrong_at` INTEGER NOT NULL,
                    `wrong_count` INTEGER NOT NULL,
                    `last_attempt_id` TEXT,
                    `updated_at_timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`question_id`),
                    FOREIGN KEY(`question_id`) REFERENCES `questions`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_wrong_questions_subtopic_id` ON `wrong_questions` (`subtopic_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_wrong_questions_last_wrong_at_ordering` ON `wrong_questions` (`last_wrong_at`, `question_id`)")
        }
    }

    /**
     * Migration from Version 6 (wrong questions) to Version 7 (bookmarked questions).
     *
     * Adds:
     * - bookmarked_questions: Dedicated table recording student saved bookmarks for questions.
     *   Foreign key links directly to questions(id) with CASCADE on delete.
     *   Indexes subtopic_id for fast filtering and (bookmarked_at, question_id) for deterministic ordering.
     *
     * Preserves:
     * - All existing foundation, educational hierarchy, syllabus metadata, questions, practice attempts, and wrong questions.
     */
    val MIGRATION_6_7: Migration = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `bookmarked_questions` (
                    `question_id` TEXT NOT NULL,
                    `subtopic_id` TEXT NOT NULL,
                    `bookmarked_at` INTEGER NOT NULL,
                    `updated_at_timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`question_id`),
                    FOREIGN KEY(`question_id`) REFERENCES `questions`(`id`) ON UPDATE CASCADE ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarked_questions_subtopic_id` ON `bookmarked_questions` (`subtopic_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_bookmarked_questions_bookmarked_at_ordering` ON `bookmarked_questions` (`bookmarked_at`, `question_id`)")
        }
    }

    /**
     * Migration from Version 7 (bookmarked questions) to Version 8 (previous year questions).
     *
     * Adds:
     * - previous_year_questions: Dedicated table recording Previous Year Question (PYQ) metadata.
     *   Foreign key links directly to questions(id) with CASCADE on delete, exams(id) with RESTRICT,
     *   and papers(id) with RESTRICT.
     *   Indexes question_id, exam_id, paper_id, year, (paper_id, year), and unique (question_id, year, session).
     *
     * Preserves:
     * - All existing foundation, educational hierarchy, syllabus metadata, questions, practice attempts, wrong questions, and bookmarks.
     */
    val MIGRATION_7_8: Migration = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `previous_year_questions` (
                    `id` TEXT NOT NULL,
                    `question_id` TEXT NOT NULL,
                    `exam_id` TEXT NOT NULL,
                    `paper_id` TEXT NOT NULL,
                    `year` INTEGER NOT NULL,
                    `session` TEXT,
                    `source` TEXT,
                    `verification_status` TEXT NOT NULL,
                    `sort_order` INTEGER NOT NULL,
                    `updated_at_timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`question_id`) REFERENCES `questions`(`id`) ON UPDATE CASCADE ON DELETE CASCADE,
                    FOREIGN KEY(`exam_id`) REFERENCES `exams`(`id`) ON UPDATE CASCADE ON DELETE RESTRICT,
                    FOREIGN KEY(`paper_id`) REFERENCES `papers`(`id`) ON UPDATE CASCADE ON DELETE RESTRICT
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pyq_question_id` ON `previous_year_questions` (`question_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pyq_exam_id` ON `previous_year_questions` (`exam_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pyq_paper_id` ON `previous_year_questions` (`paper_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pyq_year` ON `previous_year_questions` (`year`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pyq_paper_id_year` ON `previous_year_questions` (`paper_id`, `year`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_pyq_unique_question_year_session` ON `previous_year_questions` (`question_id`, `year`, `session`)")
        }
    }

    /**
     * Array of all defined database migrations.
     */
    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5,
        MIGRATION_5_6,
        MIGRATION_6_7,
        MIGRATION_7_8
    )
}
