package com.example.domain.model

/**
 * Pure domain model representing a Topic under a Subject.
 *
 * Free from Room, SQLite, and Android dependencies.
 */
data class Topic(
    val id: String,
    val subjectId: String,
    val name: String,
    val description: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)
