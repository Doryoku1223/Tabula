package com.tabula.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Loading : HomeUiState
    object PermissionRequired : HomeUiState
    object PermissionDenied : HomeUiState
    data class Success(
        val monthLabel: String,
        val remainingCount: Int
    ) : HomeUiState
}

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun setInitialPermission(granted: Boolean) {
        if (granted) {
            startLoading()
        } else {
            _uiState.value = HomeUiState.PermissionRequired
        }
    }

    fun onPermissionResult(granted: Boolean, shouldShowRationale: Boolean) {
        if (granted) {
            startLoading()
            return
        }
        _uiState.value = if (shouldShowRationale) {
            HomeUiState.PermissionRequired
        } else {
            HomeUiState.PermissionDenied
        }
    }

    private fun startLoading() {
        loadJob?.cancel()
        _uiState.value = HomeUiState.Loading
        loadJob = viewModelScope.launch {
            delay(600)
            _uiState.value = HomeUiState.Success(
                monthLabel = "October 2025",
                remainingCount = 120
            )
        }
    }
}
