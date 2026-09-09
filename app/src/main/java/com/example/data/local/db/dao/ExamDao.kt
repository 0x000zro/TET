package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.ExamEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Exams.
 */
@Dao
interface ExamDao : DatabaseContract.BaseDao {

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    fun getExamByIdFlow(id: String): Flow<ExamEntity?>

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    suspend fun getExamById(id: String): ExamEntity?

    @Query("SELECT * FROM exams ORDER BY sort_order ASC, name ASC")
    fun getAllExamsFlow(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE is_active = 1 ORDER BY sort_order ASC, name ASC")
    fun getActiveExamsFlow(): Flow<List<ExamEntity>>

    @Query("SELECT COUNT(*) FROM exams")
    suspend fun getExamCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateExam(exam: ExamEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateExams(exams: List<ExamEntity>)

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: String)

    @Query("DELETE FROM exams")
    suspend fun clearAllExams()
}
