package com.example.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.local.db.DatabaseContract

/**
 * Room Entity storing synchronization state for educational content sources.
 * Keeps track of content versions, sync timestamps, and status without mixing with AppState or StudentProgress.
 */
@Entity(tableName = DatabaseContract.TABLE_CONTENT_SYNC_STATE)
data class ContentSyncStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "content_source")
    val contentSource: String,

    @ColumnInfo(name = "content_version")
    val contentVersion: Int = 1,

    @ColumnInfo(name = "last_successful_sync_timestamp")
    val lastSuccessfulSyncTimestamp: Long? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "IDLE",

    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,

    @ColumnInfo(name = "updated_at_timestamp")
    override val updatedAtTimestamp: Long = System.currentTimeMillis()
) : DatabaseContract.BaseEntity
