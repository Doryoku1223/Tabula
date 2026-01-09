package com.tabula.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tabula.data.CurationMode
import com.tabula.data.Photo
import com.tabula.data.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.net.Uri

data class HomeUiState(
    val monthLabel: String = "2024 OCT",
    val photos: List<Photo> = emptyList(),
    val kept: List<Photo> = emptyList(),
    val discarded: List<Photo> = emptyList(),
    val isSetComplete: Boolean = false,
    val isLoading: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PhotoRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadedOnce = false
    var totalReviewed by mutableStateOf(0)
        private set
    var keptCount by mutableStateOf(0)
        private set

    fun loadSessionIfNeeded() {
        if (loadedOnce) return
        loadedOnce = true
        loadSession()
    }

    fun refreshSet() {
        loadedOnce = false
        loadSessionIfNeeded()
    }

    fun swipeLeft(photo: Photo) {
        totalReviewed += 1
        updateAfterSwipe(photo, keep = false)
    }

    fun swipeRight(photo: Photo) {
        totalReviewed += 1
        keptCount += 1
        updateAfterSwipe(photo, keep = true)
    }

    fun loadNextBatch() {
        totalReviewed = 0
        keptCount = 0
        _uiState.value = HomeUiState(
            monthLabel = "2024 OCT",
            photos = buildDummySet(),
            isSetComplete = false,
            isLoading = false
        )
    }

    private fun loadSession() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch(Dispatchers.IO) {
            repository.refreshIndex()
            val photos = repository.getPhotos(0, 15, CurationMode.RANDOM)
            val month = photos.firstOrNull()?.let { formatMonth(it) } ?: "2024 OCT"
            withContext(Dispatchers.Main) {
                _uiState.value = HomeUiState(
                    monthLabel = month,
                    photos = photos,
                    isSetComplete = photos.isEmpty(),
                    isLoading = false
                )
            }
        }
    }

    private fun updateAfterSwipe(photo: Photo, keep: Boolean) {
        val current = _uiState.value
        val remaining = current.photos.drop(1)
        val kept = if (keep) current.kept + photo else current.kept
        val discarded = if (keep) current.discarded else current.discarded + photo
        _uiState.value = current.copy(
            photos = remaining,
            kept = kept,
            discarded = discarded,
            isSetComplete = remaining.isEmpty()
        )
    }

    private fun formatMonth(photo: Photo): String {
        val formatter = SimpleDateFormat("yyyy MMM", Locale.US)
        return formatter.format(Date(photo.dateAdded * 1000L)).uppercase(Locale.US)
    }

    private fun buildDummySet(): List<Photo> {
        val now = System.currentTimeMillis() / 1000L
        return (1L..15L).map { id ->
            Photo(
                id = id,
                uri = Uri.EMPTY,
                dateAdded = now - id
            )
        }
    }
}
