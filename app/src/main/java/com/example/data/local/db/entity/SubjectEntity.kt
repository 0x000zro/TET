package com.example.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.local.db.DatabaseContract

/**
 * Room Entity storing a Subject under a Paper.
 *
 * Foreign key references PaperEntity with RESTRICT on delete to prevent accidental cascade deletion.
 */
@Entity(
    tableName = DatabaseContract.TABLE_SUBJECTS,
    foreignKeys = [
        ForeignKey(
            entity = PaperEntity::class,
            parentColumns = ["id"],
            childColumns = ["paper_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["paper_id"])
    ]
)
data class SubjectEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "paper_id")
    val paperId: String,

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
