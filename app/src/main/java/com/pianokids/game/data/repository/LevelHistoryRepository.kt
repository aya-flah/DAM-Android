package com.pianokids.game.data.repository

import com.pianokids.game.data.api.RetrofitClient
import com.pianokids.game.data.models.LevelHistoryEntry
import com.pianokids.game.data.models.PlayHistoryRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LevelHistoryRepository {

    /**
     * Fetch user's play history from backend (GET /play-history/:userId)
     */
    suspend fun getHistory(userId: String): List<LevelHistoryEntry>? =
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.playHistoryApi.getHistory(userId)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    /**
     * Save user's play history to backend (POST /play-history)
     */
    suspend fun saveHistory(request: PlayHistoryRequest): LevelHistoryEntry? =
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.playHistoryApi.saveHistory(request)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}
