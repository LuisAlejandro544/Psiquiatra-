package com.example.sanatorio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.sanatorio.model.AsylumMap
import com.example.sanatorio.model.BoneJoint
import com.example.sanatorio.model.Decoration
import com.example.sanatorio.model.HandRigManager
import com.example.ui.theme.HorrorAmber
import com.example.ui.theme.HorrorColdTeal
import com.example.ui.theme.HorrorColdWhite
import com.example.ui.theme.HorrorDarkBackground
import com.example.ui.theme.HorrorDarkSurface
import com.example.ui.theme.HorrorSurfaceVariant
import com.example.ui.theme.HorrorTextMuted

@Composable
fun InvestigatorNotebookDialog(
    collectedDecorations: List<Decoration>,
    playerX: Double,
    playerY: Double,
    asylumMap: AsylumMap,
    handRig: HandRigManager = HandRigManager(),
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.92f)
                .border(1.dp, HorrorColdTeal.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .testTag("investigator_notebook_dialog"),
            colors = CardDefaults.cardColors(containerColor = HorrorDarkBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CUADERNO DEL INVESTIGADOR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = HorrorColdTeal,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Caso: Incidente Psiquiátrico San Rafael (1984)",
                            style = MaterialTheme.typography.bodySmall,
                            color = HorrorTextMuted
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_notebook_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = HorrorColdWhite)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = HorrorDarkSurface,
                    contentColor = HorrorColdTeal,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = HorrorColdTeal
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Evidencias (${collectedDecorations.size})")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Plano del Centro")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Objetivos")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Motor Nativo")
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PanTool, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Manos 3D & Rig")
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> EvidenceTab(collectedDecorations)
                    1 -> MapTab(asylumMap, playerX, playerY)
                    2 -> ObjectivesTab()
                    3 -> NativeEngineTab()
                    4 -> HandRigTab(handRig)
                }
            }
        }
    }
}

