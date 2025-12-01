package com.pianokids.game.utils.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.pianokids.game.data.models.Sublevel

@Composable
fun SublevelSelectionDialog(
    sublevels: List<Sublevel>,
    onDismiss: () -> Unit,
    onPlay: (Sublevel, PianoMode) -> Unit
) {
    var selectedMode by remember { mutableStateOf<PianoMode?>(null) }
    var selectedSublevel by remember { mutableStateOf<Sublevel?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(12.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                //---------------------------------------------------------
                // TITLE
                //---------------------------------------------------------
                Text(
                    text = "Choose Sublevel",
                    fontSize = 26.sp,
                    color = Color(0xFF2C3E50),
                    fontWeight = FontWeight.Bold
                )

                Divider(color = Color(0xFF667EEA), thickness = 1.dp)

                //---------------------------------------------------------
                // MODE SELECTOR
                //---------------------------------------------------------
                Text(
                    text = "Choose Your Instrument",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2C3E50)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    ModeButton(
                        label = "App Piano",
                        emoji = "🎹",
                        isSelected = selectedMode == PianoMode.APP_PIANO,
                        selectedColor = Color(0xFF667EEA),
                        onClick = { selectedMode = PianoMode.APP_PIANO }
                    )

                    ModeButton(
                        label = "My Piano",
                        emoji = "🎼",
                        isSelected = selectedMode == PianoMode.REAL_PIANO,
                        selectedColor = Color(0xFF00D9FF),
                        onClick = { selectedMode = PianoMode.REAL_PIANO }
                    )
                }

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFF667EEA).copy(alpha = 0.2f),
                    thickness = 1.dp
                )

                //---------------------------------------------------------
                // SUBLEVEL SELECTOR (NEW LOGIC)
                //---------------------------------------------------------
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    itemsIndexed(sublevels) { index, sublevel ->

                        val unlocked = sublevel.unlocked
                        val earnedStars = sublevel.starsEarned
                        val isSelected = selectedSublevel?._id == sublevel._id

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            // --- Circle button ---
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            !unlocked -> Color.Gray.copy(alpha = 0.4f)
                                            isSelected -> Color(0xFF667EEA)
                                            else -> Color(0xFF00D9FF).copy(alpha = 0.2f)
                                        }
                                    )
                                    .border(
                                        width = 3.dp,
                                        color = when {
                                            !unlocked -> Color.Gray
                                            isSelected -> Color(0xFF667EEA)
                                            else -> Color(0xFF00D9FF)
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable(enabled = unlocked) {
                                        selectedSublevel = sublevel
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (!unlocked) Color.DarkGray else Color.Black
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            // --- Stars earned display ---
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(sublevel.maxStars) { index ->

                                    val isFilled = index < sublevel.starsEarned
                                    val isUnlocked = sublevel.unlocked

                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = when {
                                            isFilled -> Color(0xFFFFD700)                      // ⭐ GOLD
                                            isUnlocked -> Color.LightGray.copy(alpha = 0.4f)  // hollow star
                                            else -> Color.Gray.copy(alpha = 0.2f)             // locked
                                        },
                                        modifier = Modifier
                                            .size(if (isFilled) 18.dp else 14.dp)
                                    )
                                }
                            }


                        }
                    }
                }

                //---------------------------------------------------------
                // PLAY BUTTON
                //---------------------------------------------------------
                Button(
                    onClick = {
                        val sub = selectedSublevel
                        val mode = selectedMode
                        if (sub != null && mode != null) {
                            onPlay(sub, mode)
                        }
                    },
                    enabled = selectedMode != null &&
                            selectedSublevel != null &&
                            (selectedSublevel?.unlocked == true),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        "START MISSION 🚀",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                //---------------------------------------------------------
                // CANCEL BUTTON
                //---------------------------------------------------------
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF667EEA)
                    )
                ) {
                    Text("Cancel", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


//-------------------------------------------------------------
// REUSABLE MODE BUTTON
//-------------------------------------------------------------
@Composable
fun ModeButton(
    label: String,
    emoji: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring()
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .size(width = 140.dp, height = 110.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) selectedColor else Color.White
        ),
        border = BorderStroke(
            width = 3.dp,
            color = if (isSelected) selectedColor else selectedColor.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(emoji, fontSize = 40.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isSelected) Color.White else selectedColor
            )
        }
    }
}
