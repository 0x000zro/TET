package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.BookmarkedQuestionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for student question bookmarks (Step 14).
 *
 * Enforces:
 * - Deterministic ordering: `bookmarked_at DESC, question_id ASC`.
 * - Reactive observations via Kotlin [Flow].
 * - Safe bookmark toggling and atomic mutations.
 * - Targeted deletion of bookmark records without touching questions or syllabus data.
 */
@Dao
interface BookmarkedQuestionDao : DatabaseContract.BaseDao {

    @Query("SELECT * FROM bookmarked_questions ORDER BY bookmarked_at DESC, question_id ASC")
    fun getAllBookmarkedQuestionsFlow(): Flow<List<BookmarkedQuestionEntity>>

    @Query("SELECT * FROM bookmarked_questions ORDER BY bookmarked_at DESC, question_id ASC")
    suspend fun getAllBookmarkedQuestions(): List<BookmarkedQuestionEntity>

    @Query("SELECT * FROM bookmarked_questions WHERE subtopic_id = :subtopicId ORDER BY bookmarked_at DESC, question_id ASC")
    fun getBookmarkedQuestionsBySubtopicIdFlow(subtopicId: String): Flow<List<BookmarkedQuestionEntity>>

    @Query("SELECT * FROM bookmarked_questions WHERE subtopic_id = :subtopicId ORDER BY bookmarked_at DESC, question_id ASC")
    suspend fun getBookmarkedQuestionsBySubtopicId(subtopicId: String): List<BookmarkedQuestionEntity>

    @Query("SELECT * FROM bookmarked_questions WHERE question_id = :questionId LIMIT 1")
    suspend fun getBookmarkedQuestionById(questionId: String): BookmarkedQuestionEntity?

    @Query("SELECT * FROM bookmarked_questions WHERE question_id = :questionId LIMIT 1")
    fun getBookmarkedQuestionByIdFlow(questionId: String): Flow<BookmarkedQuestionEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_questions WHERE question_id = :questionId)")
    fun isQuestionBookmarkedFlow(questionId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarked_questions WHERE question_id = :questionId)")
    suspend fun isQuestionBookmarked(questionId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: BookmarkedQuestionEntity)

    @Query("DELETE FROM bookmarked_questions WHERE question_id = :questionId")
    suspend fun deleteByQuestionId(questionId: String): Int

    @Query("DELETE FROM bookmarked_questions")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM bookmarked_questions")
    fun getCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM bookmarked_questions")
    suspend fun getCount(): Int

    /**
     * Atomically toggles bookmark state for a question.
     * Returns true if newly bookmarked, false if unbookmarked.
     */
    @Transaction
    suspend fun toggleBookmark(questionId: String, subtopicId: String, timestamp: Long): Boolean {
        val existing = getBookmarkedQuestionById(questionId)
        return if (existing != null) {
            deleteByQuestionId(questionId)
            false
        } else {
            insertOrUpdate(
                BookmarkedQuestionEntity(
                    questionId = questionId,
                    subtopicId = subtopicId,
                    bookmarkedAt = timestamp,
                    updatedAtTimestamp = timestamp
                )
            )
            true
        }
    }
}
