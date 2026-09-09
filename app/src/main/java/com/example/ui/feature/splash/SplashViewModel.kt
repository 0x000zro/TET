package com.example.ui.feature.splash

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SplashUiState(
    val isInitialized: Boolean = true,
    val statusMessage: String = "Project Foundation Active & Ready",
    val progress: Float = 1.0f
)

/**
 * Fast, reliable SplashViewModel without artificial delays or blocking operations.
 */
class SplashViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
}

