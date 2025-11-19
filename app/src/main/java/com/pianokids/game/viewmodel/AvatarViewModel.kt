package com.pianokids.game.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pianokids.game.data.models.*
import com.pianokids.game.data.repository.AvatarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AvatarViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AvatarRepository(application)
    
    private val _avatars = MutableStateFlow<List<Avatar>>(emptyList())
    val avatars: StateFlow<List<Avatar>> = _avatars.asStateFlow()
    
    private val _activeAvatar = MutableStateFlow<Avatar?>(null)
    val activeAvatar: StateFlow<Avatar?> = _activeAvatar.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadUserAvatars() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            repository.getUserAvatars().fold(
                onSuccess = { avatarList ->
                    _avatars.value = avatarList
                    _activeAvatar.value = avatarList.firstOrNull { it.isActive }
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
            
            val createDto = CreateAvatarDto(
                name = name,
                customization = CreateAvatarCustomizationDto(
                    style = "default",
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
            
            repository.createAvatar(createDto).fold(
                onSuccess = { avatar ->
                    Log.d("AvatarViewModel", "Avatar created: ${avatar.name}")
                    // Reload avatars to refresh the list
                    loadUserAvatars()
                },
                onFailure = { exception ->
                    _error.value = exception.message
                    Log.e("AvatarViewModel", "Failed to create avatar", exception)
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
                    Log.d("AvatarViewModel", "Active avatar set: ${avatar.name}")
                    // Reload avatars to update active status
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
    
    fun clearError() {
        _error.value = null
    }
}
