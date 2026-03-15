package com.example.codedroid.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random

// State hewan peliharaan
enum class PetState {
    IDLE_RIGHT,
    IDLE_LEFT,
    WALKING_RIGHT,
    WALKING_LEFT,
    SLEEPING,
    SURPRISED
}

@Composable
fun VirtualPet(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.toFloat() - 40f // padding

    // State Posisi
    var currentX by remember { mutableStateOf(screenWidth / 2f) }
    var targetX by remember { mutableStateOf(currentX) }

    // State Karakter & Mood
    var petMood by remember { mutableStateOf(PetState.IDLE_RIGHT) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogText by remember { mutableStateOf("") }
    
    // Smooth animation untuk perpindahan letak
    val animatedX by animateFloatAsState(
        targetValue = targetX,
        animationSpec = tween(durationMillis = 2000, easing = LinearEasing),
        label = "PetMovement"
    )

    // AI Logic (State Machine sangat sederhana)
    LaunchedEffect(Unit) {
        while (true) {
            val moodRoll = Random.nextInt(100)

            if (moodRoll < 30) {
                // Sleep
                petMood = PetState.SLEEPING
                delay(4000)
            } else if (moodRoll < 70) {
                // Berjalan ke posisi random
                val arahKanan = Random.nextBoolean()
                petMood = if (arahKanan) PetState.WALKING_RIGHT else PetState.WALKING_LEFT
                
                // Set target X (mencegah jalan ke luar layar)
                val distance = Random.nextFloat() * 100f + 50f
                targetX = if (arahKanan) {
                    (currentX + distance).coerceAtMost(screenWidth - 10f)
                } else {
                    (currentX - distance).coerceAtLeast(10f)
                }
                
                delay(2000) // Waktu sesuai durasi tween x
                currentX = targetX
                petMood = if (arahKanan) PetState.IDLE_RIGHT else PetState.IDLE_LEFT
                delay(1500) // Diam sejenak
            } else {
                // Diam
                val isRight = petMood == PetState.WALKING_RIGHT || petMood == PetState.IDLE_RIGHT
                petMood = if (isRight) PetState.IDLE_RIGHT else PetState.IDLE_LEFT
                delay(3000)
            }
        }
    }

    // Mendapatkan bentuk Emoji berdasarkan state
    val petEmoji = when (petMood) {
        PetState.WALKING_RIGHT -> "🐈" 
        PetState.WALKING_LEFT -> "🐈"
        PetState.IDLE_RIGHT -> "🐱"
        PetState.IDLE_LEFT -> "🐱"
        PetState.SLEEPING -> "😴"
        PetState.SURPRISED -> "🙀"
    }

    // Tampilkan pada layar
    Box(modifier = modifier.fillMaxWidth().height(60.dp)) {
        Column(
            modifier = Modifier
                .offset(x = animatedX.dp, y = 0.dp)
                .wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dialog / Chat Bubble
            if (petMood == PetState.SLEEPING) {
                Text("zZz..", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 2.dp).offset(x = 10.dp))
            } else if (showDialog && dialogText.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(dialogText, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            // The Pet Render
            Text(
                text = petEmoji,
                fontSize = 28.sp,
                modifier = Modifier
                    .offset(x = if (petMood == PetState.WALKING_LEFT || petMood == PetState.IDLE_LEFT) (-2).dp else 0.dp) // Efek kecil untuk hadap kiri (walau standar emoji sering kanan)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        // Reaksi Klik
                        petMood = PetState.SURPRISED
                        showDialog = true
                        dialogText = listOf("Meow!", "Purr..", "❤️", "Ngoding terus?", "Feed me!").random()
                    }
            )
        }
    }

    // Timer hapus dialog
    LaunchedEffect(showDialog) {
        if (showDialog) {
            delay(2500)
            showDialog = false
            dialogText = ""
            // Kembalikan ke normal
            petMood = PetState.IDLE_RIGHT
        }
    }
}
