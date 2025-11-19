package com.pianokids.game.utils.components

import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
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
    // Use Ready Player Me public endpoint for avatar creation
    val readyPlayerMeUrl = "https://ready.player.me/?frameApi"

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "⚠️ Unable to load avatar creator",
                    color = RainbowRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Please check your internet connection or try again later",
                    color = RainbowRed,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
                Text(
                    text = "Error: $loadError",
                    color = Color(0xFFFF9800),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(4.dp)
                )
            }
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
                        // Enable JavaScript and DOM storage
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true

                        // Additional WebView settings for better compatibility
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        
                        // Enable cookie support for session management
                        android.webkit.CookieManager.getInstance().setAcceptCookie(true)
                        android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        // Cache configuration
                        settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                        settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        
                        // Set proper user agent
                        settings.userAgentString = "Mozilla/5.0 (Android; Mobile; rv:91.0) Gecko/91.0 Firefox/91.0"

                        // Enable debugging for WebView
                        WebView.setWebContentsDebuggingEnabled(true)

                        // Set background to white
                        setBackgroundColor(android.graphics.Color.WHITE)

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                Log.d("WebView", "Page finished loading: $url")
                                isLoading = false
                                loadError = null // Clear any previous errors

                                // Inject message listener after page loads
                                view?.evaluateJavascript("""
                                    (function() {
                                        console.log('Injecting Ready Player Me listener');
                                        window.addEventListener('message', function(event) {
                                            console.log('RPM Message received:', JSON.stringify(event.data));
                                            try {
                                                if (event.data && typeof event.data === 'object') {
                                                    if (event.data.eventName === 'v1.avatar.exported') {
                                                        const avatarUrl = event.data.data.url;
                                                        console.log('Avatar exported URL:', avatarUrl);
                                                        if (avatarUrl && window.AndroidInterface) {
                                                            window.AndroidInterface.onAvatarExported(avatarUrl);
                                                        }
                                                    }
                                                }
                                            } catch(e) {
                                                console.error('Error handling message:', e);
                                            }
                                        });
                                        console.log('Ready Player Me listener installed');
                                    })();
                                """.trimIndent(), null)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                val errorCode = error?.errorCode ?: -1
                                val errorMsg = error?.description?.toString() ?: "Unknown error"
                                val url = request?.url?.toString() ?: "Unknown URL"

                                Log.e("WebView", "Error loading $url: Code=$errorCode, Message=$errorMsg")

                                // Map error codes to human-readable messages
                                val errorDescription = when (errorCode) {
                                    android.webkit.WebViewClient.ERROR_TIMEOUT -> 
                                        "Connection timeout - check internet connection"
                                    android.webkit.WebViewClient.ERROR_HOST_LOOKUP -> 
                                        "Cannot resolve domain - check internet connection"
                                    android.webkit.WebViewClient.ERROR_CONNECT -> 
                                        "Cannot connect to server"
                                    android.webkit.WebViewClient.ERROR_BAD_URL -> 
                                        "Invalid URL"
                                    android.webkit.WebViewClient.ERROR_FILE_NOT_FOUND -> 
                                        "Page not found (404)"
                                    android.webkit.WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME -> 
                                        "Unsupported authentication"
                                    else -> errorMsg
                                }

                                loadError = "[$errorCode] $errorDescription"
                                isLoading = false
                            }

                            override fun onReceivedHttpError(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?,
                                errorResponse: android.webkit.WebResourceResponse?
                            ) {
                                super.onReceivedHttpError(view, request, errorResponse)
                                val statusCode = errorResponse?.statusCode ?: -1
                                val url = request?.url?.toString() ?: "Unknown"
                                Log.e("WebView", "HTTP Error $statusCode: $url")
                                
                                if (statusCode == 404 || statusCode == 500) {
                                    loadError = "Server error (HTTP $statusCode)"
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                consoleMessage?.let {
                                    Log.d("WebView-Console", "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}")
                                }
                                return true
                            }

                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                Log.d("WebView", "Loading progress: $newProgress%")
                                if (newProgress == 100) {
                                    isLoading = false
                                }
                            }
                        }

                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun onAvatarExported(avatarUrl: String) {
                                Log.d("WebView", "Avatar exported via JavaScript Interface: $avatarUrl")
                                onAvatarCreated(avatarUrl)
                            }
                        }, "AndroidInterface")

                        Log.d("WebView", "Loading Ready Player Me URL: $readyPlayerMeUrl")
                        
                        // Load URL with proper headers
                        val headers = HashMap<String, String>()
                        headers["User-Agent"] = "Mozilla/5.0 (Android; Mobile; rv:91.0) Gecko/91.0 Firefox/91.0"
                        loadUrl(readyPlayerMeUrl, headers)
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