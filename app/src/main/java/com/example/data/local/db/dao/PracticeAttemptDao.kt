package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.PracticeAttemptEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for completed Practice Attempts (Step 11).
 *
 * Guarantees:
 * - Deterministic ordering: newest completed attempt first (`completed_at DESC, id DESC`).
 * - Idempotent insertion: Uses [OnConflictStrategy.REPLACE] or [OnConflictStrategy.IGNORE] to prevent duplicate attempt records.
 * - Reactive flow observation for subtopic history and global recent history.
 */
@Dao
interface PracticeAttemptDao : DatabaseContract.BaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttempt(attempt: PracticeAttemptEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAttempt(attempt: PracticeAttemptEntity): Long

    @Query("SELECT * FROM practice_attempts WHERE id = :id LIMIT 1")
    suspend fun getAttemptById(id: String): PracticeAttemptEntity?

    @Query("SELECT * FROM practice_attempts WHERE id = :id LIMIT 1")
    fun getAttemptByIdFlow(id: String): Flow<PracticeAttemptEntity?>

    @Query("SELECT * FROM practice_attempts WHERE subtopic_id = :subtopicId ORDER BY completed_at DESC, id DESC")
    suspend fun getAttemptsBySubtopicId(subtopicId: String): List<PracticeAttemptEntity>

    @Query("SELECT * FROM practice_attempts WHERE subtopic_id = :subtopicId ORDER BY completed_at DESC, id DESC")
    fun getAttemptsBySubtopicIdFlow(subtopicId: String): Flow<List<PracticeAttemptEntity>>

    @Query("SELECT * FROM practice_attempts ORDER BY completed_at DESC, id DESC LIMIT :limit")
    fun getRecentAttemptsFlow(limit: Int = 20): Flow<List<PracticeAttemptEntity>>

    @Query("SELECT * FROM practice_attempts ORDER BY completed_at DESC, id DESC")
    fun getAllAttemptsFlow(): Flow<List<PracticeAttemptEntity>>

    @Query("SELECT * FROM practice_attempts ORDER BY completed_at DESC, id DESC")
    suspend fun getAllAttempts(): List<PracticeAttemptEntity>

    @Query("SELECT COUNT(*) FROM practice_attempts WHERE subtopic_id = :subtopicId")
    suspend fun getAttemptCountBySubtopicId(subtopicId: String): Int

    @Query("SELECT COUNT(*) FROM practice_attempts WHERE subtopic_id = :subtopicId")
    fun getAttemptCountBySubtopicIdFlow(subtopicId: String): Flow<Int>

    @Delete
    suspend fun deleteAttempt(attempt: PracticeAttemptEntity)

    @Query("DELETE FROM practice_attempts WHERE id = :id")
    suspend fun deleteAttemptById(id: String)

    @Query("DELETE FROM practice_attempts WHERE subtopic_id = :subtopicId")
    suspend fun deleteAttemptsBySubtopicId(subtopicId: String)
}
