package com.pianokids.game.api

import com.pianokids.game.data.models.Sublevel
import com.pianokids.game.data.models.SublevelProgressRequest
import com.pianokids.game.data.models.UserLevelDataResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SublevelProgressApi {

    @POST("sublevels/progress")
    suspend fun submitProgress(@Body body: SublevelProgressRequest): List<Sublevel>

    @GET("sublevels/progress/{userId}/{levelId}")
    suspend fun getUserSublevels(
        @Path("userId") userId: String,
        @Path("levelId") levelId: String
    ): List<Sublevel>
}
