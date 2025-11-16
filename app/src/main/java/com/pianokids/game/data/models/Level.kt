package com.pianokids.game.data.models

data class Level(
    val _id: String,
    val order: Int,
    val title: String,
    val theme: String,
    val story: String,
    val expectedNotes: List<String>,
    val difficulty: Int,
    val backgroundUrl: String?,
    val bossUrl: String?,
    val musicUrl: String?,
    val starsUnlocked: Int,
    val mapPosition: MapPosition,
    val islandImageUrl: String,
    val nextLevelId: String?,
    val colorTheme: String?
)

data class MapPosition(
    val x: Float,
    val y: Float
)
