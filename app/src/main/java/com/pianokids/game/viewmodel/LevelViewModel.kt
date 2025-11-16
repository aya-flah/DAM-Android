package com.pianokids.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pianokids.game.data.models.Level
import com.pianokids.game.data.models.LevelProgressRequest
import com.pianokids.game.data.repository.LevelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LevelUiState(
    val isLoading: Boolean = false,
    val currentLevel: Level? = null,
    val unlockedLevels: Map<String, Boolean> = emptyMap(),
    val progressPercentage: Float = 0f,
    val currentNoteIndex: Int = 0,
    val isLevelCompleted: Boolean = false,
    val isFailed: Boolean = false,
    val wrongNoteCount: Int = 0,
    val showWrongAnimation: Boolean = false,
    val wrongMessage: String? = null
)

class LevelViewModel : ViewModel() {

    private val repository = LevelRepository()

    private val _uiState = MutableStateFlow(LevelUiState())
    val uiState: StateFlow<LevelUiState> = _uiState

    private var expectedNotes: List<String> = emptyList()

    /**
     * Load unlocked levels for a user
     */
    fun loadUnlockedLevels(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val unlockedResponse = repository.getUnlockedLevels(userId)

            val unlockedMap = unlockedResponse?.levels?.associate {
                it.levelId to it.unlocked
            } ?: emptyMap()

            _uiState.value = _uiState.value.copy(
                unlockedLevels = unlockedMap,
                isLoading = false
            )
        }
    }

    /**
     * Load a single level by its backend id
     */
    fun loadLevel(levelId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val level = repository.getLevelById(levelId)

            if (level != null) {
                expectedNotes = level.expectedNotes

                _uiState.value = _uiState.value.copy(
                    currentLevel = level,
                    progressPercentage = 0f,
                    currentNoteIndex = 0,
                    isLevelCompleted = false,
                    isFailed = false,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Called when user presses a piano note
     */
    fun onNotePlayed(note: String) {
        val state = _uiState.value
        val index = state.currentNoteIndex

        if (state.isLevelCompleted || state.isFailed) return
        if (expectedNotes.isEmpty()) return

        val expected = expectedNotes[index].lowercase()

        // ---------------------------
        // CORRECT NOTE
        // ---------------------------
        if (note.lowercase() == expected) {

            val newIndex = index + 1
            val progress = newIndex.toFloat() / expectedNotes.size.toFloat()

            // Reset wrong-note count + hide previous error
            _uiState.value = state.copy(
                wrongNoteCount = 0,
                showWrongAnimation = false,
                wrongMessage = null
            )

            // If last note → complete
            if (newIndex == expectedNotes.size) {
                _uiState.value = _uiState.value.copy(
                    currentNoteIndex = newIndex,
                    progressPercentage = 1f,
                    isLevelCompleted = true
                )
            } else {
                // Normal progress advance
                _uiState.value = _uiState.value.copy(
                    currentNoteIndex = newIndex,
                    progressPercentage = progress
                )
            }

            return
        }

        // ---------------------------
        // WRONG NOTE
        // ---------------------------
        val newWrongCount = state.wrongNoteCount + 1

        if (newWrongCount >= 3) {
            _uiState.value = state.copy(
                isFailed = true,
                wrongNoteCount = 0,
                showWrongAnimation = false,
                wrongMessage = null
            )
            return
        }

        _uiState.value = state.copy(
            wrongNoteCount = newWrongCount,
            showWrongAnimation = true,
            wrongMessage = "Wrong note! Try again!"
        )
    }


    /**
     * Save progress
     */
    fun saveProgress(userId: String) {
        val level = _uiState.value.currentLevel ?: return

        viewModelScope.launch {
            val score = (_uiState.value.progressPercentage * 100).toInt()

            val stars = when {
                score >= 90 -> 3
                score >= 70 -> 2
                score >= 40 -> 1
                else -> 0
            }

            repository.saveProgress(
                LevelProgressRequest(
                    userId = userId,
                    levelId = level._id,     // ✔ updated from _id → id
                    stars = stars,
                    score = score,
                    completed = true
                )
            )
        }
    }

    fun clearWrongAnimation() {
        _uiState.value = _uiState.value.copy(showWrongAnimation = false)
    }
}
