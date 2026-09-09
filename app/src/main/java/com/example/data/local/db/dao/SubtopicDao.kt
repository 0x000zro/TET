package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.SubtopicEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Subtopics under Topics.
 */
@Dao
interface SubtopicDao : DatabaseContract.BaseDao {

    @Query("SELECT * FROM subtopics WHERE id = :id LIMIT 1")
    fun getSubtopicByIdFlow(id: String): Flow<SubtopicEntity?>

    @Query("SELECT * FROM subtopics WHERE id = :id LIMIT 1")
    suspend fun getSubtopicById(id: String): SubtopicEntity?

    @Query("SELECT * FROM subtopics WHERE topic_id = :topicId ORDER BY sort_order ASC, name ASC")
    fun getSubtopicsByTopicIdFlow(topicId: String): Flow<List<SubtopicEntity>>

    @Query("SELECT * FROM subtopics WHERE topic_id = :topicId AND is_active = 1 ORDER BY sort_order ASC, name ASC")
    fun getActiveSubtopicsByTopicIdFlow(topicId: String): Flow<List<SubtopicEntity>>

    @Query("SELECT COUNT(*) FROM subtopics WHERE topic_id = :topicId")
    suspend fun getSubtopicCountByTopicId(topicId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSubtopic(subtopic: SubtopicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSubtopics(subtopics: List<SubtopicEntity>)

    @Update
    suspend fun updateSubtopic(subtopic: SubtopicEntity)

    @Query("DELETE FROM subtopics WHERE id = :id")
    suspend fun deleteSubtopicById(id: String)

    @Query("DELETE FROM subtopics WHERE topic_id = :topicId")
    suspend fun deleteSubtopicsByTopicId(topicId: String)
}
