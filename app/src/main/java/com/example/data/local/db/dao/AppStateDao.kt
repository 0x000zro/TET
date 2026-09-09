package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.AppStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for application-level state.
 */
@Dao
interface AppStateDao : DatabaseContract.BaseDao {

    @Query("SELECT * FROM app_state WHERE id = :id LIMIT 1")
    fun getAppStateFlow(id: String = AppStateEntity.DEFAULT_ID): Flow<AppStateEntity?>

    @Query("SELECT * FROM app_state WHERE id = :id LIMIT 1")
    suspend fun getAppState(id: String = AppStateEntity.DEFAULT_ID): AppStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAppState(state: AppStateEntity)

    @Query("DELETE FROM app_state WHERE id = :id")
    suspend fun deleteAppState(id: String = AppStateEntity.DEFAULT_ID)
}
