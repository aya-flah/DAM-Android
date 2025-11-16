// SocialLoginManager.kt
package com.pianokids.game.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.pianokids.game.R

class SocialLoginManager(private val context: Context) {

    private var googleSignInClient: GoogleSignInClient
    private var facebookCallbackManager: CallbackManager? = null
    private var isFacebookInitialized = false
    private var onFacebookSuccess: ((String) -> Unit)? = null
    private var onFacebookFailure: ((Exception) -> Unit)? = null
    private var onGoogleSuccess: ((String) -> Unit)? = null
    private var onGoogleFailure: ((Exception) -> Unit)? = null


    init {
        // FIXED Google client ID - remove duplicates
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("151121581529-9fa29qlmv5bacqrqinm0od67t4v32tbj.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)

        // Initialize Facebook only if available
        try {
            facebookCallbackManager = CallbackManager.Factory.create()
        } catch (e: Exception) {
            Log.e("SocialLogin", "Facebook initialization failed", e)
        }
    }

    fun signInWithGoogle(
        launcher: ActivityResultLauncher<Intent>,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        this.onGoogleSuccess = onSuccess
        this.onGoogleFailure = onFailure
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (idToken.isNullOrEmpty()) {
                Log.e("SocialLogin", "Google ID Token is null or empty")
                onGoogleFailure?.invoke(Exception("Google authentication failed - no token received"))
                return
            }

            Log.d("SocialLogin", "Google ID Token received: ${idToken.take(20)}...")
            onGoogleSuccess?.invoke(idToken)
        } catch (e: ApiException) {
            Log.e("SocialLogin", "Google sign-in failed", e)
            onGoogleFailure?.invoke(e)
        }
    }
    private fun initializeFacebook() {
        try {
            facebookCallbackManager = CallbackManager.Factory.create()
            isFacebookInitialized = true
            Log.d("SocialLogin", "Facebook initialized successfully")
        } catch (e: Exception) {
            Log.e("SocialLogin", "Facebook initialization failed", e)
            isFacebookInitialized = false
        }
    }




    fun loginWithFacebook(
        activity: Activity,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("SocialLogin", "Attempting Facebook login...")
        Log.d("SocialLogin", "Facebook initialized: $isFacebookInitialized")

        if (!isFacebookInitialized) {
            val error = Exception("Facebook login is not available. Please try another method.")
            Log.e("SocialLogin", "Facebook not initialized", error)
            onFailure(error)
            return
        }

        this.onFacebookSuccess = onSuccess
        this.onFacebookFailure = onFailure

        Log.d("SocialLogin", "Registering Facebook callback...")

        LoginManager.getInstance().registerCallback(
            facebookCallbackManager!!,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Log.d("SocialLogin", "Facebook login successful")
                    val accessToken = result.accessToken.token
                    Log.d("SocialLogin", "Facebook Access Token: ${accessToken.take(10)}...")
                    Log.d("SocialLogin", "Token permissions: ${result.accessToken.permissions}")
                    Log.d("SocialLogin", "Token declined permissions: ${result.accessToken.declinedPermissions}")

                    if (accessToken.isNullOrEmpty()) {
                        Log.e("SocialLogin", "Facebook token is empty")
                        onFailure(Exception("Facebook token is empty"))
                        return
                    }

                    Log.d("SocialLogin", "Calling onSuccess with Facebook token")
                    onSuccess(accessToken)
                }

                override fun onCancel() {
                    Log.w("SocialLogin", "Facebook login cancelled by user")
                    onFailure(Exception("Facebook login cancelled"))
                }

                override fun onError(error: FacebookException) {
                    Log.e("SocialLogin", "Facebook login error", error)
                    Log.e("SocialLogin", "Facebook error message: ${error.message}")
                    Log.e("SocialLogin", "Facebook error localized: ${error.localizedMessage}")
                    onFailure(error)
                }
            }
        )

        Log.d("SocialLogin", "Starting Facebook login with permissions...")
        // Request basic permissions - make sure these are correct
        LoginManager.getInstance().logInWithReadPermissions(activity, listOf("email", "public_profile"))
    }

    fun handleFacebookResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("SocialLogin", "Handling Facebook result - Request: $requestCode, Result: $resultCode")
        Log.d("SocialLogin", "Intent data: ${data?.extras?.keySet()}")

        if (isFacebookInitialized) {
            facebookCallbackManager?.onActivityResult(requestCode, resultCode, data)
        } else {
            Log.e("SocialLogin", "Facebook not initialized when handling result")
        }
    }

    fun signOutGoogle() {
        googleSignInClient.signOut()

    }
        fun signOutFacebook() {
            if (isFacebookInitialized) {
                LoginManager.getInstance().logOut()
            }
        }

        fun isFacebookAvailable(): Boolean {
            return isFacebookInitialized
        }
    }