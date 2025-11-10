// AuthApi.kt
package com.pianokids.game.data.api

import com.pianokids.game.data.models.AuthResponse
import com.pianokids.game.data.models.SocialLoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
// AuthApiService.kt
interface AuthApiService {
    @POST("/auth/social-login") // Make sure the path is correct
    suspend fun socialLogin(@Body request: SocialLoginRequest): Response<AuthResponse>

    @GET("/auth/verify")
    suspend fun verifyToken(@Header("Authorization") token: String): Response<Unit>
}

data class SocialLoginRequest(
    val token: String,
    val provider: String
)