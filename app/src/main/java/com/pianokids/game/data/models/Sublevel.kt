package com.pianokids.game.data.models

data class Sublevel(
    val _id: String,
    val levelId: String,
    val index: Int,
    val title: String? = null,
    val description: String? = null,
    val difficulty: Int,
    val notes: List<String>,
    val maxStars: Int,
    val requiredStars: Int,
    val trackName: String? = null,
    val backgroundUrl: String? = null,
    val bossUrl: String? = null,
    val heroUrl: String? = null,
    val trackUrl: String? = null,

    // backend enriched response fields
    val unlocked: Boolean = false,
    val starsEarned: Int = 0,
    val completed: Boolean = false,
    val totalStars: Int = 0,
    val previousCompleted: Boolean = false,
)
