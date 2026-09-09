package com.example.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.local.db.DatabaseContract

/**
 * Room Entity storing application-level state.
 * Represents single-row state for local initialization, versioning, and lifecycle verification.
 */
@Entity(tableName = DatabaseContract.TABLE_APP_STATE)
data class AppStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = DEFAULT_ID,

    @ColumnInfo(name = "is_first_launch")
    val isFirstLaunch: Boolean = true,

    @ColumnInfo(name = "is_content_initialized")
    val isContentInitialized: Boolean = false,

    @ColumnInfo(name = "last_known_content_version")
    val lastKnownContentVersion: Int = 1,

    @ColumnInfo(name = "schema_version")
    val schemaVersion: Int = DatabaseContract.DATABASE_VERSION,

    @ColumnInfo(name = "is_compatible")
    val isCompatible: Boolean = true,

    @ColumnInfo(name = "last_launch_timestamp")
    val lastLaunchTimestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at_timestamp")
    override val updatedAtTimestamp: Long = System.currentTimeMillis()
) : DatabaseContract.BaseEntity {
    companion object {
        const val DEFAULT_ID = "app_state_singleton"
    }
}
