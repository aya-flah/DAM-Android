package com.pianokids.game.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pianokids.game.data.models.*
import com.pianokids.game.data.repository.AvatarRepository
import com.pianokids.game.utils.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AvatarViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AvatarRepository(application)
    private val userPrefs = UserPreferences(application) // ✅ add this

    private val _avatars = MutableStateFlow<List<Avatar>>(emptyList())
    val avatars: StateFlow<List<Avatar>> = _avatars.asStateFlow()
    
    private val _activeAvatar = MutableStateFlow<Avatar?>(null)
    val activeAvatar: StateFlow<Avatar?> = _activeAvatar.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isGeneratingAI = MutableStateFlow(false)
    val isGeneratingAI: StateFlow<Boolean> = _isGeneratingAI.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _aiGenerationResponse = MutableStateFlow<AvatarGenerationResponse?>(null)
    val aiGenerationResponse: StateFlow<AvatarGenerationResponse?> = _aiGenerationResponse.asStateFlow()

    fun loadUserAvatars() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getUserAvatars().fold(
                onSuccess = { avatarList ->
                    _avatars.value = avatarList
                    val active = avatarList.firstOrNull { it.isActive }
                    _activeAvatar.value = active

                    // ✅ Save thumbnail if present
                    active?.avatarImageUrl
                        ?.takeIf { it.isNotBlank() }
                        ?.let { userPrefs.saveAvatarThumbnail(it) }

                    Log.d("AvatarViewModel", "Loaded ${avatarList.size} avatars")
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    Log.e("AvatarViewModel", "Failed to load avatars", exception)
                }
            )

            _isLoading.value = false
        }
    }

    fun loadActiveAvatar() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getActiveAvatar().fold(
                onSuccess = { avatar ->
                    _activeAvatar.value = avatar

                    // ✅ Save thumbnail here too
                    avatar.avatarImageUrl
                        ?.takeIf { it.isNotBlank() }
                        ?.let { userPrefs.saveAvatarThumbnail(it) }

                    Log.d("AvatarViewModel", "Loaded active avatar: ${avatar.name}")
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    Log.e("AvatarViewModel", "Failed to load active avatar", exception)
                }
            )

            _isLoading.value = false
        }
    }
    
    fun createAvatar(name: String, avatarImageUrl: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            Log.d("AvatarViewModel", "🎯 Creating avatar - Name: $name, URL: $avatarImageUrl")
            
            val createDto = CreateAvatarDto(
                name = name,
                customization = CreateAvatarCustomizationDto(
                    style = "anime",  // Must be one of: anime, cartoon, pixel, realistic
                    bodyType = "medium",
                    skinTone = "medium",
                    hairstyle = "short",
                    hairColor = "brown",
                    eyeStyle = "default",
                    eyeColor = "brown",
                    clothingType = null,
                    clothingColor = null,
                    accessories = null
                ),
                avatarImageUrl = avatarImageUrl
            )
            
            Log.d("AvatarViewModel", "🎯 DTO created: $createDto")
            Log.d("AvatarViewModel", "🎯 Calling repository.createAvatar...")
            
            repository.createAvatar(createDto).fold(
                onSuccess = { avatar ->
                    Log.d("AvatarViewModel", "✅ Avatar created successfully: ${avatar.name}, ID: ${avatar.id}")
                    Log.d("AvatarViewModel", "✅ Avatar image URL: ${avatar.avatarImageUrl}")
                    // Reload avatars to refresh the list
                    loadUserAvatars()
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    Log.e("AvatarViewModel", "❌ Failed to create avatar: ${exception.message}", exception)
                    Log.e("AvatarViewModel", "❌ Exception type: ${exception.javaClass.simpleName}")
                }
            )
            
            _isLoading.value = false
        }
    }

    fun setActiveAvatar(avatarId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.setActiveAvatar(avatarId).fold(
                onSuccess = { avatar ->
                    _activeAvatar.value = avatar

                    // ✅ Save thumbnail whenever active avatar changes
                    avatar.avatarImageUrl
                        ?.takeIf { it.isNotBlank() }
                        ?.let { userPrefs.saveAvatarThumbnail(it) }

                    Log.d("AvatarViewModel", "Active avatar set: ${avatar.name}")
                    loadUserAvatars()
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    Log.e("AvatarViewModel", "Failed to set active avatar", exception)
                }
            )

            _isLoading.value = false
        }
    }
    
    fun deleteAvatar(avatarId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.deleteAvatar(avatarId).fold(
                onSuccess = { message ->
                    Log.d("AvatarViewModel", "Avatar deleted: $message")
                    // Reload avatars to refresh the list
                    loadUserAvatars()
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    Log.e("AvatarViewModel", "Failed to delete avatar", exception)
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun updateExpression(avatarId: String, expression: String) {
        viewModelScope.launch {
            repository.updateExpression(avatarId, expression).fold(
                onSuccess = { avatar ->
                    if (_activeAvatar.value?.id == avatarId) {
                        _activeAvatar.value = avatar
                    }
                    Log.d("AvatarViewModel", "Expression updated: $expression")
                },
                onFailure = { exception ->
                    Log.e("AvatarViewModel", "Failed to update expression", exception)
                }
            )
        }
    }
    
    fun updateEnergy(avatarId: String, energyDelta: Int) {
        viewModelScope.launch {
            repository.updateEnergy(avatarId, energyDelta).fold(
                onSuccess = { avatar ->
                    if (_activeAvatar.value?.id == avatarId) {
                        _activeAvatar.value = avatar
                    }
                    Log.d("AvatarViewModel", "Energy updated: ${avatar.energy}")
                },
                onFailure = { exception ->
                    Log.e("AvatarViewModel", "Failed to update energy", exception)
                }
            )
        }
    }
    
    fun addExperience(avatarId: String, xpGain: Int) {
        viewModelScope.launch {
            repository.addExperience(avatarId, xpGain).fold(
                onSuccess = { avatar ->
                    if (_activeAvatar.value?.id == avatarId) {
                        _activeAvatar.value = avatar
                    }
                    Log.d("AvatarViewModel", "Experience added: ${avatar.experience}")
                },
                onFailure = { exception ->
                    Log.e("AvatarViewModel", "Failed to add experience", exception)
                }
            )
        }
    }
    
    // Gemini AI Avatar Generation (preview only)
    fun generateAvatarFromPrompt(prompt: String, name: String, style: String = "cartoon") {
        viewModelScope.launch {
            _isGeneratingAI.value = true
            _isLoading.value = true
            _error.value = null
            _aiGenerationResponse.value = null
            
            Log.d("AvatarViewModel", "🤖 Generating avatar preview with AI - Name: $name, Prompt: $prompt, Style: $style")
            
            repository.generateAvatarFromPrompt(prompt, name, style).fold(
                onSuccess = { response ->
                    Log.d("AvatarViewModel", "✅ AI Avatar preview generated successfully")
                    Log.d("AvatarViewModel", "✅ Name: ${response.name}")
                    Log.d("AvatarViewModel", "✅ Description: ${response.description}")
                    Log.d("AvatarViewModel", "✅ Generation Source: ${response.generationSource}")
                    
                    // Emit generation response for preview (NOT saved to DB yet)
                    _aiGenerationResponse.value = response
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    Log.e("AvatarViewModel", "❌ Failed to generate avatar with AI: ${exception.message}", exception)
                }
            )
            
            _isGeneratingAI.value = false
            _isLoading.value = false
        }
    }
    
    // Save AI avatar after user approves preview
    fun saveAIAvatar(previewData: Any) {
        viewModelScope.launch {
            _isLoading.value = true
            
            Log.d("AvatarViewModel", "💾 Saving AI avatar...")
            
            repository.saveAIAvatar(previewData).fold(
                onSuccess = { response ->
                    Log.d("AvatarViewModel", "✅ AI Avatar saved successfully!")
                    Log.d("AvatarViewModel", "✅ Avatar ID: ${response.avatarId}")
                    
                    // Save thumbnail if available
                    response.avatarImageUrl
                        ?.takeIf { it.isNotBlank() }
                        ?.let { userPrefs.saveAvatarThumbnail(it) }
                    
                    // Reload avatars to include the new saved avatar
                    loadUserAvatars()
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    Log.e("AvatarViewModel", "❌ Failed to save AI avatar: ${exception.message}", exception)
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun clearAIGenerationResponse() {
        _aiGenerationResponse.value = null
    }
    
    fun clearError() {
        _error.value = null
    }
}
