package com.ucp.biomass.ar.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ucp.biomass.ar.presentation.theme.BioMassCyanPrimary

/**
 * Efecto de resplandor 'shimmer' continuo proyectado sobre las zonas anatómicas
 * que se encuentran activamente calculando las integrales de volumen y densidad.
 */
@Composable
fun ShimmerMeshOverlay(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1800f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    val shimmerColors = listOf(
        Color.Transparent,
        BioMassCyanPrimary.copy(alpha = 0.05f),
        BioMassCyanPrimary.copy(alpha = 0.18f),
        BioMassCyanPrimary.copy(alpha = 0.05f),
        Color.Transparent
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 400f, translateAnim + 400f)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush)
    )
}
