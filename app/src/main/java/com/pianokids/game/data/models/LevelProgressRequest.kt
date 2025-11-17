package com.pianokids.game.data.models

data class LevelProgressRequest(
    val userId: String,
    val levelId: String,
    val stars: Int,
    val score: Int,
    val completed: Boolean
)