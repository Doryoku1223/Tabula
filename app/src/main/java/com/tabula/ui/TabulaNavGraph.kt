package com.tabula.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tabula.data.UserPreferencesRepository
import com.tabula.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

private object Routes {
    const val Onboarding = "onboarding"
    const val Splash = "splash"
    const val Home = "home"
}

@Composable
fun TabulaNavGraph(
    userPreferencesRepository: UserPreferencesRepository,
    navController: NavHostController = rememberNavController()
) {
    val onboardingCompleted by userPreferencesRepository.onboardingCompletedFlow.collectAsState(
        initial = false
    )
    val scope = rememberCoroutineScope()
    val startDestination = if (onboardingCompleted) Routes.Splash else Routes.Onboarding

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.Splash) {
            SplashScreen(
                onEnter = {
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onFinish = {
                    scope.launch {
                        userPreferencesRepository.setOnboardingCompleted(true)
                    }
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Home) {
            HomeRoute()
        }
    }
}

@Composable
private fun HomeRoute() {
    val homeViewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    HomeScreen(viewModel = homeViewModel)
}
