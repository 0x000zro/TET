package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.LocalPreferenceEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local non-sensitive device preferences.
 */
@Dao
interface LocalPreferenceDao : DatabaseContract.BaseDao {

    @Query("SELECT * FROM local_preferences WHERE preference_key = :key LIMIT 1")
    fun getPreferenceFlow(key: String): Flow<LocalPreferenceEntity?>

    @Query("SELECT preference_value FROM local_preferences WHERE preference_key = :key LIMIT 1")
    suspend fun getPreferenceValue(key: String): String?

    @Query("SELECT * FROM local_preferences ORDER BY preference_key ASC")
    fun getAllPreferencesFlow(): Flow<List<LocalPreferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreference(preference: LocalPreferenceEntity)

    @Query("DELETE FROM local_preferences WHERE preference_key = :key")
    suspend fun deletePreference(key: String)

    @Query("DELETE FROM local_preferences")
    suspend fun clearAllPreferences()
}
