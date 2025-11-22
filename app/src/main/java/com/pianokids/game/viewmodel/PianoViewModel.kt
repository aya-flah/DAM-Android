package com.pianokids.game.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pianokids.game.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Piano gameplay
 * Handles all piano interaction logic, scoring, and progression
 * Reusable across all levels
 */
class PianoViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _pianoState = MutableStateFlow(PianoState())
    val pianoState: StateFlow<PianoState> = _pianoState.asStateFlow()
    
    // Batman-themed solfege names
    private val batmanSolfege = mapOf(
        "Do" to "BAT",
        "Ré" to "DARK",
        "Mi" to "KNIGHT",
        "Fa" to "HERO",
        "Sol" to "GOTHAM",
        "La" to "CAPE",
        "Si" to "SIGNAL"
    )
    
    /**
     * Initialize piano for a specific level
     */
    fun initializeLevel(levelNumber: Int, lesson: PianoLesson? = null) {
        val theme = when (levelNumber) {
            1 -> LevelTheme.BATMAN
            2 -> LevelTheme.SPIDERMAN
            3 -> LevelTheme.MOONLIGHT
            4 -> LevelTheme.NARUTO
            5 -> LevelTheme.AVENGERS
            6 -> LevelTheme.POKEMON
            else -> LevelTheme.BATMAN
        }
        
        val config = createConfigForTheme(theme)
        
        _pianoState.value = PianoState(
            config = config,
            currentLesson = lesson,
            levelNumber = levelNumber
        )
    }
    
    /**
     * Handle key press
     */
    fun onKeyPressed(key: PianoKey) {
        val currentState = _pianoState.value
        val newPressedKeys = currentState.pressedKeys + key.note
        
        _pianoState.value = currentState.copy(
            pressedKeys = newPressedKeys,
            lastPressedKey = key
        )
        
        // If in lesson mode, check if correct note
        val lesson = currentState.currentLesson
        if (lesson != null && currentState.gameMode == GameMode.LESSON) {
            checkNote(key, lesson)
        }
    }
    
    /**
     * Handle key release
     */
    fun onKeyReleased(key: PianoKey) {
        val currentState = _pianoState.value
        val newPressedKeys = currentState.pressedKeys - key.note
        
        // Add to played notes history
        val playedNote = PlayedNote(
            key = key,
            timestamp = System.currentTimeMillis(),
            isCorrect = null // Will be set if in lesson mode
        )
        
        _pianoState.value = currentState.copy(
            pressedKeys = newPressedKeys,
            playedNotes = currentState.playedNotes + playedNote
        )
    }
    
    /**
     * Check if played note is correct in lesson mode
     */
    private fun checkNote(key: PianoKey, lesson: PianoLesson) {
        val currentState = _pianoState.value
        val expectedNote = lesson.sequence.getOrNull(currentState.currentNoteIndex)
        
        if (expectedNote != null) {
            val isCorrect = key.solfege == expectedNote || key.note == expectedNote
            
            if (isCorrect) {
                val newScore = currentState.score + 100
                val newIndex = currentState.currentNoteIndex + 1
                val isComplete = newIndex >= lesson.sequence.size
                
                _pianoState.value = currentState.copy(
                    score = newScore,
                    currentNoteIndex = newIndex,
                    isLessonComplete = isComplete,
                    stars = calculateStars(newScore, lesson.sequence.size)
                )
                
                if (isComplete) {
                    onLessonComplete()
                }
            } else {
                // Wrong note - maybe show feedback
                val newErrors = currentState.errorCount + 1
                _pianoState.value = currentState.copy(
                    errorCount = newErrors
                )
            }
        }
    }
    
    /**
     * Calculate stars based on performance
     */
    private fun calculateStars(score: Int, totalNotes: Int): Int {
        val maxScore = totalNotes * 100
        val percentage = (score.toFloat() / maxScore) * 100
        
        return when {
            percentage >= 90 -> 3
            percentage >= 70 -> 2
            percentage >= 50 -> 1
            else -> 0
        }
    }
    
    /**
     * Handle lesson completion
     */
    private fun onLessonComplete() {
        viewModelScope.launch {
            // Here you can save progress to backend
            // authRepository.updateProgress(...)
        }
    }
    
    /**
     * Get themed solfege name for Batman level
     */
    fun getThemedSolfege(solfege: String, theme: LevelTheme): String {
        return if (theme == LevelTheme.BATMAN) {
            batmanSolfege[solfege] ?: solfege
        } else {
            solfege
        }
    }
    
    /**
     * Switch between Free Play and Lesson mode
     */
    fun setGameMode(mode: GameMode) {
        _pianoState.value = _pianoState.value.copy(gameMode = mode)
    }
    
    /**
     * Reset current lesson/session
     */
    fun resetSession() {
        val currentState = _pianoState.value
        _pianoState.value = currentState.copy(
            score = 0,
            currentNoteIndex = 0,
            errorCount = 0,
            playedNotes = emptyList(),
            pressedKeys = emptySet(),
            isLessonComplete = false,
            stars = 0
        )
    }
    
    /**
     * Create configuration for specific theme
     */
    private fun createConfigForTheme(theme: LevelTheme): PianoConfig {
        return when (theme) {
            LevelTheme.BATMAN -> PianoConfig(
                levelTheme = theme,
                noteType = NoteType.SOLFEGE,
                whiteKeyColors = listOf(
                    Color(0xFF1A1A1A), // Dark gray/black
                    Color(0xFF2C2C2C),
                    Color(0xFF3D3D3D),
                    Color(0xFFFFD700), // Gold accent
                    Color(0xFF1A1A1A),
                    Color(0xFF2C2C2C),
                    Color(0xFF3D3D3D),
                    Color(0xFFFFD700),
                    Color(0xFF1A1A1A),
                    Color(0xFF2C2C2C),
                    Color(0xFF3D3D3D),
                    Color(0xFFFFD700)
                ),
                highlightColor = Color(0xFFFFD700), // Gold
                showBatSignal = true,
                backgroundGradient = listOf(
                    Color(0xFF0A0A0A),
                    Color(0xFF1A1A1A),
                    Color(0xFF2D1B4E)
                )
            )
            LevelTheme.SPIDERMAN -> PianoConfig(
                levelTheme = theme,
                noteType = NoteType.SOLFEGE,
                whiteKeyColors = listOf(
                    Color(0xFFE63946), Color(0xFF1D3557), 
                    Color(0xFFE63946), Color(0xFF1D3557)
                ).flatMap { color -> listOf(color, color, color) }, // Red and Blue pattern
                highlightColor = Color(0xFFFFFFFF)
            )
            else -> PianoConfig(levelTheme = theme, noteType = NoteType.SOLFEGE)
        }
    }
}

/**
 * State for Piano gameplay
 */
data class PianoState(
    val config: PianoConfig = PianoConfig(),
    val currentLesson: PianoLesson? = null,
    val levelNumber: Int = 1,
    val gameMode: GameMode = GameMode.FREE_PLAY,
    val pressedKeys: Set<String> = emptySet(),
    val playedNotes: List<PlayedNote> = emptyList(),
    val lastPressedKey: PianoKey? = null,
    val score: Int = 0,
    val stars: Int = 0,
    val currentNoteIndex: Int = 0,
    val errorCount: Int = 0,
    val isLessonComplete: Boolean = false
)

/**
 * Game modes for piano
 */
enum class GameMode {
    FREE_PLAY,  // Just play freely
    LESSON      // Follow the lesson sequence
}
