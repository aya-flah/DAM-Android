package com.pianokids.game.data.repository

import com.pianokids.game.data.api.RetrofitClient
import com.pianokids.game.data.models.Level
import com.pianokids.game.data.models.LevelProgressRequest
import com.pianokids.game.data.models.UnlockedLevelsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LevelRepository {

    /**
     * Fetch full levels from backend (/levels)
     */
    suspend fun getAllLevels(): List<Level>? = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.levelApi.getAllLevels()
            if (response.isSuccessful) {
                response.body()
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    /**
     * Fetch ONE level by ID (/levels/:levelId)
     */
    suspend fun getLevelById(levelId: String): Level? = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.levelApi.getLevelById(levelId)
            if (response.isSuccessful) {
                response.body()
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    /**
     * Fetch unlocked levels (/levels/unlocked/:userId)
     */
    suspend fun getUnlockedLevels(userId: String): UnlockedLevelsResponse? =
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.levelApi.getUnlockedLevels(userId)
                if (response.isSuccessful) {
                    response.body()
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
    }


    suspend fun getUserTotalStars(userId: String): Int =
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.levelApi.getUserTotalStars(userId)

                if (response.isSuccessful) {
                    // Expected JSON = { "totalStars": number }
                    response.body()?.totalStars ?: 0
                } else {
                    0
                }
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }




}