package com.example.domain.model

/**
 * Pure domain model for application-level state.
 * Independent of Room, Android SDK, and SQLite frameworks.
 */
data class AppState(
    val id: String = DEFAULT_ID,
    val isFirstLaunch: Boolean = true,
    val isContentInitialized: Boolean = false,
    val lastKnownContentVersion: Int = 1,
    val schemaVersion: Int = 1,
    val isCompatible: Boolean = true,
    val lastLaunchTimestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val DEFAULT_ID = "app_state_singleton"
    }
}
