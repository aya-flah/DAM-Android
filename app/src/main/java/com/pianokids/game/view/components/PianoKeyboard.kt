package com.pianokids.game.view.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianokids.game.data.models.*
import com.pianokids.game.utils.PianoSoundManager

/**
 * Main reusable Piano Keyboard component
 * This can be used across all levels with different configurations
 * 
 * @param config Visual configuration for the piano (colors, theme, labels)
 * @param onKeyPressed Callback when a key is pressed
 * @param onKeyReleased Callback when a key is released
 * @param modifier Optional modifier for the piano container
 */
@Composable
fun PianoKeyboard(
    config: PianoConfig = PianoConfig(),
    onKeyPressed: (PianoKey) -> Unit = {},
    onKeyReleased: (PianoKey) -> Unit = {},
    modifier: Modifier = Modifier,
    pressedKeys: Set<String> = emptySet()
) {
    val keys = remember { createPianoKeys(config.noteType) }
    
    // Use all keys (white and black)
    val whiteKeys = keys.filter { !it.isBlackKey }
    val blackKeys = keys.filter { it.isBlackKey }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
            .padding(8.dp)
    ) {
        // White keys
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            whiteKeys.forEachIndexed { index, key ->
                val keyColor = config.whiteKeyColors.getOrNull(index % config.whiteKeyColors.size)
                    ?: Color.White
                
                WhitePianoKey(
                    key = key,
                    color = keyColor,
                    isPressed = pressedKeys.contains(key.note),
                    showLabel = config.showLabels,
                    highlightColor = config.highlightColor,
                    onKeyPressed = { 
                        onKeyPressed(key)
                        PianoSoundManager.playNote(key.solfege)
                    },
                    onKeyReleased = { onKeyReleased(key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Black keys overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Position black keys over white keys
            whiteKeys.forEachIndexed { index, whiteKey ->
                Box(modifier = Modifier.weight(1f)) {
                    // Check if there's a black key after this white key
                    val blackKey = blackKeys.find { 
                        it.note == whiteKey.note + "#" 
                    }
                    
                    if (blackKey != null && (whiteKey.note == "C" || whiteKey.note == "D" || 
                        whiteKey.note == "F" || whiteKey.note == "G" || whiteKey.note == "A")) {
                        BlackPianoKey(
                            key = blackKey,
                            isPressed = pressedKeys.contains(blackKey.note),
                            showLabel = config.showLabels,
                            highlightColor = config.highlightColor,
                            onKeyPressed = {
                                onKeyPressed(blackKey)
                                PianoSoundManager.playNote(blackKey.solfege)
                            },
                            onKeyReleased = { onKeyReleased(blackKey) },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(40.dp)
                                .fillMaxHeight()
                                .offset(x = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual black piano key
 */
@Composable
private fun BlackPianoKey(
    key: PianoKey,
    isPressed: Boolean,
    showLabel: Boolean,
    highlightColor: Color,
    onKeyPressed: () -> Unit,
    onKeyReleased: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed || localPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "black_key_scale"
    )
    
    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
            .scale(scale)
            .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
            .background(
                if (isPressed || localPressed) {
                    Color(0xFF4A4A4A)  // Solid color instead of gradient for better hardware rendering
                } else {
                    Color(0xFF1A1A1A)  // Solid color for black keys
                }
            )
            .border(
                width = 1.dp,
                color = if (isPressed || localPressed) highlightColor else Color.Black,
                shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp)
            )
            .pointerInput(key.note) {
                detectTapGestures(
                    onPress = {
                        localPressed = true
                        onKeyPressed()
                        tryAwaitRelease()
                        localPressed = false
                        onKeyReleased()
                    }
                )
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        if (showLabel) {
            Text(
                text = key.displayLabel,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

/**
 * Individual white piano key with touch handling
 */
@Composable
private fun WhitePianoKey(
    key: PianoKey,
    color: Color,
    isPressed: Boolean,
    showLabel: Boolean,
    highlightColor: Color,
    onKeyPressed: () -> Unit,
    onKeyReleased: () -> Unit,
    modifier: Modifier = Modifier
) {
    var localPressed by remember { mutableStateOf(false) }
    
    // Animation for key press
    val scale by animateFloatAsState(
        targetValue = if (isPressed || localPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "key_scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed || localPressed) 2.dp else 8.dp,
        animationSpec = tween(durationMillis = 100),
        label = "key_elevation"
    )
    
    Box(
        modifier = modifier
            .fillMaxHeight()
            .shadow(elevation, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            .background(
                if (isPressed || localPressed) {
                    Brush.verticalGradient(
                        listOf(
                            color.copy(alpha = 0.7f),
                            color.copy(alpha = 0.9f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            color,
                            color.copy(alpha = 0.8f)
                        )
                    )
                }
            )
            .border(
                width = 2.dp,
                color = if (isPressed || localPressed) highlightColor else Color.Black.copy(alpha = 0.3f),
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
            )
            .pointerInput(key.note) {
                detectTapGestures(
                    onPress = {
                        localPressed = true
                        onKeyPressed()
                        tryAwaitRelease()
                        localPressed = false
                        onKeyReleased()
                    }
                )
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        if (showLabel) {
            Text(
                text = key.displayLabel,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

/**
 * Preview helper for testing different configurations
 */
@Composable
fun PianoKeyboardPreview(config: PianoConfig = PianoConfig()) {
    var pressedKeys by remember { mutableStateOf(setOf<String>()) }
    
    PianoKeyboard(
        config = config,
        onKeyPressed = { key ->
            pressedKeys = pressedKeys + key.note
        },
        onKeyReleased = { key ->
            pressedKeys = pressedKeys - key.note
        },
        pressedKeys = pressedKeys
    )
}
