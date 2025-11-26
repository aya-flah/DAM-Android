package com.pianokids.game.data.repository

import android.content.Context
import android.net.Uri
import com.pianokids.game.api.MusicApiService
import com.pianokids.game.data.models.KaraokeResponse
import com.pianokids.game.data.models.MusicRecognitionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

/**
 * Repository for music recognition and karaoke features
 */
class MusicRepository(
    private val apiService: MusicApiService,
    private val context: Context
) {

    /**
     * Recognize song from audio file
     * @param audioFile File containing audio data (WAV/MP3/PCM)
     * @return Music recognition result or null on failure
     */
    suspend fun recognizeSong(audioFile: File): MusicRecognitionResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val requestFile = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
                val audioPart = MultipartBody.Part.createFormData(
                    "audio",
                    audioFile.name,
                    requestFile
                )

                val response = apiService.recognizeSong(audioPart)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Recognize song from audio file URI
     * @param audioUri URI of audio file
     * @return Music recognition result or null on failure
     */
    suspend fun recognizeSongFromUri(audioUri: Uri): MusicRecognitionResponse? {
        return withContext(Dispatchers.IO) {
            try {
                // Copy URI content to temporary file
                val tempFile = File.createTempFile("audio_", ".wav", context.cacheDir)
                context.contentResolver.openInputStream(audioUri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val result = recognizeSong(tempFile)
                tempFile.delete() // Clean up
                result
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Upload LRC lyrics file
     * @param lrcFile File containing LRC data
     * @param songId Optional song identifier (will use filename if not provided)
     * @return Parsed karaoke response or null on failure
     */
    suspend fun uploadLrc(lrcFile: File, songId: String? = null): KaraokeResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val requestFile = lrcFile.asRequestBody("text/plain".toMediaTypeOrNull())
                val lrcPart = MultipartBody.Part.createFormData(
                    "lrc",
                    lrcFile.name,
                    requestFile
                )

                val songIdBody = songId?.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.uploadLrc(lrcPart, songIdBody)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Get lyrics by song ID
     * @param songId Song identifier
     * @return Karaoke response with lyrics or null on failure
     */
    suspend fun getLyrics(songId: String): KaraokeResponse? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getLyrics(songId)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Get all available song IDs
     * @return List of song IDs or empty list on failure
     */
    suspend fun getAllSongs(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllSongs()
                if (response.isSuccessful) {
                    response.body()?.songs ?: emptyList()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    /**
     * Delete lyrics by song ID
     * @param songId Song identifier
     * @return true if successful, false otherwise
     */
    suspend fun deleteLyrics(songId: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteLyrics(songId)
                response.isSuccessful
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}