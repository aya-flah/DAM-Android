package com.pianokids.game.data.models

data class Level(
    val _id: String,
    val order: Int = 0,
    val title: String = "",
    val theme: String = "",
    val story: String = "",
    val expectedNotes: List<String> = emptyList(),
    val difficulty: Int = 1,
    val backgroundUrl: String? = null,
    val bossUrl: String? = null,
    val musicUrl: String? = null,
    val previewAudioUrl: String? = null,  // Song preview URL
    val previewDuration: Int = 10,        // Duration in seconds
    val autoPlayPreview: Boolean = false,  // Auto-play on level load
    val starsUnlocked: Int = 0,
    val mapPosition: MapPosition? = null,
    val islandImageUrl: String? = null,
    val nextLevelId: String? = null,
    val colorTheme: String? = null
)

data class MapPosition(
    val x: Float,
    val y: Float
)

// Generate default spiral path positions for levels without mapPosition
fun Level.getEffectivePosition(index: Int): MapPosition {
    if (mapPosition != null) return mapPosition
    
    // Create a spiral path pattern
    val angle = (index * 0.8f) % (2 * Math.PI.toFloat())
    val radius = 0.3f + (index * 0.05f)
    return MapPosition(
        x = 0.5f + radius * kotlin.math.cos(angle),
        y = 0.3f + radius * kotlin.math.sin(angle) + (index * 0.15f)
    )
}
