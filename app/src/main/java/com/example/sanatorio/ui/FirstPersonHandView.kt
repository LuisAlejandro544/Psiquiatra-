package com.example.sanatorio.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanatorio.model.GameState
import com.example.ui.theme.HorrorBloodRed
import com.example.ui.theme.HorrorColdTeal
import com.example.ui.theme.HorrorColdWhite
import com.example.ui.theme.HorrorDarkSurface
import com.example.ui.theme.HorrorSurfaceVariant
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renderizador en primera persona del asset 3D 'investigator_hands' (Manos y Linterna).
 * Muestra el brazo, muñeca, palma y dedos articulados sujetando la linterna pesada de aluminio.
 * Reacciona dinámicamente al paso del jugador (sway), estado de la linterna y temblores de cordura.
 */
@Composable
fun FirstPersonHandView(
    gameState: GameState,
    onToggleFlashlight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLightOn = gameState.isFlashlightOn
    val isLowSanity = gameState.sanity < 35f
    val isInspecting = gameState.targetDecoration != null

    // Balanceo natural de las manos al caminar
    val bobOffset = gameState.headBob
    val swayX = sin(bobOffset.toDouble()).toFloat() * 16f
    val swayY = abs(cos(bobOffset.toDouble())).toFloat() * 12f

    // Temblor de pánico por pérdida de cordura (Anim_Tremble_Insanity)
    val timeMillis = System.currentTimeMillis()
    val trembleX = if (isLowSanity) (sin(timeMillis * 0.04) * 6f).toFloat() else 0f
    val trembleY = if (isLowSanity) (cos(timeMillis * 0.035) * 5f).toFloat() else 0f

    // Animación suave de pulsación del pulgar en el interruptor mecánico
    val thumbPressY by animateFloatAsState(
        targetValue = if (isLightOn) 4f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "thumb_switch_press"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("first_person_hand_container")
    ) {
        // Lienzo de dibujo de la mano y linterna en perspectiva primera persona (esquina inferior derecha)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("first_person_hand_canvas")
                .clickable { onToggleFlashlight() }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // Ancla de la mano del investigador (esquina inferior derecha)
            val baseX = canvasW * 0.72f + swayX + trembleX
            val baseY = canvasH * 0.76f + swayY + trembleY

            drawInvestigatorHandAndFlashlight(
                baseX = baseX,
                baseY = baseY,
                isLightOn = isLightOn,
                isLowSanity = isLowSanity,
                thumbPressY = thumbPressY
            )
        }

        // Badge indicador sutil del estado del modelo de manos en tiempo real
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp)
                .testTag("hand_rig_status_badge"),
            shape = RoundedCornerShape(8.dp),
            color = HorrorDarkSurface.copy(alpha = 0.85f),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isLowSanity) HorrorBloodRed.copy(alpha = 0.6f) else HorrorColdTeal.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PanTool,
                    contentDescription = null,
                    tint = if (isLowSanity) HorrorBloodRed else HorrorColdTeal,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        isLowSanity -> "MANO 3D: TEMBLOR (CORDURA CRÍTICA)"
                        isInspecting -> "MANO 3D: EXAMINAR EVIDENCIA"
                        else -> "MANO 3D: AGARRE ACTIVO (8 HUESOS)"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = HorrorColdWhite
                )
            }
        }
    }
}

/**
 * Dibuja la geometría estilizada de la mano con guante de investigación forense
 * (Bone_Forearm_Wrist, Bone_Hand_Palm, Bone_Thumb, Bone_Index, Bone_Middle, Bone_Ring_Pinky)
 * y el cuerpo metálico de la linterna industrial.
 */
