package com.tabula.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tabula.data.UserPreferencesRepository
import com.tabula.viewmodel.TabulaViewModel
import kotlinx.coroutines.launch

private object Routes {
    const val Onboarding = "onboarding"
    const val Splash = "splash"
    const val Home = "home"
    const val Settings = "settings"
    const val Review = "review"
    const val About = "about"
}

@Composable
fun TabulaNavGraph(
    userPreferencesRepository: UserPreferencesRepository,
    viewModel: TabulaViewModel,
    navController: NavHostController = rememberNavController()
) {
    val onboardingCompleted by userPreferencesRepository.onboardingCompletedFlow.collectAsState(
        initial = false
    )
    val scope = rememberCoroutineScope()
    val startDestination = if (onboardingCompleted) Routes.Splash else Routes.Onboarding

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(320),
                initialOffsetX = { it }
            ) + fadeIn(animationSpec = tween(220))
        },
        exitTransition = {
            slideOutHorizontally(
                animationSpec = tween(240),
                targetOffsetX = { -it / 4 }
            ) + scaleOut(
                targetScale = 0.98f,
                animationSpec = tween(240)
            ) + fadeOut(animationSpec = tween(160))
        },
        popEnterTransition = {
            slideInHorizontally(
                animationSpec = tween(260),
                initialOffsetX = { -it / 4 }
            ) + scaleIn(
                initialScale = 0.98f,
                animationSpec = tween(260)
            ) + fadeIn(animationSpec = tween(200))
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(240),
                targetOffsetX = { it }
            ) + fadeOut(animationSpec = tween(180))
        }
    ) {
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
            HomeRoute(
                viewModel = viewModel,
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenReview = { navController.navigate(Routes.Review) }
            )
        }
        composable(Routes.Settings) {
            SettingsRoute(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenAbout = { navController.navigate(Routes.About) }
            )
        }
        composable(Routes.Review) {
            ReviewRoute(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.About) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
private fun HomeRoute(
    viewModel: TabulaViewModel,
    onOpenSettings: () -> Unit,
    onOpenReview: () -> Unit
) {
    TabulaScreen(
        viewModel = viewModel,
        onOpenSettings = onOpenSettings,
        onOpenReview = onOpenReview
    )
}

@Composable
private fun SettingsRoute(
    viewModel: TabulaViewModel,
    onBack: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        sessionSize = uiState.sessionSize,
        curationMode = uiState.curationMode,
        themeMode = uiState.themeMode,
        languageMode = uiState.languageMode,
        isLimitedAccess = uiState.isLimitedAccess,
        onBack = onBack,
        onSessionSizeChange = { viewModel.updateSessionSize(it) },
        onCurationModeChange = { viewModel.updateCurationMode(it) },
        onThemeModeChange = { viewModel.updateThemeMode(it) },
        onLanguageModeChange = { viewModel.updateLanguage(it) },
        onOpenAbout = onOpenAbout
    )
}

@Composable
private fun ReviewRoute(
    viewModel: TabulaViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    ReviewScreen(
        trashBin = uiState.trashBin,
        onConfirmBurn = { viewModel.confirmBurn() },
        onRestoreSelected = { selected -> viewModel.restoreSelected(selected) },
        onDeleteSelected = { selected -> viewModel.deleteSelected(selected) },
        onBack = onBack,
        modifier = Modifier.statusBarsPadding()
    )
}
