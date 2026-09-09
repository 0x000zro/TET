package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.SyllabusMetadataEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Syllabus Metadata.
 */
@Dao
interface SyllabusMetadataDao : DatabaseContract.BaseDao {

    @Query("SELECT * FROM syllabus_metadata WHERE node_id = :nodeId LIMIT 1")
    fun getMetadataByNodeIdFlow(nodeId: String): Flow<SyllabusMetadataEntity?>

    @Query("SELECT * FROM syllabus_metadata WHERE node_id = :nodeId LIMIT 1")
    suspend fun getMetadataByNodeId(nodeId: String): SyllabusMetadataEntity?

    @Query("SELECT * FROM syllabus_metadata WHERE node_id IN (:nodeIds)")
    suspend fun getMetadataByNodeIds(nodeIds: List<String>): List<SyllabusMetadataEntity>

    @Query("SELECT * FROM syllabus_metadata WHERE node_id IN (:nodeIds)")
    fun getMetadataByNodeIdsFlow(nodeIds: List<String>): Flow<List<SyllabusMetadataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMetadata(metadata: SyllabusMetadataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMetadataList(metadataList: List<SyllabusMetadataEntity>)

    @Update
    suspend fun updateMetadata(metadata: SyllabusMetadataEntity)

    @Query("DELETE FROM syllabus_metadata WHERE node_id = :nodeId")
    suspend fun deleteMetadataByNodeId(nodeId: String)

    @Query("DELETE FROM syllabus_metadata")
    suspend fun clearAllMetadata()
}
