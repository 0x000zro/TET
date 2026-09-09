package com.example.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.local.db.DatabaseContract

/**
 * Normalized Room Entity storing syllabus metadata associated with any hierarchy node
 * (Exam, Paper, Subject, Topic, or Subtopic).
 *
 * Designed with:
 * - Stable primary key (`node_id`) matching the corresponding hierarchy entity ID.
 * - Normalized separate storage so existing hierarchy tables remain clean and lightweight.
 * - Non-speculative fields: learning objective, short note, estimated minutes.
 * - Safe deletion behavior: No dangerous cascade deletion.
 */
@Entity(tableName = DatabaseContract.TABLE_SYLLABUS_METADATA)
data class SyllabusMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "node_id")
    val nodeId: String,

    @ColumnInfo(name = "learning_objective")
    val learningObjective: String = "",

    @ColumnInfo(name = "short_note")
    val shortNote: String = "",

    @ColumnInfo(name = "estimated_minutes")
    val estimatedMinutes: Int = 0,

    @ColumnInfo(name = "updated_at_timestamp")
    override val updatedAtTimestamp: Long = System.currentTimeMillis()
) : DatabaseContract.BaseEntity
