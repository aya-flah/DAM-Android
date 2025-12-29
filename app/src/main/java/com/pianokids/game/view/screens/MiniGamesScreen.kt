package com.pianokids.game.view.screens

import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pianokids.game.R
import com.pianokids.game.ui.theme.*
import com.pianokids.game.utils.SoundManager
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class MusicNote(
    val name: String,
    val frenchName: String,
    val imageRes: Int,
    val soundRes: Int,
    val color: Color,
    val position: Int
)

private val musicNotes = listOf(
    MusicNote("Do", "Do", R.drawable.notes_do, R.raw.note_do, Color(0xFFE53935), 0),
    MusicNote("Re", "Ré", R.drawable.notes_re, R.raw.note_re, Color(0xFFFF9800), 1),
    MusicNote("Mi", "Mi", R.drawable.notes_me, R.raw.note_mi, Color(0xFFFFEB3B), 2),
    MusicNote("Fa", "Fa", R.drawable.notes_fa, R.raw.note_fa, Color(0xFF4CAF50), 3),
    MusicNote("Sol", "Sol", R.drawable.notes_sol, R.raw.note_sol, Color(0xFF2196F3), 4),
    MusicNote("La", "La", R.drawable.notes_la, R.raw.note_la, Color(0xFF3F51B5), 5),
    MusicNote("Si", "Si", R.drawable.notes_si, R.raw.note_si, Color(0xFF9C27B0), 6)
)

private enum class GameMode {
    MENU,
    GUESS_NOTE,
    FIND_NOTE,
    MEMORY_MATCH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniGamesScreen(
    onNavigateBack: () -> Unit
) {
    var gameMode by remember { mutableStateOf(GameMode.MENU) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF667EEA),
            Color(0xFF764BA2),
            Color(0xFFF093FB)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Floating decorations
        FloatingStars()

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            TopAppBar(
                title = {
                    Text(
                        text = when (gameMode) {
                            GameMode.MENU -> "🎮 Mini Games"
                            GameMode.GUESS_NOTE -> "🎵 Guess the Note"
                            GameMode.FIND_NOTE -> "🔍 Find the Note"
                            GameMode.MEMORY_MATCH -> "🧠 Memory Match"
                        },
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        SoundManager.playClick()
                        if (gameMode == GameMode.MENU) {
                            onNavigateBack()
                        } else {
                            gameMode = GameMode.MENU
                        }
                    }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            when (gameMode) {
                GameMode.MENU -> GameMenuScreen(
                    onSelectGame = { mode ->
                        SoundManager.playClick()
                        gameMode = mode
                    }
                )
                GameMode.GUESS_NOTE -> GuessTheNoteGame(
                    onBack = { gameMode = GameMode.MENU }
                )
                GameMode.FIND_NOTE -> FindTheNoteGame(
                    onBack = { gameMode = GameMode.MENU }
                )
                GameMode.MEMORY_MATCH -> MemoryMatchGame(
                    onBack = { gameMode = GameMode.MENU }
                )
            }
        }
    }
}

@Composable
private fun FloatingStars() {
    val transition = rememberInfiniteTransition(label = "stars")
    
    repeat(8) { index ->
        val offsetX = remember { Random.nextFloat() * 300f + 20f }
        val offsetY = remember { Random.nextFloat() * 600f + 50f }
        val delay = remember { index * 200 }
        
        val float by transition.animateFloat(
            initialValue = 0f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000 + delay, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "float-$index"
        )
        
        val scale by transition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500 + delay, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale-$index"
        )
        
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier
                .offset(x = offsetX.dp, y = (offsetY + float).dp)
                .scale(scale)
                .size(24.dp)
        )
    }
}

@Composable
private fun GameMenuScreen(
    onSelectGame: (GameMode) -> Unit
) {
    val games = listOf(
        Triple(GameMode.GUESS_NOTE, "🎵 Guess the Note", "See a note, pick its name!"),
        Triple(GameMode.FIND_NOTE, "🔍 Find the Note", "Hear the name, find it on piano!"),
        Triple(GameMode.MEMORY_MATCH, "🧠 Memory Match", "Match notes with their names!")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        
        Text(
            text = "Learn Music Notes! 🎹",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Play fun games to remember\nDo, Re, Mi, Fa, Sol, La, Si",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Color.White.copy(alpha = 0.9f)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        games.forEach { (mode, title, description) ->
            GameCard(
                title = title,
                description = description,
                onClick = { onSelectGame(mode) }
            )
        }
    }
}

