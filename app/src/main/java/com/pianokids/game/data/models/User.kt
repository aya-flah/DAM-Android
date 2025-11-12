package com.pianokids.game.data.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("photoUrl")
    val photoUrl: String? = null,

    @SerializedName("provider")
    val provider: String,

    @SerializedName("score")
    val score: Int,

    @SerializedName("level")
    val level: Int
)