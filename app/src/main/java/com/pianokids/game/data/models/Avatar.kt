package com.pianokids.game.data.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Avatar(
    @SerializedName("_id")
    val id: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("customization")
    val customization: AvatarCustomization,
    
    @SerializedName("isActive")
    val isActive: Boolean,
    
    @SerializedName("expression")
    val expression: String,
    
    @SerializedName("avatarImageUrl")
    val avatarImageUrl: String?,
    
    @SerializedName("energy")
    val energy: Int,
    
    @SerializedName("experience")
    val experience: Int,
    
    @SerializedName("level")
    val level: Int,
    
    @SerializedName("state")
    val state: String,
    
    @SerializedName("outfits")
    val outfits: AvatarOutfits,
    
    @SerializedName("createdAt")
    val createdAt: String,
    
    @SerializedName("updatedAt")
    val updatedAt: String
)

data class AvatarCustomization(
    @SerializedName("style")
    val style: String,
    
    @SerializedName("bodyType")
    val bodyType: String,
    
    @SerializedName("skinTone")
    val skinTone: String,
    
    @SerializedName("hairstyle")
    val hairstyle: String,
    
    @SerializedName("hairColor")
    val hairColor: String,
    
    @SerializedName("eyeStyle")
    val eyeStyle: String,
    
    @SerializedName("eyeColor")
    val eyeColor: String,
    
    @SerializedName("clothingType")
    val clothingType: String? = null,
    
    @SerializedName("clothingColor")
    val clothingColor: String? = null,
    
    @SerializedName("accessories")
    val accessories: List<String>? = null
)

data class AvatarOutfits(
    @SerializedName("unlocked")
    val unlocked: List<String>,
    
    @SerializedName("equipped")
    val equipped: String
)

// DTOs for API requests
data class CreateAvatarDto(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("customization")
    val customization: CreateAvatarCustomizationDto,
    
    @SerializedName("avatarImageUrl")
    val avatarImageUrl: String?
)

data class CreateAvatarCustomizationDto(
    @SerializedName("style")
    val style: String,
    
    @SerializedName("bodyType")
    val bodyType: String,
    
    @SerializedName("skinTone")
    val skinTone: String,
    
    @SerializedName("hairstyle")
    val hairstyle: String,
    
    @SerializedName("hairColor")
    val hairColor: String,
    
    @SerializedName("eyeStyle")
    val eyeStyle: String,
    
    @SerializedName("eyeColor")
    val eyeColor: String,
    
    @SerializedName("clothingType")
    val clothingType: String? = null,
    
    @SerializedName("clothingColor")
    val clothingColor: String? = null,
    
    @SerializedName("accessories")
    val accessories: List<String>? = null
)

data class UpdateAvatarDto(
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("customization")
    val customization: CreateAvatarCustomizationDto? = null,
    
    @SerializedName("expression")
    val expression: String? = null,
    
    @SerializedName("energy")
    val energy: Int? = null,
    
    @SerializedName("state")
    val state: String? = null,
    
    @SerializedName("isActive")
    val isActive: Boolean? = null,
    
    @SerializedName("equippedOutfit")
    val equippedOutfit: String? = null,
    
    @SerializedName("avatarImageUrl")
    val avatarImageUrl: String? = null
)

data class AvatarStats(
    @SerializedName("level")
    val level: Int,
    
    @SerializedName("experience")
    val experience: Int,
    
    @SerializedName("energy")
    val energy: Int,
    
    @SerializedName("outfitsUnlocked")
    val outfitsUnlocked: Int
)
