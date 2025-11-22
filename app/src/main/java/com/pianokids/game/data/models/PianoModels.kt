package com.pianokids.game.data.models

import androidx.compose.ui.graphics.Color

/**
 * Represents a single piano key with its musical properties
 */
data class PianoKey(
    val note: String,              // "C", "D", "E", etc.
    val solfege: String,           // "Do", "Ré", "Mi", etc.
    val octave: Int,               // 4, 5, etc.
    val isBlackKey: Boolean,       // true for sharp/flat keys
    val soundResourceId: Int,      // R.raw.c4, R.raw.d4, etc.
    val frequency: Float,          // Hz for pitch detection
    val displayLabel: String       // What shows on the key (depends on NoteType)
)

/**
 * Configuration for piano appearance and behavior
 * This allows each level to customize the piano without changing core logic
 */
data class PianoConfig(
    val levelTheme: LevelTheme = LevelTheme.BATMAN,
    val noteType: NoteType = NoteType.SOLFEGE,
    val instrument: InstrumentType = InstrumentType.PIANO,
    val whiteKeyColors: List<Color> = rainbowColors(),
    val blackKeyColor: Color = Color(0xFF2C2C2C),
    val highlightColor: Color = Color(0xFFFFD700),
    val showLabels: Boolean = true,
    val enableMultiTouch: Boolean = true,
    val showBatSignal: Boolean = false, // Special Batman theme feature
    val backgroundGradient: List<Color> = listOf(
        Color(0xFF2D1B4E),
        Color(0xFF4A2C5E)
    )
)

/**
 * Theme for each level
 */
enum class LevelTheme {
    BATMAN,      // Level 1 - Dark knight theme
    SPIDERMAN,   // Level 2 - Red/blue theme
    MOONLIGHT,   // Level 3 - Night theme
    NARUTO,      // Level 4 - Orange/ninja theme
    AVENGERS,    // Level 5 - Hero theme
    POKEMON      // Level 6 - Colorful theme
}

/**
 * How notes are displayed on keys
 */
enum class NoteType {
    SOLFEGE,  // Do, Ré, Mi, Fa, Sol, La, Si
    LETTER    // C, D, E, F, G, A, B
}

/**
 * Instrument types for different sound sets
 */
enum class InstrumentType {
    PIANO,      // Classic piano sound
    GUITAR,     // Acoustic guitar
    VIOLIN,     // Violin/strings
    FLUTE,      // Flute/woodwind
    XYLOPHONE,  // Xylophone/percussion
    SYNTH       // Synthesizer/electronic
}

/**
 * State of a piano key during interaction
 */
enum class KeyState {
    IDLE,
    PRESSED,
    CORRECT,   // For tutorial/learning mode
    INCORRECT  // For tutorial/learning mode
}

/**
 * Represents a played note in the game
 */
data class PlayedNote(
    val key: PianoKey,
    val timestamp: Long,
    val isCorrect: Boolean? = null  // null for free play mode
)

/**
 * Level-specific piano lesson data
 */
data class PianoLesson(
    val levelNumber: Int,
    val lessonName: String,
    val notesToLearn: List<String>,  // e.g., ["Do", "Ré", "Mi"]
    val sequence: List<String>,      // Notes to play in order
    val difficulty: LessonDifficulty,
    val description: String
)

enum class LessonDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

/**
 * Helper function to generate rainbow colors for white keys
 */
fun rainbowColors(): List<Color> = listOf(
    Color(0xFFFF6B6B), // Red
    Color(0xFFFF8E53), // Orange  
    Color(0xFFFFA500), // Orange
    Color(0xFFFFD93D), // Yellow
    Color(0xFFFFED4E), // Yellow
    Color(0xFF6BCF7F), // Green
    Color(0xFF4D96FF), // Light Blue
    Color(0xFF6C5CE7), // Purple
    Color(0xFFA29BFE), // Light Purple
    Color(0xFFFF6B9D), // Pink
    Color(0xFFFF8FAB), // Light Pink
    Color(0xFFC56CF0)  // Purple
)

/**
 * Creates default piano keys for one octave (C4 to B4)
 * This is the foundation for building the full keyboard
 */
fun createPianoKeys(noteType: NoteType = NoteType.SOLFEGE, startOctave: Int = 4): List<PianoKey> {
    val keys = mutableListOf<PianoKey>()
    
    // Define all notes in an octave with their properties
    val noteDefinitions = listOf(
        Triple("C", "Do", false),
        Triple("C#", "Do#", true),
        Triple("D", "Ré", false),
        Triple("D#", "Ré#", true),
        Triple("E", "Mi", false),
        Triple("F", "Fa", false),
        Triple("F#", "Fa#", true),
        Triple("G", "Sol", false),
        Triple("G#", "Sol#", true),
        Triple("A", "La", false),
        Triple("A#", "La#", true),
        Triple("B", "Si", false)
    )
    
    // Standard piano frequencies (A4 = 440Hz)
    val baseFrequencies = mapOf(
        "C" to 261.63f, "C#" to 277.18f, "D" to 293.66f, "D#" to 311.13f,
        "E" to 329.63f, "F" to 349.23f, "F#" to 369.99f, "G" to 392.00f,
        "G#" to 415.30f, "A" to 440.00f, "A#" to 466.16f, "B" to 493.88f
    )
    
    noteDefinitions.forEach { (letter, solfege, isBlack) ->
        val displayLabel = when (noteType) {
            NoteType.SOLFEGE -> solfege
            NoteType.LETTER -> letter
        }
        
        keys.add(
            PianoKey(
                note = letter,
                solfege = solfege,
                octave = startOctave,
                isBlackKey = isBlack,
                soundResourceId = 0, // Will be set by SoundManager
                frequency = baseFrequencies[letter] ?: 440f,
                displayLabel = displayLabel
            )
        )
    }
    
    return keys
}
