package com.ucp.biomass.ar.presentation.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ucp.biomass.ar.domain.model.Somatotype
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

/**
 * Diálogo interactivo premium y educativo: "¿Cuál es mi Somatotipo?"
 *
 * Explica en lenguaje claro las características físicas, proporciona la prueba
 * clínica de la muñeca (Heath-Carter) y ofrece recomendación biométrica
 * en tiempo real basada en la silueta de la cámara.
 */
@Composable
fun SomatotypeGuideDialog(
    currentSomatotype: Somatotype,
    suggestedSomatotype: Somatotype?,
    onSelectSomatotype: (Somatotype) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, BioMassBorderOutline, RoundedCornerShape(20.dp)),
            color = BioMassSurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = BioMassSapphirePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GUÍA DE SOMATOTIPOS",
                                color = BioMassTextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "Ajusta la densidad tisular (δ) para la máxima exactitud en peso",
                            color = BioMassTextSecondary,
                            fontSize = 11.sp
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

                // Contenido desplazable
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Tarjeta de Sugerencia Automática por Cámara (si hay silueta)
                    if (suggestedSomatotype != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BioMassSurfaceElevated)
                                .border(1.dp, BioMassSapphirePrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = BioMassChampagneGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SUGERENCIA POR TU SILUETA EN CÁMARA",
                                    color = BioMassChampagneGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "El análisis geométrico de hombros y caderas detecta complexión ${suggestedSomatotype.displayName}.",
                                color = BioMassTextPrimary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    onSelectSomatotype(suggestedSomatotype)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BioMassSapphirePrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = BioMassSurfaceDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "APLICAR ${suggestedSomatotype.displayName.uppercase()}",
                                    color = BioMassSurfaceDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Prueba Rápida de la Muñeca (Método Clínico)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BioMassSurfaceVariant)
                            .border(1.dp, BioMassBorderSubtle, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = BioMassJadeSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PRUEBA RÁPIDA DE LA MUÑECA (TEST CLÍNICO)",
                                color = BioMassJadeSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rodea tu muñeca izquierda con el pulgar y dedo medio de tu mano derecha. ¿Cómo se tocan?",
                            color = BioMassTextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Opciones de 1 toque para la prueba de muñeca
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            WristOptionPill(
                                label = "Se cruzan",
                                subLabel = "Ectomorfo",
                                isSelected = currentSomatotype == Somatotype.ECTOMORPH,
                                onClick = {
                                    onSelectSomatotype(Somatotype.ECTOMORPH)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            WristOptionPill(
                                label = "Se tocan justo",
                                subLabel = "Mesomorfo",
                                isSelected = currentSomatotype == Somatotype.MESOMORPH,
                                onClick = {
                                    onSelectSomatotype(Somatotype.MESOMORPH)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            WristOptionPill(
                                label = "No se tocan",
                                subLabel = "Endomorfo",
                                isSelected = currentSomatotype == Somatotype.ENDOMORPH,
                                onClick = {
                                    onSelectSomatotype(Somatotype.ENDOMORPH)
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Detalle exhaustivo de cada somatotipo
                    Somatotype.entries.forEach { somatotype ->
                        SomatotypeCardDetail(
                            somatotype = somatotype,
                            isSelected = somatotype == currentSomatotype,
                            onSelect = {
                                onSelectSomatotype(somatotype)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WristOptionPill(
    label: String,
    subLabel: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) BioMassJadeSuccess else BioMassBorderOutline,
        label = "WristBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) BioMassSurfaceElevated else BioMassSurfaceDark,
        label = "WristBg"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = if (isSelected) BioMassJadeSuccess else BioMassTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = subLabel,
            color = BioMassTextSecondary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun SomatotypeCardDetail(
    somatotype: Somatotype,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) BioMassSapphirePrimary else BioMassBorderOutline,
        label = "CardBorder"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) BioMassSurfaceElevated else BioMassSurfaceVariant,
        label = "CardContainer"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = somatotype.displayName.uppercase(),
                        color = if (isSelected) BioMassSapphirePrimary else BioMassTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BioMassSapphirePrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SELECCIONADO",
                                color = BioMassSapphirePrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = somatotype.tagline,
                    color = BioMassTextSecondary,
                    fontSize = 11.sp
                )
            }

            Text(
                text = "δ̄ = ${somatotype.bulkDensityKgM3.toInt()} kg/m³",
                color = BioMassTextTertiary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = somatotype.description,
            color = BioMassTextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rasgos principales en viñetas
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            somatotype.keyTraits.forEach { trait ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(BioMassSapphirePrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = trait,
                        color = BioMassTextPrimary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!isSelected) {
            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(containerColor = BioMassSurfaceDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BioMassBorderOutline, RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "SELECCIONAR ${somatotype.displayName.uppercase()}",
                    color = BioMassTextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
