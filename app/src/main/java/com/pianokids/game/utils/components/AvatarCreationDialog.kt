package com.pianokids.game.utils.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
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

@Composable
fun AvatarCreationDialog(
    onDismiss: () -> Unit,
    onCreateAvatar: (name: String, avatarImageUrl: String?) -> Unit,
    onCreateAvatarWithAI: (name: String, prompt: String, style: String) -> Unit = { _, _, _ -> }
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
                            onCreateWithAI = {
                                currentStep = AvatarCreationStep.AI_PROMPT
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

                    AvatarCreationStep.AI_PROMPT -> {
                        AIAvatarPromptDialog(
                            avatarName = avatarName,
                            onGenerateAvatar = { prompt, style ->
                                onCreateAvatarWithAI(avatarName, prompt, style)
                                onDismiss()
                            },
                            onBack = {
                                currentStep = AvatarCreationStep.AVATAR_CHOICE
                            },
                            onDismiss = onDismiss
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
    AI_PROMPT,
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
    onCreateWithAI: () -> Unit,
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

        // AI Avatar Generation Option
        Button(
            onClick = onCreateWithAI,
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✨",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "Create with AI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667EEA)
                    )
                }
                Text(
                    text = "Describe your dream character (e.g., Naruto, Mickey Mouse)",
                    fontSize = 12.sp,
                    color = Color(0xFF667EEA).copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Ready Player Me Option
        Button(
            onClick = onCreateWithReadyPlayerMe,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎭 Create 3D Avatar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF667EEA)
                )
                Text(
                    text = "Design your own 3D character",
                    fontSize = 12.sp,
                    color = Color(0xFF667EEA).copy(alpha = 0.7f)
                )
            }
        }

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

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ReadyPlayerMeStep(
    onAvatarCreated: (String) -> Unit,
    onBack: () -> Unit,
    onCancel: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    val readyPlayerMeUrl = "https://demo.readyplayer.me/avatar?frameApi"

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Loading avatar creator...",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        if (loadError != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚠️ Error loading avatar creator",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = loadError ?: "",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
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
                        // Force hardware acceleration
                        setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                        
                        // Enable all rendering features
                        isVerticalScrollBarEnabled = true
                        isHorizontalScrollBarEnabled = true
                        
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            
                            // Enable WebGL and 3D rendering
                            javaScriptCanOpenWindowsAutomatically = true
                            mediaPlaybackRequiresUserGesture = false
                            
                            // Cache and loading
                            cacheMode = WebSettings.LOAD_DEFAULT
                            loadsImagesAutomatically = true
                            blockNetworkImage = false
                            blockNetworkLoads = false
                            
                            // Viewport and scaling
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            builtInZoomControls = false
                            displayZoomControls = false
                            
                            // Security
                            allowFileAccess = false
                            allowContentAccess = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            
                            // Performance
                            setRenderPriority(WebSettings.RenderPriority.HIGH)
                            
                            // Enable modern web features
                            setGeolocationEnabled(false)
                            setSupportMultipleWindows(false)
                        }

                        // Enable cookies
                        val cookieManager = CookieManager.getInstance()
                        cookieManager.setAcceptCookie(true)
                        cookieManager.setAcceptThirdPartyCookies(this, true)

                        // Enable debugging
                        WebView.setWebContentsDebuggingEnabled(true)
                        
                        setBackgroundColor(android.graphics.Color.WHITE)

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                Log.d("RPM-WebView", "Page started: $url")
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                Log.d("RPM-WebView", "Page finished: $url")
                                isLoading = false
                                loadError = null
                                
                                // Inject JS after page loads
                                view?.postDelayed({
                                    injectMessageListener(view)
                                }, 500)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    val errorMsg = error?.description?.toString() ?: "Unknown error"
                                    Log.e("RPM-WebView", "Error: $errorMsg")
                                    loadError = errorMsg
                                    isLoading = false
                                }
                            }
                            
                            override fun onReceivedHttpError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                errorResponse: WebResourceResponse?
                            ) {
                                super.onReceivedHttpError(view, request, errorResponse)
                                if (request?.isForMainFrame == true) {
                                    Log.e("RPM-WebView", "HTTP Error: ${errorResponse?.statusCode}")
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                Log.d("RPM-WebView", "Progress: $newProgress%")
                                if (newProgress == 100) {
                                    isLoading = false
                                }
                            }
                            
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                consoleMessage?.let {
                                    Log.d("RPM-Console", "[${it.messageLevel()}] ${it.message()}")
                                }
                                return true
                            }
                        }

                        addJavascriptInterface(
                            ReadyPlayerMeInterface { avatarUrl ->
                                Log.d("RPM-WebView", "🎯 Avatar GLB URL received: $avatarUrl")
                                
                                // Convert GLB URL to PNG thumbnail
                                val pngUrl = convertGlbToPngUrl(avatarUrl)
                                Log.d("RPM-WebView", "🎯 Converted to PNG URL: $pngUrl")
                                
                                Handler(Looper.getMainLooper()).post {
                                    Log.d("RPM-WebView", "🎯 Executing onAvatarCreated on main thread")
                                    onAvatarCreated(pngUrl)
                                }
                            },
                            "AndroidInterface"
                        )

                        Log.d("RPM-WebView", "Loading URL: $readyPlayerMeUrl")
                        loadUrl(readyPlayerMeUrl)
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

