package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.feature.shell.MainShellScreen
import com.example.ui.feature.splash.SplashScreen

/**
 * Scalable navigation host for the educational platform.
 * Houses the initial foundation routes and allows direct plug-and-play
 * addition of nested module graphs in future steps.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppRoute.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = AppRoute.Splash.route) {
            SplashScreen(
                onNavigateToMain = {
                    navController.navigate(AppRoute.MainShell.route) {
                        popUpTo(AppRoute.Splash.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = AppRoute.MainShell.route) {
            MainShellScreen()
        }
    }
}
