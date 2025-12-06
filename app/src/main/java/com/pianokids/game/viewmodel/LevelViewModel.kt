package com.pianokids.game.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pianokids.game.data.models.Level
import com.pianokids.game.data.models.Sublevel
import com.pianokids.game.data.models.SublevelProgressRequest
import com.pianokids.game.data.repository.LevelRepository
import com.pianokids.game.data.repository.SublevelProgressRepository
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
    val wrongMessage: String? = null,
    val sublevels: List<Sublevel> = emptyList(),
    val selectedSublevel: Sublevel? = null,
    val feedbackMessage: String? = null,
    val isFeedbackPositive: Boolean = true,
    val noteDurations: List<Float> = emptyList(),
    val isAwaitingCorrect: Boolean = false
)

class LevelViewModel : ViewModel() {

    private val repository = LevelRepository()

    private val _uiState = MutableStateFlow(LevelUiState())
    val uiState: StateFlow<LevelUiState> = _uiState

    private var expectedNotes: List<String> = emptyList()
    private var noteDurations: List<Float> = emptyList()
    private var lastPlayedNormalized: String? = null
    private var wrongStreak: Int = 0
    private var lastCorrectTimeMs: Long = 0

    /**
     * Load unlocked levels for a user (original behaviour for now)
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
                val adjustedLevel = adjustLevelIfNeeded(level)

                expectedNotes = adjustedLevel.expectedNotes
                noteDurations = buildDurationsFor(adjustedLevel)

                _uiState.value = _uiState.value.copy(
                    currentLevel = adjustedLevel,
                    progressPercentage = 0f,
                    currentNoteIndex = 0,
                    isLevelCompleted = false,
                    isFailed = false,
                    wrongNoteCount = 0,
                    wrongMessage = null,
                    feedbackMessage = null,
                    isFeedbackPositive = true,
                    noteDurations = noteDurations,
                    isAwaitingCorrect = false,
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
        if (index >= expectedNotes.size) return

        // Normalize notes
        fun normalize(s: String): String {
            var normalized = s.lowercase()
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("à", "a")
                .replace("ù", "u")
                .replace("ô", "o")
                .replace("♭", "b")
                .replace("♯", "#")
                .trim()

            val enharmonicMap = mapOf(
                "do#" to "reb",
                "re#" to "mib",
                "fa#" to "solb",
                "sol#" to "lab",
                "la#" to "sib",
                "dob" to "si",
                "fab" to "mi",
                "solb" to "fa#",
                "lab" to "sol#"
            )

            return enharmonicMap[normalized] ?: normalized
        }

        val expected = normalize(expectedNotes[index])
        val played = normalize(note)

        Log.d("LevelViewModel", "Played='$played' Expected='$expected'")

        // ---------------------------
        // CORRECT NOTE
        // ---------------------------
        if (played == expected) {
            wrongStreak = 0
            lastCorrectTimeMs = System.currentTimeMillis()
            val newIndex = index + 1
            val progress = newIndex.toFloat() / expectedNotes.size.toFloat()

            if (newIndex == expectedNotes.size) {
                // Completed!
                _uiState.value = state.copy(
                    currentNoteIndex = newIndex,
                    progressPercentage = 1f,
                    wrongNoteCount = state.wrongNoteCount,
                    showWrongAnimation = false,
                    wrongMessage = null,
                    feedbackMessage = "Mission complete! 🎉",
                    isFeedbackPositive = true,
                    isAwaitingCorrect = false,
                    isLevelCompleted = true
                )
            } else {
                _uiState.value = state.copy(
                    currentNoteIndex = newIndex,
                    progressPercentage = progress,
                    showWrongAnimation = false,
                    wrongMessage = null,
                    feedbackMessage = "Great job!",
                    isFeedbackPositive = true,
                    isAwaitingCorrect = false
                )
            }
            return
        }

        // ---------------------------
        // WRONG NOTE
        // ---------------------------
        val now = System.currentTimeMillis()

        if (now - lastCorrectTimeMs < 300) {
            _uiState.value = state.copy(
                feedbackMessage = "Almost!",
                isFeedbackPositive = false,
                isAwaitingCorrect = true
            )
            return
        }

        wrongStreak = if (played == lastPlayedNormalized) wrongStreak + 1 else 1
        lastPlayedNormalized = played

        if (wrongStreak < 2) {
            _uiState.value = state.copy(
                feedbackMessage = "Almost!",
                isFeedbackPositive = false,
                isAwaitingCorrect = true
            )
            return
        }

        val newWrongCount = state.wrongNoteCount + 1

        if (newWrongCount >= 3) {
            _uiState.value = state.copy(
                isFailed = true,
                wrongNoteCount = 3,
                showWrongAnimation = false,
                feedbackMessage = "We'll try again!",
                isFeedbackPositive = false,
                isAwaitingCorrect = true
            )
            return
        }

        _uiState.value = state.copy(
            showWrongAnimation = true,
            wrongNoteCount = newWrongCount,
            feedbackMessage = "Oops!",
            isFeedbackPositive = false,
            isAwaitingCorrect = true
        )
    }

    // Load sublevels
    fun loadSublevels(userId: String, levelId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val response = sublevelProgressRepo.getUserSublevels(userId, levelId)

            _uiState.value = _uiState.value.copy(
                sublevels = response ?: emptyList(),
                isLoading = false
            )
        }
    }

    fun selectSublevel(sublevel: Sublevel) {
        expectedNotes = sublevel.notes
        noteDurations = List(sublevel.notes.size) { 1f }

        _uiState.value = _uiState.value.copy(
            selectedSublevel = sublevel,
            currentNoteIndex = 0,
            progressPercentage = 0f,
            isLevelCompleted = false,
            isFailed = false,
            wrongNoteCount = 0,
            wrongMessage = null,
            noteDurations = noteDurations
        )
    }

    private val sublevelProgressRepo = SublevelProgressRepository()

    /**
     * ⭐⭐⭐ MISTAKE-BASED STAR SYSTEM ⭐⭐⭐
     */
    fun saveProgress(userId: String) {
        val state = _uiState.value
        val level = state.currentLevel ?: return
        val sublevel = state.selectedSublevel ?: return

        val wrongs = state.wrongNoteCount

        val stars = when (wrongs) {
            0 -> 3
            1 -> 2
            2 -> 1
            else -> 0
        }

        viewModelScope.launch {
            val request = SublevelProgressRequest(
                userId = userId,
                levelId = level._id,
                sublevelId = sublevel._id,
                stars = stars,
                score = 0,
                completed = true
            )

            val updatedSubs = sublevelProgressRepo.submitProgress(request)

            if (updatedSubs != null) {
                _uiState.value = _uiState.value.copy(
                    sublevels = updatedSubs,
                    selectedSublevel = updatedSubs.firstOrNull { it._id == sublevel._id }
                )
            }
        }
    }

    fun clearWrongAnimation() {
        _uiState.value = _uiState.value.copy(showWrongAnimation = false)
    }

    private fun adjustLevelIfNeeded(level: Level): Level = level

    private fun buildDurationsFor(level: Level): List<Float> {
        return if (level.theme.equals("Batman", ignoreCase = true)) {
            listOf(1.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 1.5f)
        } else {
            List(level.expectedNotes.size) { 1f }
        }
    }
}
