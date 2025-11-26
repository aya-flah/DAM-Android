package com.pianokids.game.view.screens.music

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.pianokids.game.api.MusicApiService
import com.pianokids.game.data.repository.MusicRepository
import kotlinx.coroutines.launch
import java.io.File

/**
 * Example Activity showing how to use Music Recognition feature
 * This is a reference implementation for the kids piano learning app
 */
class MusicRecognitionActivity : AppCompatActivity() {
    
    private lateinit var musicRepository: MusicRepository
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false
    
    // Request permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startRecording()
        } else {
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize repository with API service
        // Note: You need to initialize Retrofit and create apiService first
        // val apiService = RetrofitClient.create<MusicApiService>()
        // musicRepository = MusicRepository(apiService, applicationContext)
    }
    
    /**
     * Start recording audio for recognition
     */
    fun startRecordingForRecognition() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startRecording()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    /**
     * Start recording audio
     */
    private fun startRecording() {
        try {
            // Create temporary file for audio
            audioFile = File.createTempFile("audio_recording_", ".wav", cacheDir)
            
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }
            
            isRecording = true
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Stop recording and send to backend for recognition
     */
    fun stopRecordingAndRecognize() {
        if (!isRecording) return
        
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            // Send audio to backend for recognition
            audioFile?.let { file ->
                recognizeSong(file)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to stop recording", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Recognize song using the backend API
     */
    private fun recognizeSong(audioFile: File) {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@MusicRecognitionActivity, "Recognizing song...", Toast.LENGTH_SHORT).show()
                
                val result = musicRepository.recognizeSong(audioFile)
                
                if (result != null) {
                    // Display results
                    val message = """
                        Song Found!
                        Title: ${result.title}
                        Artist: ${result.artist}
                        Album: ${result.album}
                        Confidence: ${(result.confidence * 100).toInt()}%
                    """.trimIndent()
                    
                    Toast.makeText(this@MusicRecognitionActivity, message, Toast.LENGTH_LONG).show()
                    
                    // You can now load the piano tutorial for this song
                    // loadPianoTutorial(result.title)
                } else {
                    Toast.makeText(this@MusicRecognitionActivity, "Song not recognized", Toast.LENGTH_SHORT).show()
                }
                
                // Clean up
                audioFile.delete()
                
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MusicRecognitionActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
    }
}
