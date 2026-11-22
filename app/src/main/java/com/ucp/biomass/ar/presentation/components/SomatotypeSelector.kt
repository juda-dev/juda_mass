package com.ucp.biomass.ar.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.HelpOutline
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
import com.ucp.biomass.ar.domain.model.Somatotype
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

/**
 * Selector Morfológico de Somatotipos con estética ejecutiva de precisión.
 * Modifica directamente la constante física de densidad promedio (delta)
 * y el gradiente radial utilizado por las Integrales Triples en el motor de dominio.
 */
@Composable
fun SomatotypeSelector(
    selectedSomatotype: Somatotype,
    onSomatotypeSelected: (Somatotype) -> Unit,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BioMassSurfaceDark)
            .border(1.dp, BioMassBorderOutline, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // Barra Superior: Título y acceso a la guía explicativa
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(end = 6.dp)
            ) {
                Text(
                    text = "SOMATOTIPO Y DENSIDAD TISULAR",
                    color = BioMassTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "Densidad base: δ̄ = ${selectedSomatotype.bulkDensityKgM3.toInt()} kg/m³",
                    color = BioMassChampagneGold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // Botón interactivo para abrir la Guía y Test de Somatotipo
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BioMassSurfaceElevated)
                    .border(1.dp, BioMassSapphirePrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .clickable { onOpenGuide() }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = BioMassSapphirePrimary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "¿Cuál soy yo?",
                    color = BioMassSapphirePrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Contenedor segmentado de los 3 somatotipos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(BioMassSurfaceVariant)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Somatotype.entries.forEach { somatotype ->
                val isSelected = somatotype == selectedSomatotype

                val targetBgColor = if (isSelected) BioMassSurfaceElevated else BioMassSurfaceVariant
                val targetTextColor = if (isSelected) BioMassTextPrimary else BioMassTextSecondary
                val targetBorderColor = if (isSelected) BioMassSapphirePrimary else androidx.compose.ui.graphics.Color.Transparent

                val animatedBg by animateColorAsState(
                    targetValue = targetBgColor,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "SomaBg"
                )
                val animatedText by animateColorAsState(
                    targetValue = targetTextColor,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "SomaText"
                )
                val animatedBorder by animateColorAsState(
                    targetValue = targetBorderColor,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "SomaBorder"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(animatedBg)
                        .border(1.dp, animatedBorder, RoundedCornerShape(8.dp))
                        .clickable { onSomatotypeSelected(somatotype) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = somatotype.displayName.uppercase(),
                            color = animatedText,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (somatotype) {
                                Somatotype.ECTOMORPH -> "Delgado"
                                Somatotype.MESOMORPH -> "Atlético"
                                Somatotype.ENDOMORPH -> "Robusto"
                            },
                            color = if (isSelected) BioMassSapphirePrimary else BioMassTextTertiary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Breve explicación de la selección activa
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(BioMassSapphirePrimary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${selectedSomatotype.tagline}: ${selectedSomatotype.description}",
                color = BioMassTextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
