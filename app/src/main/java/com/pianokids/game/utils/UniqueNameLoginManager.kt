package com.pianokids.game.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.pianokids.game.data.models.KidProfile
import java.util.Locale

/**
 * Handles registering kid-specific unique names and building lightweight identities.
 * This keeps the backend untouched while letting us validate uniqueness locally.
 */
class UniqueNameLoginManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun validateUniqueName(rawInput: String): String? {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) {
            return "Choisis un nom magique !"
        }
        if (trimmed.length < 3) {
            return "Au moins 3 lettres, s'il te plaît."
        }
        if (trimmed.length > 18) {
            return "Ce nom est un peu long (18 max)."
        }
        val regex = "^[a-zA-Z0-9_]+$".toRegex()
        if (!regex.matches(trimmed)) {
            return "Lettres, chiffres ou _ uniquement."
        }
        return null
    }

    fun isNameTaken(uniqueName: String): Boolean {
        val normalized = normalize(uniqueName)
        return getRegisteredNames().contains(normalized)
    }

    fun reserveName(uniqueName: String) {
        val normalized = normalize(uniqueName)
        val names = getRegisteredNames().toMutableSet()
        names.add(normalized)
        prefs.edit { putStringSet(KEY_REGISTERED_NAMES, names) }
    }

    fun releaseName(uniqueName: String) {
        val normalized = normalize(uniqueName)
        val names = getRegisteredNames().toMutableSet()
        names.remove(normalized)
        prefs.edit { putStringSet(KEY_REGISTERED_NAMES, names) }
    }

    fun buildKidEmail(uniqueName: String): String {
        val normalized = normalize(uniqueName)
        return "$normalized@pianokids.fun"
    }

    fun saveProfile(userPrefs: UserPreferences, profile: KidProfile) {
        reserveName(profile.uniqueName)
        userPrefs.saveKidProfile(profile)
        userPrefs.setSeenWelcome(true)
        val normalized = normalize(profile.uniqueName)
        prefs.edit {
            putString(profileKey(normalized), gson.toJson(profile))
        }
    }

    fun getStoredProfile(uniqueName: String): KidProfile? {
        val normalized = normalize(uniqueName)
        val json = prefs.getString(profileKey(normalized), null) ?: return null
        return runCatching { gson.fromJson(json, KidProfile::class.java) }.getOrNull()
    }

    private fun getRegisteredNames(): Set<String> {
        return prefs.getStringSet(KEY_REGISTERED_NAMES, emptySet()) ?: emptySet()
    }

    private fun normalize(input: String): String {
        return input.trim().lowercase(Locale.ROOT)
    }

    private fun profileKey(normalizedUniqueName: String) = "$KEY_PROFILE_PREFIX$normalizedUniqueName"

    companion object {
        private const val PREF_FILE = "kid_unique_names"
        private const val KEY_REGISTERED_NAMES = "registered_unique_names"
        private const val KEY_PROFILE_PREFIX = "kid_profile_"
    }
}
