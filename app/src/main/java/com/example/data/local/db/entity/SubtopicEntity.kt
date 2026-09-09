package com.example.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.local.db.DatabaseContract

/**
 * Room Entity storing a Subtopic under a Topic.
 *
 * Foreign key references TopicEntity with RESTRICT on delete.
 */
@Entity(
    tableName = DatabaseContract.TABLE_SUBTOPICS,
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topic_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["topic_id"])
    ]
)
data class SubtopicEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "topic_id")
    val topicId: String,

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
