package com.pianokids.game.utils.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianokids.game.data.models.Sublevel

@Composable
fun SublevelSelectionDialog(
    sublevels: List<Sublevel>,
    onDismiss: () -> Unit,
    onPlay: (Sublevel, PianoMode) -> Unit
) {
    var selectedMode by remember { mutableStateOf<PianoMode?>(null) }
    var selectedSublevel by remember { mutableStateOf<Sublevel?>(null) }
    var isVisible by remember { mutableStateOf(false) }

    // Entrance animation trigger
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // BACKDROP WITH GRADIENT
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3C3A5A),   // deep lavender
                            Color(0xFF2C2A48),   // darker purple
                            Color(0xFF1E1C33)    // almost-night violet
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {

            // Animated background particles
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF667EEA).copy(alpha = glowAlpha * 0.15f),
                                Color.Transparent,
                                Color(0xFF00D9FF).copy(alpha = glowAlpha * 0.15f)
                            )
                        )
                    )
            )

            // MAIN CARD
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn()
            ) {

                val outerShape = RoundedCornerShape(32.dp)
                val innerShape = RoundedCornerShape(26.dp)

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .wrapContentHeight()
                        .padding(vertical = 16.dp),
                    shape = outerShape,
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(
                        width = 3.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF667EEA),  // purple-blue
                                Color(0xFF00D9FF),  // aqua
                                Color(0xFFFFC857)   // gold
                            )
                        )
                    )
                ) {

                    // Outer clipping
                    Box(modifier = Modifier.clip(innerShape)) {

                        // BLURRED FROSTED BACKGROUND
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
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.30f),
                                    shape = innerShape
                                )
                        )

                        Column(
                            modifier = Modifier
                                .padding(28.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {

                            // HEADER
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Text(
                                        text = "🎵",
                                        fontSize = 28.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "CHOOSE YOUR ADVENTURE",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF2C3E50),
                                        letterSpacing = 2.sp
                                    )
                                }
                            }

                            Divider(
                                color = Color(0xFF667EEA).copy(alpha = 0.2f),
                                thickness = 2.dp
                            )

                            //---------------------------------------------------------
                            // TWO-COLUMN LAYOUT
                            //---------------------------------------------------------
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {

                                // LEFT COLUMN - MODE SELECTOR
                                Column(
                                    modifier = Modifier
                                        .weight(0.42f)
                                        .height(300.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "🎹",
                                            fontSize = 20.sp
                                        )
                                        Text(
                                            text = "Choose Instrument",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF2C3E50)
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        ModeButton(
                                            label = "App Piano",
                                            emoji = "🎹",
                                            isSelected = selectedMode == PianoMode.APP_PIANO,
                                            selectedColor = Color(0xFF667EEA),
                                            onClick = { selectedMode = PianoMode.APP_PIANO },
                                            modifier = Modifier.fillMaxWidth(),
                                            glowAlpha = glowAlpha
                                        )

                                        ModeButton(
                                            label = "My Piano",
                                            emoji = "🎼",
                                            isSelected = selectedMode == PianoMode.REAL_PIANO,
                                            selectedColor = Color(0xFF00C9A7),
                                            onClick = { selectedMode = PianoMode.REAL_PIANO },
                                            modifier = Modifier.fillMaxWidth(),
                                            glowAlpha = glowAlpha
                                        )
                                    }
                                }

                                // RIGHT COLUMN - SUBLEVEL SELECTOR
                                Column(
                                    modifier = Modifier
                                        .weight(0.58f)
                                        .height(300.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "⭐",
                                            fontSize = 20.sp
                                        )
                                        Text(
                                            text = "Select Your Level",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF2C3E50)
                                        )
                                    }

                                    // Sublevel Grid
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(0xFFF7F2FF),  // pastel lilac
                                                        Color(0xFFEFF6FF)   // pastel blue-white
                                                    )
                                                ),
                                                RoundedCornerShape(20.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = Color(0xFF667EEA).copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(20.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            val chunkedSublevels = sublevels.chunked(3)

                                            chunkedSublevels.forEach { rowSublevels ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                ) {
                                                    rowSublevels.forEach { sublevel ->
                                                        val index = sublevels.indexOf(sublevel)
                                                        val unlocked = sublevel.unlocked
                                                        val isSelected =
                                                            selectedSublevel?._id == sublevel._id

                                                        Box(
                                                            modifier = Modifier.weight(1f),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            SublevelButton(
                                                                index = index,
                                                                sublevel = sublevel,
                                                                isSelected = isSelected,
                                                                unlocked = unlocked,
                                                                onClick = {
                                                                    selectedSublevel = sublevel
                                                                }
                                                            )
                                                        }
                                                    }

                                                    repeat(3 - rowSublevels.size) {
                                                        Spacer(Modifier.weight(1f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            //---------------------------------------------------------
                            // SELECTED SUBLEVEL INFO
                            //---------------------------------------------------------
                            selectedSublevel?.let { sublevel ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    elevation = CardDefaults.cardElevation(0.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color(0xFFE8F5FF),
                                                        Color(0xFFF0E8FF)
                                                    )
                                                ),
                                                RoundedCornerShape(20.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = Color(0xFF667EEA).copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(16.dp)
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "🎯",
                                                    fontSize = 24.sp
                                                )
                                                Text(
                                                    text = sublevel.title
                                                        ?: "Level ${sublevel.index}",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF2C3E50)
                                                )
                                            }

                                            if (!sublevel.description.isNullOrEmpty()) {
                                                Text(
                                                    text = sublevel.description,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF546E7A),
                                                    lineHeight = 20.sp
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "🎼",
                                                    fontSize = 18.sp
                                                )
                                                Text(
                                                    text = sublevel.notes.joinToString(" • "),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF667EEA)
                                                )
                                            }
                                        }
                                    }
                                }
                            }


                            //---------------------------------------------------------
                            // BOTTOM HINT
                            //---------------------------------------------------------
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFFFF9C4).copy(alpha = 0.8f),
                                                Color(0xFFFFE082).copy(alpha = 0.8f)
                                            )
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFFFD54F).copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💡", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Pick your instrument and level to start your musical adventure!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF57C00),
                                    textAlign = TextAlign.Center
                                )
                            }

                            //---------------------------------------------------------
                            // ACTION BUTTONS ROW
                            //---------------------------------------------------------
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // CANCEL button (always enabled)
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(2.dp, Color(0xFF667EEA))
                                ) {
                                    Text(
                                        "Cancel",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF667EEA)
                                    )
                                }

                                // PLAY Button (disabled until ready)
                                val canPlay = selectedMode != null &&
                                        selectedSublevel != null &&
                                        selectedSublevel?.unlocked == true

                                Button(
                                    onClick = {
                                        if (canPlay) {
                                            onPlay(selectedSublevel!!, selectedMode!!)
                                        }
                                    },
                                    enabled = canPlay,
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (canPlay) Color(0xFF4CAF50) else Color(
                                            0xFF9E9E9E
                                        ),
                                        disabledContainerColor = Color(0xFFBDBDBD)
                                    )
                                ) {
                                    Text(
                                        "Start Mission 🚀",
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
}

    //-------------------------------------------------------------
// SUBLEVEL BUTTON WITH ANIMATIONS
//-------------------------------------------------------------
    @Composable
    fun SublevelButton(
        index: Int,
        sublevel: Sublevel,
        isSelected: Boolean,
        unlocked: Boolean,
        onClick: () -> Unit
    ) {
        val scale by animateFloatAsState(
            targetValue = if (isSelected) 1.08f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.7f)
        )

        // Soft modern colors
        val unlockedGradient = Brush.linearGradient(
            listOf(
                Color(0xFFE3F2FD),   // soft baby blue
                Color(0xFFD1E8FF)    // pastel blue
            )
        )

        val selectedGradient = Brush.linearGradient(
            listOf(
                Color(0xFF80D6FF),   // bright sky blue
                Color(0xFF4FA8FF)    // primary blue
            )
        )

        val lockedGradient = Brush.linearGradient(
            listOf(
                Color(0xFFE0E0E0),
                Color(0xFFCCCCCC)
            )
        )

        val borderColor = when {
            !unlocked -> Color(0xFFB0B0B0)
            isSelected -> Color(0xFF4FA8FF)
            else -> Color(0xFFCDE4FF)
        }

        val textColor = when {
            !unlocked -> Color.White
            isSelected -> Color.White
            else -> Color(0xFF2C3E50)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(
                        when {
                            !unlocked -> lockedGradient
                            isSelected -> selectedGradient
                            else -> unlockedGradient
                        }
                    )
                    .border(
                        width = 3.dp,
                        color = borderColor,
                        shape = CircleShape
                    )
                    .clickable(enabled = unlocked, onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (!unlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                } else {
                    Text(
                        text = "${index + 1}",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            // Stars underneath (cleaner style)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp)
            ) {
                repeat(sublevel.maxStars) { starIndex ->
                    val isFilled = starIndex < sublevel.starsEarned

                    Icon(
                        imageVector = Icons.Default.Star,
                        tint = if (isFilled) Color(0xFFFFC400) else Color(0xFFE0E0E0),
                        contentDescription = null,
                        modifier = Modifier.size(if (isFilled) 18.dp else 16.dp)
                    )
                }
            }
        }
    }


    //-------------------------------------------------------------
// MODE BUTTON WITH BOUNCE ANIMATION
//-------------------------------------------------------------
    @Composable
    fun ModeButton(
        label: String,
        emoji: String,
        isSelected: Boolean,
        selectedColor: Color,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        glowAlpha: Float
    ) {
        val scale by animateFloatAsState(
            targetValue = if (isSelected) 1.05f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        Box(modifier = modifier) {
            // Glow effect when selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    selectedColor.copy(alpha = glowAlpha * 0.4f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                )
            }

            Card(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .height(110.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) selectedColor else Color.White
                ),
                border = BorderStroke(
                    width = 3.dp,
                    color = if (isSelected) selectedColor else selectedColor.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 6.dp else 2.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 40.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = label,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (isSelected) Color.White else selectedColor
                    )
                }
            }
        }
    }
