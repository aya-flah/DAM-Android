
package com.pianokids.game.data.api

import com.pianokids.game.api.LevelApi
import com.pianokids.game.api.AvatarApiService
import com.pianokids.game.api.MusicApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://192.168.100.21:3000/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS) // Increased from default 10s
        .readTimeout(60, TimeUnit.SECONDS)    // Increased from default 10s for AI generation
        .writeTimeout(30, TimeUnit.SECONDS)   // Increased from default 10s
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApiService = retrofit.create(AuthApiService::class.java)

    // LevelAPI
    val levelApi: LevelApi = retrofit.create(LevelApi::class.java)
    val avatarApi: AvatarApiService = retrofit.create(AvatarApiService::class.java)
    val musicApi: MusicApiService = retrofit.create(MusicApiService::class.java)
}