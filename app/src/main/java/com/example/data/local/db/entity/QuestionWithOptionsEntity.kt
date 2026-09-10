package com.example.data.local.db.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Room Relation data class grouping a Question with all its child Options.
 */
data class QuestionWithOptionsEntity(
    @Embedded
    val question: QuestionEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "question_id"
    )
    val options: List<QuestionOptionEntity> = emptyList()
)
