package com.example.sanatorio.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Highlight
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renderizador en primera persona del asset 3D 'investigator_hands' sosteniendo
 * el nuevo modelo 'vintage_flashlight' (Linterna de época con caja de batería roja,
 * reflector pulido y haz cónico de luz incandescente con efecto de encendido).
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

    // Animación suave de pulsación del pulgar en el interruptor mecánico de la linterna vintage
    val thumbPressY by animateFloatAsState(
        targetValue = if (isLightOn) 6f else 0f,
        animationSpec = tween(durationMillis = 130),
        label = "thumb_switch_press"
    )

    // Efecto de encendido: calentamiento del filamento incandescente de 1984
    val filamentWarmup by animateFloatAsState(
        targetValue = if (isLightOn) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 240),
        label = "filament_warmup"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("first_person_hand_container")
    ) {
        // Lienzo de dibujo de la mano y la linterna vintage
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("first_person_hand_canvas")
                .clickable { onToggleFlashlight() }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // Anclaje de la mano y linterna en el cuadrante inferior derecho
            val baseX = canvasW * 0.73f + swayX + trembleX
            val baseY = canvasH * 0.77f + swayY + trembleY

            drawVintageFlashlightAndHand(
                baseX = baseX,
                baseY = baseY,
                isLightOn = isLightOn,
                filamentWarmup = filamentWarmup,
                isLowSanity = isLowSanity,
                thumbPressY = thumbPressY
            )
        }

        // Badge indicador del estado del rig y linterna vintage
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 96.dp)
                .testTag("hand_rig_status_badge"),
            shape = RoundedCornerShape(8.dp),
            color = HorrorDarkSurface.copy(alpha = 0.88f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isLowSanity) HorrorBloodRed.copy(alpha = 0.7f) else HorrorColdTeal.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isLightOn) Icons.Default.Highlight else Icons.Default.PanTool,
                    contentDescription = null,
                    tint = if (isLowSanity) HorrorBloodRed else if (isLightOn) Color(0xFFFFD43B) else HorrorColdTeal,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        isLowSanity -> "LINTERNA VINTAGE: TEMBLOR DE PÁNICO"
                        isInspecting -> "LINTERNA VINTAGE: INSPECCIONANDO"
                        isLightOn -> "LINTERNA VINTAGE: HAZ ACTIVO (1984)"
                        else -> "LINTERNA VINTAGE: APAGADA (CLICK PARA ENCENDER)"
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
 * Renderizado de la linterna 'vintage_flashlight' con guante de investigación 'investigator_hands'.
 * Refleja fielmente el modelo 3D descargado:
 * - Caja de batería roja desgastada de época con tornillería y marcas de óxido.
 * - Asa superior gruesa de baquelita negra y soporte reforzado.
 * - Reflector parabólico pulido de aluminio orientado hacia el frente con lente estriada.
 * - Cono volumétrico de iluminación lógica con cálida incandescencia y partículas de polvo.
 */
