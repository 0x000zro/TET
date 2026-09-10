package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.WrongQuestionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for student mistake records (Step 13).
 *
 * Enforces:
 * - Deterministic ordering: `last_wrong_at DESC, question_id ASC`.
 * - Reactive observations via Kotlin [Flow].
 * - Safe mistake recording with automatic `wrong_count` incrementing and timestamp updates.
 * - Targeted deletion of mistake records without touching questions or syllabus data.
 */
@Dao
interface WrongQuestionDao : DatabaseContract.BaseDao {

    @Query("SELECT * FROM wrong_questions ORDER BY last_wrong_at DESC, question_id ASC")
    fun getAllWrongQuestionsFlow(): Flow<List<WrongQuestionEntity>>

    @Query("SELECT * FROM wrong_questions ORDER BY last_wrong_at DESC, question_id ASC")
    suspend fun getAllWrongQuestions(): List<WrongQuestionEntity>

    @Query("SELECT * FROM wrong_questions WHERE subtopic_id = :subtopicId ORDER BY last_wrong_at DESC, question_id ASC")
    fun getWrongQuestionsBySubtopicIdFlow(subtopicId: String): Flow<List<WrongQuestionEntity>>

    @Query("SELECT * FROM wrong_questions WHERE subtopic_id = :subtopicId ORDER BY last_wrong_at DESC, question_id ASC")
    suspend fun getWrongQuestionsBySubtopicId(subtopicId: String): List<WrongQuestionEntity>

    @Query("SELECT * FROM wrong_questions WHERE question_id = :questionId LIMIT 1")
    suspend fun getWrongQuestionById(questionId: String): WrongQuestionEntity?

    @Query("SELECT * FROM wrong_questions WHERE question_id = :questionId LIMIT 1")
    fun getWrongQuestionByIdFlow(questionId: String): Flow<WrongQuestionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: WrongQuestionEntity)

    @Update
    suspend fun update(entity: WrongQuestionEntity)

    @Query("DELETE FROM wrong_questions WHERE question_id = :questionId")
    suspend fun deleteByQuestionId(questionId: String): Int

    @Query("DELETE FROM wrong_questions")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM wrong_questions")
    suspend fun getCount(): Int

    /**
     * Records or updates a mistake atomically.
     * If a record already exists, increments wrongCount, updates lastWrongAt and lastAttemptId.
     * If not, creates a new mistake record with wrongCount = 1 and firstWrongAt = lastWrongAt = timestamp.
     */
    @Transaction
    suspend fun recordOrUpdateMistake(
        questionId: String,
        subtopicId: String,
        attemptId: String?,
        timestamp: Long
    ) {
        val existing = getWrongQuestionById(questionId)
        if (existing == null) {
            insertOrUpdate(
                WrongQuestionEntity(
                    questionId = questionId,
                    subtopicId = subtopicId,
                    firstWrongAt = timestamp,
                    lastWrongAt = timestamp,
                    wrongCount = 1,
                    lastAttemptId = attemptId,
                    updatedAtTimestamp = timestamp
                )
            )
        } else {
            insertOrUpdate(
                existing.copy(
                    wrongCount = existing.wrongCount + 1,
                    lastWrongAt = timestamp,
                    lastAttemptId = attemptId ?: existing.lastAttemptId,
                    updatedAtTimestamp = timestamp
                )
            )
        }
    }
}
