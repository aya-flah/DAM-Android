package com.pianokids.game.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class UserData(
    val _id: String,
    val fullName: String,
    val email: String,
    val level: Int,
    val totalStars: Int,
    val avatarURL: String?
)

data class LoginResponse(
    val access_token: String,
    val user: UserData
)

data class RegisterResponse(
    val access_token: String,
    val user: UserData
)

class AuthApiService {
    private val baseUrl = "http://192.168.1.103:3000"



    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        avatarURL: String? = null
    ): Result<RegisterResponse> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/auth/register")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.doInput = true

            // Create JSON body
            val jsonBody = JSONObject().apply {
                put("fullName", fullName)
                put("email", email)
                put("password", password)
                if (avatarURL != null) {
                    put("avatarURL", avatarURL)
                }
            }

            // Send request
            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(jsonBody.toString())
            writer.flush()
            writer.close()

            // Read response
            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK ||
                responseCode == HttpURLConnection.HTTP_CREATED) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val jsonResponse = JSONObject(response)
                val userData = jsonResponse.getJSONObject("user")

                val user = UserData(
                    _id = userData.getString("_id"),
                    fullName = userData.getString("fullName"),
                    email = userData.getString("email"),
                    level = userData.optInt("level", 1),
                    totalStars = userData.optInt("totalStars", 0),
                    avatarURL = userData.optString("avatarURL", null)
                )

                val registerResponse = RegisterResponse(
                    access_token = jsonResponse.getString("access_token"),
                    user = user
                )

                Result.success(registerResponse)
            } else {
                val errorReader = BufferedReader(InputStreamReader(connection.errorStream))
                val errorResponse = errorReader.readText()
                errorReader.close()

                val errorJson = JSONObject(errorResponse)
                val errorMessage = errorJson.optString("message", "Registration failed")

                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/auth/login")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.doInput = true

            // Create JSON body
            val jsonBody = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            // Send request
            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(jsonBody.toString())
            writer.flush()
            writer.close()

            // Read response
            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val jsonResponse = JSONObject(response)
                val userData = jsonResponse.getJSONObject("user")

                val user = UserData(
                    _id = userData.getString("_id"),
                    fullName = userData.getString("fullName"),
                    email = userData.getString("email"),
                    level = userData.optInt("level", 1),
                    totalStars = userData.optInt("totalStars", 0),
                    avatarURL = userData.optString("avatarURL", null)
                )

                val loginResponse = LoginResponse(
                    access_token = jsonResponse.getString("access_token"),
                    user = user
                )

                Result.success(loginResponse)
            } else {
                val errorReader = BufferedReader(InputStreamReader(connection.errorStream))
                val errorResponse = errorReader.readText()
                errorReader.close()

                val errorJson = JSONObject(errorResponse)
                val errorMessage = errorJson.optString("message", "Invalid credentials")

                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(token: String): Result<UserData> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl/auth/profile")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.doInput = true

            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val userData = JSONObject(response)

                val user = UserData(
                    _id = userData.getString("_id"),
                    fullName = userData.getString("fullName"),
                    email = userData.getString("email"),
                    level = userData.optInt("level", 1),
                    totalStars = userData.optInt("totalStars", 0),
                    avatarURL = userData.optString("avatarURL", null)
                )

                Result.success(user)
            } else {
                Result.failure(Exception("Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}