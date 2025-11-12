package com.pianokids.game.view.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.pianokids.game.R
import com.pianokids.game.data.repository.AuthRepository
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.SocialLoginManager
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences
import com.pianokids.game.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import kotlin.math.sin

data class GameLevel(
    val number: Int,
    val title: String,
    val world: String,
    val isUnlocked: Boolean,
    val stars: Int,
    val emoji: String,
    val color: Color,
    val position: IslandPosition,
    val islandImageRes: Int? = null
)

data class IslandPosition(
    val xOffset: Float,
    val yOffset: Float
)

data class DecorativeIsland(
    val imageRes: Int,
    val position: IslandPosition,
    val size: Float = 1f,
    val offsetY: Float = 0f
)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val userPrefs = remember { UserPreferences(context) }
    val authRepository = remember { AuthRepository(context) }
    val socialLoginManager = remember { SocialLoginManager(context) }
    val scope = rememberCoroutineScope()

    var showComingSoonDialog by remember { mutableStateOf(false) }
    var showGuestLimitDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val authViewModel: AuthViewModel = viewModel()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    // ✅ UTILISE LE STATEFLOW POUR LE NOM
    val userName by authViewModel.userName.collectAsState()

    val user = userPrefs.getUser()

    // ✅ SUPPRIME LA LOGIQUE LOCALE DU NOM (maintenant géré par ViewModel)
    // val userName = when { ... }

    val userPhotoUrl = user?.photoUrl
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

    val levels = remember(isLoggedIn) {
        listOf(
            GameLevel(
                number = 1,
                title = "Electric Meadow",
                world = "Pokémon Plains",
                isUnlocked = true,
                stars = 0,
                emoji = "⚡",
                color = RainbowYellow,
                position = IslandPosition(0.12f, 0.1f),
                islandImageRes = R.drawable.island1
            ),
            GameLevel(
                number = 2,
                title = "Springfield Suburbs",
                world = "Donut Paradise",
                isUnlocked = isLoggedIn,
                stars = 0,
                emoji = "🍩",
                color = RainbowOrange,
                position = IslandPosition(0.26f, -0.15f),
                islandImageRes = R.drawable.island4
            ),
            GameLevel(
                number = 3,
                title = "Power Training",
                world = "Saiyan Island",
                isUnlocked = isLoggedIn,
                stars = 0,
                emoji = "🥋",
                color = RainbowRed,
                position = IslandPosition(0.40f, 0.08f),
                islandImageRes = R.drawable.island3
            ),
            GameLevel(
                number = 4,
                title = "Moonlight Magic",
                world = "Crystal Kingdom",
                isUnlocked = isLoggedIn,
                stars = 0,
                emoji = "🌙",
                color = RainbowPink,
                position = IslandPosition(0.53f, -0.12f),
                islandImageRes = R.drawable.island5
            ),
            GameLevel(
                number = 5,
                title = "Shield of Justice",
                world = "Hero's Landing",
                isUnlocked = isLoggedIn,
                stars = 0,
                emoji = "🛡️",
                color = RainbowBlue,
                position = IslandPosition(0.66f, 0.15f),
                islandImageRes = R.drawable.island8
            ),
            GameLevel(
                number = 6,
                title = "Hidden Village",
                world = "Ninja's Path",
                isUnlocked = isLoggedIn,
                stars = 0,
                emoji = "🥷",
                color = RainbowIndigo,
                position = IslandPosition(0.78f, -0.08f),
                islandImageRes = R.drawable.island7
            ),
            GameLevel(
                number = 7,
                title = "Mischief Manor",
                world = "Cat & Mouse Chase",
                isUnlocked = isLoggedIn,
                stars = 0,
                emoji = "🐭",
                color = RainbowViolet,
                position = IslandPosition(0.89f, 0.10f),
                islandImageRes = R.drawable.island6
            ),
            GameLevel(
                number = 8,
                title = "Tech Tower",
                world = "Stark Industries",
                isUnlocked = isLoggedIn,
                stars = 0,
                emoji = "🤖",
                color = RainbowGreen,
                position = IslandPosition(1.00f, 0f),
                islandImageRes = R.drawable.island2
            )
        )
    }


    val decorativeIslands = remember {
        listOf(
            DecorativeIsland(
                imageRes = R.drawable.i_3, // Réutilise tes images existantes
                position = IslandPosition(0.20f, -0.35f),
                size = 0.9f,
                offsetY = -200f
            ),
            DecorativeIsland(
                imageRes = R.drawable.i_2,
                position = IslandPosition(0.48f, 0.30f),
                size = 0.8f,
                offsetY = -150f
            ),
            DecorativeIsland(
                imageRes = R.drawable.i_4,
                position = IslandPosition(0.72f, -0.25f),
                size = 0.6f,
                offsetY = -180f
            ),
            DecorativeIsland(
                imageRes = R.drawable.i_1,
                position = IslandPosition(0.95f, 0.25f),
                size = 0.8f,
                offsetY = -170f
            )

        )
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            socialLoginManager.handleGoogleSignInResult(task)
        }
    }

    LaunchedEffect(Unit) {
        SoundManager.startBackgroundMusic()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedOceanBackground()

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = RainbowYellow)
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            CompactGameHeader(
                userName = userName,
                userPhotoUrl = userPhotoUrl,
                totalStars = totalStars,
                maxStars = levels.size * 3,
                isLoggedIn = isLoggedIn,
                onBackClick = onNavigateBack,
                onProfileClick = onNavigateToProfile
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState)
            ) {
                Box(
                    modifier = Modifier
                        .width(4200.dp) // Augmenté pour plus d'espace
                        .fillMaxHeight()
                ) {
                    // Animated clouds in background
                    AnimatedClouds()

                    // Flying birds
                    AnimatedBirds()


                    decorativeIslands.forEach { decorativeIsland ->
                        FloatingDecorativeIsland(
                            decorativeIsland = decorativeIsland,
                            mapWidth = 3800.dp
                        )
                    }

                    // Wooden bridges between islands
                    // Remplace la section Canvas des ponts par ce code avec des chaînes

                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val mapWidth = size.width
                        val centerY = size.height / 2

                        for (i in 0 until levels.size - 1) {
                            val current = levels[i]
                            val next = levels[i + 1]

                            val startX = current.position.xOffset * mapWidth
                            val startY = centerY + (current.position.yOffset * size.height * 0.4f)
                            val endX = next.position.xOffset * mapWidth
                            val endY = centerY + (next.position.yOffset * size.height * 0.4f)

                            // ✅ CHAÎNE SUSPENDUE
                            val chainPath = Path().apply {
                                moveTo(startX, startY)
                                // Courbe de la chaîne qui pend
                                val controlY = ((startY + endY) / 2) + 120f // Plus de pendaison
                                quadraticBezierTo(
                                    (startX + endX) / 2, controlY,
                                    endX, endY
                                )
                            }

                            // Ombre de la chaîne
                            drawPath(
                                path = Path().apply {
                                    moveTo(startX + 3f, startY + 3f)
                                    val controlY = ((startY + endY) / 2) + 123f
                                    quadraticBezierTo(
                                        (startX + endX) / 2 + 3f, controlY,
                                        endX + 3f, endY + 3f
                                    )
                                },
                                color = Color.Black.copy(alpha = 0.3f),
                                style = Stroke(width = 12f)
                            )

                            // Chaîne principale (métal gris foncé)
                            drawPath(
                                path = chainPath,
                                color = if (next.isUnlocked)
                                    Color(0xFF4A5568) // Gris métal foncé
                                else
                                    Color(0xFF718096).copy(alpha = 0.5f), // Gris clair pour verrouillé
                                style = Stroke(
                                    width = 10f,
                                    cap = StrokeCap.Round
                                )
                            )

                            // Reflet métallique sur la chaîne
                            drawPath(
                                path = Path().apply {
                                    moveTo(startX - 1f, startY - 1f)
                                    val controlY = ((startY + endY) / 2) + 119f
                                    quadraticBezierTo(
                                        (startX + endX) / 2 - 1f, controlY,
                                        endX - 1f, endY - 1f
                                    )
                                },
                                color = Color.White.copy(alpha = 0.3f),
                                style = Stroke(width = 3f)
                            )

                            // ✅ MAILLONS DE LA CHAÎNE (effet réaliste)
                            val distance = kotlin.math.sqrt(
                                ((endX - startX) * (endX - startX) + (endY - startY) * (endY - startY)).toDouble()
                            ).toFloat()
                            val numLinks = (distance / 30f).toInt() // Un maillon tous les 30 pixels

                            for (j in 0..numLinks) {
                                val t = j.toFloat() / numLinks

                                // Position sur la courbe
                                val bezierX = (1 - t) * (1 - t) * startX +
                                        2 * (1 - t) * t * ((startX + endX) / 2) +
                                        t * t * endX
                                val bezierY = (1 - t) * (1 - t) * startY +
                                        2 * (1 - t) * t * (((startY + endY) / 2) + 120f) +
                                        t * t * endY

                                // Dessiner un maillon (petit ovale)
                                val linkWidth = 8f
                                val linkHeight = 14f

                                // Ombre du maillon
                                drawOval(
                                    color = Color.Black.copy(alpha = 0.2f),
                                    topLeft = Offset(bezierX - linkWidth / 2 + 1f, bezierY - linkHeight / 2 + 1f),
                                    size = Size(linkWidth, linkHeight)
                                )

                                // Maillon métallique
                                drawOval(
                                    color = if (next.isUnlocked)
                                        Color(0xFF4A5568)
                                    else
                                        Color(0xFF718096).copy(alpha = 0.5f),
                                    topLeft = Offset(bezierX - linkWidth / 2, bezierY - linkHeight / 2),
                                    size = Size(linkWidth, linkHeight)
                                )

                                // Reflet sur le maillon
                                drawOval(
                                    color = Color.White.copy(alpha = 0.4f),
                                    topLeft = Offset(bezierX - linkWidth / 2 + 1f, bezierY - linkHeight / 2 + 1f),
                                    size = Size(linkWidth / 2, linkHeight / 2)
                                )
                            }

                            // ✅ CROCHETS AUX EXTRÉMITÉS (ancrage aux îles)
                            fun drawHook(x: Float, y: Float) {
                                // Base du crochet (cercle)
                                drawCircle(
                                    color = Color(0xFF2D3748),
                                    radius = 12f,
                                    center = Offset(x, y)
                                )
                                drawCircle(
                                    color = Color(0xFF4A5568),
                                    radius = 10f,
                                    center = Offset(x, y)
                                )

                                // Reflet
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.5f),
                                    radius = 4f,
                                    center = Offset(x - 2f, y - 2f)
                                )

                                // Boulons du crochet (détails)
                                drawCircle(
                                    color = Color(0xFF2D3748),
                                    radius = 2f,
                                    center = Offset(x - 4f, y)
                                )
                                drawCircle(
                                    color = Color(0xFF2D3748),
                                    radius = 2f,
                                    center = Offset(x + 4f, y)
                                )
                            }

                            // Dessiner les crochets aux deux extrémités
                            drawHook(startX, startY)
                            drawHook(endX, endY)
                        }
                    }




                    // ✅ ÎLES PRINCIPALES PLUS GRANDES
                    levels.forEach { level ->
                        FloatingIslandHorizontal(
                            level = level,
                            mapWidth = 3800.dp,
                            onClick = {
                                if (level.isUnlocked) {
                                    showComingSoonDialog = true
                                } else {
                                    showGuestLimitDialog = true
                                }
                                SoundManager.playClick()
                            }
                        )
                    }
                }
            }
        }

        if (showComingSoonDialog) {
            ComingSoonDialog { showComingSoonDialog = false }
        }
        if (showGuestLimitDialog) {
            GuestLimitDialog(
                onDismiss = { showGuestLimitDialog = false },
                onLoginClick = {
                    showGuestLimitDialog = false
                    showLoginDialog = true
                }
            )
        }
        if (showLoginDialog) {
            LoginChooserDialog(
                onDismiss = { showLoginDialog = false },
                onGoogleClick = {
                    isLoading = true
                    socialLoginManager.signInWithGoogle(
                        launcher = googleSignInLauncher,
                        onSuccess = { idToken ->
                            scope.launch {
                                val result = authRepository.loginWithSocial(
                                    token = idToken,
                                    provider = "google"
                                )
                                isLoading = false
                                result.onSuccess {
                                    showLoginDialog = false
                                }
                                result.onFailure { }
                            }
                        },
                        onFailure = {
                            isLoading = false
                        }
                    )
                },
                onFacebookClick = {
                    activity?.let { act ->
                        isLoading = true
                        socialLoginManager.loginWithFacebook(
                            activity = act,
                            onSuccess = { accessToken ->
                                scope.launch {
                                    val result = authRepository.loginWithSocial(
                                        token = accessToken,
                                        provider = "facebook"
                                    )
                                    isLoading = false
                                    result.onSuccess {
                                        showLoginDialog = false
                                    }
                                    result.onFailure { }
                                }
                            },
                            onFailure = {
                                isLoading = false
                            }
                        )
                    }
                }
            )
        }
    }
}

