package com.pianokids.game.api

import com.pianokids.game.data.models.LevelHistoryEntry
import com.pianokids.game.data.models.PlayHistoryRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PlayHistoryApi {

    @POST("play-history")
    suspend fun saveHistory(@Body body: PlayHistoryRequest): Response<LevelHistoryEntry>

    @GET("play-history/{userId}")
    suspend fun getHistory(@Path("userId") userId: String): Response<List<LevelHistoryEntry>>
}
