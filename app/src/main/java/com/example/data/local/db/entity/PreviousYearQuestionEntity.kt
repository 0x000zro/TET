package com.example.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.local.db.DatabaseContract
import com.example.domain.model.pyq.PreviousYearQuestion
import com.example.domain.model.pyq.PyqVerificationStatus

/**
 * Room entity representing Previous Year Question (PYQ) metadata (Step 15).
 *
 * Requirements:
 * - Stable primary key [id].
 * - Foreign key referencing [QuestionEntity.id] with CASCADE on delete so no orphan PYQ record remains.
 * - Foreign keys referencing [ExamEntity.id] and [PaperEntity.id] with RESTRICT on delete.
 * - Indexes on question_id, exam_id, paper_id, year, (paper_id, year).
 * - Unique constraint on (question_id, year, session) to prevent duplicate PYQ metadata.
 * - Single Source of Truth: Does NOT duplicate question text, options, correct answers, explanations, or syllabus names.
 */
@Entity(
    tableName = DatabaseContract.TABLE_PREVIOUS_YEAR_QUESTIONS,
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExamEntity::class,
            parentColumns = ["id"],
            childColumns = ["exam_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PaperEntity::class,
            parentColumns = ["id"],
            childColumns = ["paper_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["question_id"], name = "index_pyq_question_id"),
        Index(value = ["exam_id"], name = "index_pyq_exam_id"),
        Index(value = ["paper_id"], name = "index_pyq_paper_id"),
        Index(value = ["year"], name = "index_pyq_year"),
        Index(value = ["paper_id", "year"], name = "index_pyq_paper_id_year"),
        Index(
            value = ["question_id", "year", "session"],
            unique = true,
            name = "index_pyq_unique_question_year_session"
        )
    ]
)
data class PreviousYearQuestionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "question_id")
    val questionId: String,

    @ColumnInfo(name = "exam_id")
    val examId: String,

    @ColumnInfo(name = "paper_id")
    val paperId: String,

    @ColumnInfo(name = "year")
    val year: Int,

    @ColumnInfo(name = "session")
    val session: String? = null,

    @ColumnInfo(name = "source")
    val source: String? = null,

    @ColumnInfo(name = "verification_status")
    val verificationStatus: String,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "updated_at_timestamp")
    override val updatedAtTimestamp: Long = System.currentTimeMillis()
) : DatabaseContract.BaseEntity

fun PreviousYearQuestionEntity.toDomain(): PreviousYearQuestion =
    PreviousYearQuestion(
        id = id,
        questionId = questionId,
        examId = examId,
        paperId = paperId,
        year = year,
        session = session,
        source = source,
        verificationStatus = try {
            PyqVerificationStatus.valueOf(verificationStatus)
        } catch (_: Exception) {
            PyqVerificationStatus.UNVERIFIED
        },
        sortOrder = sortOrder,
        updatedAtTimestamp = updatedAtTimestamp
    )

fun PreviousYearQuestion.toEntity(): PreviousYearQuestionEntity =
    PreviousYearQuestionEntity(
        id = id,
        questionId = questionId,
        examId = examId,
        paperId = paperId,
        year = year,
        session = session,
        source = source,
        verificationStatus = verificationStatus.name,
        sortOrder = sortOrder,
        updatedAtTimestamp = updatedAtTimestamp
    )
