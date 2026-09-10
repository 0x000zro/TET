package com.example.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.local.db.DatabaseContract

/**
 * Room entity representing a student's mistake record for a question.
 *
 * Requirements (Step 13):
 * - [questionId] as the stable identity (PrimaryKey).
 * - Foreign key referencing [QuestionEntity.id] with CASCADE on delete.
 *   Note: Removing a [WrongQuestionEntity] does NOT delete or affect the underlying [QuestionEntity].
 * - [subtopicId] reference indexed for fast subtopic filtering.
 * - [lastWrongAt] and [questionId] compound index for deterministic ordering: `lastWrongAt DESC, questionId ASC`.
 * - Pure mistake metadata: does NOT duplicate question text, options, answer key, or explanation.
 */
@Entity(
    tableName = DatabaseContract.TABLE_WRONG_QUESTIONS,
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["subtopic_id"], name = "index_wrong_questions_subtopic_id"),
        Index(value = ["last_wrong_at", "question_id"], name = "index_wrong_questions_last_wrong_at_ordering")
    ]
)
data class WrongQuestionEntity(
    @PrimaryKey
    @ColumnInfo(name = "question_id")
    val questionId: String,

    @ColumnInfo(name = "subtopic_id")
    val subtopicId: String,

    @ColumnInfo(name = "first_wrong_at")
    val firstWrongAt: Long,

    @ColumnInfo(name = "last_wrong_at")
    val lastWrongAt: Long,

    @ColumnInfo(name = "wrong_count")
    val wrongCount: Int,

    @ColumnInfo(name = "last_attempt_id")
    val lastAttemptId: String? = null,

    @ColumnInfo(name = "updated_at_timestamp")
    override val updatedAtTimestamp: Long = System.currentTimeMillis()
) : DatabaseContract.BaseEntity
