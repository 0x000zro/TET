package com.example.domain.model.pyq

/**
 * Pure Kotlin domain model representing Previous Year Question (PYQ) metadata (Step 15).
 *
 * Relationship & Architecture Rules:
 * - A PYQ references exactly one canonical [com.example.domain.model.Question] via [questionId].
 * - Single Source of Truth: Strictly does NOT duplicate question text, options, answer keys,
 *   explanations, or syllabus hierarchy strings. The existing [Question] entity remains the
 *   single source of truth for all question content.
 * - Uses stable String IDs.
 * - Deterministic ordering: year DESC -> session/shift -> questionId ASC.
 */
data class PreviousYearQuestion(
    val id: String,
    val questionId: String,
    val examId: String,
    val paperId: String,
    val year: Int,
    val session: String? = null,
    val source: String? = null,
    val verificationStatus: PyqVerificationStatus = PyqVerificationStatus.UNVERIFIED,
    val sortOrder: Int = 0,
    val updatedAtTimestamp: Long = System.currentTimeMillis()
)
