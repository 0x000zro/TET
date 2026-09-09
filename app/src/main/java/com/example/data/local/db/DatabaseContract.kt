package com.example.data.local.db

/**
 * Architectural blueprint for the offline-first local database.
 * This contract establishes Room database readiness without prematurely
 * instantiating entities or DAO tables prior to Step 2 feature specifications.
 */
object DatabaseContract {
    const val DATABASE_NAME = "eduprep_local_offline.db"
    const val DATABASE_VERSION = 3

    // Foundation Table Names
    const val TABLE_APP_STATE = "app_state"
    const val TABLE_LOCAL_PREFERENCES = "local_preferences"
    const val TABLE_CONTENT_SYNC_STATE = "content_sync_state"

    // Educational Hierarchy Table Names
    const val TABLE_EXAMS = "exams"
    const val TABLE_PAPERS = "papers"
    const val TABLE_SUBJECTS = "subjects"
    const val TABLE_TOPICS = "topics"
    const val TABLE_SUBTOPICS = "subtopics"

    // Syllabus Metadata Table Name
    const val TABLE_SYLLABUS_METADATA = "syllabus_metadata"

    /**
     * Marker interface for Room DAOs
     */
    interface BaseDao

    /**
     * Marker interface for Room Entities
     */
    interface BaseEntity {
        val updatedAtTimestamp: Long
    }
}
