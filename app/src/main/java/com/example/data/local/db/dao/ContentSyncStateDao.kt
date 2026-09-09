package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.ContentSyncStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for content synchronization tracking.
 */
@Dao
interface ContentSyncStateDao : DatabaseContract.BaseDao {

    @Query("SELECT * FROM content_sync_state ORDER BY content_source ASC")
    fun getAllSyncStatesFlow(): Flow<List<ContentSyncStateEntity>>

    @Query("SELECT * FROM content_sync_state WHERE content_source = :source LIMIT 1")
    fun getSyncStateFlow(source: String): Flow<ContentSyncStateEntity?>

    @Query("SELECT * FROM content_sync_state WHERE content_source = :source LIMIT 1")
    suspend fun getSyncState(source: String): ContentSyncStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSyncState(state: ContentSyncStateEntity)

    @Query("DELETE FROM content_sync_state WHERE content_source = :source")
    suspend fun deleteSyncState(source: String)

    @Query("DELETE FROM content_sync_state")
    suspend fun clearAllSyncStates()
}