// ✅ COMPOSABLE POUR LES ÎLES DÉCORATIVES
@Composable
fun FloatingDecorativeIsland(
    decorativeIsland: DecorativeIsland,
    mapWidth: androidx.compose.ui.unit.Dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "decorative_island")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .offset(
                x = (decorativeIsland.position.xOffset * mapWidth.value).dp,
                y = 0.dp
            )
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset(
                    y = (decorativeIsland.position.yOffset * 200 +
                            floatOffset + decorativeIsland.offsetY).dp
                )
                .scale(scale)
                .alpha(0.7f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = decorativeIsland.imageRes),
                contentDescription = "Decorative Island",
                modifier = Modifier.size((180 * decorativeIsland.size).dp),
                contentScale = ContentScale.Fit,
                alpha = 0.8f
            )
        }
    }
}

// ✅ ÎLES PRINCIPALES ENCORE PLUS GRANDES
@Composable
fun FloatingIslandHorizontal(
    level: GameLevel,
    mapWidth: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "island_${level.number}")

    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000 + level.number * 200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .offset(
                x = (level.position.xOffset * mapWidth.value).dp,
                y = 0.dp
            )
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset(y = (level.position.yOffset * 200 + floatOffset).dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ✅ ÎLES ENCORE PLUS GRANDES (380dp au lieu de 300dp)
                Box(
                    modifier = Modifier.size(380.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (level.islandImageRes != null) {
                        Image(
                            painter = painterResource(id = level.islandImageRes),
                            contentDescription = "Island ${level.number}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ✅ CARTE DE NIVEAU PLUS GRANDE (180dp)
                Card(
                    modifier = Modifier
                        .size(180.dp)
                        .offset(y = (-160).dp)
                        .clickable(enabled = true, onClick = onClick),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (level.isUnlocked)
                            Color.White
                        else Color.White.copy(alpha = 0.7f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (level.isUnlocked) 14.dp else 5.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(
                                    if (level.isUnlocked) level.color.copy(alpha = 0.2f)
                                    else Color.LightGray.copy(alpha = 0.3f)
                                )
                                .border(4.dp, level.color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (level.isUnlocked) {
                                Text(text = level.emoji, fontSize = 38.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${level.number}",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp,
                                color = level.color
                            )
                        )

                        Text(
                            text = level.title,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )

                        if (level.isUnlocked) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(3) { index ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star",
                                        tint = if (index < level.stars)
                                            RainbowYellow
                                        else Color.LightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}




@Composable
fun CompactGameHeader(
    userName: String,
    userPhotoUrl: String?,
    totalStars: Int,
    maxStars: Int,
    isLoggedIn: Boolean,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    SoundManager.playClick()
                    onBackClick()
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RainbowBlue.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = RainbowBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(RainbowOrange, RainbowPink)
                            )
                        )
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (userPhotoUrl != null) {
                        AsyncImage(
                            model = userPhotoUrl,
                            contentDescription = "Profile Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = if (isLoggedIn) "🎹" else "👤",
                            fontSize = 24.sp
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        maxLines = 1
                    )
                    if (!isLoggedIn) {
                        Text(
                            text = "Guest Mode",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = RainbowOrange
                            )
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(RainbowYellow.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Stars",
                    tint = RainbowYellow,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "$totalStars/$maxStars",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = RainbowOrange
                    )
                )
            }
        }
    }
}


@Composable
fun AnimatedOceanBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "ocean")

    val wavePhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )

    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF87CEEB),
                    Color(0xFF6DB4E0),
                    Color(0xFF4A9FD8),
                    Color(0xFF3A8FCC)
                )
            )
        )

        for (layer in 0 until 5) {
            val wavePath = Path()
            val amplitude = 30f + layer * 15f
            val frequency = 0.01f - layer * 0.001f
            val yBase = height * 0.5f + layer * 60f
            val phase = if (layer % 2 == 0) wavePhase1 else wavePhase2

            wavePath.moveTo(0f, yBase)

            for (x in 0 until (width * 2).toInt() step 5) {
                val y = yBase + amplitude * sin((x * frequency + phase * 0.02f).toDouble()).toFloat()
                wavePath.lineTo(x.toFloat(), y)
            }

            wavePath.lineTo(width * 2, height)
            wavePath.lineTo(0f, height)
            wavePath.close()

            drawPath(
                path = wavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4FC3F7).copy(alpha = 0.3f - layer * 0.05f),
                        Color(0xFF29B6F6).copy(alpha = 0.2f - layer * 0.04f),
                        Color(0xFF03A9F4).copy(alpha = 0.1f - layer * 0.03f)
                    ),
                    startY = yBase,
                    endY = height
                )
            )

            val highlightPath = Path()
            highlightPath.moveTo(0f, yBase)
            for (x in 0 until (width * 2).toInt() step 5) {
                val y = yBase + amplitude * sin((x * frequency + phase * 0.02f).toDouble()).toFloat()
                highlightPath.lineTo(x.toFloat(), y)
            }

            drawPath(
                path = highlightPath,
                color = Color.White.copy(alpha = 0.15f - layer * 0.02f),
                style = Stroke(width = 2f)
            )
        }
    }
}

