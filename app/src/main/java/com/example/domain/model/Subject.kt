package com.example.domain.model

/**
 * Pure domain model representing a Subject under a Paper.
 *
 * Free from Room, SQLite, and Android dependencies.
 */
data class Subject(
    val id: String,
    val paperId: String,
    val name: String,
    val shortName: String,
    val description: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)
