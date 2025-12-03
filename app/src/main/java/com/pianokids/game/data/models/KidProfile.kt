package com.pianokids.game.data.models

/**
 * Local-only kid profile used when children create a unique name instead of using social login.
 * Acts as a lightweight identity so we can personalize the UI without touching the backend.
 */
data class KidProfile(
    val uniqueName: String,
    val displayName: String,
    val age: Int,
    val avatarEmoji: String,
    val avatarColorHex: String,
    val backendAvatarId: String? = null,
    val backendAvatarName: String? = null,
    val backendAvatarImageUrl: String? = null
)
