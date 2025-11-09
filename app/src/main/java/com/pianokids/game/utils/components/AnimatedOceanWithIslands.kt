package com.pianokids.game.utils.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pianokids.game.R
import kotlin.math.sin

@Composable
fun AnimatedOceanWithIslands() {
    val inf = rememberInfiniteTransition(label = "ocean")

    val waveOffset by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 6000,
                delayMillis = 0,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    // Floating animation for the island
    val islandFloat by inf.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 3500,
                delayMillis = 0,
                easing = EaseInOutSine
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "islandFloat"
    )

    Box(Modifier.fillMaxSize()) {
        // Single big island on the right
        Image(
            painter = painterResource(R.drawable.floating_land),
            contentDescription = null,
            modifier = Modifier
                .offset(x = 0.dp, y = (120 + islandFloat).dp)
                .size(640.dp)
                .alpha(0.85f),
            contentScale = ContentScale.Fit
        )

        // Animated waves
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // First wave layer
            drawPath(
                path = Path().apply {
                    moveTo(0f, h * 0.84f)
                    for (i in 0..w.toInt() step 18) {
                        val x = i.toFloat()
                        val y = h * 0.84f + sin((x + waveOffset) * 0.018f) * 34f
                        lineTo(x, y)
                    }
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                },
                color = Color(0xFF1E88E5).copy(alpha = 0.32f)
            )

            // Second wave layer
            drawPath(
                path = Path().apply {
                    moveTo(0f, h * 0.89f)
                    for (i in 0..w.toInt() step 18) {
                        val x = i.toFloat()
                        val y = h * 0.89f + sin((x - waveOffset) * 0.014f) * 28f
                        lineTo(x, y)
                    }
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                },
                color = Color(0xFF1565C0).copy(alpha = 0.42f)
            )
        }
    }
}

// 👇 Preview composable to visualize it in Android Studio
@Preview(showBackground = true)
@Composable
fun AnimatedOceanWithIslandsPreview() {
    AnimatedOceanWithIslands()
}
