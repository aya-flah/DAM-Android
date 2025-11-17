package com.pianokids.game.data.models

data class UnlockedLevelsResponse(
    val userId: String,
    val levels: List<UnlockedLevelItem>
)

data class UnlockedLevelItem(
    val levelId: String,
    val title: String,
    val theme: String,
    val unlocked: Boolean,
    val starsUnlocked: Int,
    val backgroundUrl: String?,
    val bossUrl: String?,
    val musicUrl: String?
)
