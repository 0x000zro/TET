package com.example.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.local.db.DatabaseContract

/**
 * Room Entity storing answer options for questions.
 *
 * Foreign key references QuestionEntity with ForeignKey.CASCADE so options
 * are safely removed when their parent question is deleted.
 */
@Entity(
    tableName = DatabaseContract.TABLE_QUESTION_OPTIONS,
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["question_id"])
    ]
)
data class QuestionOptionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "question_id")
    val questionId: String,

    @ColumnInfo(name = "option_text")
    val optionText: String,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "is_correct")
    val isCorrect: Boolean = false,

    @ColumnInfo(name = "updated_at_timestamp")
    override val updatedAtTimestamp: Long = System.currentTimeMillis()
) : DatabaseContract.BaseEntity
