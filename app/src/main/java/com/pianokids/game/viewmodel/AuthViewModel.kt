package com.pianokids.game.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pianokids.game.data.repository.AuthRepository
import com.pianokids.game.utils.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepo = AuthRepository(application.applicationContext)
    private val userPrefs = UserPreferences(application.applicationContext)

    /** Public read-only flow for login state */
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    /** Public read-only flow for user name */
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    init {
        // Check login state on start
        refreshLoginState()
        refreshUserName()
    }

    /** Call this after a successful social-login */
    fun onLoginSuccess() {
        refreshLoginState()
        refreshUserName()
    }

    /** Call this on logout */
    fun onLogout() {
        authRepo.logout()
        _isLoggedIn.value = false
        _userName.value = "Guest Player"
    }

    /** Update user name (called from ProfileScreen when name is edited) */
    fun updateUserName(newName: String) {
        _userName.value = newName
    }

    /** Refresh the flow – checks local token + backend verification */
    private fun refreshLoginState() {
        viewModelScope.launch {
            val hasToken = userPrefs.hasLocalToken()
            _isLoggedIn.value = hasToken && userPrefs.verifyTokenWithBackend()
        }
    }

    /** Refresh user name from preferences */
    private fun refreshUserName() {
        viewModelScope.launch {
            val user = userPrefs.getUser()
            _userName.value = when {
                user != null -> user.name
                _isLoggedIn.value -> userPrefs.getFullName()
                else -> "Guest Player"
            }
        }
    }

    // ── Social login helpers (return Result for UI handling) ─────────────────
    suspend fun loginWithGoogle(idToken: String) = login("google", idToken)
    suspend fun loginWithFacebook(accessToken: String) = login("facebook", accessToken)

    private suspend fun login(provider: String, token: String) =
        authRepo.loginWithSocial(token, provider).also { result ->
            result.onSuccess { onLoginSuccess() }
        }
}