private fun DrawScope.drawVintageFlashlightAndHand(
    baseX: Float,
    baseY: Float,
    isLightOn: Boolean,
    filamentWarmup: Float,
    isLowSanity: Boolean,
    thumbPressY: Float
) {
    // -------------------------------------------------------------
    // 0. CONO VOLUMÉTRICO DE ILUMINACIÓN (Efecto de haz de luz en niebla)
    // -------------------------------------------------------------
    val reflectorOriginX = baseX - 125f
    val reflectorOriginY = baseY - 75f

    if (filamentWarmup > 0.05f) {
        // Haz volumétrico que viaja hacia el centro del campo visual
        val beamPath = Path().apply {
            moveTo(reflectorOriginX - 15f, reflectorOriginY - 35f)
            lineTo(-40f, -80f) // Hacia la esquina superior izquierda
            lineTo(size.width * 0.45f, size.height * 0.35f)
            lineTo(reflectorOriginX + 25f, reflectorOriginY + 15f)
            close()
        }

        drawPath(
            path = beamPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFFF9DB).copy(alpha = 0.32f * filamentWarmup),
                    Color(0xFFFFE066).copy(alpha = 0.16f * filamentWarmup),
                    Color(0xFFFF922B).copy(alpha = 0.04f * filamentWarmup),
                    Color.Transparent
                ),
                start = Offset(reflectorOriginX, reflectorOriginY),
                end = Offset(size.width * 0.15f, size.height * 0.15f)
            )
        )

        // Resplandor cálido directo sobre la lente
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFFFFF).copy(alpha = 0.85f * filamentWarmup),
                    Color(0xFFFFF3BF).copy(alpha = 0.55f * filamentWarmup),
                    Color(0xFFFFD43B).copy(alpha = 0.25f * filamentWarmup),
                    Color.Transparent
                ),
                center = Offset(reflectorOriginX - 8f, reflectorOriginY - 18f),
                radius = 140f
            ),
            radius = 140f,
            center = Offset(reflectorOriginX - 8f, reflectorOriginY - 18f)
        )
    }

    // -------------------------------------------------------------
    // 1. ANTEBRAZO CON MANGA FORENSE OSCURA (Bone_Forearm_Wrist)
    // -------------------------------------------------------------
    val forearmPath = Path().apply {
        moveTo(baseX + 190f, size.height + 60f)
        lineTo(baseX + 85f, baseY + 65f)
        lineTo(baseX - 25f, baseY + 105f)
        lineTo(baseX - 55f, size.height + 60f)
        close()
    }
    drawPath(
        path = forearmPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF1B232A), Color(0xFF11171D), Color(0xFF090D10)),
            start = Offset(baseX + 50f, baseY + 50f),
            end = Offset(baseX + 140f, size.height)
        )
    )

    // -------------------------------------------------------------
    // 2. MODELO VINTAGE FLASHLIGHT: CAJA DE BATERÍA ROJA DESGASTADA
    // -------------------------------------------------------------
    // Cuerpo rectangular característico de linternas industriales de 1984
    val bodyAngle = -0.42f
    val bodyWidth = 150f
    val bodyHeight = 54f
    val bodyLeft = baseX - 45f
    val bodyTop = baseY + 10f

    // Sombra proyectada del cuerpo
    drawRoundRect(
        color = Color(0xFF080B0E),
        topLeft = Offset(bodyLeft - 8f, bodyTop - 4f),
        size = Size(bodyWidth + 8f, bodyHeight + 8f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Caja roja de batería metálica con desgaste
    val redVintagePrimary = Color(0xFFA82323)
    val redVintageDark = Color(0xFF6B1414)
    val redVintageHighlight = Color(0xFFC93B3B)

    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(redVintageHighlight, redVintagePrimary, redVintageDark),
            start = Offset(bodyLeft, bodyTop),
            end = Offset(bodyLeft, bodyTop + bodyHeight)
        ),
        topLeft = Offset(bodyLeft, bodyTop),
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = CornerRadius(5f, 5f)
    )

    // Franja estriada de refuerzo y remaches de latón
    drawRoundRect(
        color = Color(0xFF4A1010),
        topLeft = Offset(bodyLeft + 18f, bodyTop + 6f),
        size = Size(bodyWidth - 36f, bodyHeight - 12f),
        cornerRadius = CornerRadius(3f, 3f),
        style = Stroke(width = 2.5f)
    )

    // Remaches de esquinas
    listOf(
        Offset(bodyLeft + 10f, bodyTop + 10f),
        Offset(bodyLeft + bodyWidth - 10f, bodyTop + 10f),
        Offset(bodyLeft + 10f, bodyTop + bodyHeight - 10f),
        Offset(bodyLeft + bodyWidth - 10f, bodyTop + bodyHeight - 10f)
    ).forEach { rivetPos ->
        drawCircle(color = Color(0xFFD4AF37), radius = 3.5f, center = rivetPos)
        drawCircle(color = Color(0xFF3B2F04), radius = 3.5f, center = rivetPos, style = Stroke(1f))
    }

    // -------------------------------------------------------------
    // 3. ASA SUPERIOR DE BAQUELITA NEGRA (Chunky black handle)
    // -------------------------------------------------------------
    val handlePath = Path().apply {
        moveTo(bodyLeft + 25f, bodyTop)
        lineTo(bodyLeft + 20f, bodyTop - 32f)
        lineTo(bodyLeft + bodyWidth - 30f, bodyTop - 32f)
        lineTo(bodyLeft + bodyWidth - 35f, bodyTop)
    }
    drawPath(
        path = handlePath,
        color = Color(0xFF1E2327),
        style = Stroke(width = 16f, cap = StrokeCap.Round)
    )
    drawPath(
        path = handlePath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF374151), Color(0xFF111827)),
            start = Offset(bodyLeft, bodyTop - 35f),
            end = Offset(bodyLeft, bodyTop)
        ),
        style = Stroke(width = 11f, cap = StrokeCap.Round)
    )

    // -------------------------------------------------------------
    // 4. REFLECTOR PARABÓLICO PULIDO Y BISEL FRONTAL (Polished Reflector)
    // -------------------------------------------------------------
    val headConePath = Path().apply {
        moveTo(bodyLeft + 10f, bodyTop - 6f)
        lineTo(reflectorOriginX + 22f, reflectorOriginY + 28f)
        lineTo(reflectorOriginX - 16f, reflectorOriginY - 26f)
        lineTo(bodyLeft - 18f, bodyTop - 24f)
        close()
    }
    drawPath(
        path = headConePath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFCBD5E1),
                Color(0xFF64748B),
                Color(0xFF334155),
                Color(0xFF1E293B)
            ),
            start = Offset(bodyLeft, bodyTop),
            end = Offset(reflectorOriginX, reflectorOriginY)
        )
    )

    // Anillo de aluminio pulido / Bisel estriado exterior
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFF1F5F9), Color(0xFF64748B), Color(0xFF0F172A)),
            start = Offset(reflectorOriginX - 25f, reflectorOriginY - 35f),
            end = Offset(reflectorOriginX + 25f, reflectorOriginY + 35f)
        ),
        radius = 28f,
        center = Offset(reflectorOriginX, reflectorOriginY),
        style = Stroke(width = 6f)
    )

    // Cristal estriado de la lente
    val lensBulbCenter = Offset(reflectorOriginX - 2f, reflectorOriginY - 4f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = if (filamentWarmup > 0.05f) {
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFFFE066),
                    Color(0xFFD97706),
                    Color(0xFF451A03)
                )
            } else {
                listOf(
                    Color(0xFF64748B),
                    Color(0xFF334155),
                    Color(0xFF0F172A)
                )
            },
            center = lensBulbCenter,
            radius = 24f
        ),
        radius = 24f,
        center = lensBulbCenter
    )

    // Bulbo de filamento de tungsteno visible
    drawCircle(
        color = if (filamentWarmup > 0.05f) Color(0xFFFFFFFF) else Color(0xFF94A3B8),
        radius = 4.5f,
        center = lensBulbCenter
    )

    // -------------------------------------------------------------
    // 5. INTERRUPTOR MECÁNICO DESLIZANTE DE PALANCA
    // -------------------------------------------------------------
    val switchBaseX = bodyLeft + 35f
    val switchBaseY = bodyTop - 8f
    drawRoundRect(
        color = Color(0xFF1F2937),
        topLeft = Offset(switchBaseX, switchBaseY),
        size = Size(24f, 12f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    val sliderY = switchBaseY + thumbPressY - 2f
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = if (isLightOn) {
                listOf(Color(0xFF22C55E), Color(0xFF15803D))
            } else {
                listOf(Color(0xFF9CA3AF), Color(0xFF4B5563))
            },
            start = Offset(switchBaseX, sliderY),
            end = Offset(switchBaseX + 16f, sliderY + 8f)
        ),
        topLeft = Offset(switchBaseX + 4f, sliderY),
        size = Size(16f, 8f),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // -------------------------------------------------------------
    // 6. GUANTE DE INVESTIGACIÓN FORENSE (Bone_Hand_Palm & Dedos)
    // -------------------------------------------------------------
    val leatherMain = Color(0xFF423023)
    val leatherHighlight = Color(0xFF70523C)
    val leatherShadow = Color(0xFF241A12)

    // Palma y base de la mano sujetando el cuerpo de la linterna
    val palmPath = Path().apply {
        moveTo(baseX + 80f, baseY + 65f)
        lineTo(baseX + 18f, baseY - 8f)
        lineTo(baseX - 40f, baseY + 18f)
        lineTo(baseX + 15f, baseY + 98f)
        close()
    }
    drawPath(
        path = palmPath,
        brush = Brush.linearGradient(
            colors = listOf(leatherHighlight, leatherMain, leatherShadow),
            start = Offset(baseX - 25f, baseY - 10f),
            end = Offset(baseX + 55f, baseY + 90f)
        )
    )

    // Pulgar descansando sobre el interruptor (Bone_Thumb)
    val thumbStartX = baseX + 25f
    val thumbStartY = baseY + 12f
    val thumbEndX = switchBaseX + 12f
    val thumbEndY = sliderY + 2f

    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(leatherHighlight, leatherMain),
            start = Offset(thumbStartX, thumbStartY),
            end = Offset(thumbEndX, thumbEndY)
        ),
        start = Offset(thumbStartX, thumbStartY),
        end = Offset(thumbEndX, thumbEndY),
        strokeWidth = 24f,
        cap = StrokeCap.Round
    )
    drawCircle(
        color = leatherShadow,
        radius = 8f,
        center = Offset(thumbEndX, thumbEndY)
    )

    // Cuatro dedos envolviendo la base de la linterna (Índice, Medio, Anular, Meñique)
    val fingerGripPoints = listOf(
        Pair(Offset(baseX - 30f, baseY - 10f), Offset(baseX - 58f, baseY + 20f)), // Índice
        Pair(Offset(baseX - 6f, baseY + 8f), Offset(baseX - 34f, baseY + 38f)),   // Medio
        Pair(Offset(baseX + 18f, baseY + 24f), Offset(baseX - 10f, baseY + 54f)), // Anular
        Pair(Offset(baseX + 42f, baseY + 40f), Offset(baseX + 14f, baseY + 70f))  // Meñique
    )

    fingerGripPoints.forEach { (fStart, fEnd) ->
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(leatherHighlight, leatherMain, leatherShadow),
                start = fStart,
                end = fEnd
            ),
            start = fStart,
            end = fEnd,
            strokeWidth = 22f,
            cap = StrokeCap.Round
        )

        // Nudillo articulado con costura de cuero
        drawCircle(
            color = if (filamentWarmup > 0.05f) Color(0xFF8C674B) else leatherMain,
            radius = 10.5f,
            center = fStart
        )
        drawCircle(
            color = leatherShadow,
            radius = 10.5f,
            center = fStart,
            style = Stroke(width = 2f)
        )
    }

    // Reflejo especular dorado en el cuero del guante cuando la linterna alumbra
    if (filamentWarmup > 0.05f) {
        drawLine(
            color = Color(0xFFFFD43B).copy(alpha = 0.5f * filamentWarmup),
            start = Offset(baseX - 32f, baseY - 12f),
            end = Offset(baseX - 44f, baseY + 2f),
            strokeWidth = 5f,
            cap = StrokeCap.Round
        )
    }
}
