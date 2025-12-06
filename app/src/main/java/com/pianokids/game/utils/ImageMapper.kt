package com.pianokids.game.utils

import com.pianokids.game.R

object ImageMapper {

    fun levelEmoji(theme: String): String = when (theme) {
        "Batman" -> "🦇"
        "Spider-Man" -> "🕷️"
        "Conan" -> "🍃"
        "BlackPanther" -> "⚡"
        "Avengers Mix" -> "⭐"
        "Hunter x Hunter" -> "🎣"
        else -> "🎵"
    }

    fun bossImage(theme: String?): Int {
        return when (theme) {
            "Batman" -> R.drawable.batman
            "Spider-Man" -> R.drawable.spiderman
            "Conan" -> R.drawable.conan
            "BlackPanther" -> R.drawable.black_panther
            "Avengers Mix" -> R.drawable.ironman
            "Hunter x Hunter" -> R.drawable.hxh
            else -> R.drawable.kirb
        }
    }

    fun islandImage(theme: String?): Int {
        return when (theme) {
            "Batman" -> R.drawable.level_1
            "Spider-Man" -> R.drawable.level_2
            "Conan" -> R.drawable.level_3
            "BlackPanther" -> R.drawable.level_4
            "Avengers Mix" -> R.drawable.level_5
            "Hunter x Hunter" -> R.drawable.level_6
            else -> R.drawable.level_1
        }
    }
}
