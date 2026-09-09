package com.example.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.local.db.DatabaseContract

/**
 * Room Entity storing a Paper under an Exam.
 *
 * Foreign key references ExamEntity with RESTRICT on delete/update to prevent accidental corruption.
 */
@Entity(
    tableName = DatabaseContract.TABLE_PAPERS,
    foreignKeys = [
        ForeignKey(
            entity = ExamEntity::class,
            parentColumns = ["id"],
            childColumns = ["exam_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["exam_id"])
    ]
)
data class PaperEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "exam_id")
    val examId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "short_name")
    val shortName: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "updated_at_timestamp")
    override val updatedAtTimestamp: Long = System.currentTimeMillis()
) : DatabaseContract.BaseEntity
