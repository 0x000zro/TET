package com.example.domain.model

/**
 * Encapsulates the verified architectural foundation state of the application.
 */
data class ArchitecturalLayerInfo(
    val layerName: String,
    val description: String,
    val status: String,
    val isVerified: Boolean = true
)

data class AppFoundationInfo(
    val appName: String,
    val version: String,
    val targetSdk: Int,
    val minSdk: Int,
    val architecturePattern: String,
    val offlineFirstReady: Boolean,
    val networkContractReady: Boolean,
    val layers: List<ArchitecturalLayerInfo>
)
