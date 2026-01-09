package com.tabula.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.app.RecoverableSecurityException
import android.os.Build
import android.Manifest
import com.tabula.data.CurationMode
import com.tabula.data.LanguageMode
import com.tabula.data.Photo
import com.tabula.data.ThemeMode
import com.tabula.data.UserPreferencesRepository
import com.tabula.data.DeletePermissionRequired
import com.tabula.domain.DeletePhotosUseCase
import com.tabula.domain.GetSessionUseCase
import com.tabula.domain.IndexingProgressUseCase
import com.tabula.domain.RefreshIndexUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TabulaViewModel(
    private val getSessionUseCase: GetSessionUseCase,
    private val deletePhotosUseCase: DeletePhotosUseCase,
    private val refreshIndexUseCase: RefreshIndexUseCase,
    private val indexingProgressUseCase: IndexingProgressUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TabulaState())
    val uiState: StateFlow<TabulaState> = _uiState.asStateFlow()

    init {
        observeIndexing()
        observePreferences()
        rescanLibrary()
    }

    fun refreshSession() {
        _uiState.update { state ->
            state.copy(
                photoStack = emptyList(),
                currentPhoto = null,
                nextPhoto = null,
                currentIndex = 0,
                totalCount = 0,
                pendingDeleteIntent = null,
                isSessionComplete = false
            )
        }
        viewModelScope.launch {
            loadNewSessionInternal()
        }
    }

    fun rescanLibrary() {
        _uiState.update { state ->
            state.copy(
                photoStack = emptyList(),
                currentPhoto = null,
                nextPhoto = null,
                currentIndex = 0,
                totalCount = 0,
                pendingDeleteIntent = null,
                isSessionComplete = false
            )
        }
        viewModelScope.launch {
            refreshIndexUseCase()
            loadNewSessionInternal()
        }
    }

    fun updateSessionSize(size: Int) {
        val normalized = size.coerceIn(5, 50)
        _uiState.update { state ->
            state.copy(sessionSize = normalized)
        }
        refreshSession()
    }

    fun updateCurationMode(mode: CurationMode) {
        _uiState.update { state ->
            state.copy(curationMode = mode)
        }
        refreshSession()
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(mode)
        }
    }

    fun updateLanguage(mode: LanguageMode) {
        viewModelScope.launch {
            userPreferencesRepository.setLanguage(mode)
        }
    }

    fun requiredMediaPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun updateMediaAccess(readImagesGranted: Boolean, visualSelectedGranted: Boolean) {
        val limited = visualSelectedGranted && !readImagesGranted
        _uiState.update { state ->
            state.copy(isLimitedAccess = limited)
        }
    }

    fun markForDeletion(photo: Photo) {
        _uiState.update { state ->
            val updatedStack = state.photoStack.filterNot { it.id == photo.id }
            val updatedTrash = state.trashBin + photo
            val currentIndex = currentIndexFor(state.totalCount, updatedStack.size)
            state.copy(
                photoStack = updatedStack,
                currentPhoto = updatedStack.firstOrNull(),
                nextPhoto = updatedStack.getOrNull(1),
                currentIndex = currentIndex,
                trashBin = updatedTrash,
                isSessionComplete = updatedStack.isEmpty()
            )
        }
    }

    fun keepPhoto(photo: Photo) {
        _uiState.update { state ->
            val updatedStack = state.photoStack.filterNot { it.id == photo.id }
            val currentIndex = currentIndexFor(state.totalCount, updatedStack.size)
            state.copy(
                photoStack = updatedStack,
                currentPhoto = updatedStack.firstOrNull(),
                nextPhoto = updatedStack.getOrNull(1),
                currentIndex = currentIndex,
                isSessionComplete = updatedStack.isEmpty()
            )
        }
    }

    fun onSwipeLeft(photo: Photo) {
        keepPhoto(photo)
    }

    fun onSwipeRight(photo: Photo) {
        keepPhoto(photo)
    }

    fun openReview() {
        _uiState.update { state ->
            state.copy(isSessionComplete = true)
        }
    }

    fun restoreSelected(photos: List<Photo>) {
        if (photos.isEmpty()) return
        _uiState.update { state ->
            val remaining = state.trashBin.filterNot { trash -> photos.any { it.id == trash.id } }
            state.copy(trashBin = remaining, isSessionComplete = true)
        }
    }

    fun deleteSelected(photos: List<Photo>) {
        if (photos.isEmpty()) return
        pendingDeletePhotos = photos
        pendingDeleteShouldReload = false
        pendingDeleteMode = PendingDeleteMode.RECOVERABLE
        performDelete(photos, shouldReload = false)
    }

    fun confirmBurn() {
        val photos = uiState.value.trashBin
        if (photos.isEmpty()) return
        pendingDeletePhotos = photos
        pendingDeleteShouldReload = true
        pendingDeleteMode = PendingDeleteMode.RECOVERABLE
        performDelete(photos, shouldReload = true)
    }

    fun onDeletePermissionResult(isGranted: Boolean) {
        if (!isGranted) {
            _uiState.update { state ->
                state.copy(pendingDeleteIntent = null)
            }
            pendingDeletePhotos = emptyList()
            pendingDeleteShouldReload = false
            pendingDeleteMode = PendingDeleteMode.NONE
            return
        }

        val photos = pendingDeletePhotos
        val shouldReload = pendingDeleteShouldReload
        if (pendingDeleteMode == PendingDeleteMode.BATCH) {
            _uiState.update { state ->
                val remaining = state.trashBin.filterNot { trash ->
                    photos.any { it.id == trash.id }
                }
                val isComplete = if (shouldReload) false else remaining.isNotEmpty()
                state.copy(
                    trashBin = if (shouldReload) emptyList() else remaining,
                    pendingDeleteIntent = null,
                    isSessionComplete = isComplete
                )
            }
            pendingDeletePhotos = emptyList()
            pendingDeleteShouldReload = false
            pendingDeleteMode = PendingDeleteMode.NONE
            if (shouldReload) {
                loadNewSession()
            }
        } else {
            performDelete(photos, shouldReload)
        }
    }

    private fun currentIndexFor(totalCount: Int, remainingCount: Int): Int {
        if (totalCount == 0) return 0
        return if (remainingCount <= 0) totalCount else totalCount - remainingCount + 1
    }

    private suspend fun loadNewSessionInternal() {
        val mode = _uiState.value.curationMode
        val photos = getSessionUseCase(
            offset = 0,
            mode = mode,
            sessionSize = _uiState.value.sessionSize
        )
        _uiState.update { state ->
            val totalCount = photos.size
            val currentIndex = if (photos.isNotEmpty()) 1 else 0
            state.copy(
                photoStack = photos,
                currentPhoto = photos.firstOrNull(),
                nextPhoto = photos.getOrNull(1),
                currentIndex = currentIndex,
                totalCount = totalCount,
                isSessionComplete = photos.isEmpty()
            )
        }
    }

    private fun observeIndexing() {
        viewModelScope.launch {
            indexingProgressUseCase.progress.collect { progress ->
                _uiState.update { state ->
                    state.copy(
                        indexingProgress = progress,
                        isIndexing = progress in 0..99
                    )
                }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPreferencesRepository.themeModeFlow.collect { mode ->
                _uiState.update { state -> state.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.languageFlow.collect { mode ->
                _uiState.update { state -> state.copy(languageMode = mode) }
            }
        }
    }

    private fun loadNewSession() {
        viewModelScope.launch {
            loadNewSessionInternal()
        }
    }

    private fun performDelete(photos: List<Photo>, shouldReload: Boolean) {
        viewModelScope.launch {
            try {
                deletePhotosUseCase(photos.map { it.uri.toString() })
                _uiState.update { state ->
                    val remaining = state.trashBin.filterNot { trash ->
                        photos.any { it.id == trash.id }
                    }
                    val isComplete = if (shouldReload) false else remaining.isNotEmpty()
                    state.copy(
                        trashBin = if (shouldReload) emptyList() else remaining,
                        pendingDeleteIntent = null,
                        isSessionComplete = isComplete
                    )
                }
                pendingDeletePhotos = emptyList()
                pendingDeleteShouldReload = false
                if (shouldReload) {
                    loadNewSession()
                }
            } catch (e: DeletePermissionRequired) {
                pendingDeleteMode = PendingDeleteMode.BATCH
                _uiState.update { state ->
                    state.copy(pendingDeleteIntent = e.pendingIntent.intentSender)
                }
            } catch (e: RecoverableSecurityException) {
                pendingDeleteMode = PendingDeleteMode.RECOVERABLE
                _uiState.update { state ->
                    state.copy(pendingDeleteIntent = e.userAction.actionIntent.intentSender)
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(pendingDeleteIntent = null)
                }
                pendingDeletePhotos = emptyList()
                pendingDeleteShouldReload = false
                pendingDeleteMode = PendingDeleteMode.NONE
            }
        }
    }

    private var pendingDeletePhotos: List<Photo> = emptyList()
    private var pendingDeleteShouldReload: Boolean = false
    private var pendingDeleteMode: PendingDeleteMode = PendingDeleteMode.NONE

    private enum class PendingDeleteMode {
        NONE,
        RECOVERABLE,
        BATCH
    }
}
