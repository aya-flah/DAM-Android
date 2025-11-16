package com.pianokids.game.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pianokids.game.data.api.RetrofitClient
import com.pianokids.game.data.repository.AuthRepository
import com.pianokids.game.data.repository.LevelRepository
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
        Log.d("AuthViewModel", "========== AUTHVIEWMODEL INIT ==========")
        // Check login state on start
        refreshLoginState()
        refreshUserName()
    }

    /** Call this after a successful social-login */
    suspend fun onLoginSuccess() {
        Log.d("AuthViewModel", "========== onLoginSuccess() CALLED ==========")
        refreshLoginState()
        refreshUserName()
        // Wait a bit to ensure state is updated
        kotlinx.coroutines.delay(100)
        Log.d("AuthViewModel", "========== onLoginSuccess() COMPLETE - isLoggedIn=${_isLoggedIn.value} ==========")
    }

    /** Call this on logout */
    fun onLogout() {
        Log.d("AuthViewModel", "========== onLogout() CALLED ==========")
        authRepo.logout()
        _isLoggedIn.value = false
        _userName.value = "Guest Player"
    }

    /** Update user name (called from ProfileScreen when name is edited) */
    fun updateUserName(newName: String) {
        Log.d("AuthViewModel", "updateUserName called: $newName")
        _userName.value = newName
    }

    /** Refresh the flow – checks local token + backend verification */
    private fun refreshLoginState() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "--- refreshLoginState START ---")

            val hasToken = userPrefs.hasLocalToken()
            Log.d("AuthViewModel", "hasLocalToken: $hasToken")

            val token = userPrefs.getAuthToken()
            Log.d("AuthViewModel", "Token (first 10 chars): ${token?.take(10)}")

            val isGuestMode = userPrefs.isGuestMode()
            Log.d("AuthViewModel", "isGuestMode: $isGuestMode")

            val isVerified = if (hasToken) {
                val verified = authRepo.verifyToken()
                Log.d("AuthViewModel", "Token verified: $verified")
                verified
            } else {
                Log.d("AuthViewModel", "No token to verify")
                false
            }

            _isLoggedIn.value = hasToken && isVerified
            Log.d("AuthViewModel", "Final isLoggedIn value: ${_isLoggedIn.value}")
            Log.d("AuthViewModel", "--- refreshLoginState END ---")
        }
    }

    /** Refresh user name from preferences */
    private fun refreshUserName() {
        viewModelScope.launch {
            Log.d("AuthViewModel", "--- refreshUserName START ---")

            val user = userPrefs.getUser()
            Log.d("AuthViewModel", "User from prefs: $user")

            val fullName = userPrefs.getFullName()
            Log.d("AuthViewModel", "Full name from prefs: $fullName")

            _userName.value = when {
                user != null -> {
                    Log.d("AuthViewModel", "Using user.name: ${user.name}")
                    user.name
                }
                _isLoggedIn.value -> {
                    Log.d("AuthViewModel", "Using fullName: $fullName")
                    fullName
                }
                else -> {
                    Log.d("AuthViewModel", "Using Guest Player")
                    "Guest Player"
                }
            }

            Log.d("AuthViewModel", "Final userName value: ${_userName.value}")
            Log.d("AuthViewModel", "--- refreshUserName END ---")
        }
    }

    // ── Social login helpers (return Result for UI handling) ─────────────────
    suspend fun loginWithGoogle(idToken: String) = login("google", idToken)
    suspend fun loginWithFacebook(accessToken: String) = login("facebook", accessToken)

    private suspend fun login(provider: String, token: String) =
        authRepo.loginWithSocial(token, provider).also { result ->
            result.onSuccess {
                Log.d("AuthViewModel", "Login successful in ViewModel, calling onLoginSuccess()")
                onLoginSuccess()
            }
            result.onFailure { error ->
                Log.e("AuthViewModel", "Login failed in ViewModel: ${error.message}")
            }
        }
}