package com.example.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.local.db.DatabaseContract

/**
 * Room Entity storing non-sensitive device-local preferences.
 * Sensitive data such as tokens or passwords MUST NEVER be stored here.
 */
@Entity(tableName = DatabaseContract.TABLE_LOCAL_PREFERENCES)
data class LocalPreferenceEntity(
    @PrimaryKey
    @ColumnInfo(name = "preference_key")
    val preferenceKey: String,

    @ColumnInfo(name = "preference_value")
    val preferenceValue: String,

    @ColumnInfo(name = "updated_at_timestamp")
    override val updatedAtTimestamp: Long = System.currentTimeMillis()
) : DatabaseContract.BaseEntity
