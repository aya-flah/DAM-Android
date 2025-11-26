package com.pianokids.game.view.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pianokids.game.data.api.RetrofitClient
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.SoundManager
import com.pianokids.game.viewmodel.MusicViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun MusicRecognitionScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: MusicViewModel = viewModel(
        factory = MusicViewModel.Factory(RetrofitClient.musicApi, context)
    )
    val scope = rememberCoroutineScope()
    
    val isRecording by viewModel.isRecording.collectAsState()
    val isRecognizing by viewModel.isRecognizing.collectAsState()
    val recognitionResult by viewModel.recognitionResult.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var hasPermission by remember { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            viewModel.startRecording()
        }
    }
    
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        RainbowIndigo.copy(alpha = 0.3f),
                        RainbowBlue.copy(alpha = 0.2f),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        SoundManager.playClick()
                        viewModel.stopRecording()
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.9f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = RainbowBlue
                    )
                }
                
                Text(
                    text = "🎵 Song Recognition",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = RainbowIndigo
                    )
                )
                
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Main Content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    when {
                        // Show result
                        recognitionResult != null -> {
                            RecognitionResultView(
                                result = recognitionResult!!,
                                onTryAgain = {
                                    viewModel.clearResult()
                                }
                            )
                        }
                        
                        // Show error
                        error != null -> {
                            ErrorView(
                                error = error!!,
                                onTryAgain = {
                                    viewModel.clearError()
                                }
                            )
                        }
                        
                        // Show recording UI
                        isRecording -> {
                            RecordingView(
                                onStop = {
                                    SoundManager.playClick()
                                    viewModel.stopRecordingAndRecognize()
                                }
                            )
                        }
                        
                        // Show recognizing UI
                        isRecognizing -> {
                            RecognizingView()
                        }
                        
                        // Show start UI
                        else -> {
                            StartRecordingView(
                                onStart = {
                                    SoundManager.playClick()
                                    if (hasPermission) {
                                        viewModel.startRecording()
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Instructions
            if (recognitionResult == null && error == null && !isRecording && !isRecognizing) {
                InstructionCard()
            }
        }
    }
}

@Composable
fun StartRecordingView(onStart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎸",
            fontSize = 80.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Ready to Recognize",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Play a song or hum a melody",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onStart,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            colors = ButtonDefaults.buttonColors(
                containerColor = RainbowRed
            ),
            elevation = ButtonDefaults.buttonElevation(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardVoice,
                contentDescription = "Record",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Tap to Start",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = RainbowRed
            )
        )
    }
}

@Composable
fun RecordingView(onStop: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            RainbowRed.copy(alpha = 0.8f),
                            RainbowOrange.copy(alpha = 0.6f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Recording",
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "🔴 Recording...",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = RainbowRed
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Listening to the music",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onStop,
            modifier = Modifier
                .height(56.dp)
                .width(200.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RainbowBlue
            )
        ) {
            Icon(imageVector = Icons.Default.Stop, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Stop & Recognize", fontSize = 16.sp)
        }
    }
}

@Composable
fun RecognizingView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(80.dp),
            color = RainbowIndigo,
            strokeWidth = 6.dp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "🔍 Recognizing...",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Finding your song",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
fun RecognitionResultView(
    result: com.pianokids.game.data.models.MusicRecognitionResponse,
    onTryAgain: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success animation
        Text(
            text = "✨",
            fontSize = 80.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Song Found!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = RainbowGreen
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Song details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = RainbowBlue.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                ResultRow("🎵 Title", result.title)
                Spacer(modifier = Modifier.height(12.dp))
                ResultRow("👤 Artist", result.artist)
                Spacer(modifier = Modifier.height(12.dp))
                ResultRow("💿 Album", result.album)
                Spacer(modifier = Modifier.height(12.dp))
                ResultRow("🎯 Confidence", "${(result.confidence).toInt()}%")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onTryAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RainbowIndigo
            )
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Another Song", fontSize = 16.sp)
        }
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ErrorView(error: String, onTryAgain: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "😕",
            fontSize = 80.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Oops!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = RainbowOrange
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onTryAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RainbowBlue
            )
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Try Again", fontSize = 16.sp)
        }
    }
}

@Composable
fun InstructionCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = RainbowYellow.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = RainbowOrange,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Play a song for at least 5 seconds for best results",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF333333)
            )
        }
    }
}
