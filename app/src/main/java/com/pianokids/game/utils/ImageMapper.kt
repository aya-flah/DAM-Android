package com.pianokids.game.utils

import com.pianokids.game.R

object ImageMapper {

    fun levelEmoji(theme: String): String = when (theme) {
        "Batman" -> "🦇"
        "Spider-Man" -> "🕸️"
        "Detective Conan" -> "🔎"
        "Black Panther" -> "🐾"
        "Ronin Warriors" -> "🥷"
        "Hunter x Hunter" -> "🎯"
        else -> "🎵"
    }

    fun bossImage(theme: String?): Int {
        return when (theme) {
            "Batman" -> R.drawable.batman
            "Spider-Man" -> R.drawable.spiderman
            "Detective Conan" -> R.drawable.conan
            "Black Panther" -> R.drawable.black_panther
            "Ronin Warriors" -> R.drawable.ryo_rw
            "Hunter x Hunter" -> R.drawable.hxh
            else -> R.drawable.kirb
        }
    }

    fun islandImage(theme: String?): Int {
        return when (theme) {
            "Batman" -> R.drawable.level_1
            "Spider-Man" -> R.drawable.level_2
            "Detective Conan" -> R.drawable.level_3
            "Black Panther" -> R.drawable.level_4
            "Ronin Warriors" -> R.drawable.level_5
            "Hunter x Hunter" -> R.drawable.level_6
            else -> R.drawable.kirb
        }
    }

    fun backgroundFor(theme: String?): Int {
        return when (theme) {

            "Batman" -> R.drawable.bg_batman_gif
            "Spider-Man" -> R.drawable.bg_spiderman_gif
            "Detective Conan" -> R.drawable.bg_conan_gif
            "Black Panther" -> R.drawable.bg_panther_gif
            "Ronin Warriors" -> R.drawable.bg_ronin_warriors_gif
            "Hunter x Hunter" -> R.drawable.bg_hxh_gif

            else -> R.drawable.ocean
        }
    }
}
