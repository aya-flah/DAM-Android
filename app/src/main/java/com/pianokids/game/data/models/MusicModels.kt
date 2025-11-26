package com.pianokids.game.data.models

import com.google.gson.annotations.SerializedName

/**
 * Response from music recognition endpoint
 */
data class MusicRecognitionResponse(
    @SerializedName("title")
    val title: String,
    
    @SerializedName("artist")
    val artist: String,
    
    @SerializedName("album")
    val album: String,
    
    @SerializedName("confidence")
    val confidence: Double
)
