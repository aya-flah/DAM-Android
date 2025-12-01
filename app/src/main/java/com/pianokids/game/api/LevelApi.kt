package com.pianokids.game.api

import com.pianokids.game.data.models.Level
import com.pianokids.game.data.models.LevelProgressRequest
import com.pianokids.game.data.models.Sublevel
import com.pianokids.game.data.models.UnlockedLevelsResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LevelApi {

    /**
     * Fetch all levels (full metadata)
     * GET /levels
     */
    @GET("levels")
    suspend fun getAllLevels(): Response<List<Level>>

    /**
     * Fetch a single level by its MongoDB ObjectId
     * GET /levels/{id}
     */
    @GET("levels/{id}")
    suspend fun getLevelById(
        @Path("id") id: String
    ): Response<Level>

    /**
     * Save user progress for a level
     * POST /levels/progress
     */
    @POST("levels/progress")
    suspend fun saveLevelProgress(
        @Body body: LevelProgressRequest
    ): Response<ResponseBody>

    /**
     * Fetch unlocked/locked state for all levels for a specific user
     * GET /levels/unlocked/{userId}
     */
    @GET("levels/unlocked/{userId}")
    suspend fun getUnlockedLevels(
        @Path("userId") userId: String
    ): Response<UnlockedLevelsResponse>
}
