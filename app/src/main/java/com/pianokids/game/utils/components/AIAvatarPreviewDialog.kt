package com.pianokids.game.utils.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.pianokids.game.data.models.AvatarGenerationResponse
import com.pianokids.game.ui.theme.*
import okhttp3.OkHttpClient
import coil.ImageLoader
import java.util.concurrent.TimeUnit

@Composable
fun AIAvatarPreviewDialog(
    avatarName: String,
    generationResponse: AvatarGenerationResponse,
    onSave: () -> Unit,
    onRegenerate: () -> Unit,
    onDismiss: () -> Unit,
    isSaving: Boolean = false
) {
    android.util.Log.d("AIAvatarPreviewDialog", "🎨 Dialog is rendering!")
    android.util.Log.d("AIAvatarPreviewDialog", "Avatar Name: $avatarName")
    android.util.Log.d("AIAvatarPreviewDialog", "Image URL: ${generationResponse.avatarImageUrl}")
    
    var showPulse by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(32.dp),
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
                        RoundedCornerShape(32.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Success Header
                    Text(
                        text = "🎨 Your AI Avatar",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    // Avatar Image - BIG RECTANGLE
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .shadow(16.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Show loading while image loads
                            var imageLoaded by remember { mutableStateOf(false) }
                            
                            if (!imageLoaded) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(56.dp),
                                        color = Color(0xFF667EEA),
                                        strokeWidth = 5.dp
                                    )
                                    Text(
                                        text = "Loading your avatar...",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF667EEA),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Please wait a few seconds",
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            
                            // REAL AI generated avatar image - FULL SIZE!
                            val context = LocalContext.current
                            
                            // Custom image loader with longer timeout for AI images
                            val imageLoader = remember {
                                ImageLoader.Builder(context)
                                    .okHttpClient {
                                        OkHttpClient.Builder()
                                            .connectTimeout(30, TimeUnit.SECONDS)
                                            .readTimeout(120, TimeUnit.SECONDS) // 2 minutes for AI image generation
                                            .writeTimeout(30, TimeUnit.SECONDS)
                                            .build()
                                    }
                                    .build()
                            }
                            
                            val imageRequest = remember(generationResponse.avatarImageUrl) {
                                ImageRequest.Builder(context)
                                    .data(generationResponse.avatarImageUrl)
                                    .crossfade(true)
                                    .build()
                            }
                            
                            AsyncImage(
                                model = imageRequest,
                                imageLoader = imageLoader,
                                contentDescription = "AI Generated Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp)),
                                contentScale = ContentScale.Fit,
                                onSuccess = { 
                                    imageLoaded = true
                                    android.util.Log.d("AIAvatarPreviewDialog", "✅ Image loaded!")
                                },
                                onError = { error ->
                                    android.util.Log.e("AIAvatarPreviewDialog", "❌ Image load error", error.result.throwable)
                                }
                            )
                        }
                    }

                    // Avatar Name & Description
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = avatarName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        if (generationResponse.aiGeneratedDescription != null) {
                            Text(
                                text = generationResponse.aiGeneratedDescription,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    // Action Buttons - Cancel, Regenerate, Save
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button
                        OutlinedButton(
                            onClick = onDismiss,
                            enabled = !isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = BorderStroke(2.dp, Color.White)
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Regenerate Button
                        Button(
                            onClick = onRegenerate,
                            enabled = !isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFA726),
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Text(
                                text = "Regenerate",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Save Button
                        Button(
                            onClick = onSave,
                            enabled = !isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text(
                                    text = "Save",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }


                }
            }
        }
    }
}

@Composable
private fun AttributeChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF667EEA).copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF667EEA),
            fontWeight = FontWeight.Medium
        )
    }
}
