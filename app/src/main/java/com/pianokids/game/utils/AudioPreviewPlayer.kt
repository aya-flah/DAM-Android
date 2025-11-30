package com.pianokids.game.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Audio Preview Player for Level Song Playback
 * Streams audio from URL before gameplay starts
 * Works with both Android and iOS backend (same URLs)
 */
object AudioPreviewPlayer {
    
    private var mediaPlayer: MediaPlayer? = null
    private var isInitialized = false
    
    // Playback state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _currentPosition = MutableStateFlow(0)
    val currentPosition: StateFlow<Int> = _currentPosition
    
    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    /**
     * Prepare audio from URL
     * @param audioUrl Full URL to audio file (e.g., http://192.168.100.21:3000/audio/levels/batman-preview.mp3)
     * @param onReady Callback when audio is ready to play
     * @param onComplete Callback when playback completes
     * @param onError Callback on error
     */
    fun prepare(
        context: Context,
        audioUrl: String,
        onReady: () -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        release() // Clean up any existing player
        
        _isLoading.value = true
        _error.value = null
        
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                
                setDataSource(audioUrl)
                
                setOnPreparedListener { mp ->
                    isInitialized = true
                    _duration.value = mp.duration
                    _isLoading.value = false
                    onReady()
                    Log.d("AudioPreviewPlayer", "Audio prepared: ${mp.duration}ms")
                }
                
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = 0
                    onComplete()
                    Log.d("AudioPreviewPlayer", "Playback completed")
                }
                
                setOnErrorListener { _, what, extra ->
                    val errorMsg = "Error: what=$what, extra=$extra"
                    _error.value = errorMsg
                    _isLoading.value = false
                    _isPlaying.value = false
                    onError(errorMsg)
                    Log.e("AudioPreviewPlayer", errorMsg)
                    true
                }
                
                prepareAsync() // Non-blocking preparation
            }
            
            Log.d("AudioPreviewPlayer", "Preparing audio from: $audioUrl")
            
        } catch (e: Exception) {
            val errorMsg = "Failed to prepare audio: ${e.message}"
            _error.value = errorMsg
            _isLoading.value = false
            onError(errorMsg)
            Log.e("AudioPreviewPlayer", errorMsg, e)
        }
    }
    
    /**
     * Start or resume playback
     */
    fun play() {
        if (!isInitialized) {
            Log.w("AudioPreviewPlayer", "Cannot play: not initialized")
            return
        }
        
        try {
            mediaPlayer?.start()
            _isPlaying.value = true
            startProgressTracking()
            Log.d("AudioPreviewPlayer", "Playback started")
        } catch (e: Exception) {
            Log.e("AudioPreviewPlayer", "Failed to play: ${e.message}")
            _error.value = "Failed to play audio"
        }
    }
    
    /**
     * Pause playback
     */
    fun pause() {
        try {
            mediaPlayer?.pause()
            _isPlaying.value = false
            Log.d("AudioPreviewPlayer", "Playback paused")
        } catch (e: Exception) {
            Log.e("AudioPreviewPlayer", "Failed to pause: ${e.message}")
        }
    }
    
    /**
     * Stop playback and reset
     */
    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                seekTo(0)
            }
            _isPlaying.value = false
            _currentPosition.value = 0
            Log.d("AudioPreviewPlayer", "Playback stopped")
        } catch (e: Exception) {
            Log.e("AudioPreviewPlayer", "Failed to stop: ${e.message}")
        }
    }
    
    /**
     * Seek to position
     * @param positionMs Position in milliseconds
     */
    fun seekTo(positionMs: Int) {
        try {
            mediaPlayer?.seekTo(positionMs)
            _currentPosition.value = positionMs
        } catch (e: Exception) {
            Log.e("AudioPreviewPlayer", "Failed to seek: ${e.message}")
        }
    }
    
    /**
     * Release resources
     * Call when leaving screen or destroying activity
     */
    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            isInitialized = false
            _isPlaying.value = false
            _isLoading.value = false
            _currentPosition.value = 0
            _duration.value = 0
            _error.value = null
            Log.d("AudioPreviewPlayer", "Resources released")
        } catch (e: Exception) {
            Log.e("AudioPreviewPlayer", "Error releasing: ${e.message}")
        }
    }
    
    /**
     * Track playback progress for UI updates
     */
    private fun startProgressTracking() {
        Thread {
            while (_isPlaying.value && isInitialized) {
                try {
                    mediaPlayer?.let { mp ->
                        if (mp.isPlaying) {
                            _currentPosition.value = mp.currentPosition
                        }
                    }
                    Thread.sleep(100) // Update every 100ms
                } catch (e: Exception) {
                    break
                }
            }
        }.start()
    }
    
    /**
     * Get current position as percentage (0.0 to 1.0)
     */
    fun getProgressPercentage(): Float {
        val dur = _duration.value
        val pos = _currentPosition.value
        return if (dur > 0) pos.toFloat() / dur.toFloat() else 0f
    }
}
