package com.example.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.local.db.DatabaseContract

/**
 * Room Entity storing a Topic under a Subject.
 *
 * Foreign key references SubjectEntity with RESTRICT on delete.
 */
@Entity(
    tableName = DatabaseContract.TABLE_TOPICS,
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subject_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["subject_id"])
    ]
)
data class TopicEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "subject_id")
    val subjectId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "updated_at_timestamp")
    override val updatedAtTimestamp: Long = System.currentTimeMillis()
) : DatabaseContract.BaseEntity
