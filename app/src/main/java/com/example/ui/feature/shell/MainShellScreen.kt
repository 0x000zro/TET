package com.example.ui.feature.shell

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.navigation.ShellDestination
import com.example.ui.feature.home.HomeScreen
import com.example.ui.feature.placeholder.DestinationPlaceholderScreen
import com.example.ui.theme.LocalDimensions

/**
 * Production-quality Main Shell providing navigation, edge-to-edge support,
 * responsive layout constraints, and system bar handling.
 */
@Composable
fun MainShellScreen(
    modifier: Modifier = Modifier,
    viewModel: MainShellViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dimensions = LocalDimensions.current

    // Android System Back Navigation handling
    BackHandler(enabled = uiState.currentDestination != ShellDestination.Home || uiState.backStack.size > 1) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (uiState.isBottomBarVisible) {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("main_navigation_bar"),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = dimensions.elevationMedium
                ) {
                    ShellDestination.primaryBottomNavDestinations.forEach { destination ->
                        val isSelected = uiState.currentDestination == destination

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateToDestination(destination) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = stringResource(destination.titleResId)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(destination.titleResId),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            modifier = Modifier.testTag("nav_tab_${destination.route}"),
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = uiState.currentDestination,
                label = "shell_destination_crossfade"
            ) { destination ->
                when (destination) {
                    ShellDestination.Home -> {
                        HomeScreen(
                            onNavigateToDestination = { target ->
                                viewModel.navigateToDestination(target)
                            }
                        )
                    }

                    ShellDestination.Exams -> {
                        com.example.ui.feature.syllabus.SyllabusScreen(
                            onNavigateBackToShell = { viewModel.navigateBack() }
                        )
                    }

                    ShellDestination.Practice,
                    ShellDestination.MockTests,
                    ShellDestination.EBooks,
                    ShellDestination.Videos,
                    ShellDestination.ProfileSettings -> {
                        DestinationPlaceholderScreen(
                            destination = destination,
                            onNavigateBack = { viewModel.navigateBack() },
                            onItemClick = { /* Placeholder action */ }
                        )
                    }

                    ShellDestination.More -> {
                        MoreTabContent(
                            uiState = uiState,
                            onNavigateToDestination = { target ->
                                viewModel.navigateToDestination(target)
                            }
                        )
                    }
                }
            }
        }
    }
}
