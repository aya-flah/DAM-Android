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

    /** Public read-only flow */
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        // Check login state on start
        refreshLoginState()
    }

    /** Call this after a successful social-login */
    fun onLoginSuccess() = refreshLoginState()

    /** Call this on logout */
    fun onLogout() {
        authRepo.logout()
        _isLoggedIn.value = false
    }


    /** Refresh the flow – checks local token + backend verification */
    private fun refreshLoginState() {
        viewModelScope.launch {
            val hasToken = userPrefs.hasLocalToken()
            _isLoggedIn.value = hasToken && userPrefs.verifyTokenWithBackend()
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
