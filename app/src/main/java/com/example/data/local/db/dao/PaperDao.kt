package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.PaperEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Papers under Exams.
 */
@Dao
interface PaperDao : DatabaseContract.BaseDao {

    @Query("SELECT * FROM papers WHERE id = :id LIMIT 1")
    fun getPaperByIdFlow(id: String): Flow<PaperEntity?>

    @Query("SELECT * FROM papers WHERE id = :id LIMIT 1")
    suspend fun getPaperById(id: String): PaperEntity?

    @Query("SELECT * FROM papers WHERE exam_id = :examId ORDER BY sort_order ASC, name ASC")
    fun getPapersByExamIdFlow(examId: String): Flow<List<PaperEntity>>

    @Query("SELECT * FROM papers WHERE exam_id = :examId AND is_active = 1 ORDER BY sort_order ASC, name ASC")
    fun getActivePapersByExamIdFlow(examId: String): Flow<List<PaperEntity>>

    @Query("SELECT COUNT(*) FROM papers WHERE exam_id = :examId")
    suspend fun getPaperCountByExamId(examId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePaper(paper: PaperEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePapers(papers: List<PaperEntity>)

    @Update
    suspend fun updatePaper(paper: PaperEntity)

    @Query("DELETE FROM papers WHERE id = :id")
    suspend fun deletePaperById(id: String)

    @Query("DELETE FROM papers WHERE exam_id = :examId")
    suspend fun deletePapersByExamId(examId: String)
}
