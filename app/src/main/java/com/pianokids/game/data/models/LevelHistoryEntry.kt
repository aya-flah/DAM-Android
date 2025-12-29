package com.pianokids.game.data.models

/**
 * Metadata returned by the backend for a recorded play session.
 */
data class LevelHistoryEntry(
    val id: String,
    val userId: String,
    val levelId: String,
    val levelTitle: String,
    val levelTheme: String,
    val sublevelId: String,
    val sublevelTitle: String?,
    val notes: List<String>,
    val noteDurations: List<Float>,
    val durationMs: Long,
    val stars: Int,
    val completed: Boolean,
    val wrongNotes: Int,
    val createdAt: String
)
