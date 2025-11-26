package com.pianokids.game.utils.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.ImageLoader
import com.pianokids.game.data.models.Avatar
import com.pianokids.game.ui.theme.*
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@Composable
fun AvatarImageView(
    avatarUrl: String?,
    size: Int = 80,
    borderColor: Color = Color.Transparent,
    borderWidth: Int = 0,
    modifier: Modifier = Modifier
) {
    // Debug logging
    LaunchedEffect(avatarUrl) {
        android.util.Log.d("AvatarImageView", "🖼️ Loading avatar image: $avatarUrl")
    }
    
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .then(
                if (borderWidth > 0) {
                    Modifier.border(borderWidth.dp, borderColor, CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl != null && avatarUrl.isNotEmpty()) {
            val context = LocalContext.current
            
            // Custom image loader with longer timeout for AI images
            val imageLoader = remember {
                ImageLoader.Builder(context)
                    .okHttpClient {
                        OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(120, TimeUnit.SECONDS) // 2 minutes for AI images
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build()
                    }
                    .build()
            }
            
            val imageRequest = remember(avatarUrl) {
                ImageRequest.Builder(context)
                    .data(avatarUrl)
                    .crossfade(true)
                    .build()
            }
            
            AsyncImage(
                model = imageRequest,
                imageLoader = imageLoader,
                contentDescription = "Avatar",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                onSuccess = { 
                    android.util.Log.d("AvatarImageView", "✅ Image loaded successfully: $avatarUrl")
                },
                onError = { error ->
                    android.util.Log.e("AvatarImageView", "❌ Failed to load image: $avatarUrl", error.result.throwable)
                }
            )
        } else {
            android.util.Log.w("AvatarImageView", "⚠️ No avatar URL provided, showing fallback")
            // Fallback when no avatar URL
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(RainbowPink, RainbowOrange)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    fontSize = (size / 2).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun AvatarsSection(
    avatars: List<Avatar>,
    activeAvatar: Avatar?,
    isLoading: Boolean,
    onCreateAvatar: () -> Unit,
    onSelectAvatar: (Avatar) -> Unit,
    modifier: Modifier = Modifier,
    onDeleteAvatar: ((Avatar) -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Avatars",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Button(
                onClick = onCreateAvatar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Create",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (avatars.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        Color.White.copy(alpha = 0.15f),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "👤",
                        fontSize = 48.sp
                    )
                    Text(
                        text = "No avatars yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Create your first avatar to get started!",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(avatars) { avatar ->
                    AvatarCard(
                        avatar = avatar,
                        isActive = activeAvatar?.id == avatar.id,
                        onClick = { onSelectAvatar(avatar) },
                        onDelete = onDeleteAvatar
                    )
                }
            }
        }
    }
}

@Composable
fun AvatarCard(
    avatar: Avatar,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: ((Avatar) -> Unit)? = null
) {
    Box(modifier = modifier.width(120.dp)) {
        Column(
            modifier = Modifier
                .width(120.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .background(
                    if (isActive) {
                        Brush.verticalGradient(
                            colors = listOf(
                                RainbowBlue.copy(alpha = 0.3f),
                                RainbowIndigo.copy(alpha = 0.3f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.White.copy(alpha = 0.05f)
                            )
                        )
                    }
                )
                .then(
                    if (isActive) {
                        Modifier.border(3.dp, RainbowBlue, RoundedCornerShape(16.dp))
                    } else {
                        Modifier
                    }
                )
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Avatar Image
            AvatarImageView(
                avatarUrl = avatar.avatarImageUrl,
                size = 80,
                borderColor = if (isActive) RainbowBlue else Color.Transparent,
                borderWidth = if (isActive) 2 else 0
            )

            // Avatar Name
            Text(
                text = avatar.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Status Badge
            if (isActive) {
                Text(
                    text = "Active",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = RainbowBlue,
                    modifier = Modifier
                        .background(
                            RainbowBlue.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            } else {
                Text(
                    text = "Level ${avatar.level}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // Delete Button (Top-Right Corner)
        if (onDelete != null) {
            IconButton(
                onClick = { onDelete(avatar) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .background(Color(0xFFFF6B6B), CircleShape)
                    .shadow(4.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AvatarDetailDialog(
    avatar: Avatar?,
    onDismiss: () -> Unit
) {
    if (avatar == null) return
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF667EEA).copy(alpha = 0.95f),
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = avatar.name,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Avatar Image
                AvatarImageView(
                    avatarUrl = avatar.avatarImageUrl,
                    size = 200,
                    borderColor = RainbowBlue,
                    borderWidth = 6
                )
                
                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatColumn(
                        icon = "⭐",
                        label = "Level ${avatar.level}",
                        value = ""
                    )
                    
                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp),
                        color = Color.White.copy(alpha = 0.3f)
                    )
                    
                    StatColumn(
                        icon = "📊",
                        label = "${avatar.experience} XP",
                        value = ""
                    )
                    
                    Divider(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp),
                        color = Color.White.copy(alpha = 0.3f)
                    )
                    
                    StatColumn(
                        icon = "⚡",
                        label = "${avatar.energy}%",
                        value = ""
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Close", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun StatColumn(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
