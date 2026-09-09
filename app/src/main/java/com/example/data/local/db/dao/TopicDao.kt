package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Topics under Subjects.
 */
@Dao
interface TopicDao : DatabaseContract.BaseDao {

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    fun getTopicByIdFlow(id: String): Flow<TopicEntity?>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    suspend fun getTopicById(id: String): TopicEntity?

    @Query("SELECT * FROM topics WHERE subject_id = :subjectId ORDER BY sort_order ASC, name ASC")
    fun getTopicsBySubjectIdFlow(subjectId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE subject_id = :subjectId AND is_active = 1 ORDER BY sort_order ASC, name ASC")
    fun getActiveTopicsBySubjectIdFlow(subjectId: String): Flow<List<TopicEntity>>

    @Query("SELECT COUNT(*) FROM topics WHERE subject_id = :subjectId")
    suspend fun getTopicCountBySubjectId(subjectId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTopic(topic: TopicEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTopics(topics: List<TopicEntity>)

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :id")
    suspend fun deleteTopicById(id: String)

    @Query("DELETE FROM topics WHERE subject_id = :subjectId")
    suspend fun deleteTopicsBySubjectId(subjectId: String)
}
