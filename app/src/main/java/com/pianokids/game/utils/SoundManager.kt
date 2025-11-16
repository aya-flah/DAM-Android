// app/src/main/java/com/pianokids/game/utils/SoundManager.kt
package com.pianokids.game.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.os.Build
import com.pianokids.game.R

object SoundManager {
    private var clickPlayer: MediaPlayer? = null
    private var bgPlayer: MediaPlayer? = null

    private var soundPool: SoundPool? = null
    private var typingSoundId: Int? = null

    private var vibrator: Vibrator? = null

    private var isSoundEnabled = true
    private var isMusicEnabled = true
    private var isVibrationEnabled = true

    fun init(context: Context) {
        clickPlayer = MediaPlayer.create(context, R.raw.click1)
        bgPlayer = MediaPlayer.create(context, R.raw.bg_music)?.apply {
            isLooping = true
            setVolume(0.4f, 0.4f)
        }

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .build()

        typingSoundId = soundPool?.load(context, R.raw.typing_sound, 1)


        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    fun playClick() {
        if (!isSoundEnabled) return

        clickPlayer?.let {
            if (it.isPlaying) {
                it.seekTo(0)
            } else {
                it.start()
            }
        }

        vibrate()
    }

    fun startBackgroundMusic() {
        if (!isMusicEnabled) return
        bgPlayer?.start()
    }

    fun stopBackgroundMusic() {
        bgPlayer?.pause()
    }

    private fun vibrate(duration: Long = 50) {
        if (!isVibrationEnabled) return

        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(duration)
            }
        }
    }

    // Sound control methods
    fun enableSound() {
        isSoundEnabled = true
    }

    fun disableSound() {
        isSoundEnabled = false
    }

    fun isSoundEnabled(): Boolean = isSoundEnabled

    // Music control methods
    fun enableMusic() {
        isMusicEnabled = true
        if (isMusicEnabled) {
            startBackgroundMusic()
        }
    }

    fun disableMusic() {
        isMusicEnabled = false
        stopBackgroundMusic()
    }

    fun isMusicEnabled(): Boolean = isMusicEnabled

    // Vibration control methods
    fun enableVibration() {
        isVibrationEnabled = true
    }

    fun disableVibration() {
        isVibrationEnabled = false
    }

    fun isVibrationEnabled(): Boolean = isVibrationEnabled

    fun playTyping() {
        if (!isSoundEnabled) return
        typingSoundId?.let { id ->
            soundPool?.play(id, 1f, 1f, 1, 0, 1.0f)
        }
    }


    fun release() {
        clickPlayer?.release()
        bgPlayer?.release()
        soundPool?.release()
        soundPool = null
        typingSoundId = null
        clickPlayer = null
        bgPlayer = null
        vibrator = null
    }
}