package com.example.domain.model

/**
 * Pure domain model representing a Paper under an Exam.
 *
 * Free from Room, SQLite, and Android dependencies.
 */
data class Paper(
    val id: String,
    val examId: String,
    val name: String,
    val shortName: String,
    val description: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)
