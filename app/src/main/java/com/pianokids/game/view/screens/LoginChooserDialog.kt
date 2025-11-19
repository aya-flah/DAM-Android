package com.pianokids.game.view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianokids.game.R
import com.pianokids.game.utils.SoundManager

@Composable
fun LoginChooserDialog(
    onDismiss: () -> Unit,
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Login Method", textAlign = TextAlign.Center) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { SoundManager.playClick(); onGoogleClick() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.ic_google), null, Modifier.size(24.dp).padding(end = 12.dp))
                        Text("Continue with Google", color = Color.Black, fontWeight = FontWeight.Medium)
                    }
                }
                Button(
                    onClick = { SoundManager.playClick(); onFacebookClick() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painterResource(R.drawable.ic_facebook), null, Modifier.size(24.dp).padding(end = 12.dp), colorFilter = ColorFilter.tint(Color.White))
                        Text("Continue with Facebook", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}