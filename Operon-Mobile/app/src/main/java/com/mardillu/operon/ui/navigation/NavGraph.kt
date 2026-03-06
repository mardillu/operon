package com.mardillu.operon.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mardillu.operon.ui.screens.HomeScreen
import com.mardillu.operon.ui.screens.OnboardingScreen
import com.mardillu.operon.ui.screens.ExecutionModeScreen
import com.mardillu.operon.viewmodel.MainViewModel
import com.mardillu.operon.data.PreferencesManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

object Routes {
    const val ONBOARDING = "onboarding"
    const val EXECUTION_MODE = "execution_mode"
    const val HOME = "home"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    viewModel: MainViewModel,
    preferencesManager: PreferencesManager,
    onEnableAccessibility: () -> Unit,
    onRequestScreenCapture: () -> Unit,
    onRequestOverlayPermission: () -> Unit
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onEnableAccessibility = onEnableAccessibility,
                onRequestScreenCapture = onRequestScreenCapture,
                onRequestOverlayPermission = onRequestOverlayPermission,
                onProceed = { navController.navigate(Routes.EXECUTION_MODE) }
            )
        }
        composable(Routes.EXECUTION_MODE) {
            val scope = rememberCoroutineScope()
            val currentMode by preferencesManager.executionModeFlow.collectAsState(
                initial = com.mardillu.operon.data.ExecutionMode.ASK_SOME_RECOMMENDED
            )
            ExecutionModeScreen(
                currentMode = currentMode,
                onModeSelected = { mode ->
                    scope.launch { preferencesManager.saveExecutionMode(mode) }
                },
                onProceed = { 
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true } // Pops Onboarding and ExecutionMode from stack
                    }
                }
            )
        }
        composable(Routes.HOME) {
            val scope = rememberCoroutineScope()
            val currentMode by preferencesManager.executionModeFlow.collectAsState(
                initial = com.mardillu.operon.data.ExecutionMode.ASK_SOME_RECOMMENDED
            )
            HomeScreen(
                viewModel = viewModel,
                currentMode = currentMode,
                onModeSelected = { mode ->
                    scope.launch { preferencesManager.saveExecutionMode(mode) }
                }
            )
        }
    }
}
