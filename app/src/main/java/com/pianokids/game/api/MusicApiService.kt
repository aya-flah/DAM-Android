package com.pianokids.game.api

import com.pianokids.game.data.models.MusicRecognitionResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface MusicApiService {

    @Multipart
    @POST("music/recognize")
    suspend fun recognizeSong(
        @Part audioFile: MultipartBody.Part
    ): Response<MusicRecognitionResponse>
}