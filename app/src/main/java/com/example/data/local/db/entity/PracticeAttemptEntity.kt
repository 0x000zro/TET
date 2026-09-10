package com.example.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.local.db.DatabaseContract

/**
 * Dedicated Room Entity storing completed student practice attempts (Step 11).
 *
 * Designed with:
 * - Stable primary key [id] (UUID).
 * - Indexed [subtopic_id] for performant history lookup by subtopic.
 * - Indexed [completed_at] for deterministic time-ordered sorting (newest first).
 * - Only stores aggregate metrics; strictly avoids storing question text or answer keys.
 * - Non-destructive schema migration from version 4 to 5.
 */
@Entity(
    tableName = DatabaseContract.TABLE_PRACTICE_ATTEMPTS,
    indices = [
        Index(value = ["subtopic_id"]),
        Index(value = ["completed_at"])
    ]
)
data class PracticeAttemptEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "subtopic_id")
    val subtopicId: String,

    @ColumnInfo(name = "total_questions")
    val totalQuestions: Int,

    @ColumnInfo(name = "answered_questions")
    val answeredQuestions: Int,

    @ColumnInfo(name = "correct_answers")
    val correctAnswers: Int,

    @ColumnInfo(name = "incorrect_answers")
    val incorrectAnswers: Int,

    @ColumnInfo(name = "percentage_score")
    val percentageScore: Double,

    @ColumnInfo(name = "started_at")
    val startedAt: Long,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long,

    @ColumnInfo(name = "updated_at_timestamp")
    override val updatedAtTimestamp: Long = System.currentTimeMillis()
) : DatabaseContract.BaseEntity
