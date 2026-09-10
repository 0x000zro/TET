package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.QuestionEntity
import com.example.data.local.db.entity.QuestionWithOptionsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Educational Questions.
 *
 * Enforces deterministic ordering by sort_order ASC, id ASC.
 */
@Dao
interface QuestionDao : DatabaseContract.BaseDao {

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    fun getQuestionByIdFlow(id: String): Flow<QuestionEntity?>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionById(id: String): QuestionEntity?

    @Transaction
    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    fun getQuestionWithOptionsByIdFlow(id: String): Flow<QuestionWithOptionsEntity?>

    @Transaction
    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionWithOptionsById(id: String): QuestionWithOptionsEntity?

    @Query("SELECT * FROM questions WHERE subtopic_id = :subtopicId ORDER BY sort_order ASC, id ASC")
    fun getQuestionsBySubtopicIdFlow(subtopicId: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE subtopic_id = :subtopicId AND is_active = 1 ORDER BY sort_order ASC, id ASC")
    fun getActiveQuestionsBySubtopicIdFlow(subtopicId: String): Flow<List<QuestionEntity>>

    @Transaction
    @Query("SELECT * FROM questions WHERE subtopic_id = :subtopicId ORDER BY sort_order ASC, id ASC")
    fun getQuestionsWithOptionsBySubtopicIdFlow(subtopicId: String): Flow<List<QuestionWithOptionsEntity>>

    @Transaction
    @Query("SELECT * FROM questions WHERE subtopic_id = :subtopicId AND is_active = 1 ORDER BY sort_order ASC, id ASC")
    fun getActiveQuestionsWithOptionsBySubtopicIdFlow(subtopicId: String): Flow<List<QuestionWithOptionsEntity>>

    @Transaction
    @Query("SELECT * FROM questions WHERE subtopic_id = :subtopicId ORDER BY sort_order ASC, id ASC")
    suspend fun getQuestionsWithOptionsBySubtopicId(subtopicId: String): List<QuestionWithOptionsEntity>

    @Query("SELECT COUNT(*) FROM questions WHERE subtopic_id = :subtopicId")
    suspend fun getQuestionCountBySubtopicId(subtopicId: String): Int

    @Query("SELECT COUNT(*) FROM questions WHERE subtopic_id = :subtopicId AND is_active = 1")
    suspend fun getActiveQuestionCountBySubtopicId(subtopicId: String): Int

    @Query("SELECT COUNT(*) FROM questions WHERE subtopic_id = :subtopicId AND is_active = 1")
    fun getActiveQuestionCountBySubtopicIdFlow(subtopicId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateQuestion(question: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateQuestions(questions: List<QuestionEntity>)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestionById(id: String)

    @Query("DELETE FROM questions WHERE subtopic_id = :subtopicId")
    suspend fun deleteQuestionsBySubtopicId(subtopicId: String)
}
