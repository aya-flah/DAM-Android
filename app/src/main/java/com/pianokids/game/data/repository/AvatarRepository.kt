package com.pianokids.game.data.repository

import android.content.Context
import android.util.Log
import com.pianokids.game.api.AvatarApiService
import com.pianokids.game.data.api.RetrofitClient
import com.pianokids.game.data.models.*
import com.pianokids.game.utils.UserPreferences

class AvatarRepository(private val context: Context) {
    private val api: AvatarApiService = RetrofitClient.avatarApi
    private val userPrefs = UserPreferences(context)
    
    private fun getAuthHeaders(): Pair<String, String>? {
        val authToken = userPrefs.getAuthToken()
        val providerId = userPrefs.getProviderId()
        
        return if (authToken != null && providerId != null) {
            Pair(authToken, providerId)
        } else {
            Log.e("AvatarRepository", "Missing auth credentials")
            null
        }
    }
    
    suspend fun createAvatar(createAvatarDto: CreateAvatarDto): Result<Avatar> {
        return try {
            val (authToken, providerId) = getAuthHeaders() ?: return Result.failure(
                Exception("Not authenticated")
            )
            
            val response = api.createAvatar(createAvatarDto, authToken, providerId)
            
            if (response.isSuccessful && response.body() != null) {
                val avatar = response.body()!!
                Log.d("AvatarRepository", "Avatar created: ${avatar.name}")
                Result.success(avatar)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AvatarRepository", "Create avatar failed: $errorBody")
                Result.failure(Exception("Failed to create avatar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Create avatar error", e)
            Result.failure(e)
        }
    }
    
    suspend fun getUserAvatars(): Result<List<Avatar>> {
        return try {
            val (authToken, providerId) = getAuthHeaders() ?: return Result.failure(
                Exception("Not authenticated")
            )
            
            val response = api.getUserAvatars(authToken, providerId)
            
            if (response.isSuccessful && response.body() != null) {
                val avatars = response.body()!!
                Log.d("AvatarRepository", "Fetched ${avatars.size} avatars")
                Result.success(avatars)
            } else {
                Log.e("AvatarRepository", "Get avatars failed: ${response.code()}")
                Result.failure(Exception("Failed to get avatars: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Get avatars error", e)
            Result.failure(e)
        }
    }
    
    suspend fun getActiveAvatar(): Result<Avatar> {
        return try {
            val (authToken, providerId) = getAuthHeaders() ?: return Result.failure(
                Exception("Not authenticated")
            )
            
            val response = api.getActiveAvatar(authToken, providerId)
            
            if (response.isSuccessful && response.body() != null) {
                val avatar = response.body()!!
                Log.d("AvatarRepository", "Active avatar: ${avatar.name}")
                Result.success(avatar)
            } else {
                Log.e("AvatarRepository", "Get active avatar failed: ${response.code()}")
                Result.failure(Exception("Failed to get active avatar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Get active avatar error", e)
            Result.failure(e)
        }
    }
    
    suspend fun getAvatar(avatarId: String): Result<Avatar> {
        return try {
            val (authToken, providerId) = getAuthHeaders() ?: return Result.failure(
                Exception("Not authenticated")
            )
            
            val response = api.getAvatar(avatarId, authToken, providerId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get avatar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Get avatar error", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateAvatar(avatarId: String, updateAvatarDto: UpdateAvatarDto): Result<Avatar> {
        return try {
            val (authToken, providerId) = getAuthHeaders() ?: return Result.failure(
                Exception("Not authenticated")
            )
            
            val response = api.updateAvatar(avatarId, updateAvatarDto, authToken, providerId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update avatar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Update avatar error", e)
            Result.failure(e)
        }
    }
    
    suspend fun setActiveAvatar(avatarId: String): Result<Avatar> {
        return try {
            val (authToken, providerId) = getAuthHeaders() ?: return Result.failure(
                Exception("Not authenticated")
            )
            
            val response = api.setActiveAvatar(avatarId, authToken, providerId)
            
            if (response.isSuccessful && response.body() != null) {
                val avatar = response.body()!!
                
                // Save avatar thumbnail URL to UserPreferences
                avatar.avatarImageUrl?.let { url ->
                    if (url.isNotEmpty()) {
                        userPrefs.saveAvatarThumbnail(url)
                    }
                }
                
                Log.d("AvatarRepository", "Set active avatar: ${avatar.name}")
                Result.success(avatar)
            } else {
                Result.failure(Exception("Failed to set active avatar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Set active avatar error", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteAvatar(avatarId: String): Result<String> {
        return try {
            val (authToken, providerId) = getAuthHeaders() ?: return Result.failure(
                Exception("Not authenticated")
            )
            
            val response = api.deleteAvatar(avatarId, authToken, providerId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.message)
            } else {
                Result.failure(Exception("Failed to delete avatar: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Delete avatar error", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateExpression(avatarId: String, expression: String): Result<Avatar> {
        return try {
            val (authToken, providerId) = getAuthHeaders() ?: return Result.failure(
                Exception("Not authenticated")
            )
            
            val body = mapOf("expression" to expression)
            val response = api.updateExpression(avatarId, body, authToken, providerId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update expression: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Update expression error", e)
            Result.failure(e)
        }
    }
    
    suspend fun updateEnergy(avatarId: String, energyDelta: Int): Result<Avatar> {
        return try {
            val (authToken, providerId) = getAuthHeaders() ?: return Result.failure(
                Exception("Not authenticated")
            )
            
            val body = mapOf("energyDelta" to energyDelta)
            val response = api.updateEnergy(avatarId, body, authToken, providerId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update energy: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Update energy error", e)
            Result.failure(e)
        }
    }
    
    suspend fun addExperience(avatarId: String, xpGain: Int): Result<Avatar> {
        return try {
            val (authToken, providerId) = getAuthHeaders() ?: return Result.failure(
                Exception("Not authenticated")
            )
            
            val body = mapOf("xpGain" to xpGain)
            val response = api.addExperience(avatarId, body, authToken, providerId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to add experience: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Add experience error", e)
            Result.failure(e)
        }
    }
    
    suspend fun getAvatarStats(avatarId: String): Result<AvatarStats> {
        return try {
            val (authToken, providerId) = getAuthHeaders() ?: return Result.failure(
                Exception("Not authenticated")
            )
            
            val response = api.getAvatarStats(avatarId, authToken, providerId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get avatar stats: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Get avatar stats error", e)
            Result.failure(e)
        }
    }
    
    suspend fun equipOutfit(avatarId: String, outfitId: String): Result<Avatar> {
        return try {
            val (authToken, providerId) = getAuthHeaders() ?: return Result.failure(
                Exception("Not authenticated")
            )
            
            val response = api.equipOutfit(avatarId, outfitId, authToken, providerId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to equip outfit: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Equip outfit error", e)
            Result.failure(e)
        }
    }
    
    suspend fun unlockOutfit(avatarId: String, outfitId: String): Result<Avatar> {
        return try {
            val (authToken, providerId) = getAuthHeaders() ?: return Result.failure(
                Exception("Not authenticated")
            )
            
            val response = api.unlockOutfit(avatarId, outfitId, authToken, providerId)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to unlock outfit: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AvatarRepository", "Unlock outfit error", e)
            Result.failure(e)
        }
    }
}
