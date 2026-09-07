package com.example.sanatorio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.sanatorio.model.Decoration
import com.example.sanatorio.model.DecorationType
import com.example.ui.theme.HorrorAmber
import com.example.ui.theme.HorrorBloodRed
import com.example.ui.theme.HorrorColdTeal
import com.example.ui.theme.HorrorColdWhite
import com.example.ui.theme.HorrorDarkBackground
import com.example.ui.theme.HorrorSurfaceVariant

@Composable
fun InspectionDialog(
    decoration: Decoration,
    onDismiss: () -> Unit,
    onSaveToNotes: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .border(1.dp, HorrorColdTeal.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .testTag("inspection_dialog"),
            colors = CardDefaults.cardColors(containerColor = HorrorDarkBackground)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = when (decoration.type) {
                            DecorationType.PATIENT_FILE -> Icons.Default.Description
                            DecorationType.WHEELCHAIR, DecorationType.GURNEY -> Icons.Default.LocalHospital
                            DecorationType.BLOOD_MARK -> Icons.Default.Warning
                            else -> Icons.Default.FolderSpecial
                        }
                        val tintColor = if (decoration.type == DecorationType.BLOOD_MARK) HorrorBloodRed else HorrorColdTeal

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(tintColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = tintColor, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "EVIDENCIA DE INVESTIGACIÓN",
                                style = MaterialTheme.typography.labelSmall,
                                color = HorrorColdTeal,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = decoration.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = HorrorColdWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_inspection_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = HorrorColdWhite)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Object Description
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HorrorSurfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = decoration.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = HorrorColdWhite.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )
                }

                // Lore Snippet (if available)
                if (!decoration.loreSnippet.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NOTAS CLÍNICAS / HALLAZGO:",
                        style = MaterialTheme.typography.labelMedium,
                        color = HorrorAmber,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, HorrorAmber.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .background(Color(0xFF14130F), RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = decoration.loreSnippet,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = HorrorAmber.copy(alpha = 0.95f),
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onSaveToNotes,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HorrorColdTeal,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("save_evidence_button")
                    ) {
                        Text(
                            text = if (decoration.isInspected) "Archivado en Cuaderno ✓" else "Archivar en Cuaderno",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
