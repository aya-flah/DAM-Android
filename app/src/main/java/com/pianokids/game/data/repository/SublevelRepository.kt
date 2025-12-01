package com.pianokids.game.data.repository

import com.pianokids.game.data.api.RetrofitClient
import com.pianokids.game.data.models.Sublevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SublevelRepository {

    suspend fun getSublevels(levelId: String): List<Sublevel>? =
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.sublevelApi.getSublevelsByLevel(levelId)
                if (response.isSuccessful) response.body() else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}
