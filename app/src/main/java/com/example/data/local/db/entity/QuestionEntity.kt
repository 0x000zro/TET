package com.example.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.local.db.DatabaseContract

/**
 * Room Entity storing educational questions.
 *
 * Canonical syllabus hierarchy relationship:
 * Exam -> Paper -> Subject -> Topic -> Subtopic -> Question.
 *
 * Foreign key references SubtopicEntity with ForeignKey.RESTRICT to prevent
 * accidental cascade deletion of the question bank when syllabus nodes are pruned.
 */
@Entity(
    tableName = DatabaseContract.TABLE_QUESTIONS,
    foreignKeys = [
        ForeignKey(
            entity = SubtopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["subtopic_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["subtopic_id"])
    ]
)
data class QuestionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "subtopic_id")
    val subtopicId: String,

    @ColumnInfo(name = "question_text")
    val questionText: String,

    @ColumnInfo(name = "question_type")
    val questionType: String,

    @ColumnInfo(name = "difficulty")
    val difficulty: String,

    @ColumnInfo(name = "explanation")
    val explanation: String = "",

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "updated_at_timestamp")
    override val updatedAtTimestamp: Long = System.currentTimeMillis()
) : DatabaseContract.BaseEntity
