package com.pianokids.game.utils

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "piano_kids_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_LEVEL = "level"
        private const val KEY_TOTAL_STARS = "total_stars"
        private const val KEY_AVATAR_URL = "avatar_url"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    fun saveUserData(
        token: String,
        userId: String,
        fullName: String,
        email: String,
        level: Int = 1,
        totalStars: Int = 0,
        avatarURL: String? = null
    ) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_ID, userId)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_EMAIL, email)
            putInt(KEY_LEVEL, level)
            putInt(KEY_TOTAL_STARS, totalStars)
            putString(KEY_AVATAR_URL, avatarURL)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun updateLevel(level: Int) {
        prefs.edit().putInt(KEY_LEVEL, level).apply()
    }

    fun updateStars(totalStars: Int) {
        prefs.edit().putInt(KEY_TOTAL_STARS, totalStars).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getFullName(): String = prefs.getString(KEY_FULL_NAME, "Player") ?: "Player"

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun getLevel(): Int = prefs.getInt(KEY_LEVEL, 1)

    fun getTotalStars(): Int = prefs.getInt(KEY_TOTAL_STARS, 0)

    fun getAvatarURL(): String? = prefs.getString(KEY_AVATAR_URL, null)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun clearUserData() {
        prefs.edit().clear().apply()
    }

    fun logout() {
        clearUserData()
    }
}