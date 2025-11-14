// UserPreferences.kt
package com.pianokids.game.utils

import com.pianokids.game.data.models.User
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.pianokids.game.data.models.AuthUser

class UserPreferences(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("piano_kids_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

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
                // Clear guest mode when saving a real auth token
                putBoolean(PREF_IS_GUEST, false)
                Log.d("UserPreferences", "Auth token saved, guest mode cleared")
            }
        }
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun hasLocalToken(): Boolean {
        return !getAuthToken().isNullOrEmpty()
    }

    // ── PROVIDER ID ────────────────────────────────────────────────────────
    fun saveProviderId(providerId: String?) {
        prefs.edit {
            if (providerId.isNullOrEmpty()) {
                remove(KEY_PROVIDER_ID)
                Log.w("UserPreferences", "Attempted to save null or empty provider ID")
            } else {
                putString(KEY_PROVIDER_ID, providerId)
                Log.d("UserPreferences", "Provider ID saved: $providerId")
            }
        }
    }

    fun getProviderId(): String? {
        return prefs.getString(KEY_PROVIDER_ID, null)
    }

    // ── USER DATA ──────────────────────────────────────────────────────────
    fun saveUser(user: AuthUser) {
        prefs.edit {
            val userJson = gson.toJson(user)
            putString(KEY_USER_DATA, userJson)
            // Clear guest mode when saving real user data
            putBoolean(PREF_IS_GUEST, false)
            Log.d("UserPreferences", "User saved: $userJson, guest mode cleared")
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

    // ── Logout ───────────────────────────────────────────────────────────
    fun clearAuth() {
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_PROVIDER_ID)
            .remove(KEY_USER_DATA)
            .putBoolean("is_logged_in", false)
            .putBoolean(PREF_IS_GUEST, false)
            .apply()
        Log.d("UserPreferences", "Auth cleared")
    }

    // ── Extra getters ────────────────────────────────────────────────────
    fun getFullName(): String = prefs.getString("full_name", "Player") ?: "Player"
    fun getEmail(): String? = prefs.getString("email", null)
    fun getLevel(): Int = prefs.getInt("level", 1)
    fun getTotalStars(): Int = prefs.getInt("total_stars", 0)

    fun setSeenWelcome(seen: Boolean) {
        prefs.edit().putBoolean(KEY_SEEN_WELCOME, seen).apply()
    }

    fun getSeenWelcome(): Boolean {
        return prefs.getBoolean(KEY_SEEN_WELCOME, false)
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

    // ── GUEST MODE ────────────────────────────────────────────────────────
    fun clearGuestMode() {
        prefs.edit {
            putBoolean(PREF_IS_GUEST, false)
            Log.d("UserPreferences", "Guest mode cleared")
        }
    }

    fun setGuestMode(isGuest: Boolean) {
        prefs.edit().putBoolean(PREF_IS_GUEST, isGuest).apply()
        Log.d("UserPreferences", "Guest mode set to: $isGuest")
    }

    fun isGuestMode(): Boolean {
        val isGuest = prefs.getBoolean(PREF_IS_GUEST, false)
        val hasToken = hasLocalToken()

        // If we have a token, we're definitely not in guest mode
        if (hasToken && isGuest) {
            Log.w("UserPreferences", "Inconsistent state: has token but guest flag is true, clearing guest mode")
            clearGuestMode()
            return false
        }

        Log.d("UserPreferences", "Is guest mode: $isGuest, has token: $hasToken")
        return isGuest
    }

    // Only level 1 is unlocked
    fun getUnlockedLevels(): List<Int> {
        return listOf(1)
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_PROVIDER_ID = "provider_id"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_SEEN_WELCOME = "seen_welcome"
        private const val PREF_IS_GUEST = "is_guest_mode"
    }
}