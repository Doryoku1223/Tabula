package com.tabula

import android.app.Activity
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.Color
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tabula.data.PhotoRepository
import com.tabula.data.UserPreferencesRepository
import com.tabula.data.LanguageMode
import com.tabula.data.ThemeMode
import com.tabula.domain.DeletePhotosUseCase
import com.tabula.domain.GetSessionUseCase
import com.tabula.domain.IndexingProgressUseCase
import com.tabula.domain.RefreshIndexUseCase
import com.tabula.ui.TabulaNavGraph
import com.tabula.viewmodel.TabulaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashReady = androidx.compose.runtime.mutableStateOf(false)
        installSplashScreen().setKeepOnScreenCondition { !splashReady.value }
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val repo = PhotoRepository(applicationContext)
        val userPrefs = UserPreferencesRepository(applicationContext)
        val getSession = GetSessionUseCase(repo)
        val deletePhotos = DeletePhotosUseCase(repo)
        val refreshIndex = RefreshIndexUseCase(repo)
        val indexingProgress = IndexingProgressUseCase(repo)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TabulaViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return TabulaViewModel(
                        getSession,
                        deletePhotos,
                        refreshIndex,
                        indexingProgress,
                        userPrefs
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        setContent {
            val themeMode by userPrefs.themeModeFlow.collectAsState(initial = ThemeMode.DARK)
            val languageMode by userPrefs.languageFlow.collectAsState(initial = LanguageMode.CN)
            val onboardingCompleted by userPrefs.onboardingCompletedFlow.collectAsState(initial = false)

            LaunchedEffect(languageMode) {
                val tags = if (languageMode == LanguageMode.CN) "zh-CN" else "en"
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tags))
            }
            LaunchedEffect(onboardingCompleted) {
                withFrameNanos { splashReady.value = true }
            }

            val isDarkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            val colorScheme = if (isDarkTheme) {
                darkColorScheme(
                    primary = Color.White,
                    onPrimary = Color.Black,
                    background = Color.Black,
                    onBackground = Color.White,
                    surface = Color.Black,
                    onSurface = Color.White
                )
            } else {
                lightColorScheme(
                    primary = Color.Black,
                    onPrimary = Color.White,
                    background = Color.White,
                    onBackground = Color.Black,
                    surface = Color.White,
                    onSurface = Color.Black
                )
            }

            MaterialTheme(colorScheme = colorScheme) {
                val viewModel: TabulaViewModel = viewModel(factory = factory)

                val uiState by viewModel.uiState.collectAsState()
                val deletePermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult()
                ) { result ->
                    viewModel.onDeletePermissionResult(result.resultCode == Activity.RESULT_OK)
                }

                LaunchedEffect(uiState.pendingDeleteIntent) {
                    val intentSender = uiState.pendingDeleteIntent ?: return@LaunchedEffect
                    val request = IntentSenderRequest.Builder(intentSender).build()
                    deletePermissionLauncher.launch(request)
                }

                TabulaNavGraph(userPreferencesRepository = userPrefs)
            }
        }
    }
}
