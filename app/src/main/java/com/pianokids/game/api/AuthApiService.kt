package com.pianokids.game.data.api

import com.pianokids.game.data.models.AuthResponse
import com.pianokids.game.data.models.SocialLoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PATCH

interface AuthApiService {
    @POST("/auth/social-login")
    suspend fun socialLogin(@Body request: SocialLoginRequest): Response<AuthResponse>

    @POST("/auth/dev-login")
    suspend fun devLogin(@Body request: DevLoginRequest): Response<AuthResponse>

    @GET("/auth/verify")
    suspend fun verifyToken(
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<VerifyResponse>

    @PATCH("/auth/update-name")
    suspend fun updateName(
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String,
        @Body request: UpdateNameRequest
    ): Response<UpdateNameResponse>
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

data class DevLoginRequest(
    val email: String,
    val name: String
)

data class UpdateNameRequest(
    val name: String
)

data class UpdateNameResponse(
    val success: Boolean,
    val name: String
)