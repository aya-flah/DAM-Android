package com.pianokids.game.data.models

data class SublevelProgressRequest(
    val userId: String,
    val levelId: String,
    val sublevelId: String,
    val stars: Int,
    val score: Int,
    val completed: Boolean
)
