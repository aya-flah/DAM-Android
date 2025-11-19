package com.pianokids.game.utils.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

@Composable
fun ConfettiExplosion(
    modifier: Modifier = Modifier,
    particleCount: Int = 60
) {
    val particles = remember {
        List(particleCount) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = 0f,
                radius = Random.nextInt(6, 14).toFloat(),
                color = randomConfettiColor(),
                speed = Random.nextFloat() * 1.8f + 0.5f
            )
        }
    }

    val anim = rememberInfiniteTransition()

    val time by anim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        )
    )

    Canvas(modifier = modifier) {
        particles.forEach { p ->
            val px = p.x * size.width
            val py = (p.y + time * p.speed) * size.height % size.height

            drawCircle(
                color = p.color,
                radius = p.radius,
                center = Offset(px, py)
            )
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color,
    val speed: Float
)

private fun randomConfettiColor(): Color {
    val colors = listOf(
        Color(0xFFFF4081),
        Color(0xFF448AFF),
        Color(0xFFFFC107),
        Color(0xFF4CAF50),
        Color(0xFFFF5722),
        Color(0xFF9C27B0)
    )
    return colors.random()
}
