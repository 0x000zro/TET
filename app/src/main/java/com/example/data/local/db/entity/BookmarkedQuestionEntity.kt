package com.example.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.local.db.DatabaseContract

/**
 * Room entity representing a student's bookmark on a question (Step 14).
 *
 * Requirements:
 * - [questionId] as the stable identity (PrimaryKey).
 * - Foreign key referencing [QuestionEntity.id] with CASCADE on delete.
 *   Note: Removing a [BookmarkedQuestionEntity] does NOT delete or affect the underlying [QuestionEntity].
 * - [subtopicId] reference indexed for fast subtopic filtering.
 * - [bookmarkedAt] and [questionId] compound index for deterministic ordering: `bookmarkedAt DESC, questionId ASC`.
 * - Pure bookmark metadata: does NOT duplicate question text, options, answer key, or explanation.
 */
@Entity(
    tableName = DatabaseContract.TABLE_BOOKMARKED_QUESTIONS,
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
        Index(value = ["subtopic_id"], name = "index_bookmarked_questions_subtopic_id"),
        Index(value = ["bookmarked_at", "question_id"], name = "index_bookmarked_questions_bookmarked_at_ordering")
    ]
)
data class BookmarkedQuestionEntity(
    @PrimaryKey
    @ColumnInfo(name = "question_id")
    val questionId: String,

    @ColumnInfo(name = "subtopic_id")
    val subtopicId: String,

    @ColumnInfo(name = "bookmarked_at")
    val bookmarkedAt: Long,

    @ColumnInfo(name = "updated_at_timestamp")
    override val updatedAtTimestamp: Long = System.currentTimeMillis()
) : DatabaseContract.BaseEntity
