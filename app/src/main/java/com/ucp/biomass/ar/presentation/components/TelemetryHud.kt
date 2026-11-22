package com.ucp.biomass.ar.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ucp.biomass.ar.domain.model.BiomassCalculationResult
import com.ucp.biomass.ar.presentation.theme.BioMassAmberOchre
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
import com.ucp.biomass.ar.presentation.theme.BioMassTextTertiary
import java.util.Locale

/**
 * Panel de Telemetría Biomecánica con diseño ejecutivo y de precisión médica.
 * Muestra el rango de peso aproximado real y los resultados de las integrales triples.
 */
@Composable
fun TelemetryHud(
    result: BiomassCalculationResult,
    onOpenHeightDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedMass by animateFloatAsState(
        targetValue = if (result.isBodyDetected) result.totalMassKg.toFloat() else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "AnimatedMass"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BioMassSurfaceDark.copy(alpha = 0.95f))
            .border(1.dp, BioMassBorderOutline, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        // Estado de detección superior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(end = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (result.isBodyDetected) BioMassJadeSuccess else BioMassAmberOchre)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (result.isBodyDetected) "CUERPO BLOQUEADO Y EN ANÁLISIS" else "BUSCANDO PERSONA...",
                    color = if (result.isBodyDetected) BioMassJadeSuccess else BioMassAmberOchre,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            if (result.isBodyDetected) {
                Text(
                    text = "${result.computationLatencyMs}ms • ${result.segmentResults.size} seg",
                    color = BioMassTextTertiary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Lectura Principal de Peso y Rango Aproximado
        if (result.isBodyDetected) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                ) {
                    Text(
                        text = "MASA ESTIMADA (M = ∭ δ dV)",
                        color = BioMassTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(Locale.US, "%.1f", animatedMass),
                            color = BioMassTextPrimary,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = " kg",
                            color = BioMassSapphirePrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 5.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = " (${result.weightNewtons.toInt()} N)",
                            color = BioMassTextTertiary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Tarjeta destacada de Rango de Aproximación Real
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(BioMassSurfaceElevated)
                        .border(1.dp, BioMassSapphirePrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "RANGO (±5%)",
                        color = BioMassSapphirePrimary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = result.weightRangeFormatted,
                        color = BioMassTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grilla de 4 métricas secundarias
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tarjeta interactiva de Estatura (con botón para calibrar)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BioMassSurfaceVariant)
                        .border(1.dp, BioMassBorderSubtle, RoundedCornerShape(10.dp))
                        .clickable { onOpenHeightDialog() }
                        .padding(8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ESTATURA",
                                color = BioMassTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar estatura",
                                tint = BioMassSapphirePrimary,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = String.format(Locale.US, "%.2f m", result.heightMeters),
                            color = BioMassSapphirePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Tocar para calibrar",
                            color = BioMassTextTertiary,
                            fontSize = 8.sp
                        )
                    }
                }

                TelemetryMetricCard(
                    label = "VOLUMEN",
                    value = String.format(Locale.US, "%.1f L", result.totalVolumeLiters),
                    subValue = String.format(Locale.US, "%.3f m³", result.totalVolumeM3),
                    modifier = Modifier.weight(1f)
                )

                TelemetryMetricCard(
                    label = "DENSIDAD",
                    value = "${result.meanDensityKgM3.toInt()}",
                    subValue = "kg/m³",
                    modifier = Modifier.weight(1f)
                )

                TelemetryMetricCard(
                    label = "IMC",
                    value = String.format(Locale.US, "%.1f", result.bmi),
                    subValue = when {
                        result.bmi < 18.5 -> "Delgado"
                        result.bmi < 25.0 -> "Normal"
                        result.bmi < 30.0 -> "Robusto"
                        else -> "Elevado"
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Estado cuando la cámara está vacía o buscando sujeto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BioMassSurfaceVariant)
                    .border(1.dp, BioMassBorderOutline, RoundedCornerShape(10.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NO SE DETECTA NINGUNA PERSONA",
                        color = BioMassAmberOchre,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ubíquese a una distancia de 1.5 a 3 metros frente a la cámara.",
                        color = BioMassTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TelemetryMetricCard(
    label: String,
    value: String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BioMassSurfaceVariant)
            .border(1.dp, BioMassBorderSubtle, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Text(
            text = label,
            color = BioMassTextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = BioMassTextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = subValue,
            color = BioMassTextTertiary,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