@Composable
private fun GameCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "card")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B2559)
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF64748B)
                    )
                )
            }
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color(0xFF667EEA),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF667EEA).copy(alpha = 0.15f))
                    .padding(12.dp)
            )
        }
    }
}

// ============ GAME 1: GUESS THE NOTE ============
@Composable
private fun GuessTheNoteGame(onBack: () -> Unit) {
    var currentNote by remember { mutableStateOf(musicNotes.random()) }
    var score by remember { mutableStateOf(0) }
    var showFeedback by remember { mutableStateOf<Boolean?>(null) }
    var streak by remember { mutableStateOf(0) }

    LaunchedEffect(showFeedback) {
        if (showFeedback != null) {
            delay(800)
            showFeedback = null
            currentNote = musicNotes.random()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Score display
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ScoreBadge(label = "Score", value = score.toString(), color = RainbowYellow)
            ScoreBadge(label = "Streak", value = "🔥 $streak", color = RainbowOrange)
        }

        Spacer(Modifier.height(20.dp))

        // Display the note visually
        NoteDisplay(note = currentNote, showFeedback = showFeedback)

        Text(
            text = "What note is this?",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        // Answer buttons grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            items(musicNotes) { note ->
                NoteButton(
                    note = note,
                    enabled = showFeedback == null,
                    onClick = {
                        SoundManager.playClick()
                        if (note.name == currentNote.name) {
                            showFeedback = true
                            score += 10 + streak * 2
                            streak++
                        } else {
                            showFeedback = false
                            streak = 0
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ScoreBadge(label: String, value: String, color: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = Color.White.copy(alpha = 0.8f)
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
private fun NoteDisplay(note: MusicNote, showFeedback: Boolean?) {
    val scale by animateFloatAsState(
        targetValue = if (showFeedback == true) 1.2f else if (showFeedback == false) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "noteScale"
    )

    val bgColor = when (showFeedback) {
        true -> Color(0xFF4CAF50)
        false -> Color(0xFFF44336)
        null -> note.color
    }

    Box(
        modifier = Modifier
            .size(160.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = note.imageRes),
                contentDescription = note.name,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )
            if (showFeedback != null) {
                Text(
                    text = if (showFeedback) "✓ ${note.name}" else "✗",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun NoteButton(
    note: MusicNote,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = note.color,
            disabledContainerColor = note.color.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .height(60.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = note.name,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

// ============ GAME 2: FIND THE NOTE ============
@Composable
private fun FindTheNoteGame(onBack: () -> Unit) {
    val context = LocalContext.current
    var targetNote by remember { mutableStateOf(musicNotes.random()) }
    var score by remember { mutableStateOf(0) }
    var showFeedback by remember { mutableStateOf<Boolean?>(null) }

    fun playNoteSound(note: MusicNote) {
        try {
            val player = MediaPlayer.create(context, note.soundRes)
            player?.setOnCompletionListener { it.release() }
            player?.start()
        } catch (_: Exception) { }
    }

    LaunchedEffect(showFeedback) {
        if (showFeedback != null) {
            delay(800)
            showFeedback = null
            targetNote = musicNotes.random()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ScoreBadge(label = "Score", value = score.toString(), color = RainbowBlue)

        Spacer(Modifier.height(16.dp))

        // Target note to find - show image in circle
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Find this note:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF64748B)
                    )
                )
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(targetNote.color.copy(alpha = 0.2f))
                        .border(3.dp, targetNote.color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = targetNote.imageRes),
                        contentDescription = targetNote.name,
                        modifier = Modifier
                            .size(85.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
Spacer(Modifier.height(16.dp))

        // Piano-like keys
        Text(
            text = "Tap the correct key!",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White
            )
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            musicNotes.forEach { note ->
                PianoKey(
                    note = note,
                    isCorrect = showFeedback == true && note.name == targetNote.name,
                    isWrong = showFeedback == false && note.name == targetNote.name,
                    enabled = showFeedback == null,
                    onClick = {
                        playNoteSound(note)
                        if (note.name == targetNote.name) {
                            showFeedback = true
                            score += 10
                        } else {
                            showFeedback = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PianoKey(
    note: MusicNote,
    isCorrect: Boolean,
    isWrong: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bgColor = when {
        isCorrect -> Color(0xFF4CAF50)
        isWrong -> Color(0xFFF44336)
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .width(80.dp)
            .height(180.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = note.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect || isWrong) Color.White else note.color
                )
            )
        }
    }
}

// ============ GAME 3: MEMORY MATCH ============
private data class MemoryCard(
    val id: Int,
    val note: MusicNote,
    val isName: Boolean, // true = shows name, false = shows emoji
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)

@Composable
private fun MemoryMatchGame(onBack: () -> Unit) {
    val notesSubset = remember { musicNotes.shuffled().take(4) }
    
    var cards by remember {
        mutableStateOf(
            notesSubset.flatMapIndexed { index, note ->
                listOf(
                    MemoryCard(index * 2, note, true),
                    MemoryCard(index * 2 + 1, note, false)
                )
            }.shuffled()
        )
    }
    
    var firstCard by remember { mutableStateOf<MemoryCard?>(null) }
    var canFlip by remember { mutableStateOf(true) }
    var moves by remember { mutableStateOf(0) }
    var matchedPairs by remember { mutableStateOf(0) }

    LaunchedEffect(firstCard, cards) {
        val flippedCards = cards.filter { it.isFlipped && !it.isMatched }
        if (flippedCards.size == 2) {
            canFlip = false
            delay(1000)
            
            val (card1, card2) = flippedCards
            if (card1.note.name == card2.note.name) {
                cards = cards.map {
                    if (it.id == card1.id || it.id == card2.id) {
                        it.copy(isMatched = true)
                    } else it
                }
                matchedPairs++
            } else {
                cards = cards.map {
                    if (it.id == card1.id || it.id == card2.id) {
                        it.copy(isFlipped = false)
                    } else it
                }
            }
            
            firstCard = null
            canFlip = true
            moves++
        }
    }

    val isGameComplete = matchedPairs == notesSubset.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ScoreBadge(label = "Moves", value = moves.toString(), color = RainbowIndigo)
            ScoreBadge(label = "Pairs", value = "$matchedPairs/${notesSubset.size}", color = RainbowGreen)
        }

        if (isGameComplete) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = RainbowGreen)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉 You Won!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("In $moves moves!", color = Color.White.copy(alpha = 0.9f))
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            SoundManager.playClick()
                            cards = notesSubset.flatMapIndexed { index, note ->
                                listOf(
                                    MemoryCard(index * 2, note, true),
                                    MemoryCard(index * 2 + 1, note, false)
                                )
                            }.shuffled()
                            firstCard = null
                            moves = 0
                            matchedPairs = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Play Again", color = RainbowGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Text(
            text = "Match notes with their names!",
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(cards) { card ->
                MemoryCardView(
                    card = card,
                    onClick = {
                        if (canFlip && !card.isFlipped && !card.isMatched) {
                            SoundManager.playClick()
                            cards = cards.map {
                                if (it.id == card.id) it.copy(isFlipped = true) else it
                            }
                            if (firstCard == null) {
                                firstCard = card
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MemoryCardView(
    card: MemoryCard,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(300),
        label = "cardFlip"
    )

    val bgColor = when {
        card.isMatched -> Color(0xFF4CAF50).copy(alpha = 0.8f)
        card.isFlipped -> card.note.color.copy(alpha = 0.9f)
        else -> Color.White.copy(alpha = 0.95f)
    }

    Card(
        modifier = Modifier
            .aspectRatio(0.75f)
            .graphicsLayer { rotationY = rotation }
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (card.isFlipped || card.isMatched) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                ) {
                    if (card.isName) {
                        Text(
                            text = card.note.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    } else {
                        Image(
                            painter = painterResource(id = card.note.imageRes),
                            contentDescription = card.note.name,
                            modifier = Modifier
                                .size(180.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            } else {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF667EEA)
                    )
                )
            }
        }
    }
}
