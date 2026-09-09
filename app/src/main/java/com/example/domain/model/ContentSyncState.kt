package com.example.domain.model

/**
 * Sync status descriptor for educational content synchronizers.
 */
enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    FAILED
}

/**
 * Pure domain model representing the synchronization state of a content source.
 * Prepares the local architecture for future remote sync without implementing
 * the network sync logic prematurely.
 */
data class ContentSyncState(
    val contentSource: String,
    val contentVersion: Int = 1,
    val lastSuccessfulSyncTimestamp: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val errorMessage: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
