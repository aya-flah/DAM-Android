package com.pianokids.game.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val userName = userPrefs.getFullName()
    val userEmail = userPrefs.getEmail() ?: ""
    val userLevel = userPrefs.getLevel()
    val totalStars = userPrefs.getTotalStars()
    val maxStars = 24 // 8 levels × 3 stars

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667EEA),
                        Color(0xFF764BA2),
                        Color(0xFFF093FB)
                    )
                )
            )
    ) {
        // Floating stars animation
        FloatingStarsBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = {
                        SoundManager.playClick()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Avatar with animation
            AnimatedProfileAvatar(userName)

            Spacer(modifier = Modifier.height(24.dp))

            // User name and level
            Text(
                text = userName,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = getLevelTitle(userLevel),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Stats cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsCard(
                    icon = "🏆",
                    title = "Level",
                    value = userLevel.toString(),
                    color = RainbowYellow,
                    modifier = Modifier.weight(1f)
                )

                StatsCard(
                    icon = "⭐",
                    title = "Stars",
                    value = "$totalStars/$maxStars",
                    color = RainbowOrange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Achievements section
            AchievementsSection(totalStars, userLevel)

            Spacer(modifier = Modifier.height(24.dp))

            // User info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📧 Account Info",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = RainbowBlue
                        )
                    )

                    InfoRow(
                        icon = Icons.Default.Person,
                        label = "Name",
                        value = userName
                    )

                    InfoRow(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = userEmail
                    )

                    InfoRow(
                        icon = Icons.Default.Star,
                        label = "Progress",
                        value = "${(totalStars.toFloat() / maxStars * 100).toInt()}%"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout button
            Button(
                onClick = {
                    SoundManager.playClick()
                    showLogoutDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RainbowRed
                ),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Logout",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        // Logout confirmation dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = {
                    Text(text = "👋", fontSize = 64.sp)
                },
                title = {
                    Text(
                        text = "See you soon!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RainbowBlue
                        ),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to logout?",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            userPrefs.logout()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RainbowRed
                        )
                    ) {
                        Text("Yes, Logout", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel", color = RainbowBlue)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(28.dp)
            )
        }
    }
    LaunchedEffect(Unit) { SoundManager.startBackgroundMusic() }

}

@Composable
fun AnimatedProfileAvatar(userName: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(150.dp)
            .scale(scale)
            .rotate(rotation)
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main avatar
        Box(
            modifier = Modifier
                .size(130.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            RainbowPink,
                            RainbowOrange,
                            RainbowYellow
                        )
                    )
                )
                .border(5.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎹",
                fontSize = 64.sp
            )
        }

        // Level badge
        Box(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(RainbowBlue)
                .border(3.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⭐",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun StatsCard(
    icon: String,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = color
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextLight
                )
            )
        }
    }
}

@Composable
fun AchievementsSection(totalStars: Int, userLevel: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🏆 Achievements",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = RainbowViolet
                )
            )

            AchievementItem(
                icon = "🌟",
                title = "First Steps",
                description = "Complete your first level",
                isUnlocked = totalStars >= 1
            )

            AchievementItem(
                icon = "🎵",
                title = "Music Lover",
                description = "Earn 10 stars",
                isUnlocked = totalStars >= 10
            )

            AchievementItem(
                icon = "🎹",
                title = "Piano Master",
                description = "Reach level 5",
                isUnlocked = userLevel >= 5
            )

            AchievementItem(
                icon = "👑",
                title = "Champion",
                description = "Collect all 24 stars",
                isUnlocked = totalStars >= 24
            )
        }
    }
}

@Composable
fun AchievementItem(
    icon: String,
    title: String,
    description: String,
    isUnlocked: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isUnlocked)
                    Color(0xFFFFF9C4)
                else Color(0xFFF5F5F5)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(
                    if (isUnlocked)
                        Color(0xFFFFEB3B)
                    else Color.LightGray
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isUnlocked) icon else "🔒",
                fontSize = 28.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) Color.Black else TextLight
                )
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (isUnlocked) TextLight else Color.Gray
                )
            )
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = RainbowBlue,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextLight
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
fun FloatingStarsBackground() {
    // Animated stars for background
    val infiniteTransition = rememberInfiniteTransition(label = "stars")

    repeat(20) { index ->
        val offsetX by infiniteTransition.animateFloat(
            initialValue = (index * 50f) % 400f,
            targetValue = ((index * 50f) % 400f) + 100f,
            animationSpec = infiniteRepeatable(
                animation = tween((3000 + index * 200), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "starX$index"
        )

        val offsetY by infiniteTransition.animateFloat(
            initialValue = (index * 40f) % 800f,
            targetValue = ((index * 40f) % 800f) + 80f,
            animationSpec = infiniteRepeatable(
                animation = tween((4000 + index * 300), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "starY$index"
        )

        Box(
            modifier = Modifier
                .offset(x = offsetX.dp, y = offsetY.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.3f))
        )
    }
}