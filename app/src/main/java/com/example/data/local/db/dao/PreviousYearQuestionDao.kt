package com.example.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.db.DatabaseContract
import com.example.data.local.db.entity.PreviousYearQuestionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Previous Year Question (PYQ) metadata (Step 15).
 *
 * Enforces:
 * - Deterministic ordering: `year DESC, CASE WHEN session IS NULL THEN '' ELSE session END ASC, question_id ASC`.
 * - Reactive observations via Kotlin [Flow].
 * - Safe insertions with conflict replacement.
 * - Single source of truth: operates purely on PYQ metadata without mutating Question content.
 */
@Dao
interface PreviousYearQuestionDao : DatabaseContract.BaseDao {

    @Query(
        """
        SELECT * FROM previous_year_questions
        ORDER BY year DESC, 
                 CASE WHEN session IS NULL THEN '' ELSE session END ASC, 
                 question_id ASC
        """
    )
    fun getAllFlow(): Flow<List<PreviousYearQuestionEntity>>

    @Query(
        """
        SELECT * FROM previous_year_questions
        ORDER BY year DESC, 
                 CASE WHEN session IS NULL THEN '' ELSE session END ASC, 
                 question_id ASC
        """
    )
    suspend fun getAll(): List<PreviousYearQuestionEntity>

    @Query(
        """
        SELECT * FROM previous_year_questions
        WHERE exam_id = :examId
        ORDER BY year DESC, 
                 CASE WHEN session IS NULL THEN '' ELSE session END ASC, 
                 question_id ASC
        """
    )
    fun getByExamIdFlow(examId: String): Flow<List<PreviousYearQuestionEntity>>

    @Query(
        """
        SELECT * FROM previous_year_questions
        WHERE paper_id = :paperId
        ORDER BY year DESC, 
                 CASE WHEN session IS NULL THEN '' ELSE session END ASC, 
                 question_id ASC
        """
    )
    fun getByPaperIdFlow(paperId: String): Flow<List<PreviousYearQuestionEntity>>

    @Query(
        """
        SELECT * FROM previous_year_questions
        WHERE paper_id = :paperId AND year = :year
        ORDER BY CASE WHEN session IS NULL THEN '' ELSE session END ASC, 
                 question_id ASC
        """
    )
    fun getByPaperIdAndYearFlow(paperId: String, year: Int): Flow<List<PreviousYearQuestionEntity>>

    @Query(
        """
        SELECT pyq.* FROM previous_year_questions pyq
        INNER JOIN questions q ON pyq.question_id = q.id
        WHERE q.subtopic_id = :subtopicId
        ORDER BY pyq.year DESC, 
                 CASE WHEN pyq.session IS NULL THEN '' ELSE pyq.session END ASC, 
                 pyq.question_id ASC
        """
    )
    fun getBySubtopicIdFlow(subtopicId: String): Flow<List<PreviousYearQuestionEntity>>

    @Query("SELECT * FROM previous_year_questions WHERE question_id = :questionId LIMIT 1")
    fun getByQuestionIdFlow(questionId: String): Flow<PreviousYearQuestionEntity?>

    @Query("SELECT * FROM previous_year_questions WHERE question_id = :questionId LIMIT 1")
    suspend fun getByQuestionId(questionId: String): PreviousYearQuestionEntity?

    @Query("SELECT * FROM previous_year_questions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PreviousYearQuestionEntity?

    @Query(
        """
        SELECT DISTINCT year FROM previous_year_questions
        WHERE paper_id = :paperId
        ORDER BY year DESC
        """
    )
    fun getDistinctYearsForPaperFlow(paperId: String): Flow<List<Int>>

    @Query(
        """
        SELECT DISTINCT year FROM previous_year_questions
        WHERE paper_id = :paperId
        ORDER BY year DESC
        """
    )
    suspend fun getDistinctYearsForPaper(paperId: String): List<Int>

    @Query(
        """
        SELECT DISTINCT year FROM previous_year_questions
        ORDER BY year DESC
        """
    )
    fun getAllDistinctYearsFlow(): Flow<List<Int>>

    @Query(
        """
        SELECT * FROM previous_year_questions
        WHERE question_id = :questionId AND year = :year AND (session = :session OR (session IS NULL AND :session IS NULL))
        LIMIT 1
        """
    )
    suspend fun findExisting(questionId: String, year: Int, session: String?): PreviousYearQuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: PreviousYearQuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(entities: List<PreviousYearQuestionEntity>)

    @Query("DELETE FROM previous_year_questions WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM previous_year_questions WHERE question_id = :questionId")
    suspend fun deleteByQuestionId(questionId: String): Int

    @Query("DELETE FROM previous_year_questions")
    suspend fun deleteAll(): Int

    @Query("SELECT COUNT(*) FROM previous_year_questions")
    fun getCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM previous_year_questions")
    suspend fun getCount(): Int
}
