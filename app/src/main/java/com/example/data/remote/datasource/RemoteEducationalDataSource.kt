package com.example.data.remote.datasource

/**
 * Contract for the future remote educational content provider.
 * Keeps the architecture ready for network synchronization without
 * prematurely implementing remote API endpoints or third-party connections.
 */
interface RemoteEducationalDataSource {
    suspend fun checkNetworkAvailability(): Boolean = false
    suspend fun syncRemoteContent(): Result<Unit> = Result.success(Unit)
}
