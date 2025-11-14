package com.pianokids.game.data.api

import com.pianokids.game.data.models.AuthResponse
import com.pianokids.game.data.models.SocialLoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {
    @POST("/auth/social-login")
    suspend fun socialLogin(@Body request: SocialLoginRequest): Response<AuthResponse>

    @GET("/auth/verify")
    suspend fun verifyToken(
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<VerifyResponse>
}

data class VerifyResponse(
    val valid: Boolean,
    val user: VerifyUser
)

data class VerifyUser(
    val id: String,
    val email: String,
    val name: String
)