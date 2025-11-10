// AuthRepository.kt
package com.pianokids.game.data.repository

import android.content.Context
import android.util.Log
import com.pianokids.game.data.api.AuthApiService
import com.pianokids.game.data.api.RetrofitClient
import com.pianokids.game.data.models.AuthResponse
import com.pianokids.game.data.models.SocialLoginRequest
import com.pianokids.game.utils.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(context: Context) {
    private val api: AuthApiService = RetrofitClient.authApi
    private val prefs = UserPreferences(context)

    // AuthRepository.kt
    suspend fun loginWithSocial(token: String?, provider: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                if (token.isNullOrEmpty()) {
                    Log.e("AuthRepository", "Social login token is null or empty")
                    return@withContext Result.failure(Exception("Authentication token is missing"))
                }

                Log.d("AuthRepository", "Sending social login request - Provider: $provider")

                val request = SocialLoginRequest(token, provider)
                val response = api.socialLogin(request)

                Log.d("AuthRepository", "Response code: ${response.code()}")
                Log.d("AuthRepository", "Response isSuccessful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    Log.d("AuthRepository", "Response body: $authResponse")

                    if (authResponse != null) {
                        // EXTRA DEBUGGING: Log the actual accessToken
                        Log.d("AuthRepository", "AccessToken received: ${authResponse.accessToken?.take(10)}...")
                        Log.d("AuthRepository", "User received: ${authResponse.user}")

                        if (authResponse.accessToken.isNullOrEmpty()) {
                            Log.e("AuthRepository", "Server returned null or empty access token")
                            // Let's see the raw response
                            val rawResponse = response.raw().toString()
                            Log.e("AuthRepository", "Raw response: ${rawResponse.take(500)}")
                            return@withContext Result.failure(Exception("Authentication failed - no token received from server"))
                        }

                        // Save JWT token and user data
                        prefs.saveAuthToken(authResponse.accessToken)
                        authResponse.user?.let { user ->
                            prefs.saveUser(user)
                        }

                        Log.d("AuthRepository", "Social login successful, token saved")
                        Result.success(authResponse)
                    } else {
                        Log.e("AuthRepository", "Response body is null")
                        // Try to get error body
                        val errorBody = response.errorBody()?.string()
                        Log.e("AuthRepository", "Error body: $errorBody")
                        Result.failure(Exception("Server returned empty response: $errorBody"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("AuthRepository", "Login failed - Status: ${response.code()}, Error: $errorBody")
                    Result.failure(Exception("Login failed: ${response.code()} - $errorBody"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Network error during login", e)
                Result.failure(e)
            }
        }
    }
    suspend fun verifyToken(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val token = prefs.getAuthToken() ?: return@withContext false
                val response = api.verifyToken("Bearer $token")
                response.isSuccessful
            } catch (e: Exception) {
                Log.e("AuthRepository", "Token verification failed", e)
                false
            }
        }
    }

    fun logout() {
        prefs.clearAuth()
    }
}