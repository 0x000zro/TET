package com.example.domain.model

/**
 * Pure domain model representing a Subtopic under a Topic.
 *
 * Free from Room, SQLite, and Android dependencies.
 */
data class Subtopic(
    val id: String,
    val topicId: String,
    val name: String,
    val description: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)