private fun DrawScope.drawInvestigatorHandAndFlashlight(
    baseX: Float,
    baseY: Float,
    isLightOn: Boolean,
    isLowSanity: Boolean,
    thumbPressY: Float
) {
    // 1. Resplandor volumétrico de la lente de la linterna (si está encendida)
    if (isLightOn) {
        val lensCenterX = baseX - 110f
        val lensCenterY = baseY - 80f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFF9DB).copy(alpha = 0.75f),
                    Color(0xFFFFD43B).copy(alpha = 0.35f),
                    Color(0xFFFF922B).copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = Offset(lensCenterX, lensCenterY),
                radius = 160f
            ),
            radius = 160f,
            center = Offset(lensCenterX, lensCenterY)
        )
    }

    // 2. Antebrazo del investigador (Bone_Forearm_Wrist - Tela oscura / Manga de abrigo)
    val forearmPath = Path().apply {
        moveTo(baseX + 180f, size.height + 40f)
        lineTo(baseX + 80f, baseY + 60f)
        lineTo(baseX - 20f, baseY + 90f)
        lineTo(baseX - 40f, size.height + 40f)
        close()
    }
    drawPath(
        path = forearmPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF182026), Color(0xFF0F151A)),
            start = Offset(baseX + 50f, baseY + 50f),
            end = Offset(baseX + 120f, size.height)
        )
    )

    // 3. Cilindro principal de la Linterna (Aluminio estriado con bisel de latón)
    val flashlightAngleRad = -0.45f
    val flLength = 230f
    val flRadius = 24f

    val flStartX = baseX + 70f
    val flStartY = baseY + 45f
    val flEndX = baseX - 110f
    val flEndY = baseY - 80f

    // Tubo de aluminio
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFF4A5568),
                Color(0xFFCBD5E0),
                Color(0xFF2D3748),
                Color(0xFF1A202C)
            ),
            start = Offset(flStartX, flStartY - 20f),
            end = Offset(flStartX, flStartY + 20f)
        ),
        start = Offset(flStartX, flStartY),
        end = Offset(flEndX, flEndY),
        strokeWidth = flRadius * 2,
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )

    // Cabeza reflectora cónica de la linterna (Bisel frontal)
    val headPath = Path().apply {
        moveTo(flEndX + 20f, flEndY + 12f)
        lineTo(flEndX - 18f, flEndY - 14f)
        lineTo(flEndX - 2f, flEndY - 36f)
        lineTo(flEndX + 36f, flEndY - 10f)
        close()
    }
    drawPath(
        path = headPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFE2E8F0), Color(0xFF4A5568)),
            start = Offset(flEndX + 30f, flEndY),
            end = Offset(flEndX - 10f, flEndY - 20f)
        )
    )

    // Lente de cristal frontal
    drawCircle(
        color = if (isLightOn) Color(0xFFFFF9DB) else Color(0xFF2D3748),
        radius = 16f,
        center = Offset(flEndX - 10f, flEndY - 25f)
    )
    drawCircle(
        color = if (isLightOn) Color(0xFFFFD43B) else Color(0xFF1A202C),
        radius = 16f,
        center = Offset(flEndX - 10f, flEndY - 25f),
        style = Stroke(width = 3f)
    )

    // 4. Interruptor mecánico deslizante
    val switchX = baseX - 15f
    val switchY = baseY - 15f + thumbPressY
    drawRoundRect(
        color = if (isLightOn) HorrorColdTeal else Color(0xFF718096),
        topLeft = Offset(switchX, switchY),
        size = Size(20f, 10f),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // 5. Palma de la mano con guante de investigación (Bone_Hand_Palm - Cuero marrón gastado / Textura)
    val palmColor = Color(0xFF4A3728) // Cuero marrón de trabajo/jardinería
    val palmHighlight = Color(0xFF78593E)
    val palmShadow = Color(0xFF2C1F16)

    val palmPath = Path().apply {
        moveTo(baseX + 75f, baseY + 60f)
        lineTo(baseX + 15f, baseY - 5f)
        lineTo(baseX - 45f, baseY + 15f)
        lineTo(baseX + 10f, baseY + 95f)
        close()
    }
    drawPath(
        path = palmPath,
        brush = Brush.linearGradient(
            colors = listOf(palmHighlight, palmColor, palmShadow),
            start = Offset(baseX - 20f, baseY - 10f),
            end = Offset(baseX + 50f, baseY + 90f)
        )
    )

    // 6. Pulgar presionando el interruptor (Bone_Thumb_Metacarpal & Bone_Thumb_Phalanx)
    val thumbBaseX = baseX + 25f
    val thumbBaseY = baseY + 10f
    val thumbTipX = switchX + 10f
    val thumbTipY = switchY - 4f

    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(palmHighlight, palmColor),
            start = Offset(thumbBaseX, thumbBaseY),
            end = Offset(thumbTipX, thumbTipY)
        ),
        start = Offset(thumbBaseX, thumbBaseY),
        end = Offset(thumbTipX, thumbTipY),
        strokeWidth = 22f,
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )

    // Uña / costura del pulgar
    drawCircle(
        color = palmShadow,
        radius = 8f,
        center = Offset(thumbTipX, thumbTipY)
    )

    // 7. Dedos cerrados alrededor del tubo (Bone_Index, Bone_Middle, Bone_Ring_Pinky)
    val fingerColors = listOf(palmHighlight, palmColor, palmShadow)
    val fingers = listOf(
        Pair(Offset(baseX - 35f, baseY - 15f), Offset(baseX - 60f, baseY + 12f)), // Índice
        Pair(Offset(baseX - 10f, baseY + 2f), Offset(baseX - 35f, baseY + 30f)),  // Medio
        Pair(Offset(baseX + 15f, baseY + 18f), Offset(baseX - 10f, baseY + 46f)), // Anular
        Pair(Offset(baseX + 40f, baseY + 34f), Offset(baseX + 15f, baseY + 62f))  // Meñique
    )

    fingers.forEachIndexed { idx, (fStart, fEnd) ->
        drawLine(
            brush = Brush.linearGradient(
                colors = fingerColors,
                start = fStart,
                end = fEnd
            ),
            start = fStart,
            end = fEnd,
            strokeWidth = 20f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        // Nudillos articulados (Joints)
        drawCircle(
            color = if (isLightOn) Color(0xFFB0825B) else palmColor,
            radius = 10f,
            center = fStart
        )
        drawCircle(
            color = palmShadow,
            radius = 10f,
            center = fStart,
            style = Stroke(width = 2f)
        )
    }

    // 8. Reflejo especular en los dedos si la linterna está encendida
    if (isLightOn) {
        drawLine(
            color = Color(0xFFFFD43B).copy(alpha = 0.45f),
            start = Offset(baseX - 35f, baseY - 15f),
            end = Offset(baseX - 45f, baseY - 2f),
            strokeWidth = 4f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
