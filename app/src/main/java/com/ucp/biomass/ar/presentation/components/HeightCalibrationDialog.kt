package com.ucp.biomass.ar.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ucp.biomass.ar.presentation.theme.BioMassBorderOutline
import com.ucp.biomass.ar.presentation.theme.BioMassBorderSubtle
import com.ucp.biomass.ar.presentation.theme.BioMassChampagneGold
import com.ucp.biomass.ar.presentation.theme.BioMassJadeSuccess
import com.ucp.biomass.ar.presentation.theme.BioMassSapphirePrimary
import com.ucp.biomass.ar.presentation.theme.BioMassSurfaceDark
import com.ucp.biomass.ar.presentation.theme.BioMassSurfaceElevated
import com.ucp.biomass.ar.presentation.theme.BioMassSurfaceVariant
import com.ucp.biomass.ar.presentation.theme.BioMassTextPrimary
import com.ucp.biomass.ar.presentation.theme.BioMassTextSecondary
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Diálogo de calibración métrica de estatura exacta.
 * La estatura define la escala métrica fundamental para evaluar las Integrales Triples
 * (Volumen proporcional al cubo de la escala lineal: V ~ H³).
 */
@Composable
fun HeightCalibrationDialog(
    currentHeightM: Double,
    isManual: Boolean,
    onSaveHeight: (Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var heightCm by remember {
        mutableFloatStateOf((currentHeightM * 100.0).toFloat().coerceIn(130f, 220f))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, BioMassBorderOutline, RoundedCornerShape(20.dp)),
            color = BioMassSurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Height,
                            contentDescription = null,
                            tint = BioMassSapphirePrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CALIBRACIÓN DE ESTATURA",
                            color = BioMassTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(BioMassSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = BioMassTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pantalla de lectura principal
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BioMassSurfaceVariant)
                        .border(1.dp, BioMassBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ESTATURA SELECCIONADA",
                        color = BioMassTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.2f", heightCm / 100f),
                            color = BioMassSapphirePrimary,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = " m",
                            color = BioMassSapphirePrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = " (${heightCm.roundToInt()} cm)",
                            color = BioMassTextSecondary,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 8.dp, start = 6.dp)
                        )
                    }

                    Text(
                        text = "Escala de integración métrica: Z ∈ [0, ${(heightCm / 100f)} m]",
                        color = BioMassTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Slider continuo
                Slider(
                    value = heightCm,
                    onValueChange = { heightCm = it.roundToInt().toFloat() },
                    valueRange = 130f..215f,
                    steps = 84,
                    colors = SliderDefaults.colors(
                        thumbColor = BioMassSapphirePrimary,
                        activeTrackColor = BioMassSapphirePrimary,
                        inactiveTrackColor = BioMassSurfaceElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Botones de ajuste fino (-5, -1, +1, +5)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StepButton(label = "-5 cm") { heightCm = (heightCm - 5f).coerceIn(130f, 215f) }
                    StepButton(label = "-1 cm") { heightCm = (heightCm - 1f).coerceIn(130f, 215f) }
                    StepButton(label = "+1 cm") { heightCm = (heightCm + 1f).coerceIn(130f, 215f) }
                    StepButton(label = "+5 cm") { heightCm = (heightCm + 5f).coerceIn(130f, 215f) }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Atajos rápidos de estaturas estándar
                Text(
                    text = "VALORES FRECUENTES",
                    color = BioMassTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(160f, 165f, 170f, 175f, 180f, 185f).forEach { preset ->
                        val isCurrent = heightCm.roundToInt() == preset.roundToInt()
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCurrent) BioMassSapphirePrimary else BioMassSurfaceElevated)
                                .clickable { heightCm = preset }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${preset.toInt()}",
                                color = if (isCurrent) BioMassSurfaceDark else BioMassTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Acciones: Guardar o restablecer a automático
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onSaveHeight(null) // Restablecer a auto
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BioMassSurfaceElevated),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "AUTO-CÁMARA",
                            color = BioMassTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            onSaveHeight((heightCm / 100.0))
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BioMassSapphirePrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text(
                            text = "APLICAR ${String.format(Locale.US, "%.2f m", heightCm / 100f)}",
                            color = BioMassSurfaceDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BioMassSurfaceElevated)
            .border(1.dp, BioMassBorderOutline, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = BioMassTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
        )
    }
}
