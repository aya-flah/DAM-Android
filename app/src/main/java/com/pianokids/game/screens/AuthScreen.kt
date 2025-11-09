package com.pianokids.game.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianokids.game.R
import com.pianokids.game.ui.theme.*
import com.pianokids.game.api.AuthApiService
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showEmailLogin by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authApi = remember { AuthApiService() }
    val userPrefs = remember { UserPreferences(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SkyBlue,
                        OceanLight,
                        SeaFoam
                    )
                )
            )
    ) {
        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.cat_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (showEmailLogin && isRegisterMode) "Join the Fun!" else "Welcome Back!",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = if (showEmailLogin)
                    (if (isRegisterMode) "Create your account to start" else "Sign in to continue")
                else "Choose how you want to continue",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color.White.copy(alpha = 0.9f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (!showEmailLogin) {
                // Social login buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SocialLoginButton(
                        text = "Continue with Email",
                        icon = "📧",
                        backgroundColor = RainbowRed,
                        onClick = { showEmailLogin = true }
                    )

                    SocialLoginButton(
                        text = "Continue with Google",
                        icon = "🔍",
                        backgroundColor = RainbowBlue,
                        onClick = {
                            // TODO: Implement Google login
                        }
                    )

                    SocialLoginButton(
                        text = "Continue with Facebook",
                        icon = "👍",
                        backgroundColor = Color(0xFF1877F2),
                        onClick = {
                            // TODO: Implement Facebook login
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                }
            } else {
                // Email login/register form
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isRegisterMode) "🎉 Register" else "📧 Login",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = RainbowBlue
                            )
                        )

                        // Username field (for registration)
                        if (isRegisterMode) {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Name") },
                                placeholder = { Text("Enter your name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RainbowBlue,
                                    focusedLabelColor = RainbowBlue
                                ),
                                enabled = !isLoading
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = null
                            },
                            label = { Text("Email") },
                            placeholder = { Text("your@email.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RainbowBlue,
                                focusedLabelColor = RainbowBlue
                            ),
                            isError = errorMessage != null,
                            enabled = !isLoading
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            label = { Text("Password") },
                            placeholder = { Text("Min 6 characters") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible)
                                            Icons.Default.Visibility
                                        else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible)
                                            "Hide password"
                                        else "Show password"
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RainbowBlue,
                                focusedLabelColor = RainbowBlue
                            ),
                            isError = errorMessage != null,
                            enabled = !isLoading
                        )

                        // Error message
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Login/Register button
                        Button(
                            onClick = {
                                scope.launch {
                                    if (isRegisterMode) {
                                        // Validation
                                        if (fullName.isBlank()) {
                                            errorMessage = "Please enter your name"
                                            return@launch
                                        }
                                        if (email.isBlank() || !email.contains("@")) {
                                            errorMessage = "Please enter a valid email"
                                            return@launch
                                        }
                                        if (password.length < 6) {
                                            errorMessage = "Password must be at least 6 characters"
                                            return@launch
                                        }

                                        isLoading = true
                                        errorMessage = null

                                        val result = authApi.register(fullName, email, password)
                                        isLoading = false

                                        if (result.isSuccess) {
                                            val response = result.getOrNull()
                                            response?.let {
                                                // Save user data
                                                userPrefs.saveUserData(
                                                    token = it.access_token,
                                                    userId = it.user._id,
                                                    fullName = it.user.fullName,
                                                    email = it.user.email,
                                                    level = it.user.level,
                                                    totalStars = it.user.totalStars
                                                )
                                                onNavigateToHome()
                                            }
                                        } else {
                                            errorMessage = result.exceptionOrNull()?.message
                                                ?: "Registration failed"
                                        }
                                    } else {
                                        // Login
                                        if (email.isBlank() || !email.contains("@")) {
                                            errorMessage = "Please enter a valid email"
                                            return@launch
                                        }
                                        if (password.isBlank()) {
                                            errorMessage = "Please enter your password"
                                            return@launch
                                        }

                                        isLoading = true
                                        errorMessage = null

                                        val result = authApi.login(email, password)
                                        isLoading = false

                                        if (result.isSuccess) {
                                            val response = result.getOrNull()
                                            response?.let {
                                                // Save user data
                                                userPrefs.saveUserData(
                                                    token = it.access_token,
                                                    userId = it.user._id,
                                                    fullName = it.user.fullName,
                                                    email = it.user.email,
                                                    level = it.user.level,
                                                    totalStars = it.user.totalStars
                                                )
                                                onNavigateToHome()
                                            }
                                        } else {
                                            errorMessage = result.exceptionOrNull()?.message
                                                ?: "Login failed. Check your credentials."
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RainbowBlue
                            ),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = if (isRegisterMode) "Create Account" else "Login",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Toggle between login and register
                        TextButton(
                            onClick = {
                                isRegisterMode = !isRegisterMode
                                errorMessage = null
                            }
                        ) {
                            Text(
                                text = if (isRegisterMode)
                                    "Already have an account? Login"
                                else "Don't have an account? Register",
                                fontSize = 16.sp,
                                color = RainbowBlue
                            )
                        }

                        TextButton(
                            onClick = { showEmailLogin = false }
                        ) {
                            Text(
                                text = "← Back to options",
                                fontSize = 16.sp,
                                color = TextLight
                            )
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) { SoundManager.startBackgroundMusic() }

}

@Composable
fun SocialLoginButton(
    text: String,
    icon: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(380.dp)
            .height(70.dp),
        shape = RoundedCornerShape(35.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        elevation = ButtonDefaults.buttonElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 32.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}