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
    
    private val sampleRate = 22050
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    
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
            
            recordingThread = Thread {
                val buffer = ShortArray(bufferSize)
                
                while (isListening) {
                    val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    
                    if (readSize > 0) {
                        // Simple autocorrelation-based pitch detection
                        val frequency = detectFrequency(buffer, readSize)
                        
                        if (frequency > 0) {
                            _detectedFrequency.value = frequency
                            
                            val detectedNote = frequencyToNote(frequency)
                            if (detectedNote != null && detectedNote != _detectedNote.value) {
                                _detectedNote.value = detectedNote
                                onNoteDetected(detectedNote)
                                Log.d("PitchDetector", "Detected: $detectedNote ($frequency Hz)")
                            }
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
     * Detect frequency using autocorrelation method
     * Simple but effective for musical notes
     */
    private fun detectFrequency(audioData: ShortArray, size: Int): Float {
        // Calculate RMS to check if sound is loud enough
        val rms = sqrt(audioData.take(size).map { it * it }.average())
        if (rms < 1000) return 0f // Too quiet
        
        // Autocorrelation
        val correlations = FloatArray(size / 2)
        for (lag in 1 until size / 2) {
            var sum = 0f
            for (i in 0 until size / 2) {
                sum += audioData[i] * audioData[i + lag]
            }
            correlations[lag] = sum
        }
        
        // Find the first peak after the initial peak
        var maxCorr = 0f
        var maxLag = 0
        var foundFirstDip = false
        
        for (lag in 20 until correlations.size - 1) {
            if (!foundFirstDip && correlations[lag] < correlations[lag - 1]) {
                foundFirstDip = true
            }
            
            if (foundFirstDip && correlations[lag] > maxCorr) {
                maxCorr = correlations[lag]
                maxLag = lag
            }
        }
        
        return if (maxLag > 0) {
            sampleRate.toFloat() / maxLag
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
     * Uses logarithmic distance to find closest note
     */
    private fun frequencyToNote(frequency: Float): String? {
        if (frequency < 200f || frequency > 600f) {
            return null // Outside piano range we're interested in
        }
        
        var closestNote: String? = null
        var minDistance = Float.MAX_VALUE
        
        noteFrequencies.forEach { (note, targetFreq) ->
            val distance = abs(frequency - targetFreq)
            
            // Allow 5% tolerance (about 1/4 semitone)
            val tolerance = targetFreq * 0.05f
            
            if (distance < minDistance && distance < tolerance) {
                minDistance = distance
                closestNote = note
            }
        }
        
        return closestNote
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
