package com.pianokids.game.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.pianokids.game.R

/**
 * Manages piano sound playback with multiple instruments
 * Singleton pattern for efficient resource management
 * 
 * IMPORTANT: You need to add sound files to res/raw/ folder for each instrument
 */
object PianoSoundManager {
    
    private var soundPool: SoundPool? = null
    private val soundMaps = mutableMapOf<String, MutableMap<String, Int>>() // instrument -> (note -> soundId)
    private var isInitialized = false
    private var currentInstrument = "PIANO"
    
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
     * Load sound files for all instruments
     * Using solfège notation: Do, Ré, Mi, Fa, Sol, La, Si
     */
    private fun loadPianoSounds(context: Context) {
        soundPool?.let { pool ->
            val notes = listOf("Do", "Ré", "Mi", "Fa", "Sol", "La", "Si")
            val instruments = listOf("PIANO", "GUITAR", "VIOLIN", "FLUTE", "XYLOPHONE", "SYNTH")
            
            // Resource name mappings for each instrument
            val resourcePrefixes = mapOf(
                "PIANO" to "note",
                "GUITAR" to "guitar",
                "VIOLIN" to "violin",
                "FLUTE" to "flute",
                "XYLOPHONE" to "xylo",
                "SYNTH" to "synth"
            )
            
            // Note name to resource suffix mapping
            val noteSuffixes = mapOf(
                "Do" to "do",
                "Ré" to "re",
                "Mi" to "mi",
                "Fa" to "fa",
                "Sol" to "sol",
                "La" to "la",
                "Si" to "si"
            )
            
            // Load sounds for each instrument
            instruments.forEach { instrument ->
                val instrumentMap = mutableMapOf<String, Int>()
                val prefix = resourcePrefixes[instrument] ?: "note"
                
                notes.forEach { note ->
                    val suffix = noteSuffixes[note] ?: note.lowercase()
                    val resourceName = "${prefix}_${suffix}"
                    
                    // Try to get resource ID using reflection
                    val resourceId = try {
                        context.resources.getIdentifier(resourceName, "raw", context.packageName)
                    } catch (e: Exception) {
                        0
                    }
                    
                    if (resourceId != 0) {
                        try {
                            val soundId = pool.load(context, resourceId, 1)
                            instrumentMap[note] = soundId
                            Log.d("PianoSoundManager", "Loaded $instrument sound for note: $note ($resourceName)")
                        } catch (e: Exception) {
                            Log.w("PianoSoundManager", "Failed to load $resourceName: ${e.message}")
                        }
                    } else {
                        // Fallback to piano sound if instrument sound not available
                        if (instrument != "PIANO" && soundMaps["PIANO"]?.containsKey(note) == true) {
                            instrumentMap[note] = soundMaps["PIANO"]!![note]!!
                            Log.d("PianoSoundManager", "Using piano fallback for $instrument note: $note")
                        } else {
                            Log.w("PianoSoundManager", "Resource not found: $resourceName")
                        }
                    }
                }
                
                if (instrumentMap.isNotEmpty()) {
                    soundMaps[instrument] = instrumentMap
                    Log.d("PianoSoundManager", "Loaded ${instrumentMap.size} sounds for $instrument")
                }
            }
        }
    }
    
    /**
     * Change the current instrument
     */
    fun setInstrument(instrument: String) {
        if (soundMaps.containsKey(instrument)) {
            currentInstrument = instrument
            Log.d("PianoSoundManager", "Changed instrument to: $instrument")
        } else {
            Log.w("PianoSoundManager", "Instrument not found: $instrument, keeping $currentInstrument")
        }
    }
    
    /**
     * Play a note with the current instrument
     * @param note The note name (Do, Ré, Mi, Fa, Sol, La, Si)
     */
    fun playNote(note: String) {
        if (!isInitialized) {
            Log.w("PianoSoundManager", "Piano sounds not initialized")
            return
        }
        
        soundMaps[currentInstrument]?.get(note)?.let { soundId ->
            soundPool?.play(
                soundId,
                1.0f,  // Left volume
                1.0f,  // Right volume
                1,     // Priority
                0,     // Loop (0 = no loop)
                1.0f   // Playback rate
            )
            Log.d("PianoSoundManager", "Playing $currentInstrument note: $note")
        } ?: run {
            Log.w("PianoSoundManager", "Sound not found for $currentInstrument note: $note")
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
        soundMaps.clear()
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
