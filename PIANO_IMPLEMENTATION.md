# Piano Implementation Guide

## 🎹 Overview
Professional, reusable piano keyboard system for the Piano Kids educational game.

## 📁 Architecture

### 1. **Data Models** (`PianoModels.kt`)
- `PianoNote`: Represents a single note (Do/Ré/Mi with sound resource)
- `PianoConfig`: Level-specific customization
- `PianoKeyState`: Track key press states

### 2. **Sound Manager** (`PianoSoundManager.kt`)
- Singleton sound manager using Android SoundPool
- Optimized for low-latency note playback
- Loads all 7 notes: Do, Ré, Mi, Fa, Sol, La, Si
- **Sound files location**: `app/src/main/res/raw/`
  - `note_do.mp3`, `note_re.mp3`, `note_mi.mp3`, `note_fa.mp3`
  - `note_sol.mp3`, `note_la.mp3`, `note_si.mp3`

### 3. **ViewModel** (`PianoViewModel.kt`)
- Manages piano key states
- Handles game logic (sequences, scoring)
- Can be extended for level-specific challenges

### 4. **UI Components** (`PianoKeyboard.kt`)
- **PianoKeyboard**: Main reusable keyboard component
- **PianoKey**: Individual key with touch handling
- **Pre-configured themes**:
  - `getBatmanPianoNotes()`: Batman-themed colors
  - `getDefaultPianoNotes()`: Rainbow colors

### 5. **Screens** (`PianoPracticeScreen.kt`)
- Complete gameplay screen with:
  - Custom background per level
  - Back button & score display
  - Piano keyboard at bottom
  - Character display in center

## 🎮 Usage

### For Level 1 (Batman):
```kotlin
PianoPracticeScreen(
    levelNumber = 1,
    levelTheme = "Batman",
    backgroundImage = R.drawable.bg_level1,
    onNavigateBack = { /* navigate back */ }
)
```

### For Other Levels (Reusable):
```kotlin
// Level 2 - Spider-Man
PianoPracticeScreen(
    levelNumber = 2,
    levelTheme = "Spider-Man",
    backgroundImage = R.drawable.bg_level2,
    onNavigateBack = { /* navigate back */ }
)

// Level 3 - Moonlight
PianoPracticeScreen(
    levelNumber = 3,
    levelTheme = "Moonlight",
    backgroundImage = R.drawable.bg_level3,
    onNavigateBack = { /* navigate back */ }
)
```

## 🎵 Sound Files

All sound files are in `app/src/main/res/raw/`:
- ✅ `note_do.mp3` - Do (C)
- ✅ `note_re.mp3` - Ré (D)
- ✅ `note_mi.mp3` - Mi (E)
- ✅ `note_fa.mp3` - Fa (F)
- ✅ `note_sol.mp3` - Sol (G)
- ✅ `note_la.mp3` - La (A)
- ✅ `note_si.mp3` - Si (B)

## 🎨 Customization

### Change Piano Colors for Different Levels:
Create new color theme function in `PianoKeyboard.kt`:

```kotlin
fun getSpiderManPianoNotes(): List<PianoNote> {
    return listOf(
        PianoNote("Do", "C", R.raw.note_do, Color(0xFFE53935)), // Red
        PianoNote("Ré", "D", R.raw.note_re, Color(0xFF1565C0)), // Blue
        // ... etc
    )
}
```

### Change Notation System:
The system supports both:
- **Solfège**: Do, Ré, Mi, Fa, Sol, La, Si (default)
- **Letter**: C, D, E, F, G, A, B

## 🚀 Navigation Flow

```
HomeScreen
    ↓ (Click Level 1)
LevelOneScreen
    ↓ (Click "Play on App Piano")
PianoPracticeScreen (Piano gameplay)
    ↓ (Back button)
LevelOneScreen
```

## 📱 Features

- ✅ **Multi-touch support**: Play multiple keys simultaneously
- ✅ **Visual feedback**: Keys light up when pressed
- ✅ **Sound playback**: Authentic piano notes
- ✅ **Scoring system**: Track player progress
- ✅ **Sequence challenges**: Follow target note patterns
- ✅ **Reusable**: Same code for all 6 levels
- ✅ **Batman theme**: Level 1 specific styling

## 🔧 Technical Details

- **SoundPool**: Max 10 simultaneous streams
- **Touch detection**: `detectTapGestures` for press/release
- **State management**: Kotlin StateFlow
- **Animation**: Smooth press feedback
- **Resource IDs**: Compile-time safe with R.raw.*

## 📝 Next Steps for Your Colleague

Your colleague can easily add more levels by:
1. Adding new background images to `res/drawable/`
2. Calling `PianoPracticeScreen` with level-specific parameters
3. Creating new color themes in `PianoKeyboard.kt` (optional)
4. Adding level-specific game logic in `PianoViewModel.kt`

## 💡 Best Practices

- **Sound initialization**: Done in `MainActivity.onCreate()` via `SoundManager.init()`
- **Memory management**: `PianoSoundManager` is singleton - no manual release needed in most cases
- **Performance**: SoundPool provides low-latency audio perfect for interactive piano
- **Reusability**: Separation of concerns allows easy customization per level
