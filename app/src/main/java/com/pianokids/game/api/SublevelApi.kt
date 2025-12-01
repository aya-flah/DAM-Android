package com.pianokids.game.api

import com.pianokids.game.data.models.Sublevel
import retrofit2.http.GET
import retrofit2.http.Path

interface SublevelApi {
    @GET("sublevels/level/{levelId}")
    suspend fun getSublevelsByLevel(
        @Path("levelId") levelId: String
    ): retrofit2.Response<List<Sublevel>>
}