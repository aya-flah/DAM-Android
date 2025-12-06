package com.pianokids.game.view.screens.levels

import android.view.animation.OvershootInterpolator
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pianokids.game.R
import com.pianokids.game.utils.AudioPreviewPlayer
import com.pianokids.game.utils.PianoSoundManager
import com.pianokids.game.utils.PitchDetector
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences
import com.pianokids.game.utils.components.*
import com.pianokids.game.view.components.AudioPreviewScreen
import com.pianokids.game.view.components.FallingNotesView
import com.pianokids.game.view.components.PianoKeyboard
import com.pianokids.game.viewmodel.AvatarViewModel
import com.pianokids.game.viewmodel.LevelViewModel
import com.pianokids.game.viewmodel.PianoViewModel
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pianokids.game.utils.ImageMapper
import kotlinx.coroutines.delay

@Composable
fun LevelScreen(
    userId: String,
    levelId: String,
    viewModel: LevelViewModel = viewModel(),
    avatarViewModel: AvatarViewModel = viewModel(),
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val state = viewModel.uiState.collectAsState().value

    val activeAvatar by avatarViewModel.activeAvatar.collectAsState()

    var showIntro by remember { mutableStateOf(true) }
    var showPreview by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf<PianoMode?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showSublevelDialog by remember { mutableStateOf(false) }
    var showFailDialog by remember { mutableStateOf(false) }

    // Delay mode selection
    var pendingMode by remember { mutableStateOf<PianoMode?>(null) }

    // LOAD EVERYTHING
    LaunchedEffect(userId, levelId) {
        avatarViewModel.loadActiveAvatar()
        viewModel.loadLevel(levelId)
        viewModel.loadSublevels(userId, levelId)
    }

    // REAL PIANO MODE LISTENING
    LaunchedEffect(selectedMode, showIntro, showPreview) {
        if (selectedMode == PianoMode.REAL_PIANO && !showIntro && !showPreview) {
            PitchDetector.startListening(context) { detected ->
                val normalized = detected.lowercase().replace("é", "e").trim()
                viewModel.onNotePlayed(normalized)
            }
        }
    }

    // FAIL DIALOG TRIGGER
    LaunchedEffect(state.isFailed) {
        if (state.isFailed) showFailDialog = true
    }

    DisposableEffect(Unit) {
        onDispose {
            AudioPreviewPlayer.release()
            if (PitchDetector.isActive()) PitchDetector.stopListening()
        }
    }

    // LOADING SCREEN
    if (state.isLoading || state.currentLevel == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF00D9FF))
        }
        return
    }

    val level = state.currentLevel

    // PREVIEW AUDIO STATE
    val audioPreviewUrl = level.previewAudioUrl
    val isAudioPlaying by AudioPreviewPlayer.isPlaying.collectAsState()
    val isAudioLoading by AudioPreviewPlayer.isLoading.collectAsState()
    val audioPos by AudioPreviewPlayer.currentPosition.collectAsState()
    val audioDuration by AudioPreviewPlayer.duration.collectAsState()
    val audioProgress = if (audioDuration > 0) audioPos.toFloat() / audioDuration else 0f

    // AUTO-START PREVIEW AFTER INTRO
    LaunchedEffect(showIntro, audioPreviewUrl) {
        if (!showIntro && audioPreviewUrl != null && !showPreview) {
            showPreview = true
            AudioPreviewPlayer.prepare(
                context = context,
                audioUrl = audioPreviewUrl,
                onReady = { AudioPreviewPlayer.play() },
                onComplete = {
                    showPreview = false
                    selectedMode = pendingMode
                },
                onError = {
                    Log.e("LevelScreen", "Preview error: $it")
                    showPreview = false
                }
            )
        }
    }

    // ANIMATIONS
    val shakeOffset by animateFloatAsState(
        targetValue = if (state.showWrongAnimation) 15f else 0f,
        animationSpec = tween(300, easing = LinearEasing),
        finishedListener = { viewModel.clearWrongAnimation() }
    )

    val avatarOffsetX by animateDpAsState(
        targetValue = if (!showIntro) 0.dp else (-300).dp,
        animationSpec = tween(700, easing = { OvershootInterpolator(1.2f).getInterpolation(it) })
    )

    val bossOffsetX by animateDpAsState(
        targetValue = if (!showIntro) 0.dp else (300).dp,
        animationSpec = tween(700, 150, easing = { OvershootInterpolator(1.2f).getInterpolation(it) })
    )
    // SHOW AUDIO PREVIEW (or fallback)
    if (!showIntro && showPreview) {

        if (audioPreviewUrl != null) {
            AudioPreviewScreen(
                levelTitle = level.title,
                theme = level.theme,
                isPlaying = isAudioPlaying,
                isLoading = isAudioLoading,
                progress = audioProgress,
                onPlayPause = {
                    if (isAudioPlaying) AudioPreviewPlayer.pause()
                    else AudioPreviewPlayer.play()
                },
                onSkip = {
                    AudioPreviewPlayer.stop()
                    showPreview = false
                    selectedMode = pendingMode
                }
            )
        } else {
            // Fallback if no preview audio exists
            AudioPreviewScreen(
                levelTitle = level.title,
                theme = level.theme,
                isPlaying = false,
                isLoading = false,
                progress = 0f,
                onPlayPause = {},
                onSkip = {
                    showPreview = false
                    selectedMode = pendingMode
                }
            )
        }
        return
    }

    // MAIN GAME UI — when intro finished, preview finished, mode selected, and sublevel selected
    if (!showIntro && !showPreview && selectedMode != null && state.selectedSublevel != null) {

        Box(modifier = Modifier.fillMaxSize()) {

            val fallingAreaTopPadding = 96.dp
            val fallingAreaBottomPadding =
                if (selectedMode == PianoMode.APP_PIANO) 260.dp else 190.dp

            // --------------------------------------------------
            // BACKGROUND GIF + DARK OVERLAY
            // --------------------------------------------------
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.ocean)
                    .decoderFactory(
                        if (Build.VERSION.SDK_INT >= 28) ImageDecoderDecoder.Factory()
                        else GifDecoder.Factory()
                    )
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.6f),
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // --------------------------------------------------
            // FALLING NOTES VISUALS
            // --------------------------------------------------
            val sublevelNotes = state.selectedSublevel?.notes ?: level.expectedNotes
            val noteDurations =
                state.noteDurations.ifEmpty { List(sublevelNotes.size) { 1f } }

            FallingNotesView(
                expectedNotes = sublevelNotes,
                currentNoteIndex = state.currentNoteIndex,
                noteDurations = noteDurations,
                onNoteHit = { },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 28.dp,
                        end = 28.dp,
                        top = fallingAreaTopPadding,
                        bottom = fallingAreaBottomPadding
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                // ============================================================
                //                        TOP BAR
                // ============================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    // EXIT BUTTON
                    IconButton(
                        onClick = { onExit() },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(50))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_exit),
                            contentDescription = "Exit Level",
                            tint = Color.Red,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // SCORE BADGE
                    Box(
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Progress: ${(state.progressPercentage * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // ============================================================
                    // GAME PROGRESS BAR (UPDATED STAR LOGIC)
                    // ============================================================
                    Column(
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val totalNotes = state.selectedSublevel?.notes?.size ?: level.expectedNotes.size
                        val correctNotes = state.currentNoteIndex
                        val accuracy = if (totalNotes > 0) correctNotes.toFloat() / totalNotes else 0f

                        GameProgressBar(
                            progress = state.progressPercentage,
                            stars = when {
                                accuracy >= 0.90f -> 3
                                accuracy >= 0.70f -> 2
                                accuracy >= 0.40f -> 1
                                else -> 0
                            }
                        )
                    }

                    // ============================================================
                    // HEARTS (LIVES)
                    // ============================================================
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        repeat(3) { index ->
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint =
                                    if (index < (3 - state.wrongNoteCount)) Color(0xFFFF6B9D)
                                    else Color.Gray.copy(alpha = 0.3f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ============================================================
                // HERO (AVATAR) + BOSS SECTION
                // ============================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {

                    val bossFloatX = floatingOffset(range = 6)
                    val heroFloatX = floatingOffset(range = 6, duration = 2600)

                    // LEFT SIDE — BOSS + WRONG/CORRECT BUBBLES
                    Row(
                        modifier = Modifier.offset(x = bossOffsetX + bossFloatX),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Image(
                            painter = painterResource(id = ImageMapper.bossImage(level.theme)),
                            contentDescription = "Boss",
                            modifier = Modifier
                                .size(170.dp, 240.dp)
                                .clip(RoundedCornerShape(32.dp)),
                            contentScale = ContentScale.Crop
                        )

                        // WRONG BUBBLE
                        AnimatedVisibility(
                            visible = state.wrongMessage != null,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .padding(bottom = 8.dp, start = 10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .shadow(6.dp, RoundedCornerShape(16.dp))
                                        .background(Color(0xFFFF5E62), RoundedCornerShape(16.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = state.wrongMessage ?: "",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        // FEEDBACK BUBBLE
                        AnimatedVisibility(
                            visible = state.feedbackMessage != null,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            HeroSpeechBubble(
                                message = state.feedbackMessage.orEmpty(),
                                isPositive = state.isFeedbackPositive,
                                modifier = Modifier.padding(bottom = 8.dp, start = 12.dp)
                            )
                        }
                    }

                    // RIGHT SIDE — AVATAR
                    Row(
                        modifier = Modifier
                            .offset(x = avatarOffsetX + heroFloatX)
                            .shadow(12.dp, RoundedCornerShape(32.dp))
                            .padding(start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {

                        val avatarUrl = activeAvatar?.avatarImageUrl
                            ?: userPrefs.getAvatarThumbnail()

                        Box(
                            modifier = Modifier
                                .size(width = 170.dp, height = 240.dp)
                                .clip(RoundedCornerShape(32.dp))
                        ) {
                            if (!avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Hero Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFF667EEA).copy(alpha = 0.3f),
                                                    Color(0xFF00D9FF).copy(alpha = 0.3f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🦸", fontSize = 80.sp)
                                }
                            }
                        }
                    }
                }
                // ============================================================
                // INSTRUMENT SELECTOR (APP_PIANO MODE)
                // ============================================================
                if (selectedMode == PianoMode.APP_PIANO) {

                    var selectedInstrument by remember { mutableStateOf("PIANO") }
                    val instruments = listOf("PIANO", "GUITAR", "VIOLIN")
                    val instrumentIcons = mapOf(
                        "PIANO" to "🎹",
                        "GUITAR" to "🎸",
                        "VIOLIN" to "🎻"
                    )

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        items(instruments) { instrument ->
                            val isSelected = instrument == selectedInstrument

                            Button(
                                onClick = {
                                    selectedInstrument = instrument
                                    PianoSoundManager.setInstrument(instrument)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor =
                                        if (isSelected) Color(0xFF667EEA)
                                        else Color(0xFF2C2C2C),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .height(56.dp)
                                    .shadow(
                                        if (isSelected) 8.dp else 2.dp,
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = instrumentIcons[instrument] ?: "🎵",
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        text = instrument,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // ============================================================
                // PIANO KEYBOARD (APP MODE)
                // ============================================================
                if (selectedMode == PianoMode.APP_PIANO) {

                    val pianoViewModel: PianoViewModel = viewModel()
                    var pressedKeys by remember { mutableStateOf(setOf<String>()) }
                    var lastPlayedNote by remember { mutableStateOf("") }

                    PianoKeyboard(
                        config = pianoViewModel.pianoState.collectAsState().value.config,
                        pressedKeys = pressedKeys,
                        onKeyPressed = { key ->
                            pressedKeys = pressedKeys + key.note
                            pianoViewModel.onKeyPressed(key)

                            if (state.currentNoteIndex < (state.selectedSublevel?.notes?.size ?: 0)) {
                                if (key.solfege != lastPlayedNote) {
                                    lastPlayedNote = key.solfege
                                    viewModel.onNotePlayed(key.solfege)
                                }
                            }
                        },
                        onKeyReleased = { key ->
                            pressedKeys = pressedKeys - key.note
                            pianoViewModel.onKeyReleased(key)
                            lastPlayedNote = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(24.dp))
                            .border(
                                2.dp,
                                Color(0xFF00D9FF).copy(alpha = 0.3f),
                                RoundedCornerShape(24.dp)
                            )
                    )
                }

                // ============================================================
                // REAL PIANO MODE (MIC LISTENING)
                // ============================================================
                else if (selectedMode == PianoMode.REAL_PIANO) {

                    val detectedNote by PitchDetector.detectedNote.collectAsState()
                    val detectedFrequency by PitchDetector.detectedFrequency.collectAsState()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .shadow(12.dp, RoundedCornerShape(24.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF1A1A2E).copy(alpha = 0.9f),
                                        Color(0xFF16213E).copy(alpha = 0.9f)
                                    )
                                ),
                                RoundedCornerShape(24.dp)
                            )
                            .border(
                                2.dp,
                                Color(0xFF00D9FF).copy(alpha = 0.3f),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            val pulseScale by rememberInfiniteTransition().animateFloat(
                                initialValue = 1f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    tween(1000, easing = FastOutSlowInEasing),
                                    RepeatMode.Reverse
                                )
                            )

                            Text("🎤", fontSize = 64.sp, modifier = Modifier.scale(pulseScale))

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Play on your piano!",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (detectedNote != null) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF00D9FF).copy(alpha = 0.2f)
                                    )
                                ) {
                                    Text(
                                        text = "Detected: $detectedNote (${detectedFrequency.toInt()} Hz)",
                                        color = Color(0xFF00D9FF),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            } else {
                                Text(
                                    "I'm listening... 🎵",
                                    color = Color(0xFF00D9FF).copy(alpha = 0.7f),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }

            // ============================================================
            // FAIL DIALOG (GLASSY)
            // ============================================================
            if (showFailDialog) {

                val outerShape = RoundedCornerShape(28.dp)
                val innerShape = RoundedCornerShape(24.dp)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .wrapContentHeight()
                            .padding(vertical = 16.dp),
                        shape = outerShape,
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color(0xFF667EEA),
                                    Color(0xFF00D9FF),
                                    Color(0xFFFFC857)
                                )
                            )
                        )
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(innerShape)
                        ) {

                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.22f),
                                                Color.White.copy(alpha = 0.10f)
                                            )
                                        )
                                    )
                                    .blur(22.dp)
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = 0.30f),
                                        innerShape
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 28.dp, vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {

                                Text("💔", fontSize = 72.sp)

                                Text(
                                    "Out of Lives!",
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )

                                Text(
                                    "Choose what to do next:",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )

                                Spacer(Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        showFailDialog = false
                                        viewModel.loadLevel(levelId)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFFC857)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        "Restart Level",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        showFailDialog = false
                                        onExit()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        "Exit Level",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }
    }
    // ============================================================
    // LEVEL COMPLETED → SAVE PROGRESS + SHOW SUCCESS DIALOG
    // ============================================================
    LaunchedEffect(state.isLevelCompleted) {
        if (state.isLevelCompleted) {
            viewModel.saveProgress(userId)
            showSuccessDialog = true
        }
    }

    val totalNotes = level.expectedNotes.size
    val correctNotes = state.currentNoteIndex
    val accuracy = if (totalNotes > 0) correctNotes.toFloat() / totalNotes else 0f

    val stars = when {
        accuracy >= 0.90f -> 3
        accuracy >= 0.70f -> 2
        accuracy >= 0.40f -> 1
        else -> 0
    }

    if (showSuccessDialog) {
        LevelCompletedDialog(
            stars = stars,
            theme = level?.theme ?: "Default",
            onDismiss = {
                showSuccessDialog = false
                onExit()
            },
            onNextLevel = {
                showSuccessDialog = false

                val current = state.selectedSublevel
                val all = state.sublevels

                if (current != null) {
                    val currentIndex = all.indexOfFirst { it._id == current._id }

                    if (currentIndex != -1 && currentIndex + 1 < all.size) {
                        val next = all[currentIndex + 1]

                        if (next.unlocked) {
                            // 👉 Move to next sublevel inside same level
                            viewModel.selectSublevel(next)

                            // Show preview only before final sublevel
                            val isBeforeLastSublevel = next.index == all.size
                            showPreview = isBeforeLastSublevel
                            return@LevelCompletedDialog
                        }
                    }
                }

                // 👉 No next sublevel → return to map
                onExit()
            }
        )
    }

    // ============================================================
    // INTRO DIALOG (first launch)
    // ============================================================
    if (showIntro) {
        LevelIntroDialog(
            heroImageRes = when (level.theme) {
                "Batman" -> R.drawable.hero_batman
                "Spider-Man" -> R.drawable.hero_spiderman
                "Detective Conan" -> R.drawable.hero_conan
                "Black Panther" -> R.drawable.hero_bpanther
                "Avengers Mix" -> R.drawable.hero_batman
                "Hunter x Hunter" -> R.drawable.hero_hxh
                else -> R.drawable.hero_batman
            },
            storyText = level.story,
            onFinished = {
                showIntro = false
                showSublevelDialog = true
                SoundManager.stopBackgroundMusic()
            }
        )
    }

    // ============================================================
    // SUBLEVEL SELECTION DIALOG
    // ============================================================
    if (showSublevelDialog) {
        SublevelSelectionDialog(
            sublevels = state.sublevels,
            onDismiss = {
                showSublevelDialog = false
                onExit()
            },
            onPlay = { sublevel, mode ->
                pendingMode = mode
                viewModel.selectSublevel(sublevel)
                showSublevelDialog = false

                // Only show preview before the last sublevel
                val isBeforeLastSublevel = sublevel.index == state.sublevels.size
                showPreview = isBeforeLastSublevel

                if (!isBeforeLastSublevel) {
                    selectedMode = mode
                }
            }
        )
    }
}


@Composable
private fun HeroSpeechBubble(
    message: String,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (isPositive) Color(0xFFFFF8E1) else Color(0xFFFFEBEE)
    val borderColor = if (isPositive) Color(0xFFFFC107) else Color(0xFFFF6B6B)
    val textColor = if (isPositive) Color(0xFF4E342E) else Color(0xFFB71C1C)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(
            modifier = Modifier
                .size(width = 20.dp, height = 32.dp)
        ) {
            val tipPath = Path().apply {
                moveTo(size.width, size.height / 2f)
                lineTo(0f, 0f)
                lineTo(0f, size.height)
                close()
            }
            drawPath(tipPath, color = borderColor)

            val innerPath = Path().apply {
                moveTo(size.width - 3f, size.height / 2f)
                lineTo(3f, 3f)
                lineTo(3f, size.height - 3f)
                close()
            }
            drawPath(innerPath, color = bubbleColor)
        }

        Box(
            modifier = Modifier
                .widthIn(min = 180.dp, max = 240.dp)
                .shadow(10.dp, RoundedCornerShape(22.dp))
                .background(bubbleColor, RoundedCornerShape(22.dp))
                .border(2.dp, borderColor, RoundedCornerShape(22.dp))
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Text(
                text = message,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun GameProgressBar(
    progress: Float,
    stars: Int
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(700, easing = FastOutSlowInEasing)
    )

    // Glow animation on the progress fill
    val glowAlpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        )
    )

    // Star unlock bounce
    val bounceScale = remember(stars) { Animatable(1f) }
    LaunchedEffect(stars) {
        bounceScale.animateTo(1.3f, tween(200))
        bounceScale.animateTo(1f, tween(200))
    }

    // ----------------------------
    // CONTAINER (centered)
    // ----------------------------
    Box(
        modifier = Modifier
            .width(300.dp)   // ⬅️ Small and clean like your layout
            .height(38.dp),  // ⬅️ Taller so stars fit inside
        contentAlignment = Alignment.Center
    ) {

        // --------------------------------------
        // TRANSPARENT BACKGROUND + BORDER
        // --------------------------------------
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
                .border(3.dp, Color(0xFFB67D00), RoundedCornerShape(20.dp))
                .background(Color.Transparent)
        )

        // --------------------------------------
        // FILL BAR (behind stars)
        // --------------------------------------
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(4.dp)
                .clip(RoundedCornerShape(16.dp))
                .fillMaxWidth(animatedProgress)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF7BE495),  // soft green
                            Color(0xFF56C596)   // rich green
                        )
                    )
                )
                .drawBehind {
                    drawRoundRect(
                        color = Color.White,
                        alpha = glowAlpha,
                        cornerRadius = CornerRadius(16f, 16f)
                    )
                }
        )

        // --------------------------------------
        // STARS OVERLAY (INSIDE the bar)
        // --------------------------------------
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..3).forEach { index ->
                val unlocked = stars >= index
                val starScale = if (unlocked) bounceScale.value else 1f

                Icon(
                    painter = painterResource(
                        id = if (unlocked) R.drawable.star_filled else R.drawable.star_empty
                    ),
                    tint = Color.Unspecified,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .scale(starScale)
                )
            }
        }
    }
}

@Composable
fun floatingOffset(range: Int = 10, duration: Int = 2000): Dp {
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = -range.toFloat(),
        targetValue = range.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    return offset.dp
}