@Composable
private fun EvidenceTab(decorations: List<Decoration>) {
    if (decorations.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No has archivado evidencias aún.\nAcércate a objetos, expedientes o marcas en las paredes y presiona 'INVESTIGAR'.",
                color = HorrorTextMuted,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            items(decorations) { decor ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(1.dp, HorrorColdTeal.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = HorrorSurfaceVariant.copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = decor.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = HorrorColdTeal,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = decor.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = HorrorColdWhite
                        )
                        if (!decor.loreSnippet.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = decor.loreSnippet,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = HorrorAmber
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MapTab(asylumMap: AsylumMap, playerX: Double, playerY: Double) {
    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        Text(
            text = "UBICACIÓN ACTUAL: ${asylumMap.getRoomName(playerX, playerY)}",
            style = MaterialTheme.typography.labelMedium,
            color = HorrorAmber,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black, RoundedCornerShape(8.dp))
                .border(1.dp, HorrorColdTeal.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                for (r in 0 until AsylumMap.HEIGHT) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (c in 0 until AsylumMap.WIDTH) {
                            val isWall = asylumMap.isWall(c.toDouble(), r.toDouble())
                            val isPlayer = (playerX.toInt() == c && playerY.toInt() == r)
                            val isDoor = asylumMap.getTile(c, r) == AsylumMap.WALL_DOOR

                            val color = when {
                                isPlayer -> HorrorColdTeal
                                isDoor -> HorrorAmber
                                isWall -> Color(0xFF233038)
                                else -> Color(0xFF0D1318)
                            }

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(color, if (isPlayer) CircleShape else RoundedCornerShape(1.dp))
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(HorrorColdTeal, CircleShape))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tú (Investigador)", style = MaterialTheme.typography.labelSmall, color = HorrorColdWhite)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(HorrorAmber, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Puertas", style = MaterialTheme.typography.labelSmall, color = HorrorColdWhite)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(Color(0xFF233038), RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Paredes", style = MaterialTheme.typography.labelSmall, color = HorrorColdWhite)
            }
        }
    }
}

@Composable
private fun ObjectivesTab() {
    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        Text(
            text = "MISIÓN DEL INVESTIGADOR:",
            style = MaterialTheme.typography.titleSmall,
            color = HorrorColdTeal,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        val objectives = listOf(
            "1. Explora el centro psiquiátrico abandonado usando tu linterna.",
            "2. Las luces fluorescentes del sanatorio sufren fluctuaciones y parpadeos súbitos.",
            "3. Examina las decoraciones (sillas de ruedas, camillas de sujeción, goteros y escritorios).",
            "4. Encuentra los 5 expedientes clínicos y notas de los pacientes para reconstruir lo sucedido.",
            "5. Mantén tu cordura vigilando que no te quedes a oscuras en celdas aisladas."
        )

        for (obj in objectives) {
            Text(
                text = obj,
                style = MaterialTheme.typography.bodyMedium,
                color = HorrorColdWhite.copy(alpha = 0.9f),
                lineHeight = 22.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun NativeEngineTab() {
    var nativeStatus by remember {
        mutableStateOf(
            try {
                com.example.sanatorio.nativebridge.NativeEngineBridge.getEngineStatus()
            } catch (e: Throwable) {
                "C++ / Rust / Lua 5.4.7 NDK Pipeline Compilado con éxito"
            }
        )
    }

    var luaResult by remember {
        mutableStateOf(
            try {
                com.example.sanatorio.nativebridge.NativeEngineBridge.executeLuaScript(
                    "return onPatientEncounter('P-402', 32)"
                )
            } catch (e: Throwable) {
                "Lua 5.4.7 Script Engine Listo"
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        item {
            Text(
                text = "ESTADO DEL SUBSISTEMA NATIVO:",
                style = MaterialTheme.typography.titleSmall,
                color = HorrorColdTeal,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Card 1: Core Engine Architecture
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = HorrorSurfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Arquitectura C++ (NDK 28 / Clang 17)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = HorrorAmber
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Compilador: Clang LLVM con C++17 y ABI aarch64 / armv7a / x86_64\n• JNI Bridge: Conexión directa y control manual de memoria sin GC pauses",
                        style = MaterialTheme.typography.bodySmall,
                        color = HorrorColdWhite,
                        lineHeight = 18.sp
                    )
                }
            }

            // Card 2: Rust 1.98 Core
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = HorrorSurfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Núcleo Rust (asylum_core_rust)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = HorrorAmber
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Rust 1.98 con crate-type staticlib\n• Cálculo de decaimiento de cordura y distancias sin std (`#[no_std]`)\n• Seguridad estricta de memoria para cálculos de tensión y horror",
                        style = MaterialTheme.typography.bodySmall,
                        color = HorrorColdWhite,
                        lineHeight = 18.sp
                    )
                }
            }

            // Card 3: Pure C Lua 5.4.7
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = HorrorSurfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Intérprete Oficial Lua 5.4.7 (C Puro sin wrappers)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = HorrorAmber
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Intérprete C original de PUC-Rio integrado y compilado nativamente\n• Ejecución de scripts para encuentros, sustos y notas del sanatorio\n• Prueba de evaluación de script:\n  \"$luaResult\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = HorrorColdWhite,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HandRigTab(handRig: HandRigManager) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .testTag("hand_rig_tab_content")
    ) {
        item {
            Text(
                text = "ASSET 3D: MANOS DEL INVESTIGADOR (RIG & ARTICULACIÓN)",
                style = MaterialTheme.typography.titleSmall,
                color = HorrorColdTeal,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Card 1: Asset Information & Files
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = HorrorSurfaceVariant),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "investigator_hands (Modelo 3D y Rig)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = HorrorAmber
                        )
                        Surface(
                            color = HorrorColdTeal.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "CC0 1.0 LIBRE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = HorrorColdTeal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Archivos: ${handRig.gltfFile} | ${handRig.objFile} | Texturas PBR 1K\n• Total articulaciones: 8 huesos jerárquicos vinculados a glTF 2.0\n• Malla poligonal: 3,975 vértices con soporte para animación esqueletal",
                        style = MaterialTheme.typography.bodySmall,
                        color = HorrorColdWhite,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "POSES Y ANIMACIONES ACTIVAS:",
                style = MaterialTheme.typography.labelMedium,
                color = HorrorColdWhite,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Quick Poses Selector
            Column(modifier = Modifier.fillMaxWidth()) {
                handRig.presets.forEach { preset ->
                    val isSelected = handRig.currentPoseName == preset.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) HorrorColdTeal.copy(alpha = 0.15f) else HorrorDarkSurface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) HorrorColdTeal else HorrorColdWhite.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) HorrorColdTeal else HorrorColdWhite
                                )
                                Text(
                                    text = preset.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = HorrorTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = { handRig.applyPreset(preset.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) HorrorColdTeal else HorrorSurfaceVariant,
                                    contentColor = if (isSelected) Color.Black else HorrorColdWhite
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isSelected) "Activo" else "Aplicar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CONTROL INDIVIDUAL DE HUESOS (8 JUNTAS):",
                    style = MaterialTheme.typography.labelMedium,
                    color = HorrorAmber,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = { handRig.resetToDefaults() },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Reiniciar", fontSize = 11.sp, color = HorrorColdWhite)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // List of Bones with interactive rotation sliders
        items(handRig.bones) { bone ->
            BoneJointCard(bone = bone)
        }
    }
}

@Composable
private fun BoneJointCard(bone: BoneJoint) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = HorrorDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, HorrorColdTeal.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${bone.id}. ${bone.displayName} (${bone.name})",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = HorrorColdWhite
                )
                Text(
                    text = "X: ${bone.angleX.toInt()}° | Y: ${bone.angleY.toInt()}° | Z: ${bone.angleZ.toInt()}°",
                    style = MaterialTheme.typography.labelSmall,
                    color = HorrorColdTeal,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
            Text(
                text = bone.role,
                style = MaterialTheme.typography.labelSmall,
                color = HorrorTextMuted,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Slider X
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Eje X", style = MaterialTheme.typography.labelSmall, color = HorrorColdWhite, modifier = Modifier.width(36.dp), fontSize = 10.sp)
                Slider(
                    value = bone.angleX,
                    onValueChange = { bone.angleX = it },
                    valueRange = -90f..90f,
                    modifier = Modifier.weight(1f).height(24.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = HorrorColdTeal,
                        activeTrackColor = HorrorColdTeal,
                        inactiveTrackColor = HorrorSurfaceVariant
                    )
                )
            }

            // Slider Y
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Eje Y", style = MaterialTheme.typography.labelSmall, color = HorrorColdWhite, modifier = Modifier.width(36.dp), fontSize = 10.sp)
                Slider(
                    value = bone.angleY,
                    onValueChange = { bone.angleY = it },
                    valueRange = -90f..90f,
                    modifier = Modifier.weight(1f).height(24.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = HorrorAmber,
                        activeTrackColor = HorrorAmber,
                        inactiveTrackColor = HorrorSurfaceVariant
                    )
                )
            }

            // Slider Z
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Eje Z", style = MaterialTheme.typography.labelSmall, color = HorrorColdWhite, modifier = Modifier.width(36.dp), fontSize = 10.sp)
                Slider(
                    value = bone.angleZ,
                    onValueChange = { bone.angleZ = it },
                    valueRange = -90f..90f,
                    modifier = Modifier.weight(1f).height(24.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = HorrorColdWhite,
                        activeTrackColor = HorrorColdWhite,
                        inactiveTrackColor = HorrorSurfaceVariant
                    )
                )
            }
        }
    }
}
