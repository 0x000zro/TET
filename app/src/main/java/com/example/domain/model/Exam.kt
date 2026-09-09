package com.example.domain.model

/**
 * Pure domain model representing an Exam (top level of the educational hierarchy).
 *
 * Free from Room, SQLite, and Android dependencies.
 */
data class Exam(
    val id: String,
    val name: String,
    val shortName: String,
    val description: String = "",
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)
