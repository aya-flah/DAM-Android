package com.pianokids.game.data.repository

import android.content.Context
import android.net.Uri
import com.pianokids.game.api.MusicApiService
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






}