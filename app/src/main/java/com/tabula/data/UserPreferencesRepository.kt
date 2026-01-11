package com.tabula.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LANGUAGE = stringPreferencesKey("language")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val SESSION_SIZE = androidx.datastore.preferences.core.intPreferencesKey("session_size")
        val CURATION_MODE = stringPreferencesKey("curation_mode")
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name
        ThemeMode.valueOf(raw)
    }.distinctUntilChanged()

    val languageFlow: Flow<LanguageMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.LANGUAGE] ?: LanguageMode.CN.name
        LanguageMode.valueOf(raw)
    }.distinctUntilChanged()

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }.distinctUntilChanged()

    val sessionSizeFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.SESSION_SIZE] ?: 15
    }.distinctUntilChanged()

    val curationModeFlow: Flow<CurationMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.CURATION_MODE] ?: CurationMode.RANDOM.name
        CurationMode.valueOf(raw)
    }.distinctUntilChanged()

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setLanguage(mode: LanguageMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = mode.name
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setSessionSize(size: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SESSION_SIZE] = size
        }
    }

    suspend fun setCurationMode(mode: CurationMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CURATION_MODE] = mode.name
        }
    }
}
