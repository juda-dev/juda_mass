package com.ucp.biomass.ar.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ucp.biomass.ar.domain.model.TrackingStatus
import com.ucp.biomass.ar.presentation.theme.BioMassAmberWarning
import com.ucp.biomass.ar.presentation.theme.BioMassBorderOutline
import com.ucp.biomass.ar.presentation.theme.BioMassCrimsonError
import com.ucp.biomass.ar.presentation.theme.BioMassCyanPrimary
import com.ucp.biomass.ar.presentation.theme.BioMassEmeraldSecondary
import com.ucp.biomass.ar.presentation.theme.BioMassSurfaceDark
import com.ucp.biomass.ar.presentation.theme.BioMassSurfaceElevated
import com.ucp.biomass.ar.presentation.theme.BioMassTextPrimary
import com.ucp.biomass.ar.presentation.theme.BioMassTextSecondary

/**
 * Indicador de salud de sensores: muestra la confianza del mapa de profundidad (Depth API)
 * y el estado activo del rastreo con transiciones de color defensivas.
 */
@Composable
fun SensorConfidenceGauge(
    trackingStatus: TrackingStatus,
    confidence: Double,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = confidence.toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "ConfidenceProgress"
    )

    val (badgeText, badgeColor) = when (trackingStatus) {
        TrackingStatus.ARCORE_DEPTH_ACTIVE -> Pair("ARCORE DEPTH: ACTIVO", BioMassEmeraldSecondary)
        TrackingStatus.FALLBACK_ANTHROPOMETRIC -> Pair("MODO RESPALDO: ACTIVO", BioMassAmberWarning)
        TrackingStatus.CALIBRATING -> Pair("CALIBRANDO...", BioMassCyanPrimary)
        TrackingStatus.SEARCHING -> Pair("BUSCANDO SUJETO...", BioMassAmberWarning)
        TrackingStatus.LOST -> Pair("SEÑAL PERDIDA", BioMassCrimsonError)
    }

    val animatedBadgeColor by animateColorAsState(targetValue = badgeColor, label = "BadgeColor")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BioMassSurfaceDark.copy(alpha = 0.9f))
            .border(1.dp, BioMassBorderOutline, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Estado del sensor
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(animatedBadgeColor.copy(alpha = 0.2f))
                    .border(1.dp, animatedBadgeColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badgeText,
                    color = animatedBadgeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Barra de nivel de confianza
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                color = BioMassTextPrimary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(BioMassSurfaceElevated)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(3.dp))
                        .background(animatedBadgeColor)
                )
            }
        }
    }
}
