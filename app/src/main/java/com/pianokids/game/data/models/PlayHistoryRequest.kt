package com.pianokids.game.data.models

/**
 * Payload sent to the backend when storing a level play history entry.
 */
data class PlayHistoryRequest(
    val userId: String,
    val levelId: String,
    val sublevelId: String,
    val notes: List<String>,
    val noteDurations: List<Float>,
    val durationMs: Long,
    val stars: Int,
    val completed: Boolean,
    val wrongNotes: Int
)
