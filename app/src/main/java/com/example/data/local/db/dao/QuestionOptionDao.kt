package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.QuestionOptionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Question Options.
 *
 * Enforces deterministic ordering by sort_order ASC, id ASC.
 */
@Dao
interface QuestionOptionDao : DatabaseContract.BaseDao {

    @Query("SELECT * FROM question_options WHERE id = :id LIMIT 1")
    suspend fun getOptionById(id: String): QuestionOptionEntity?

    @Query("SELECT * FROM question_options WHERE question_id = :questionId ORDER BY sort_order ASC, id ASC")
    suspend fun getOptionsForQuestion(questionId: String): List<QuestionOptionEntity>

    @Query("SELECT * FROM question_options WHERE question_id = :questionId ORDER BY sort_order ASC, id ASC")
    fun getOptionsForQuestionFlow(questionId: String): Flow<List<QuestionOptionEntity>>

    @Query("SELECT * FROM question_options WHERE question_id IN (:questionIds) ORDER BY sort_order ASC, id ASC")
    suspend fun getOptionsForQuestions(questionIds: List<String>): List<QuestionOptionEntity>

    @Query("SELECT * FROM question_options WHERE question_id IN (:questionIds) ORDER BY sort_order ASC, id ASC")
    fun getOptionsForQuestionsFlow(questionIds: List<String>): Flow<List<QuestionOptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOption(option: QuestionOptionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOptions(options: List<QuestionOptionEntity>)

    @Update
    suspend fun updateOption(option: QuestionOptionEntity)

    @Delete
    suspend fun deleteOption(option: QuestionOptionEntity)

    @Query("DELETE FROM question_options WHERE id = :id")
    suspend fun deleteOptionById(id: String)

    @Query("DELETE FROM question_options WHERE question_id = :questionId")
    suspend fun deleteOptionsForQuestion(questionId: String)
}
