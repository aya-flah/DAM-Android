package com.pianokids.game.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.pianokids.game.R

/**
 * Manages piano sound playback
 * Singleton pattern for efficient resource management
 * 
 * IMPORTANT: You need to add piano sound files to res/raw/ folder
 * Expected files: c4.mp3, d4.mp3, e4.mp3, f4.mp3, g4.mp3, a4.mp3, b4.mp3, etc.
 */
object PianoSoundManager {
    
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()
    private var isInitialized = false
    
    /**
     * Initialize sound pool and load sounds
     * Call this once when app starts or before entering piano screen
     */
    fun init(context: Context) {
        if (isInitialized) return
        
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            
            soundPool = SoundPool.Builder()
                .setMaxStreams(12) // Support multiple simultaneous notes
                .setAudioAttributes(audioAttributes)
                .build()
            
            loadPianoSounds(context)
            isInitialized = true
            
            Log.d("PianoSoundManager", "Piano sounds initialized successfully")
        } catch (e: Exception) {
            Log.e("PianoSoundManager", "Failed to initialize piano sounds", e)
        }
    }
    
    /**
     * Load piano note sound files
     * Using solfège notation: Do, Ré, Mi, Fa, Sol, La, Si
     */
    private fun loadPianoSounds(context: Context) {
        // Map solfège note names to resource IDs
        val noteSounds = mapOf(
            "Do" to R.raw.note_do,
            "Ré" to R.raw.note_re,
            "Mi" to R.raw.note_mi,
            "Fa" to R.raw.note_fa,
            "Sol" to R.raw.note_sol,
            "La" to R.raw.note_la,
            "Si" to R.raw.note_si
        )
        
        soundPool?.let { pool ->
            noteSounds.forEach { (note, resourceId) ->
                try {
                    val soundId = pool.load(context, resourceId, 1)
                    soundMap[note] = soundId
                    Log.d("PianoSoundManager", "Loaded sound for note: $note")
                } catch (e: Exception) {
                    Log.w("PianoSoundManager", "Could not load sound for note: $note - ${e.message}")
                }
            }
        }
    }
    
    /**
     * Play a piano note
     * @param note The note name (Do, Ré, Mi, Fa, Sol, La, Si)
     */
    fun playNote(note: String) {
        if (!isInitialized) {
            Log.w("PianoSoundManager", "Piano sounds not initialized")
            return
        }
        
        soundMap[note]?.let { soundId ->
            soundPool?.play(
                soundId,
                1.0f,  // Left volume
                1.0f,  // Right volume
                1,     // Priority
                0,     // Loop (0 = no loop)
                1.0f   // Playback rate
            )
            Log.d("PianoSoundManager", "Playing note: $note")
        } ?: run {
            Log.w("PianoSoundManager", "Sound not found for note: $note")
        }
    }
    
    /**
     * Stop all currently playing sounds
     */
    fun stopAllSounds() {
        soundPool?.autoPause()
    }
    
    /**
     * Resume sounds after pause
     */
    fun resumeSounds() {
        soundPool?.autoResume()
    }
    
    /**
     * Release resources - call when app is closing
     */
    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
        isInitialized = false
        Log.d("PianoSoundManager", "Piano sounds released")
    }
}

/**
 * Sound files in res/raw (using solfège notation):
 * ✅ res/raw/note_do.mp3  (Do/C - 261.63 Hz)
 * ✅ res/raw/note_re.mp3  (Ré/D - 293.66 Hz)
 * ✅ res/raw/note_mi.mp3  (Mi/E - 329.63 Hz)
 * ✅ res/raw/note_fa.mp3  (Fa/F - 349.23 Hz)
 * ✅ res/raw/note_sol.mp3 (Sol/G - 392.00 Hz)
 * ✅ res/raw/note_la.mp3  (La/A - 440.00 Hz)
 * ✅ res/raw/note_si.mp3  (Si/B - 493.88 Hz)
 */
