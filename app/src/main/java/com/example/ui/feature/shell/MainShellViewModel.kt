package com.example.ui.feature.shell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.EducationalRepositoryImpl
import com.example.domain.model.AppFoundationInfo
import com.example.domain.model.EducationalModule
import com.example.domain.repository.EducationalRepository
import com.example.navigation.ShellDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainShellUiState(
    val currentDestination: ShellDestination = ShellDestination.Home,
    val backStack: List<ShellDestination> = listOf(ShellDestination.Home),
    val foundationInfo: AppFoundationInfo? = null,
    val plannedModules: List<EducationalModule> = emptyList(),
    val isOfflineReady: Boolean = true
) {
    val isBottomBarVisible: Boolean
        get() = currentDestination.isPrimaryBottomNav
}

class MainShellViewModel(
    private val repository: EducationalRepository = EducationalRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainShellUiState())
    val uiState: StateFlow<MainShellUiState> = _uiState.asStateFlow()

    init {
        loadFoundationData()
    }

    private fun loadFoundationData() {
        viewModelScope.launch {
            repository.getFoundationInfo().collect { info ->
                _uiState.value = _uiState.value.copy(foundationInfo = info)
            }
        }
        viewModelScope.launch {
            repository.getPlannedModules().collect { modules ->
                _uiState.value = _uiState.value.copy(plannedModules = modules)
            }
        }
    }

    fun navigateToDestination(destination: ShellDestination) {
        if (_uiState.value.currentDestination == destination) return

        val currentStack = _uiState.value.backStack.toMutableList()

        if (destination.isPrimaryBottomNav) {
            // For primary tabs, maintain Home as the root base
            currentStack.clear()
            currentStack.add(ShellDestination.Home)
            if (destination != ShellDestination.Home) {
                currentStack.add(destination)
            }
        } else {
            // For secondary destinations, push to backstack
            currentStack.add(destination)
        }

        _uiState.value = _uiState.value.copy(
            currentDestination = destination,
            backStack = currentStack
        )
    }

    fun navigateBack(): Boolean {
        val currentStack = _uiState.value.backStack.toMutableList()
        return if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.lastIndex)
            val previousDestination = currentStack.last()
            _uiState.value = _uiState.value.copy(
                currentDestination = previousDestination,
                backStack = currentStack
            )
            true
        } else if (_uiState.value.currentDestination != ShellDestination.Home) {
            _uiState.value = _uiState.value.copy(
                currentDestination = ShellDestination.Home,
                backStack = listOf(ShellDestination.Home)
            )
            true
        } else {
            false
        }
    }
}

