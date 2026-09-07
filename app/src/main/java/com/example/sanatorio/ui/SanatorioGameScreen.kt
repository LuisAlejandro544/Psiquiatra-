package com.example.sanatorio.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.sanatorio.engine.gl.SanatorioGLSurfaceView
import com.example.sanatorio.engine.HorrorAudioSynthesizer
import com.example.sanatorio.model.AsylumMap
import com.example.sanatorio.model.Decoration
import com.example.sanatorio.model.GameState
import com.example.ui.theme.HorrorAmber
import com.example.ui.theme.HorrorBloodRed
import com.example.ui.theme.HorrorColdTeal
import com.example.ui.theme.HorrorColdWhite
import com.example.ui.theme.HorrorDarkBackground
import com.example.ui.theme.HorrorSurfaceVariant
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun SanatorioGameScreen() {
    val map = remember { AsylumMap() }
    val gameState = remember { GameState() }
    val audioSynth = remember { HorrorAudioSynthesizer() }
    val random = remember { Random() }

    // Joystick & Touch controls state
    var joystickDeltaX by remember { mutableFloatStateOf(0f) }
    var joystickDeltaY by remember { mutableFloatStateOf(0f) }
    var isMoving by remember { mutableStateOf(false) }

    // Audio & Native lifecycle
    DisposableEffect(Unit) {
        try {
            com.example.sanatorio.nativebridge.NativeEngineBridge.initNativeEngine()
        } catch (e: Throwable) {
            // Log fallback
        }
        audioSynth.start()
        onDispose {
            audioSynth.stop()
        }
    }

    // Flicker & Atmosphere Game Loop
    LaunchedEffect(Unit) {
        var tickCounter = 0
        var bobPhase = 0.0

        while (isActive) {
            tickCounter++

            // Fluorescent Flicker Logic
            // Occasional electrical drops & flickering
            if (random.nextFloat() < 0.15f) {
                // Short glitch
                gameState.overheadLightFlicker = (random.nextFloat() * 0.4f)
                audioSynth.isFlickeringHum = true
            } else if (random.nextFloat() < 0.03f) {
                // Temporary blackout of 600ms
                gameState.isBlackout = true
                gameState.overheadLightFlicker = 0.0f
                audioSynth.isFlickeringHum = false
                delay(400)
                gameState.isBlackout = false
            } else {
                // Stable hum
                gameState.overheadLightFlicker = 0.85f + (random.nextFloat() * 0.15f)
                audioSynth.isFlickeringHum = false
            }

            // Flashlight slight micro-flicker
            if (gameState.isFlashlightOn) {
                if (random.nextFloat() < 0.05f) {
                    gameState.flashlightFlickerFactor = 0.6f + (random.nextFloat() * 0.35f)
                } else {
                    gameState.flashlightFlickerFactor = 1.0f
                }
            }

            // Sanity drain / recover via Rust Core Engine
            try {
                gameState.sanity = com.example.sanatorio.nativebridge.NativeEngineBridge.calculateSanityRust(
                    currentSanity = gameState.sanity,
                    flashlightOn = gameState.isFlashlightOn,
                    flickerLevel = gameState.overheadLightFlicker,
                    deltaTimeSec = 0.028f
                )
            } catch (e: Throwable) {
                if (!gameState.isFlashlightOn && gameState.overheadLightFlicker < 0.3f) {
                    gameState.sanity = (gameState.sanity - 0.2f).coerceAtLeast(15f)
                } else {
                    gameState.sanity = (gameState.sanity + 0.15f).coerceAtMost(100f)
                }
            }

            // Movement Processing
            if (isMoving && (joystickDeltaX != 0f || joystickDeltaY != 0f)) {
                val moveSpeed = 0.048
                val len = sqrt(joystickDeltaX * joystickDeltaX + joystickDeltaY * joystickDeltaY)
                if (len > 0.01f) {
                    val normX = joystickDeltaX / len
                    val normY = joystickDeltaY / len

                    // forward vector: dirX, dirY
                    // right vector: -dirY, dirX
                    val moveForward = -normY * moveSpeed
                    val moveStrafe = normX * moveSpeed

                    val newX = gameState.playerX + (gameState.dirX * moveForward) + (-gameState.dirY * moveStrafe)
                    val newY = gameState.playerY + (gameState.dirY * moveForward) + (gameState.dirX * moveStrafe)

                    // Collision check with margin
                    val radius = 0.25
                    if (!map.isWall(newX + radius, gameState.playerY) &&
                        !map.isWall(newX - radius, gameState.playerY)
                    ) {
                        gameState.playerX = newX
                    }
                    if (!map.isWall(gameState.playerX, newY + radius) &&
                        !map.isWall(gameState.playerX, newY - radius)
                    ) {
                        gameState.playerY = newY
                    }

                    // Update head bob
                    bobPhase += 0.28
                    gameState.headBob = (sin(bobPhase) * 0.5).toFloat()
                    audioSynth.isWalking = true
                }
            } else {
                gameState.headBob = 0f
                audioSynth.isWalking = false
            }

            // Update current room name
            gameState.currentRoom = map.getRoomName(gameState.playerX, gameState.playerY)

            delay(16) // 60 FPS ultra fluido para física y controles táctiles
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(HorrorDarkBackground)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // 1. 3D VIEWPORT HARDWARE ACCELERATED (OpenGL ES 3.2 - GLThread 60 FPS)
        AndroidView(
            factory = { context ->
                SanatorioGLSurfaceView(context, gameState, map)
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_3d_viewport")
        )

        // 2. HORROR ATMOSPHERIC OVERLAYS (Vignette & Dirt)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                        radius = 1200f
                    )
                )
        )

        // 2.1 FIRST-PERSON VIEWMODEL: MANOS 3D Y LINTERNA DEL INVESTIGADOR
        FirstPersonHandView(
            gameState = gameState,
            onToggleFlashlight = {
                gameState.isFlashlightOn = !gameState.isFlashlightOn
                audioSynth.playFlashlightClick()
            }
        )

        // 3. TOP HUD BAR (Room Name, Sanity, Battery, Sound, Notebook)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Room badge
            Surface(
                color = HorrorSurfaceVariant.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, HorrorColdTeal.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (gameState.isBlackout) HorrorBloodRed else HorrorColdTeal,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = gameState.currentRoom.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = HorrorColdWhite,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Center: Sanity indicator
            Surface(
                color = HorrorSurfaceVariant.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, HorrorBloodRed.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CORDURA:",
                        style = MaterialTheme.typography.labelSmall,
                        color = HorrorBloodRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF261014))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(gameState.sanity / 100f)
                                .height(8.dp)
                                .background(if (gameState.sanity > 35f) HorrorColdTeal else HorrorBloodRed)
                        )
                    }
                }
            }

            // Right: Notebook & Audio buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Notebook
                Button(
                    onClick = { gameState.isNotebookOpen = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HorrorColdTeal.copy(alpha = 0.2f),
                        contentColor = HorrorColdTeal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HorrorColdTeal.copy(alpha = 0.5f)),
                    modifier = Modifier.testTag("notebook_hud_button")
                ) {
                    Icon(Icons.Default.Book, contentDescription = "Cuaderno", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    val count = map.decorations.count { it.isInspected }
                    Text("CUADERNO ($count/5)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Sound toggle
                IconButton(
                    onClick = {
                        gameState.isSoundEnabled = !gameState.isSoundEnabled
                        audioSynth.isMuted = !gameState.isSoundEnabled
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(HorrorSurfaceVariant.copy(alpha = 0.85f), CircleShape)
                        .testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (gameState.isSoundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                        contentDescription = "Sonido",
                        tint = HorrorColdWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 4. CROSSHAIR & INTERACTION PROMPT
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val target = gameState.targetDecoration
            if (target != null) {
                // Focus crosshair on object
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.dp, HorrorAmber, CircleShape)
                )
            } else {
                // Subtle default dot
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(HorrorColdWhite.copy(alpha = 0.65f), CircleShape)
                )
            }

            // Contextual Interaction Button
            AnimatedVisibility(
                visible = target != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.offset(y = 55.dp)
            ) {
                target?.let { decor ->
                    Button(
                        onClick = {
                            gameState.activeInspection = decor
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HorrorAmber,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("investigate_action_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "INVESTIGAR: ${decor.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 5. TOUCH LOOK AREA (Right Half of Screen)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .fillMaxSize()
                .align(Alignment.CenterEnd)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()

                        // Horizontal turn (yaw)
                        val rotSpeed = -dragAmount.x * 0.0055
                        val oldDirX = gameState.dirX
                        gameState.dirX = gameState.dirX * cos(rotSpeed) - gameState.dirY * sin(rotSpeed)
                        gameState.dirY = oldDirX * sin(rotSpeed) + gameState.dirY * cos(rotSpeed)

                        val oldPlaneX = gameState.planeX
                        gameState.planeX = gameState.planeX * cos(rotSpeed) - gameState.planeY * sin(rotSpeed)
                        gameState.planeY = oldPlaneX * sin(rotSpeed) + gameState.planeY * cos(rotSpeed)

                        // Vertical pitch (tilt)
                        gameState.pitch = (gameState.pitch - dragAmount.y * 0.003f).coerceIn(-0.35f, 0.35f)
                    }
                }
                .testTag("touch_look_zone")
        )

        // 6. VIRTUAL JOYSTICK (Bottom Left)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 32.dp, bottom = 24.dp)
                .size(130.dp)
                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                .border(2.dp, HorrorColdTeal.copy(alpha = 0.35f), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isMoving = true },
                        onDragEnd = {
                            isMoving = false
                            joystickDeltaX = 0f
                            joystickDeltaY = 0f
                        },
                        onDragCancel = {
                            isMoving = false
                            joystickDeltaX = 0f
                            joystickDeltaY = 0f
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        val maxRadius = 50f
                        val newX = (joystickDeltaX + dragAmount.x).coerceIn(-maxRadius, maxRadius)
                        val newY = (joystickDeltaY + dragAmount.y).coerceIn(-maxRadius, maxRadius)
                        joystickDeltaX = newX
                        joystickDeltaY = newY
                    }
                }
                .testTag("virtual_joystick_container"),
            contentAlignment = Alignment.Center
        ) {
            // Center stick knob
            Box(
                modifier = Modifier
                    .offset { IntOffset(joystickDeltaX.roundToInt(), joystickDeltaY.roundToInt()) }
                    .size(50.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(HorrorColdTeal, HorrorColdTeal.copy(alpha = 0.6f))
                        ),
                        CircleShape
                    )
                    .border(2.dp, HorrorColdWhite.copy(alpha = 0.8f), CircleShape)
                    .testTag("virtual_joystick_knob")
            )
        }

        // 7. FLASHLIGHT BUTTON & BATTERY (Bottom Right)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 28.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Flashlight toggle button
            val isLightOn = gameState.isFlashlightOn
            IconButton(
                onClick = {
                    gameState.isFlashlightOn = !gameState.isFlashlightOn
                    audioSynth.playFlashlightClick()
                },
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        if (isLightOn) HorrorAmber else HorrorSurfaceVariant,
                        CircleShape
                    )
                    .border(
                        2.dp,
                        if (isLightOn) HorrorAmber.copy(alpha = 0.9f) else HorrorColdWhite.copy(alpha = 0.3f),
                        CircleShape
                    )
                    .testTag("flashlight_toggle_button")
            ) {
                Icon(
                    imageVector = if (isLightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    contentDescription = "Linterna",
                    tint = if (isLightOn) Color.Black else HorrorColdWhite,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isLightOn) "LINTERNA: ON" else "LINTERNA: OFF",
                style = MaterialTheme.typography.labelSmall,
                color = if (isLightOn) HorrorAmber else HorrorColdWhite.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        // 8. INSPECTION DIALOG (When examining an object)
        gameState.activeInspection?.let { decoration ->
            InspectionDialog(
                decoration = decoration,
                onDismiss = { gameState.activeInspection = null },
                onSaveToNotes = {
                    decoration.isInspected = true
                    gameState.activeInspection = null
                }
            )
        }

        // 9. NOTEBOOK DIALOG (When opening notebook)
        if (gameState.isNotebookOpen) {
            InvestigatorNotebookDialog(
                collectedDecorations = map.decorations.filter { it.isInspected },
                playerX = gameState.playerX,
                playerY = gameState.playerY,
                asylumMap = map,
                handRig = gameState.handRig,
                onDismiss = { gameState.isNotebookOpen = false }
            )
        }
    }
}