private fun injectMessageListener(webView: WebView) {
    val jsCode = """
        (function() {
            if (window.rpmListenerInstalled) {
                console.log('RPM: Listener already installed');
                return;
            }
            window.rpmListenerInstalled = true;
            
            console.log('RPM: Installing message listener...');
            
            // Listen for postMessage events
            window.addEventListener('message', function(event) {
                console.log('RPM: Message received:', JSON.stringify(event.data));
                
                try {
                    var data = event.data;
                    
                    if (typeof data === 'string') {
                        try {
                            data = JSON.parse(data);
                        } catch(e) {
                            return;
                        }
                    }
                    
                    if (data && data.eventName === 'v1.avatar.exported') {
                        var avatarUrl = data.data ? data.data.url : null;
                        console.log('RPM: Avatar exported! URL:', avatarUrl);
                        
                        if (avatarUrl && window.AndroidInterface) {
                            window.AndroidInterface.onAvatarExported(avatarUrl);
                        }
                    }
                } catch(e) {
                    console.error('RPM: Error:', e);
                }
            });
            
            // Also check for the URL input field that appears in the export dialog
            var checkInterval = setInterval(function() {
                var urlInput = document.querySelector('input[type="text"]');
                if (urlInput && urlInput.value && urlInput.value.includes('models.readyplayer.me')) {
                    var avatarUrl = urlInput.value;
                    console.log('RPM: Found avatar URL in input:', avatarUrl);
                    
                    if (window.AndroidInterface) {
                        window.AndroidInterface.onAvatarExported(avatarUrl);
                        clearInterval(checkInterval);
                    }
                }
                
                // Also try to find it in the page text
                var bodyText = document.body.innerText || document.body.textContent;
                var urlMatch = bodyText.match(/https:\/\/models\.readyplayer\.me\/[a-zA-Z0-9]+\.glb/);
                if (urlMatch && urlMatch[0]) {
                    console.log('RPM: Found avatar URL in page:', urlMatch[0]);
                    
                    if (window.AndroidInterface) {
                        window.AndroidInterface.onAvatarExported(urlMatch[0]);
                        clearInterval(checkInterval);
                    }
                }
            }, 1000);
            
            console.log('RPM: Listener installed');
        })();
    """.trimIndent()

    webView.evaluateJavascript(jsCode) { result ->
        Log.d("RPM-WebView", "JS injection result: $result")
    }
}

class ReadyPlayerMeInterface(private val onAvatarExported: (String) -> Unit) {
    @JavascriptInterface
    fun onAvatarExported(avatarUrl: String) {
        Log.d("RPM-Interface", "Avatar URL received: $avatarUrl")
        onAvatarExported.invoke(avatarUrl)
    }
}

private fun convertGlbToPngUrl(glbUrl: String): String {
    // Ready Player Me GLB URL format: https://models.readyplayer.me/{id}.glb
    // PNG render URL format: https://models.readyplayer.me/{id}.png
    
    return if (glbUrl.contains("models.readyplayer.me") && glbUrl.endsWith(".glb")) {
        glbUrl.replace(".glb", ".png")
    } else {
        // If already a PNG or different format, return as is
        glbUrl
    }
}

