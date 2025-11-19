package com.pianokids.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Rainbow colors for kids
val RainbowRed = Color(0xFFFF6B6B)
val RainbowOrange = Color(0xFFFFB347)
val RainbowYellow = Color(0xFFFFF176)
val RainbowGreen = Color(0xFF81C784)
val RainbowBlue = Color(0xFF64B5F6)
val RainbowIndigo = Color(0xFF9575CD)
val RainbowViolet = Color(0xFFBA68C8)
val RainbowPink = Color(0xD5F0628A)

// Ocean/Sea themed colors
val OceanDeep = Color(0xFF0D47A1)
val OceanLight = Color(0xFF42A5F5)
val SeaFoam = Color(0xFF80DEEA)
val SandBeach = Color(0xFFFFE0B2)
val SkyBlue = Color(0xFF87CEEB)

// Game UI colors
val GameBackground = Color(0xFFF3F9FF)
val CardBackground = Color(0xFFFFFFFF)
val TextDark = Color(0xFF2C3E50)
val TextLight = Color(0xFF7F8C8D)

private val LightColorScheme = lightColorScheme(
    primary = RainbowBlue,
    secondary = RainbowGreen,
    tertiary = RainbowPink,
    background = GameBackground,
    surface = CardBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun PianoKidsGameTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}