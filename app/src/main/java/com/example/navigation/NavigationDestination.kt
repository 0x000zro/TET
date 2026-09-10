package com.example.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

/**
 * High-level application routes.
 */
sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object MainShell : AppRoute("main_shell")
}

/**
 * Shell destinations supporting primary bottom navigation and secondary quick-access destinations.
 */
sealed class ShellDestination(
    val route: String,
    val titleResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isPrimaryBottomNav: Boolean = false
) {
    data object Home : ShellDestination(
        route = "shell_home",
        titleResId = R.string.nav_home,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        isPrimaryBottomNav = true
    )

    data object Exams : ShellDestination(
        route = "shell_exams",
        titleResId = R.string.nav_exams,
        selectedIcon = Icons.AutoMirrored.Filled.Assignment,
        unselectedIcon = Icons.AutoMirrored.Outlined.Assignment,
        isPrimaryBottomNav = true
    )

    data object Practice : ShellDestination(
        route = "shell_practice",
        titleResId = R.string.nav_practice,
        selectedIcon = Icons.Filled.Quiz,
        unselectedIcon = Icons.Outlined.Quiz,
        isPrimaryBottomNav = true
    )

    data object MockTests : ShellDestination(
        route = "shell_mock_tests",
        titleResId = R.string.nav_mock_tests,
        selectedIcon = Icons.Filled.Timer,
        unselectedIcon = Icons.Outlined.Timer,
        isPrimaryBottomNav = true
    )

    data object More : ShellDestination(
        route = "shell_more",
        titleResId = R.string.nav_more,
        selectedIcon = Icons.Filled.GridView,
        unselectedIcon = Icons.Outlined.GridView,
        isPrimaryBottomNav = true
    )

    // Secondary destinations accessible from Home Quick Access and More tab:
    data object EBooks : ShellDestination(
        route = "shell_ebooks",
        titleResId = R.string.nav_ebooks,
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook,
        isPrimaryBottomNav = false
    )

    data object Videos : ShellDestination(
        route = "shell_videos",
        titleResId = R.string.nav_videos,
        selectedIcon = Icons.Filled.PlayCircle,
        unselectedIcon = Icons.Outlined.PlayCircle,
        isPrimaryBottomNav = false
    )

    data object ProfileSettings : ShellDestination(
        route = "shell_profile_settings",
        titleResId = R.string.nav_profile_settings,
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle,
        isPrimaryBottomNav = false
    )

    data object Performance : ShellDestination(
        route = "shell_performance",
        titleResId = R.string.nav_performance,
        selectedIcon = Icons.Filled.Assessment,
        unselectedIcon = Icons.Outlined.Assessment,
        isPrimaryBottomNav = false
    )

    companion object {
        val primaryBottomNavDestinations: List<ShellDestination> = listOf(
            Home,
            Exams,
            Practice,
            MockTests,
            More
        )
    }
}
