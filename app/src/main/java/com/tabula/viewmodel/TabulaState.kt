package com.tabula.viewmodel

import android.content.IntentSender
import com.tabula.data.Photo
import com.tabula.data.CurationMode
import com.tabula.data.LanguageMode
import com.tabula.data.ThemeMode

data class TabulaState(
    val photoStack: List<Photo> = emptyList(),
    val previousPhoto: Photo? = null,
    val currentPhoto: Photo? = null,
    val nextPhoto: Photo? = null,
    val currentIndex: Int = 0,
    val totalCount: Int = 0,
    val sessionMarkedCount: Int = 0,
    val curationMode: CurationMode = CurationMode.RANDOM,
    val sessionSize: Int = 15,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val languageMode: LanguageMode = LanguageMode.CN,
    val isLimitedAccess: Boolean = false,
    val indexingProgress: Int = 100,
    val isIndexing: Boolean = false,
    val trashBin: List<Photo> = emptyList(),
    val pendingDeleteIntent: IntentSender? = null,
    val isSessionComplete: Boolean = false
)
