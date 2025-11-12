// UserPreferences.kt
package com.pianokids.game.utils

import com.pianokids.game.data.models.User
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson

import com.pianokids.game.data.repository.AuthRepository
import com.pianokids.game.utils.UserPreferences.Companion.KEY_AUTH_TOKEN

class UserPreferences(private val context: Context) {      // <-- keep it as a property
    private val prefs: SharedPreferences =
        context.getSharedPreferences("piano_kids_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // keep the old suspend version **only** for AuthViewModel
    internal suspend fun verifyTokenWithBackend(): Boolean {
        return AuthRepository(context).verifyToken()
    }


    // ----- Music -------------------------------------------------
    fun getMusicEnabled(): Boolean = prefs.getBoolean("music_enabled", true)
    fun setMusicEnabled(enabled: Boolean) =
        prefs.edit().putBoolean("music_enabled", enabled).apply()

    fun getMusicVolume(): Int = prefs.getInt("music_volume", 70)
    fun setMusicVolume(volume: Int) =
        prefs.edit().putInt("music_volume", volume.coerceIn(0, 100)).apply()

    // ----- Sound -------------------------------------------------
    fun getSoundEnabled(): Boolean = prefs.getBoolean("sound_enabled", true)
    fun setSoundEnabled(enabled: Boolean) =
        prefs.edit().putBoolean("sound_enabled", enabled).apply()

    fun getSoundVolume(): Int = prefs.getInt("sound_volume", 80)
    fun setSoundVolume(volume: Int) =
        prefs.edit().putInt("sound_volume", volume.coerceIn(0, 100)).apply()

    // ----- Vibration ---------------------------------------------
    fun getVibrationEnabled(): Boolean = prefs.getBoolean("vibration_enabled", true)
    fun setVibrationEnabled(enabled: Boolean) =
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()




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

    fun setSeenWelcome(seen: Boolean) {
        prefs.edit().putBoolean("seen_welcome", seen).apply()
    }

    fun getSeenWelcome(): Boolean {
        return prefs.getBoolean("seen_welcome", false)
    }
    // ── Save extra data when logging in ───────────────────────────────────
    fun saveUserData(name: String, email: String?, level: Int, stars: Int) {
        prefs.edit()
            .putString("full_name", name)
            .putString("email", email)
            .putInt("level", level)
            .putInt("total_stars", stars)
            .apply()
    }
    fun saveFullName(name: String) {
        prefs.edit()
            .putString("full_name", name)

            .apply()
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