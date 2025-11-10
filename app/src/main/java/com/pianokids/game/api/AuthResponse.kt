
package com.pianokids.game.data.models

import User
import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("user")
    val user: User
)

