package com.pianokids.game.utils.components

import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.UserPreferences
import android.util.Log

@Composable
fun AvatarCreationDialog(
    onDismiss: () -> Unit,
    onCreateAvatar: (name: String, avatarImageUrl: String?) -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    
    var avatarName by remember { mutableStateOf("") }
    var showNameError by remember { mutableStateOf(false) }
    var currentStep by remember { mutableStateOf(AvatarCreationStep.NAME) }
    var capturedAvatarUrl by remember { mutableStateOf<String?>(null) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF667EEA),
                                Color(0xFF764BA2)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                when (currentStep) {
                    AvatarCreationStep.NAME -> {
                        NameInputStep(
                            avatarName = avatarName,
                            showError = showNameError,
                            onNameChange = { 
                                avatarName = it
                                showNameError = false
                            },
                            onNext = {
                                if (avatarName.isBlank()) {
                                    showNameError = true
                                } else {
                                    currentStep = AvatarCreationStep.AVATAR_CHOICE
                                }
                            },
                            onCancel = onDismiss
                        )
                    }
                    
                    AvatarCreationStep.AVATAR_CHOICE -> {
                        AvatarChoiceStep(
                            avatarName = avatarName,
                            onCreateWithReadyPlayerMe = {
                                currentStep = AvatarCreationStep.READY_PLAYER_ME
                            },
                            onCreateWithoutAvatar = {
                                onCreateAvatar(avatarName, null)
                                onDismiss()
                            },
                            onBack = {
                                currentStep = AvatarCreationStep.NAME
                            }
                        )
                    }
                    
                    AvatarCreationStep.READY_PLAYER_ME -> {
                        ReadyPlayerMeStep(
                            onAvatarCreated = { avatarUrl ->
                                capturedAvatarUrl = avatarUrl
                                userPrefs.saveAvatarThumbnail(avatarUrl)
                                onCreateAvatar(avatarName, avatarUrl)
                                onDismiss()
                            },
                            onBack = {
                                currentStep = AvatarCreationStep.AVATAR_CHOICE
                            },
                            onCancel = onDismiss
                        )
                    }
                    
                    AvatarCreationStep.WAITING_FOR_AVATAR -> {
                        // Not used anymore
                    }
                }
            }
        }
    }
}

enum class AvatarCreationStep {
    NAME,
    AVATAR_CHOICE,
    READY_PLAYER_ME,
    WAITING_FOR_AVATAR
}

@Composable
private fun NameInputStep(
    avatarName: String,
    showError: Boolean,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Create Your Avatar",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Choose a name for your avatar",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        OutlinedTextField(
            value = avatarName,
            onValueChange = onNameChange,
            label = { Text("Avatar Name", color = Color.White.copy(alpha = 0.7f)) },
            singleLine = true,
            isError = showError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                errorBorderColor = RainbowRed,
                cursorColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        if (showError) {
            Text(
                text = "Please enter a name",
                color = RainbowRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF667EEA)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AvatarChoiceStep(
    avatarName: String,
    onCreateWithReadyPlayerMe: () -> Unit,
    onCreateWithoutAvatar: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Avatar for \"$avatarName\"",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Choose how to create your avatar",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        // Ready Player Me Button
        Button(
            onClick = onCreateWithReadyPlayerMe,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎭 Create with Ready Player Me",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667EEA)
                )
                Text(
                    text = "Create a 3D avatar in-app",
                    fontSize = 12.sp,
                    color = Color(0xFF667EEA).copy(alpha = 0.7f)
                )
            }
        }
        
        // Skip Button
        OutlinedButton(
            onClick = onCreateWithoutAvatar,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Skip for now",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // Back Button
        TextButton(onClick = onBack) {
            Text(
                text = "← Back",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ReadyPlayerMeStep(
    onAvatarCreated: (String) -> Unit,
    onBack: () -> Unit,
    onCancel: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    
    // Ready Player Me subdomain URL with app ID
    val appId = "6918ea1dce04903d215e004c"
    val readyPlayerMeUrl = "https://pianokids-vo6xpt.readyplayer.me/avatar?frameApi&userId=$appId"
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Create Your Avatar",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        if (loadError != null) {
            Text(
                text = "Error loading avatar creator: $loadError",
                color = RainbowRed,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White, RoundedCornerShape(16.dp))
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                Log.d("WebView", "Page finished loading: $url")
                                isLoading = false
                            }
                            
                            override fun onReceivedError(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                val errorMsg = "WebView Error: ${error?.description}"
                                Log.e("WebView", errorMsg)
                                loadError = errorMsg
                                isLoading = false
                            }
                        }
                        
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onAvatarExported(avatarUrl: String) {
                                Log.d("WebView", "Avatar exported: $avatarUrl")
                                onAvatarCreated(avatarUrl)
                            }
                        }, "AndroidInterface")
                        
                        Log.d("WebView", "Loading URL: $readyPlayerMeUrl")
                        loadUrl(readyPlayerMeUrl)
                        
                        // Listen for messages from Ready Player Me
                        evaluateJavascript("""
                            (function() {
                                console.log('Ready Player Me WebView initialized');
                                window.addEventListener('message', function(event) {
                                    console.log('Message received:', event.data);
                                    if (event.data && event.data.eventName === 'v1.avatar.exported') {
                                        const avatarUrl = event.data.data.url;
                                        console.log('Avatar URL:', avatarUrl);
                                        AndroidInterface.onAvatarExported(avatarUrl);
                                    }
                                });
                            })();
                        """.trimIndent(), null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("← Back", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
