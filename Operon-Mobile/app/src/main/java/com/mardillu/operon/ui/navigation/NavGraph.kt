package com.mardillu.operon.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mardillu.operon.ui.screens.HomeScreen
import com.mardillu.operon.ui.screens.OnboardingScreen
import com.mardillu.operon.viewmodel.MainViewModel

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    viewModel: MainViewModel,
    onEnableAccessibility: () -> Unit,
    onRequestScreenCapture: () -> Unit
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onEnableAccessibility = onEnableAccessibility,
                onRequestScreenCapture = onRequestScreenCapture,
                onProceed = { navController.navigate(Routes.HOME) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                } }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(viewModel = viewModel)
        }
    }
}
