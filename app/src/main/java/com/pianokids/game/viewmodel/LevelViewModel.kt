package com.pianokids.game.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pianokids.game.data.models.Level
import com.pianokids.game.data.models.PlayHistoryRequest
import com.pianokids.game.data.models.Sublevel
import com.pianokids.game.data.models.SublevelProgressRequest
import com.pianokids.game.data.repository.LevelHistoryRepository
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
    private val historyRepository = LevelHistoryRepository()
    private val playedNotes = mutableListOf<String>()
    private val playedDurations = mutableListOf<Float>()
    private var historySessionStart = 0L
    private var lastNoteTimestamp = 0L
    private var historySaved = false

    /**
     * Load a single level by its backend id
     */
    fun loadLevel(levelId: String) {
        resetHistoryTracking()
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

        if (played.isNotBlank()) {
            recordNoteForHistory(played)
        }

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
        resetHistoryTracking()
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

            if (level != null && sublevel != null) {
                submitHistoryIfNeeded(userId, level, sublevel, stars, wrongs)
            }
        }
    }

    fun clearWrongAnimation() {
        _uiState.value = _uiState.value.copy(showWrongAnimation = false)
    }

    private fun resetHistoryTracking() {
        playedNotes.clear()
        playedDurations.clear()
        lastNoteTimestamp = 0L
        historySessionStart = 0L
        historySaved = false
    }

    private fun recordNoteForHistory(note: String) {
        if (note.isBlank()) return
        val now = System.currentTimeMillis()
        val duration = if (lastNoteTimestamp == 0L) 500L else (now - lastNoteTimestamp).coerceIn(120L, 2500L)
        playedNotes.add(note)
        playedDurations.add(duration / 1000f)
        lastNoteTimestamp = now
        if (historySessionStart == 0L) {
            historySessionStart = now
        }
    }

    private suspend fun submitHistoryIfNeeded(
        userId: String,
        level: Level,
        sublevel: Sublevel,
        stars: Int,
        wrongNotes: Int
    ) {
        if (historySaved || playedNotes.isEmpty()) return
        val durationMs = if (historySessionStart > 0L) {
            System.currentTimeMillis() - historySessionStart
        } else {
            0L
        }

        val sanitizedDurations = playedDurations.map { it.coerceAtLeast(0.1f) }

        val historyRequest = PlayHistoryRequest(
            userId = userId,
            levelId = level._id,
            sublevelId = sublevel._id,
            notes = playedNotes.toList(),
            noteDurations = sanitizedDurations,
            durationMs = durationMs,
            stars = stars,
            completed = true,
            wrongNotes = wrongNotes
        )

        historyRepository.saveHistory(historyRequest)
        historySaved = true
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
