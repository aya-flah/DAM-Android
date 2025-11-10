// UserPreferences.kt
package com.pianokids.game.utils

import User
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson

import com.pianokids.game.data.repository.AuthRepository

class UserPreferences(private val context: Context) {      // <-- keep it as a property
    private val prefs: SharedPreferences =
        context.getSharedPreferences("piano_kids_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // keep the old suspend version **only** for AuthViewModel
    internal suspend fun verifyTokenWithBackend(): Boolean {
        return AuthRepository(context).verifyToken()
    }

    // ── AUTH TOKEN ─────────────────────────────────────────────────────────
    fun saveAuthToken(token: String?) {
        prefs.edit {
            if (token.isNullOrEmpty()) {
                remove(KEY_AUTH_TOKEN)
                Log.w("UserPreferences", "Attempted to save null or empty token")
            } else {
                putString(KEY_AUTH_TOKEN, token)
            }
        }
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun hasLocalToken(): Boolean {
        return !getAuthToken().isNullOrEmpty()
    }

    // ── User ─────────────────────────────────────────────────────────────
    // ── USER DATA ──────────────────────────────────────────────────────────
    // UserPreferences.kt
    fun saveUser(user: User) {
        prefs.edit {
            val userJson = gson.toJson(user)
            putString(KEY_USER_DATA, userJson)
            Log.d("UserPreferences", "User saved: $userJson")
        }
    }

    fun getUser(): User? {
        val userJson = prefs.getString(KEY_USER_DATA, null)
        Log.d("UserPreferences", "Retrieved user JSON: $userJson")
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            Log.e("UserPreferences", "Error parsing user JSON", e)
            null
        }
    }


    // ── Login state (uses AuthRepository) ─────────────────────────────────
    suspend fun isLoggedIn(): Boolean {
        val hasToken = getAuthToken() != null
        return if (hasToken) {
            AuthRepository(context).verifyToken()   // <-- context is now available
        } else {
            false
        }
    }

    // ── Logout ───────────────────────────────────────────────────────────
    fun clearAuth() =
        prefs.edit()
            .remove("auth_token")
            .remove("user_data")
            .putBoolean("is_logged_in", false)
            .apply()

    // ── Extra getters ────────────────────────────────────────────────────
    fun getFullName(): String = prefs.getString("full_name", "Player") ?: "Player"
    fun getEmail(): String? = prefs.getString("email", null)
    fun getLevel(): Int = prefs.getInt("level", 1)
    fun getTotalStars(): Int = prefs.getInt("total_stars", 0)

    // ── Save extra data when logging in ───────────────────────────────────
    fun saveUserData(name: String, email: String?, level: Int, stars: Int) {
        prefs.edit()
            .putString("full_name", name)
            .putString("email", email)
            .putInt("level", level)
            .putInt("total_stars", stars)
            .apply()
    }
    // Add to your existing UserPreferences class
    fun setSeenWelcome(seen: Boolean) {
        prefs.edit {
            putBoolean(KEY_SEEN_WELCOME, seen)
        }
    }

    fun hasSeenWelcome(): Boolean {
        return prefs.getBoolean(KEY_SEEN_WELCOME, false)
    }


    fun clearGuestMode() {
        // Clear any guest-specific data if needed
    }
    private val PREF_HAS_SEEN_WELCOME = "has_seen_welcome"
    private val PREF_IS_GUEST = "is_guest_mode"



    fun setGuestMode(isGuest: Boolean) {
        prefs.edit().putBoolean(PREF_IS_GUEST, isGuest).apply()
    }

    fun isGuestMode(): Boolean = prefs.getBoolean(PREF_IS_GUEST, false)


    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_SEEN_WELCOME = "seen_welcome"
    }
}