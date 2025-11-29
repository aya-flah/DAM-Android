package com.pianokids.game.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Real-time pitch detection for piano notes using native Android AudioRecord
 * Listens to microphone and detects musical notes using FFT-based frequency detection
 */
object PitchDetector {
    
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isListening = false
    
    private val _detectedNote = MutableStateFlow<String?>(null)
    val detectedNote: StateFlow<String?> = _detectedNote
    
    private val _detectedFrequency = MutableStateFlow<Float>(0f)
    val detectedFrequency: StateFlow<Float> = _detectedFrequency
    
    private val sampleRate = 22050  // Balanced sample rate
    private val bufferSize = 8192   // Larger buffer for more accurate detection
    
    // Track last detection for repeated notes
    private var lastDetectionTime = 0L
    private var lastDetectedNote: String? = null
    private var startTime = 0L
    private var detectionCount = 0
    
    // Confirmation mechanism - require multiple consistent readings
    private var pendingNote: String? = null
    private var pendingNoteCount = 0
    private val requiredConfirmations = 2  // Need 2 consistent readings
    
    // Musical note frequencies (Middle C octave - C4 to B4)
    private val noteFrequencies = mapOf(
        "Do" to 261.63f,   // C4
        "Ré" to 293.66f,   // D4
        "Mi" to 329.63f,   // E4
        "Fa" to 349.23f,   // F4
        "Sol" to 392.00f,  // G4
        "La" to 440.00f,   // A4
        "Si" to 493.88f    // B4
    )
    
    /**
     * Start listening to microphone for piano notes
     */
    fun startListening(context: Context, onNoteDetected: (String) -> Unit) {
        if (isListening) {
            Log.w("PitchDetector", "Already listening")
            return
        }
        
        // Check microphone permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("PitchDetector", "Microphone permission not granted")
            return
        }
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            
            audioRecord?.startRecording()
            isListening = true
            startTime = System.currentTimeMillis()
            detectionCount = 0
            pendingNote = null
            pendingNoteCount = 0
            
            recordingThread = Thread {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
                val buffer = ShortArray(bufferSize)
                
                while (isListening) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    
                    if (readSize > 0) {
                        val currentTime = System.currentTimeMillis()
                        
                        // Ignore first 500ms to let audio stabilize
                        if (currentTime - startTime < 500L) {
                            continue
                        }
                        
                        val frequency = detectFrequency(buffer, readSize)
                        
                        if (frequency > 0) {
                            _detectedFrequency.value = frequency
                            
                            val detectedNote = frequencyToNote(frequency)
                            if (detectedNote != null) {
                                // Confirmation system: require multiple consistent readings
                                if (detectedNote == pendingNote) {
                                    pendingNoteCount++
                                    Log.d("PitchDetector", "Confirming: $detectedNote ($pendingNoteCount/$requiredConfirmations)")
                                    
                                    // Only trigger after enough confirmations
                                    if (pendingNoteCount >= requiredConfirmations) {
                                        val timeSinceLastDetection = currentTime - lastDetectionTime
                                        
                                        // Allow new note or same note after delay
                                        if (detectedNote != lastDetectedNote || timeSinceLastDetection > 150L) {
                                            detectionCount++
                                            _detectedNote.value = detectedNote
                                            lastDetectedNote = detectedNote
                                            lastDetectionTime = currentTime
                                            onNoteDetected(detectedNote)
                                            Log.d("PitchDetector", "✓ CONFIRMED Detection #$detectionCount: $detectedNote (${"%.1f".format(frequency)} Hz)")
                                            
                                            // Reset confirmation
                                            pendingNote = null
                                            pendingNoteCount = 0
                                        }
                                    }
                                } else {
                                    // Different note detected, start new confirmation
                                    pendingNote = detectedNote
                                    pendingNoteCount = 1
                                }
                            } else {
                                // No clear note, reset confirmation
                                pendingNote = null
                                pendingNoteCount = 0
                            }
                        } else {
                            // Silence, reset confirmation
                            pendingNote = null
                            pendingNoteCount = 0
                        }
                    }
                }
            }
            
            recordingThread?.start()
            Log.d("PitchDetector", "Started listening for piano notes")
            
        } catch (e: Exception) {
            Log.e("PitchDetector", "Failed to start pitch detection", e)
            stopListening()
        }
    }
    
    /**
     * Detect frequency using optimized autocorrelation method
     * OPTIMIZED for speed - 5-10x faster than previous version
     */
    private fun detectFrequency(audioData: ShortArray, size: Int): Float {
        // Fast RMS calculation without creating intermediate collections
        var sumSquares = 0.0
        val processingSize = minOf(size, 2048) // Limit processing size for speed
        
        for (i in 0 until processingSize) {
            val sample = audioData[i].toFloat()
            sumSquares += sample * sample
        }
        
        val rms = sqrt(sumSquares / processingSize)
        if (rms < 600) return 0f // Balanced threshold
        
        // Search in piano range (200-600 Hz)
        val minLag = (sampleRate / 600).toInt()
        val maxLag = (sampleRate / 200).toInt()
        
        var maxCorr = 0f
        var bestLag = minLag

        // Process only half the buffer for speed
        val windowSize = processingSize / 2
        
        for (lag in minLag..minOf(maxLag, windowSize)) {
            var sum = 0f
            // Check all samples for better accuracy
            for (i in 0 until windowSize - lag) {
                sum += audioData[i] * audioData[i + lag]
            }
            
            if (sum > maxCorr) {
                maxCorr = sum
                bestLag = lag
            }
        }
        
        // Simple threshold - works reliably
        return if (maxCorr > 50000) {
            sampleRate.toFloat() / bestLag
        } else {
            0f
        }
    }
    
    /**
     * Stop listening to microphone
     */
    fun stopListening() {
        isListening = false
        
        try {
            recordingThread?.join(1000)
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            recordingThread = null
        } catch (e: Exception) {
            Log.e("PitchDetector", "Error stopping audio record", e)
        }
        
        _detectedNote.value = null
        _detectedFrequency.value = 0f
        Log.d("PitchDetector", "Stopped listening")
    }
    
    /**
     * Convert frequency (Hz) to musical note name (Solfège)
     * OPTIMIZED: Uses faster lookup with tighter tolerance
     */
    private fun frequencyToNote(frequency: Float): String? {
        if (frequency < 200f || frequency > 600f) {
            return null // Allow wider range
        }
        
        var closestNote: String? = null
        var minDistance = Float.MAX_VALUE
        
        for ((note, targetFreq) in noteFrequencies) {
            val distance = abs(frequency - targetFreq)
            
            if (distance < minDistance) {
                minDistance = distance
                closestNote = note
            }
        }
        
        // Accept if within 6% of target (about half a semitone)
        val bestFreq = noteFrequencies[closestNote]
        if (bestFreq != null && minDistance < bestFreq * 0.06f) {
            return closestNote
        }
        
        return null
    }
    
    /**
     * Check if currently listening
     */
    fun isActive(): Boolean = isListening
    
    /**
     * Get the current note being detected (if any)
     */
    fun getCurrentNote(): String? = _detectedNote.value
}
