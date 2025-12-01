package com.pianokids.game.data.repository

import android.util.Log
import com.pianokids.game.data.api.RetrofitClient
import com.pianokids.game.data.models.Sublevel
import com.pianokids.game.data.models.SublevelProgressRequest
import com.pianokids.game.data.models.UserLevelDataResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SublevelProgressRepository {


    suspend fun getUserSublevels(userId: String, levelId: String): List<Sublevel>? =
        withContext(Dispatchers.IO) {
            try {
                RetrofitClient.sublevelProgressApi.getUserSublevels(userId, levelId)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    suspend fun submitProgress(request: SublevelProgressRequest): List<Sublevel>? =
        withContext(Dispatchers.IO) {
            try {
                RetrofitClient.sublevelProgressApi.submitProgress(request)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}
