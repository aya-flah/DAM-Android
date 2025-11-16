package com.pianokids.game.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LevelOneState(
    val isLoading: Boolean = false,
    val selectedMode: PlayMode? = null,
    val showModeSelection: Boolean = true
)

enum class PlayMode {
    APP_PIANO,
    REAL_PIANO
}

class LevelOneViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(LevelOneState())
    val uiState: StateFlow<LevelOneState> = _uiState.asStateFlow()
    
    fun selectPlayMode(mode: PlayMode) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedMode = mode,
                showModeSelection = false
            )
        }
    }
    
    fun backToModeSelection() {
        _uiState.value = _uiState.value.copy(
            selectedMode = null,
            showModeSelection = true
        )
    }
    
    fun setLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = loading)
    }
}
