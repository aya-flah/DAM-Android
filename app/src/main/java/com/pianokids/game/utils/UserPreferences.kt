// UserPreferences.kt
package com.pianokids.game.utils

import com.pianokids.game.data.models.User
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.pianokids.game.data.models.AuthUser
import com.pianokids.game.data.models.KidProfile

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
            .remove(KEY_AVATAR_THUMBNAIL)
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

    // ── KID PROFILE ───────────────────────────────────────────────────────
    fun saveKidProfile(profile: KidProfile) {
        prefs.edit {
            putString(KEY_KID_UNIQUE_NAME, profile.uniqueName)
            putString(KEY_KID_DISPLAY_NAME, profile.displayName)
            putInt(KEY_KID_AGE, profile.age)
            putString(KEY_KID_AVATAR_EMOJI, profile.avatarEmoji)
            putString(KEY_KID_AVATAR_COLOR, profile.avatarColorHex)
            putString(KEY_KID_BACKEND_AVATAR_ID, profile.backendAvatarId)
            putString(KEY_KID_BACKEND_AVATAR_NAME, profile.backendAvatarName)
            putString(KEY_KID_BACKEND_AVATAR_IMAGE, profile.backendAvatarImageUrl)
            putBoolean(KEY_KID_PROFILE_ACTIVE, true)
        }
    }

    fun getKidProfile(): KidProfile? {
        if (!hasKidProfile()) return null

        val uniqueName = prefs.getString(KEY_KID_UNIQUE_NAME, null) ?: return null
        val displayName = prefs.getString(KEY_KID_DISPLAY_NAME, null) ?: uniqueName
        val age = prefs.getInt(KEY_KID_AGE, -1)
        val emoji = prefs.getString(KEY_KID_AVATAR_EMOJI, "🎵") ?: "🎵"
        val color = prefs.getString(KEY_KID_AVATAR_COLOR, "#6A5AE0") ?: "#6A5AE0"
        val backendAvatarId = prefs.getString(KEY_KID_BACKEND_AVATAR_ID, null)
        val backendAvatarName = prefs.getString(KEY_KID_BACKEND_AVATAR_NAME, null)
        val backendAvatarImage = prefs.getString(KEY_KID_BACKEND_AVATAR_IMAGE, null)

        if (age == -1) return null

        return KidProfile(
            uniqueName = uniqueName,
            displayName = displayName,
            age = age,
            avatarEmoji = emoji,
            avatarColorHex = color,
            backendAvatarId = backendAvatarId,
            backendAvatarName = backendAvatarName,
            backendAvatarImageUrl = backendAvatarImage
        )
    }

    fun hasKidProfile(): Boolean {
        return prefs.getBoolean(KEY_KID_PROFILE_ACTIVE, false)
                && prefs.getString(KEY_KID_UNIQUE_NAME, null) != null
    }

    fun clearKidProfile() {
        prefs.edit {
            remove(KEY_KID_UNIQUE_NAME)
            remove(KEY_KID_DISPLAY_NAME)
            remove(KEY_KID_AGE)
            remove(KEY_KID_AVATAR_EMOJI)
            remove(KEY_KID_AVATAR_COLOR)
            remove(KEY_KID_BACKEND_AVATAR_ID)
            remove(KEY_KID_BACKEND_AVATAR_NAME)
            remove(KEY_KID_BACKEND_AVATAR_IMAGE)
            putBoolean(KEY_KID_PROFILE_ACTIVE, false)
        }
    }

    fun getKidEmailAlias(): String? {
        val uniqueName = prefs.getString(KEY_KID_UNIQUE_NAME, null) ?: return null
        return "$uniqueName@pianokids.fun"
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

    // ── AVATAR THUMBNAIL ─────────────────────────────────────────────────
    fun saveAvatarThumbnail(url: String) {
        prefs.edit {
            putString(KEY_AVATAR_THUMBNAIL, url)
            Log.d("UserPreferences", "Avatar thumbnail saved: $url")
        }
    }

    fun getAvatarThumbnail(): String? {
        return prefs.getString(KEY_AVATAR_THUMBNAIL, null)
    }

    fun clearAvatarThumbnail() {
        prefs.edit {
            remove(KEY_AVATAR_THUMBNAIL)
            Log.d("UserPreferences", "Avatar thumbnail cleared")
        }
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_PROVIDER_ID = "provider_id"
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_SEEN_WELCOME = "seen_welcome"
        private const val PREF_IS_GUEST = "is_guest_mode"
        private const val KEY_AVATAR_THUMBNAIL = "avatar_thumbnail"
        private const val KEY_KID_PROFILE_ACTIVE = "kid_profile_active"
        private const val KEY_KID_UNIQUE_NAME = "kid_unique_name"
        private const val KEY_KID_DISPLAY_NAME = "kid_display_name"
        private const val KEY_KID_AGE = "kid_age"
        private const val KEY_KID_AVATAR_EMOJI = "kid_avatar_emoji"
        private const val KEY_KID_AVATAR_COLOR = "kid_avatar_color"
        private const val KEY_KID_BACKEND_AVATAR_ID = "kid_backend_avatar_id"
        private const val KEY_KID_BACKEND_AVATAR_NAME = "kid_backend_avatar_name"
        private const val KEY_KID_BACKEND_AVATAR_IMAGE = "kid_backend_avatar_image"
    }
}