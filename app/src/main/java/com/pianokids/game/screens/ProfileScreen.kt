// ProfileScreen.kt
package com.pianokids.game.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.pianokids.game.data.repository.AuthRepository
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.SocialLoginManager
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences
import com.pianokids.game.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val authRepository = remember { AuthRepository(context) }
    val socialLoginManager = remember { SocialLoginManager(context) }

    var showLogoutDialog by remember { mutableStateOf(false) }


    val authViewModel: AuthViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val user = userPrefs.getUser()

    val userName = when {
        user != null -> user.name
        isLoggedIn -> userPrefs.getFullName()
        else -> "Guest Player"
    }

    val userEmail = when {
        user != null -> user.email
        isLoggedIn -> userPrefs.getEmail() ?: ""
        else -> "No email (Guest mode)"
    }

    val userPhotoUrl = user?.photoUrl
    val userProvider = user?.provider

    val userLevel = when {
        user != null -> user.level
        isLoggedIn -> userPrefs.getLevel()
        else -> 1
    }

    val totalStars = when {
        user != null -> user.score / 100
        isLoggedIn -> userPrefs.getTotalStars()
        else -> 0
    }

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
        FloatingStarsBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back button
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
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

            Spacer(Modifier.height(16.dp))

            // Avatar
            AnimatedProfileAvatar(
                userName = userName,
                userPhotoUrl = userPhotoUrl,
                userProvider = userProvider
            )

            Spacer(Modifier.height(24.dp))

            // Name + Provider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                if (userProvider != null) {
                    ProviderBadge(provider = userProvider)
                }
            }

            Text(
                text = getLevelTitle(userLevel),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                ),
                textAlign = TextAlign.Center
            )

            if (!isLoggedIn) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "Guest Mode - Progress not saved",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsCard(
                    icon = "Trophy",
                    title = "Level",
                    value = userLevel.toString(),
                    color = RainbowYellow,
                    modifier = Modifier.weight(1f)
                )

                StatsCard(
                    icon = "Star",
                    title = "Stars",
                    value = "$totalStars/$maxStars",
                    color = RainbowOrange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Achievements
            AchievementsSection(totalStars = totalStars, userLevel = userLevel)

            Spacer(Modifier.height(24.dp))

            // Account Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Account Info",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = RainbowBlue
                        )
                    )

                    InfoRow(icon = Icons.Default.Person, label = "Name", value = userName)
                    InfoRow(icon = Icons.Default.Email, label = "Email", value = userEmail)
                    if (userProvider != null) {
                        InfoRow(icon = Icons.Default.Lock, label = "Login Method", value = userProvider.capitalize())
                    }
                    InfoRow(
                        icon = Icons.Default.Star,
                        label = "Progress",
                        value = "${(totalStars.toFloat() / maxStars * 100).toInt()}%"
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Logout Button
            if (isLoggedIn) {
                Button(
                    onClick = {
                        SoundManager.playClick()
                        showLogoutDialog = true
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RainbowRed),
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout", modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Logout", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(48.dp))
        }

        // Logout Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                icon = { Text("Bye", fontSize = 64.sp) },
                title = {
                    Text(
                        "See you soon!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = RainbowBlue
                        ),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        "Are you sure you want to logout?",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            authRepository.logout()
                            socialLoginManager.signOutGoogle()
                            socialLoginManager.signOutFacebook()
                            authViewModel.onLogout()
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(RainbowRed)
                    ) {
                        Text("Yes, Logout", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MISSING COMPOSABLES (ADD THESE IN THE SAME FILE)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FloatingStarsBackground() {
    val infiniteTransition = rememberInfiniteTransition()
    val star1 by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(3000), RepeatMode.Reverse))
    val star2 by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(4000), RepeatMode.Reverse))

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(Color.White.copy(alpha = star1), 8f, center = Offset(size.width * 0.2f, size.height * 0.1f))
        drawCircle(Color.White.copy(alpha = star2), 6f, center = Offset(size.width * 0.8f, size.height * 0.15f))
    }
}

@Composable
fun AnimatedProfileAvatar(userName: String, userPhotoUrl: String?, userProvider: String?) {
    val scale by rememberInfiniteTransition().animateFloat(
        1f, 1.05f, infiniteRepeatable(tween(2000), RepeatMode.Reverse)
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(RainbowPink, RainbowOrange)))
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        if (userPhotoUrl != null) {
            AsyncImage(
                model = userPhotoUrl,
                contentDescription = "Profile",
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = userName.take(2).uppercase(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ProviderBadge(provider: String) {
    Surface(
        shape = CircleShape,
        color = when (provider.lowercase()) {
            "google" -> Color(0xFF4285F4)
            "facebook" -> Color(0xFF1877F2)
            else -> Color.Gray
        }
    ) {
        Text(
            text = provider.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp),
            fontSize = 14.sp
        )
    }
}

@Composable
fun StatsCard(icon: String, title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium.copy(color = TextLight))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

@Composable
fun AchievementsSection(totalStars: Int, userLevel: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Achievements", style = MaterialTheme.typography.titleLarge.copy(color = Color.White))
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) {
                val earned = it < (totalStars / 8)
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = if (earned) RainbowYellow else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = RainbowBlue, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = TextLight))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        }
    }
}

fun getLevelTitle(level: Int): String = when (level) {
    1 -> "Beginner"
    in 2..3 -> "Learner"
    in 4..5 -> "Player"
    in 6..7 -> "Skilled"
    in 8..10 -> "Expert"
    else -> "Master"
}