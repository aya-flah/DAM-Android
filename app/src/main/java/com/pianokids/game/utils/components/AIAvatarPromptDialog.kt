package com.pianokids.game.utils.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pianokids.game.ui.theme.*

@Composable
fun AIAvatarPromptDialog(
    avatarName: String,
    onGenerateAvatar: (prompt: String, style: String) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var prompt by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("cartoon") }
    var showPromptError by remember { mutableStateOf(false) }
    var showExamples by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF667EEA),
                                Color(0xFF764BA2)
                            )
                        ),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🤖 AI Avatar Creator",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        IconButton(
                            onClick = { showExamples = !showExamples },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Show examples",
                                tint = Color.White
                            )
                        }
                    }

                    Text(
                        text = "Describe your dream avatar for \"$avatarName\"",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )

                    // Examples section
                    AnimatedVisibility(
                        visible = showExamples,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color.White.copy(alpha = 0.15f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "💡 Example Prompts:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            val examples = listOf(
                                "Naruto with orange clothes",
                                "Mickey Mouse style character",
                                "Pikachu inspired character",
                                "Superhero with blue cape",
                                "Princess with pink hair",
                                "Ninja with black outfit",
                                "Wizard with purple robe"
                            )
                            
                            examples.forEach { example ->
                                Text(
                                    text = "• $example",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            prompt = example
                                            showExamples = false
                                        }
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .padding(8.dp),
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    // Prompt input
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = {
                            prompt = it
                            showPromptError = false
                        },
                        label = { Text("Describe your avatar", color = Color.White.copy(alpha = 0.7f)) },
                        placeholder = { Text("e.g., Naruto with orange clothes", color = Color.White.copy(alpha = 0.5f)) },
                        minLines = 3,
                        maxLines = 5,
                        isError = showPromptError,
                        enabled = !isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            errorBorderColor = RainbowRed,
                            cursorColor = Color.White,
                            disabledTextColor = Color.White.copy(alpha = 0.5f),
                            disabledBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (showPromptError) {
                        Text(
                            text = "Please describe your avatar",
                            color = RainbowRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Error message
                    if (error != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = RainbowRed.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Text(
                                    text = error,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Loading indicator
                    if (isLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(40.dp),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "🎨 Creating your avatar with AI magic...",
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "This may take a few seconds",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White,
                                disabledContentColor = Color.White.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("← Back", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                if (prompt.isBlank()) {
                                    showPromptError = true
                                } else {
                                    onGenerateAvatar(prompt, selectedStyle)
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF667EEA),
                                disabledContainerColor = Color.White.copy(alpha = 0.3f),
                                disabledContentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Info text
                    Text(
                        text = "✨ AI will create a unique, kid-friendly avatar based on your description",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