@Composable
fun AnimatedClouds() {
    val infiniteTransition = rememberInfiniteTransition(label = "clouds")

    val cloud1X by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 3800f,
        animationSpec = infiniteRepeatable(
            animation = tween(45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud1"
    )

    val cloud2X by infiniteTransition.animateFloat(
        initialValue = 500f,
        targetValue = 4300f,
        animationSpec = infiniteRepeatable(
            animation = tween(55000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud2"
    )

    val cloud3X by infiniteTransition.animateFloat(
        initialValue = 1200f,
        targetValue = 5000f,
        animationSpec = infiniteRepeatable(
            animation = tween(50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud3"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val height = size.height

        fun drawCloud(x: Float, y: Float, scale: Float) {
            translate(x, y) {
                // Main cloud circles
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 40f * scale,
                    center = Offset(0f, 0f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 50f * scale,
                    center = Offset(45f * scale, -10f * scale)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 45f * scale,
                    center = Offset(90f * scale, 0f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 35f * scale,
                    center = Offset(45f * scale, 15f * scale)
                )
            }
        }

        // Draw multiple clouds at different positions
        drawCloud(cloud1X, height * 0.12f, 1.2f)
        drawCloud(cloud2X, height * 0.20f, 0.9f)
        drawCloud(cloud3X, height * 0.15f, 1.1f)
        drawCloud(cloud1X + 600f, height * 0.25f, 0.8f)
        drawCloud(cloud2X - 400f, height * 0.10f, 1.0f)
        drawCloud(cloud3X + 300f, height * 0.22f, 1.3f)
    }
}

@Composable
fun AnimatedBirds() {
    val infiniteTransition = rememberInfiniteTransition(label = "birds")

    // More birds with different positions
    val bird1X by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 3700f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bird1X"
    )

    val bird1Y by infiniteTransition.animateFloat(
        initialValue = 80f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bird1Y"
    )

    val bird2X by infiniteTransition.animateFloat(
        initialValue = 400f,
        targetValue = 4100f,
        animationSpec = infiniteRepeatable(
            animation = tween(35000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bird2X"
    )

    val bird2Y by infiniteTransition.animateFloat(
        initialValue = 150f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bird2Y"
    )

    val bird3X by infiniteTransition.animateFloat(
        initialValue = 800f,
        targetValue = 4500f,
        animationSpec = infiniteRepeatable(
            animation = tween(32000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bird3X"
    )

    val bird3Y by infiniteTransition.animateFloat(
        initialValue = 100f,
        targetValue = 140f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bird3Y"
    )

    val wingFlap by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wingFlap"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        fun drawBird(x: Float, y: Float, scale: Float = 1f) {
            val wingAngle = wingFlap * 35f

            translate(x - 15f * scale, y) {
                rotate(-wingAngle, pivot = Offset(0f, 0f)) {
                    drawPath(
                        path = Path().apply {
                            moveTo(0f, 0f)
                            quadraticBezierTo(-22f * scale, -17f * scale, -33f * scale, -11f * scale)
                            quadraticBezierTo(-22f * scale, -6f * scale, 0f, 0f)
                        },
                        color = Color(0xFF2C3E50),
                        style = Stroke(width = 2.5f * scale)
                    )
                }
            }

            drawCircle(
                color = Color(0xFF2C3E50),
                radius = 9f * scale,
                center = Offset(x, y)
            )

            translate(x + 15f * scale, y) {
                rotate(wingAngle, pivot = Offset(0f, 0f)) {
                    drawPath(
                        path = Path().apply {
                            moveTo(0f, 0f)
                            quadraticBezierTo(22f * scale, -17f * scale, 33f * scale, -11f * scale)
                            quadraticBezierTo(22f * scale, -6f * scale, 0f, 0f)
                        },
                        color = Color(0xFF2C3E50),
                        style = Stroke(width = 2.5f * scale)
                    )
                }
            }
        }

        // Draw multiple birds
        drawBird(bird1X, bird1Y, 1.3f)
        drawBird(bird2X, bird2Y, 1.1f)
        drawBird(bird3X, bird3Y, 1.0f)
        drawBird(bird1X + 250f, bird1Y + 40f, 0.9f)
        drawBird(bird2X - 200f, bird2Y - 30f, 1.2f)
        drawBird(bird3X + 150f, bird3Y + 20f, 0.8f)
    }
}

@Composable
fun ComingSoonDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text(text = "🎵", fontSize = 64.sp) },
        title = {
            Text(
                text = "Coming Soon!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = RainbowBlue
                ),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "This level is under construction!",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "We're working hard to bring you an amazing piano learning experience! 🎹",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        color = TextLight
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    SoundManager.playClick()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RainbowBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Got it!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun GuestLimitDialog(
    onDismiss: () -> Unit,
    onLoginClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text(text = "🔒", fontSize = 64.sp) },
        title = {
            Text(
                text = "Login Required",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = RainbowOrange
                ),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "This level is locked for guests!",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Create an account or login to unlock all levels and save your progress! 🎹✨",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        color = TextLight
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    SoundManager.playClick()
                    onLoginClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RainbowBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Login Now",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                SoundManager.playClick()
                onDismiss()
            }) {
                Text("Maybe Later", color = TextLight)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}
