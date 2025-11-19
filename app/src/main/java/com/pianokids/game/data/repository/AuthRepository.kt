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
                        Log.d("AuthRepository", "AuthToken received: ${authResponse.authToken.take(10)}...")
                        Log.d("AuthRepository", "ProviderId received: ${authResponse.providerId}")
                        Log.d("AuthRepository", "User received: ${authResponse.user}")

                        if (authResponse.authToken.isEmpty()) {
                            Log.e("AuthRepository", "Server returned null or empty auth token")
                            return@withContext Result.failure(Exception("Authentication failed - no token received from server"))
                        }

                        // Save HMAC auth token, provider ID, and user data
                        prefs.saveAuthToken(authResponse.authToken)
                        prefs.saveProviderId(authResponse.providerId)
                        authResponse.user?.let { user ->
                            prefs.saveUser(user)
                        }

                        Log.d("AuthRepository", "Social login successful, token and providerId saved")
                        Result.success(authResponse)
                    } else {
                        Log.e("AuthRepository", "Response body is null")
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
                val providerId = prefs.getProviderId() ?: return@withContext false

                Log.d("AuthRepository", "Verifying token with providerId: $providerId")

                val response = api.verifyToken(token, providerId)
                val isValid = response.isSuccessful

                Log.d("AuthRepository", "Token verification result: $isValid")
                isValid
            } catch (e: Exception) {
                Log.e("AuthRepository", "Token verification failed", e)
                false
            }
        }
    }

    suspend fun loginWithDevUser(email: String, name: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AuthRepository", "Dev login request - Email: $email, Name: $name")

                val request = com.pianokids.game.data.api.DevLoginRequest(email, name)
                val response = api.devLogin(request)

                Log.d("AuthRepository", "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        prefs.saveAuthToken(authResponse.authToken)
                        prefs.saveProviderId(authResponse.providerId)
                        authResponse.user?.let { user ->
                            prefs.saveUser(user)
                        }
                        Log.d("AuthRepository", "Dev login successful")
                        Result.success(authResponse)
                    } else {
                        Result.failure(Exception("Empty response"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("AuthRepository", "Dev login failed: $errorBody")
                    Result.failure(Exception("Login failed: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Dev login error", e)
                Result.failure(e)
            }
        }
    }

    fun logout() {
        prefs.clearAuth()
    }
}