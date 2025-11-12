package com.pianokids.game.view.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.utils.UserPreferences

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { UserPreferences(context) }

    // ---- Music -------------------------------------------------
    var musicEnabled by remember { mutableStateOf(prefs.getMusicEnabled()   ) }
    var musicVolume  by remember { mutableStateOf(prefs.getMusicVolume()    ) }

    // ---- Sound -------------------------------------------------
    var soundEnabled by remember { mutableStateOf(prefs.getSoundEnabled()   ) }
    var soundVolume  by remember { mutableStateOf(prefs.getSoundVolume()    ) }

    // ---- Vibration ---------------------------------------------
    var vibrationEnabled by remember { mutableStateOf(prefs.getVibrationEnabled()) }

    // Apply changes to SoundManager immediately
    LaunchedEffect(musicEnabled, musicVolume) {
        if (musicEnabled) {
            SoundManager.enableMusic()
            // bgPlayer volume is 0-1
            SoundManager::class.java.getDeclaredField("bgPlayer")
                .apply { isAccessible = true }
                .get(SoundManager)?.let { player ->
                    (player as? android.media.MediaPlayer)?.setVolume(musicVolume / 100f, musicVolume / 100f)
                }
        } else {
            SoundManager.disableMusic()
        }
        prefs.setMusicEnabled(musicEnabled)
        prefs.setMusicVolume(musicVolume)
    }

    LaunchedEffect(soundEnabled) {
        if (soundEnabled) SoundManager.enableSound() else SoundManager.disableSound()
        prefs.setSoundEnabled(soundEnabled)
    }

    LaunchedEffect(vibrationEnabled) {
        if (vibrationEnabled) SoundManager.enableVibration() else SoundManager.disableVibration()
        prefs.setVibrationEnabled(vibrationEnabled)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings", style = MaterialTheme.typography.headlineMedium) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ---------- Music ----------
                SettingSection(title = "Music") {
                    SwitchRow(
                        text = "Enable Music",
                        checked = musicEnabled,
                        onCheckedChange = { musicEnabled = it }
                    )
                    if (musicEnabled) {
                        SliderRow(
                            label = "Music Volume",
                            value = musicVolume,
                            onValueChange = { musicVolume = it }
                        )
                    }
                }

                // ---------- Sound ----------
                SettingSection(title = "Sound Effects") {
                    SwitchRow(
                        text = "Enable Sound",
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it }
                    )
                    if (soundEnabled) {
                        SliderRow(
                            label = "Sound Volume",
                            value = soundVolume,
                            onValueChange = { soundVolume = it }
                        )
                    }
                }

                // ---------- Vibration ----------
                SettingSection(title = "Vibration") {
                    SwitchRow(
                        text = "Enable Vibration",
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    )
}

/* -------------------------------------------------------------
   Helper UI components
   ------------------------------------------------------------- */
@Composable
private fun SettingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        content()
    }
}

@Composable
private fun SwitchRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SliderRow(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("$value %", style = MaterialTheme.typography.bodyMedium)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 0f..100f,
            steps = 99,
            modifier = Modifier.fillMaxWidth()
        )
    }
}