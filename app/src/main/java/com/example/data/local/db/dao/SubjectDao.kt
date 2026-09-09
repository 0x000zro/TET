package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Subjects under Papers.
 */
@Dao
interface SubjectDao : DatabaseContract.BaseDao {

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    fun getSubjectByIdFlow(id: String): Flow<SubjectEntity?>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    suspend fun getSubjectById(id: String): SubjectEntity?

    @Query("SELECT * FROM subjects WHERE paper_id = :paperId ORDER BY sort_order ASC, name ASC")
    fun getSubjectsByPaperIdFlow(paperId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE paper_id = :paperId AND is_active = 1 ORDER BY sort_order ASC, name ASC")
    fun getActiveSubjectsByPaperIdFlow(paperId: String): Flow<List<SubjectEntity>>

    @Query("SELECT COUNT(*) FROM subjects WHERE paper_id = :paperId")
    suspend fun getSubjectCountByPaperId(paperId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSubjects(subjects: List<SubjectEntity>)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: String)

    @Query("DELETE FROM subjects WHERE paper_id = :paperId")
    suspend fun deleteSubjectsByPaperId(paperId: String)
}
