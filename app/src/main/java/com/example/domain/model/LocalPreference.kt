package com.example.domain.model

/**
 * Pure domain model for non-sensitive local device preferences.
 * Passwords, tokens, and credentials must NEVER be stored here.
 */
data class LocalPreference(
    val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)
