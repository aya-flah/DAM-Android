package com.pianokids.game.data.models

data class UserLevelDataResponse(
    val userId: String,
    val levelId: String,
    val sublevels: List<Sublevel>
)
