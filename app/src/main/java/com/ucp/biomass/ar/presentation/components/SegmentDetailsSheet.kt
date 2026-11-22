package com.ucp.biomass.ar.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ucp.biomass.ar.domain.model.AnatomicalSegment
import com.ucp.biomass.ar.domain.model.CoordinateSystemType
import com.ucp.biomass.ar.domain.model.SegmentIntegrationResult
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
 * Tarjeta unificada y responsiva del Desglose Matemático por Segmentos.
 * Permite colapsar y expandir los resultados de las 16 integrales triples
 * manteniendo perfecta uniformidad visual con el resto de la interfaz.
 */
@Composable
fun SegmentDetailsSheet(
    segmentResults: Map<AnatomicalSegment, SegmentIntegrationResult>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ArrowRotation"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BioMassSurfaceDark)
            .border(1.dp, BioMassBorderOutline, RoundedCornerShape(16.dp))
    ) {
        // Cabecera interactiva y uniforme (siempre visible)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Functions,
                    contentDescription = null,
                    tint = BioMassSapphirePrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "DESGLOSE MATEMÁTICO",
                        color = BioMassTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (segmentResults.isNotEmpty()) "${segmentResults.size} integrales evaluadas" else "Esperando sujeto...",
                        color = BioMassTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isExpanded) "Ocultar" else "Ver detalles",
                    color = BioMassSapphirePrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = BioMassSapphirePrimary,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(arrowRotation)
                )
            }
        }

        // Contenido desplegable con animación suave
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
            ) {
                // Barra de títulos de columnas limpia y sin colisiones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SEGMENTO",
                        color = BioMassTextTertiary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.weight(1.2f)
                    )
                    Text(
                        text = "MASA / VOL",
                        color = BioMassTextTertiary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "DENSIDAD",
                        color = BioMassTextTertiary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.weight(0.9f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Lista de segmentos con altura máxima controlada
                if (segmentResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(segmentResults.values.toList()) { item ->
                            SegmentResultRow(item = item)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(BioMassSurfaceVariant)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Enfoque a la persona para evaluar las 16 integrales triples.",
                            color = BioMassTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SegmentResultRow(
    item: SegmentIntegrationResult,
    modifier: Modifier = Modifier
) {
    val coordLabel = when (item.coordinateSystem) {
        CoordinateSystemType.SPHERICAL -> "Esférico (ρ,φ,θ)"
        CoordinateSystemType.CYLINDRICAL -> "Cilíndrico (r,θ,z)"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BioMassSurfaceVariant)
            .border(0.5.dp, BioMassBorderSubtle, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna 1: Nombre y Sistema de Coordenadas
        Column(modifier = Modifier.weight(1.2f)) {
            Text(
                text = item.segment.displayName,
                color = BioMassTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = coordLabel,
                color = BioMassTextTertiary,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Columna 2: Masa y Volumen
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = String.format(Locale.US, "%.2f kg", item.massKg),
                color = BioMassSapphirePrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = String.format(Locale.US, "%.2f L", item.volumeLiters),
                color = BioMassTextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Columna 3: Densidad
        Column(modifier = Modifier.weight(0.9f)) {
            Text(
                text = "${item.meanDensityKgM3.toInt()}",
                color = BioMassJadeSuccess,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "kg/m³",
                color = BioMassTextTertiary,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
