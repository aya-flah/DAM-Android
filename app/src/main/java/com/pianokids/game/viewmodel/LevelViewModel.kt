package com.pianokids.game.viewmodel

import android.util.Log
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
    val wrongMessage: String? = null,
    val score: Int = 0,
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
                    score = 0,
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

        // Guard clauses
        if (state.isLevelCompleted || state.isFailed) return
        if (expectedNotes.isEmpty()) return
        if (index >= expectedNotes.size) return

        // Normalize function to remove accents and standardize
        fun normalize(s: String): String {
            var normalized = s.lowercase()
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("à", "a")
                .replace("ù", "u")
                .replace("ô", "o")
                .replace("♭", "b")  // Flatten symbol
                .replace("♯", "#")  // Sharp symbol
                .trim()
            
            // Handle enharmonic equivalents (same note, different names)
            // C# = Db (Do# = Ré♭), D# = Eb (Ré# = Mi♭), etc.
            val enharmonicMap = mapOf(
                // Sharp to flat conversions
                "do#" to "reb",    // C# = Db (Ré♭)
                "re#" to "mib",    // D# = Eb (Mi♭)
                "fa#" to "solb",   // F# = Gb (Sol♭)
                "sol#" to "lab",   // G# = Ab (La♭)
                "la#" to "sib",    // A# = Bb (Si♭)
                // Flat names stay the same
                "reb" to "reb",
                "mib" to "mib", 
                "solb" to "solb",
                "lab" to "lab",
                "sib" to "sib",
                // Also handle "dob" = "si", "mib" = "re#", etc.
                "dob" to "si",
                "fab" to "mi",
                "solb" to "fa#",
                "lab" to "sol#",
                "sib" to "la#"
            )
            
            // Convert enharmonic equivalents
            return enharmonicMap[normalized] ?: normalized
        }

        val expected = normalize(expectedNotes[index])
        val played = normalize(note)
        
        // Debug logging
        Log.d("LevelViewModel", "RAW Note played: '$note' | RAW Expected: '${expectedNotes[index]}'")
        Log.d("LevelViewModel", "NORMALIZED played: '$played' | NORMALIZED expected: '$expected' | Match: ${played == expected}")

        // ---------------------------
        // CORRECT NOTE
        // ---------------------------
        if (played == expected) {
            // Reset wrong streak on correct
            wrongStreak = 0
            lastCorrectTimeMs = System.currentTimeMillis()
            val newIndex = index + 1
            val progress = newIndex.toFloat() / expectedNotes.size.toFloat()

            // ⭐ Correct → Increase score
            val newScore = (state.score + 10).coerceAtMost(100)

            if (newIndex == expectedNotes.size) {
                // Level completed
                _uiState.value = state.copy(
                    currentNoteIndex = newIndex,
                    progressPercentage = 1f,
                    score = newScore,
                    wrongNoteCount = 0,
                    showWrongAnimation = false,
                    wrongMessage = null,
                    feedbackMessage = "Mission complete! Gotham is safe! 🎉",
                    isFeedbackPositive = true,
                    isAwaitingCorrect = false,
                    isLevelCompleted = true
                )
            } else {
                // Normal progress
                _uiState.value = state.copy(
                    currentNoteIndex = newIndex,
                    progressPercentage = progress,
                    score = newScore,
                    wrongNoteCount = 0,
                    showWrongAnimation = false,
                    wrongMessage = null,
                    feedbackMessage = "Great ear! ${expected.uppercase()} sounded perfect!",
                    isFeedbackPositive = true,
                    isAwaitingCorrect = false
                )
            }
            return
        }

        // ---------------------------
        // WRONG NOTE
        // ---------------------------
        // Filter: ignore brief wrongs immediately after a correct (300ms grace)
        val now = System.currentTimeMillis()
        if (now - lastCorrectTimeMs < 300) {
            Log.d("LevelViewModel", "Grace period: ignoring wrong within 300ms after correct")
            _uiState.value = state.copy(
                feedbackMessage = "Almost! Hit ${expected.uppercase()} next!",
                isFeedbackPositive = false,
                isAwaitingCorrect = true
            )
            return
        }

        // Require two consecutive wrongs before counting
        wrongStreak = if (played == lastPlayedNormalized) wrongStreak + 1 else 1
        lastPlayedNormalized = played

        if (wrongStreak < 2) {
            Log.d("LevelViewModel", "Wrong streak ${wrongStreak}/2: not counting yet")
            _uiState.value = state.copy(
                feedbackMessage = "Almost! Hit ${expected.uppercase()} next!",
                isFeedbackPositive = false,
                isAwaitingCorrect = true
            )
            return
        }

        val newWrongCount = state.wrongNoteCount + 1
        val newScore = (state.score - 5).coerceAtLeast(0)

        if (newWrongCount >= 3) {
            _uiState.value = state.copy(
                isFailed = true,
                wrongNoteCount = 3,
                showWrongAnimation = false,
                wrongMessage = null,
                score = newScore,
                feedbackMessage = "We'll try again! ${expected.uppercase()} was the note.",
                isFeedbackPositive = false,
                isAwaitingCorrect = true
            )
            return
        }

        _uiState.value = state.copy(
            showWrongAnimation = true,
            wrongNoteCount = newWrongCount,
            score = newScore,
            wrongMessage = "Wrong note! Try again!",
            feedbackMessage = "Oops! We needed ${expected.uppercase()}!",
            isFeedbackPositive = false,
            isAwaitingCorrect = true
        )
    }

    /**
     * Save progress
     */
    fun saveProgress(userId: String) {
        val level = _uiState.value.currentLevel ?: return

        viewModelScope.launch {
            val score = _uiState.value.score

            val stars = when {
                score >= 85 -> 3
                score >= 60 -> 2
                score >= 30 -> 1
                else -> 0
            }

            repository.saveProgress(
                LevelProgressRequest(
                    userId = userId,
                    levelId = level._id,
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

    private fun adjustLevelIfNeeded(level: Level): Level {
        return if (level.theme.equals("Batman", ignoreCase = true)) {
            level.copy(
                expectedNotes = listOf("la", "sol", "fa", "re", "re", "re", "do")
            )
        } else {
            level
        }
    }

    private fun buildDurationsFor(level: Level): List<Float> {
        return if (level.theme.equals("Batman", ignoreCase = true)) {
            listOf(
                1.5f,  // La (1½ beats)
                0.5f,  // Sol (½ beat)
                0.5f,  // Fa (½ beat)
                0.5f,  // Re (½ beat)
                0.5f,  // Re (½ beat)
                0.5f,  // Re (½ beat)
                1.5f   // Do (1½ beats)
            )
        } else {
            List(level.expectedNotes.size) { 1f }
        }
    }
}