package com.pianokids.game.data.api

import com.pianokids.game.api.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:3000/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ────────────────────────────────────────────────
    // API SERVICES
    // ────────────────────────────────────────────────
    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val levelApi: LevelApi by lazy {
        retrofit.create(LevelApi::class.java)
    }

    val sublevelApi: SublevelApi by lazy {
        retrofit.create(SublevelApi::class.java)
    }

    val sublevelProgressApi: SublevelProgressApi by lazy {
        retrofit.create(SublevelProgressApi::class.java)
    }

    val avatarApi: AvatarApiService by lazy {
        retrofit.create(AvatarApiService::class.java)
    }

    val musicApi: MusicApiService by lazy {
        retrofit.create(MusicApiService::class.java)
    }
}