package com.pianokids.game.viewmodel

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pianokids.game.api.MusicApiService
import com.pianokids.game.data.models.MusicRecognitionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class MusicViewModel(
    private val musicApi: MusicApiService,
    private val context: Context
) : ViewModel() {
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> = _isRecognizing.asStateFlow()
    
    private val _recognitionResult = MutableStateFlow<MusicRecognitionResponse?>(null)
    val recognitionResult: StateFlow<MusicRecognitionResponse?> = _recognitionResult.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    
    fun startRecording() {
        try {
            _error.value = null
            _recognitionResult.value = null
            
            // Create temporary file
            audioFile = File.createTempFile("audio_recording_", ".m4a", context.cacheDir)
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            
            _isRecording.value = true
        } catch (e: Exception) {
            _error.value = "Failed to start recording: ${e.message}"
            cleanupRecorder()
        }
    }
    
    fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            _isRecording.value = false
        } catch (e: Exception) {
            _error.value = "Failed to stop recording: ${e.message}"
            cleanupRecorder()
        }
    }
    
    fun stopRecordingAndRecognize() {
        stopRecording()
        audioFile?.let { file ->
            recognizeSong(file)
        } ?: run {
            _error.value = "No audio file to process"
        }
    }
    
    private fun recognizeSong(file: File) {
        viewModelScope.launch {
            try {
                _isRecognizing.value = true
                _error.value = null
                
                val requestFile = file.asRequestBody("audio/*".toMediaTypeOrNull())
                val audioPart = MultipartBody.Part.createFormData(
                    "audio",
                    file.name,
                    requestFile
                )
                
                val response = musicApi.recognizeSong(audioPart)
                
                if (response.isSuccessful && response.body() != null) {
                    _recognitionResult.value = response.body()
                } else {
                    _error.value = "Song not found. Try recording again with clearer audio."
                }
            } catch (e: Exception) {
                _error.value = "Recognition failed: ${e.message ?: "Unknown error"}"
            } finally {
                _isRecognizing.value = false
                // Clean up temp file
                file.delete()
            }
        }
    }
    
    fun clearResult() {
        _recognitionResult.value = null
        _error.value = null
    }
    
    fun clearError() {
        _error.value = null
    }
    
    private fun cleanupRecorder() {
        try {
            mediaRecorder?.release()
            mediaRecorder = null
            _isRecording.value = false
        } catch (e: Exception) {
            // Ignore
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        cleanupRecorder()
        audioFile?.delete()
    }
    
    class Factory(
        private val musicApi: MusicApiService,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
                return MusicViewModel(musicApi, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
