package com.pianokids.game.api

import com.google.gson.annotations.SerializedName
import com.pianokids.game.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface AvatarApiService {
    
    @POST("/api/avatars")
    suspend fun createAvatar(
        @Body createAvatarDto: CreateAvatarDto,
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<Avatar>
    
    @GET("/api/avatars")
    suspend fun getUserAvatars(
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<List<Avatar>>
    
    @GET("/api/avatars/active")
    suspend fun getActiveAvatar(
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<Avatar>
    
    @GET("/api/avatars/{avatarId}")
    suspend fun getAvatar(
        @Path("avatarId") avatarId: String,
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<Avatar>
    
    @PUT("/api/avatars/{avatarId}")
    suspend fun updateAvatar(
        @Path("avatarId") avatarId: String,
        @Body updateAvatarDto: UpdateAvatarDto,
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<Avatar>
    
    @POST("/api/avatars/{avatarId}/activate")
    suspend fun setActiveAvatar(
        @Path("avatarId") avatarId: String,
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<Avatar>
    
    @DELETE("/api/avatars/{avatarId}")
    suspend fun deleteAvatar(
        @Path("avatarId") avatarId: String,
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<DeleteAvatarResponse>
    
    @PUT("/api/avatars/{avatarId}/expression")
    suspend fun updateExpression(
        @Path("avatarId") avatarId: String,
        @Body body: Map<String, String>,
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<Avatar>
    
    @PUT("/api/avatars/{avatarId}/energy")
    suspend fun updateEnergy(
        @Path("avatarId") avatarId: String,
        @Body body: Map<String, Int>,
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<Avatar>
    
    @POST("/api/avatars/{avatarId}/experience")
    suspend fun addExperience(
        @Path("avatarId") avatarId: String,
        @Body body: Map<String, Int>,
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<Avatar>
    
    @GET("/api/avatars/{avatarId}/stats")
    suspend fun getAvatarStats(
        @Path("avatarId") avatarId: String,
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<AvatarStats>
    
    @POST("/api/avatars/{avatarId}/outfits/{outfitId}/equip")
    suspend fun equipOutfit(
        @Path("avatarId") avatarId: String,
        @Path("outfitId") outfitId: String,
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<Avatar>
    
    @POST("/api/avatars/{avatarId}/outfits/{outfitId}/unlock")
    suspend fun unlockOutfit(
        @Path("avatarId") avatarId: String,
        @Path("outfitId") outfitId: String,
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<Avatar>
    
    // Gemini AI Avatar Generation (preview only, not saved)
    @POST("/api/avatars/generate-from-prompt")
    suspend fun generateAvatarFromPrompt(
        @Body generateDto: GenerateAvatarFromPromptDto,
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<AvatarGenerationResponse>
    
    // Save AI-generated avatar after user approves
    @POST("/api/avatars/save-ai-avatar")
    suspend fun saveAIAvatar(
        @Body body: SaveAIAvatarRequest,
        @Header("X-Auth-Token") authToken: String,
        @Header("X-Provider-ID") providerId: String
    ): Response<SaveAIAvatarResponse>
}

data class DeleteAvatarResponse(
    @SerializedName("message")
    val message: String
)

data class SaveAIAvatarRequest(
    @SerializedName("previewData")
    val previewData: Any
)
