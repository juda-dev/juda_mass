package com.ucp.biomass.ar.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.ucp.biomass.ar.domain.model.AnatomicalSegment
import com.ucp.biomass.ar.domain.model.CoordinateSystemType
import com.ucp.biomass.ar.domain.model.JointType
import com.ucp.biomass.ar.domain.model.PoseJointLandmarks
import com.ucp.biomass.ar.domain.model.SegmentGeometry
import com.ucp.biomass.ar.presentation.theme.BioMassCyanGlow
import com.ucp.biomass.ar.presentation.theme.BioMassCyanPrimary
import com.ucp.biomass.ar.presentation.theme.BioMassEmeraldSecondary
import com.ucp.biomass.ar.presentation.theme.BioMassJointPulsing
import com.ucp.biomass.ar.presentation.theme.BioMassMeshWireframe
import com.ucp.biomass.ar.presentation.theme.BioMassScanLaser
import com.ucp.biomass.ar.presentation.theme.BioMassScanLaserGlow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Lienzo AR de alto rendimiento que renderiza el esqueleto de pose, la malla alámbrica 3D
 * de cilindros y esferas sobre la persona real, y la guía de encuadre cuando no hay nadie.
 */
@Composable
fun MeshOverlayCanvas(
    landmarks: PoseJointLandmarks?,
    geometries: Map<AnatomicalSegment, SegmentGeometry>,
    isScanningActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LaserSweep")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LaserY"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "JointPulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val screenW = size.width
        val screenH = size.height

        if (landmarks == null || landmarks.landmarks.isEmpty()) {
            // No se detecta cuerpo: dibujar silueta guía punteada y retícula de encuadre
            drawTargetingGuide(screenW, screenH, pulseAlpha)
        } else {
            // Cuerpo detectado: dibujar cilindros 3D, esfera craneal y esqueleto real
            drawPersonMesh(landmarks, geometries, screenW, screenH, pulseAlpha)
            drawSkeletonBones(landmarks, screenW, screenH)
            drawJointNodes(landmarks, screenW, screenH, pulseAlpha)

            if (isScanningActive) {
                drawScanningLaser(screenW, screenH, laserProgress)
            }
        }
    }
}

/**
 * Dibuja la silueta guía ergonómica en el centro de la cámara para orientar al usuario.
 */
private fun DrawScope.drawTargetingGuide(
    screenWidth: Float,
    screenHeight: Float,
    pulseAlpha: Float
) {
    val centerX = screenWidth * 0.5f
    val centerY = screenHeight * 0.48f

    val guideColor = BioMassCyanPrimary.copy(alpha = 0.35f * pulseAlpha)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)

    // Cabeza guía
    val headRadius = screenHeight * 0.055f
    val headCenter = Offset(centerX, centerY - screenHeight * 0.28f)
    drawCircle(
        color = guideColor,
        radius = headRadius,
        center = headCenter,
        style = Stroke(width = 2.0f, pathEffect = dashEffect)
    )

    // Torso guía
    val shoulderY = headCenter.y + headRadius * 1.5f
    val hipY = centerY + screenHeight * 0.08f
    val shoulderHalfW = screenWidth * 0.16f
    val hipHalfW = screenWidth * 0.12f

    val torsoPath = Path().apply {
        moveTo(centerX - shoulderHalfW, shoulderY)
        lineTo(centerX + shoulderHalfW, shoulderY)
        lineTo(centerX + hipHalfW, hipY)
        lineTo(centerX - hipHalfW, hipY)
        close()
    }
    drawPath(
        path = torsoPath,
        color = guideColor,
        style = Stroke(width = 2.0f, pathEffect = dashEffect)
    )

    // Piernas guía
    val ankleY = screenHeight * 0.85f
    drawLine(
        color = guideColor,
        start = Offset(centerX - hipHalfW * 0.7f, hipY),
        end = Offset(centerX - hipHalfW * 0.6f, ankleY),
        strokeWidth = 2.0f,
        pathEffect = dashEffect
    )
    drawLine(
        color = guideColor,
        start = Offset(centerX + hipHalfW * 0.7f, hipY),
        end = Offset(centerX + hipHalfW * 0.6f, ankleY),
        strokeWidth = 2.0f,
        pathEffect = dashEffect
    )

    // Esquinas de retícula de enfoque
    val boxLeft = screenWidth * 0.12f
    val boxRight = screenWidth * 0.88f
    val boxTop = screenHeight * 0.10f
    val boxBottom = screenHeight * 0.88f
    val cornerLen = 28f
    val cornerColor = BioMassCyanPrimary.copy(alpha = 0.6f)

    // Superior Izq
    drawLine(cornerColor, Offset(boxLeft, boxTop), Offset(boxLeft + cornerLen, boxTop), 3f)
    drawLine(cornerColor, Offset(boxLeft, boxTop), Offset(boxLeft, boxTop + cornerLen), 3f)

    // Superior Der
    drawLine(cornerColor, Offset(boxRight, boxTop), Offset(boxRight - cornerLen, boxTop), 3f)
    drawLine(cornerColor, Offset(boxRight, boxTop), Offset(boxRight, boxTop + cornerLen), 3f)

    // Inferior Izq
    drawLine(cornerColor, Offset(boxLeft, boxBottom), Offset(boxLeft + cornerLen, boxBottom), 3f)
    drawLine(cornerColor, Offset(boxLeft, boxBottom), Offset(boxLeft, boxTop + (boxBottom - boxTop) - cornerLen), 3f)

    // Inferior Der
    drawLine(cornerColor, Offset(boxRight, boxBottom), Offset(boxRight - cornerLen, boxBottom), 3f)
    drawLine(cornerColor, Offset(boxRight, boxBottom), Offset(boxRight, boxTop + (boxBottom - boxTop) - cornerLen), 3f)
}

/**
 * Renderiza cilindros 3D y esferas directamente alineados sobre los puntos de la persona.
 */
private fun DrawScope.drawPersonMesh(
    landmarks: PoseJointLandmarks,
    geometries: Map<AnatomicalSegment, SegmentGeometry>,
    screenWidth: Float,
    screenHeight: Float,
    pulseAlpha: Float
) {
    // 1. Cabeza (Esfera / Elipsoide)
    val nose = landmarks[JointType.NOSE]
    val leftEar = landmarks[JointType.LEFT_EAR]
    val rightEar = landmarks[JointType.RIGHT_EAR]

    if (nose != null) {
        val headCenter = Offset(nose.screenX * screenWidth, (nose.screenY - 0.02f) * screenHeight)
        val earDist = if (leftEar != null && rightEar != null) {
            abs(leftEar.screenX - rightEar.screenX) * screenWidth
        } else {
            screenWidth * 0.12f
        }
        val headRadius = max(24f, earDist * 0.95f)

        // Contorno exterior
        drawCircle(
            color = BioMassCyanPrimary.copy(alpha = 0.85f * pulseAlpha),
            radius = headRadius,
            center = headCenter,
            style = Stroke(width = 2.0f)
        )
        // Anillo transversal ecuatorial
        drawOval(
            color = BioMassMeshWireframe.copy(alpha = 0.55f),
            topLeft = Offset(headCenter.x - headRadius, headCenter.y - headRadius * 0.35f),
            size = Size(headRadius * 2f, headRadius * 0.7f),
            style = Stroke(width = 1.2f)
        )
        // Halo de resplandor
        drawCircle(
            color = BioMassCyanGlow.copy(alpha = 0.12f * pulseAlpha),
            radius = headRadius * 1.15f,
            center = headCenter
        )
    }

    // 2. Torso (Cilindro Elíptico)
    val leftShoulder = landmarks[JointType.LEFT_SHOULDER]
    val rightShoulder = landmarks[JointType.RIGHT_SHOULDER]
    val leftHip = landmarks[JointType.LEFT_HIP]
    val rightHip = landmarks[JointType.RIGHT_HIP]

    if (leftShoulder != null && rightShoulder != null && leftHip != null && rightHip != null) {
        val sLeft = Offset(leftShoulder.screenX * screenWidth, leftShoulder.screenY * screenHeight)
        val sRight = Offset(rightShoulder.screenX * screenWidth, rightShoulder.screenY * screenHeight)
        val hLeft = Offset(leftHip.screenX * screenWidth, leftHip.screenY * screenHeight)
        val hRight = Offset(rightHip.screenX * screenWidth, rightHip.screenY * screenHeight)

        val torsoPath = Path().apply {
            moveTo(sLeft.x, sLeft.y)
            lineTo(sRight.x, sRight.y)
            lineTo(hRight.x, hRight.y)
            lineTo(hLeft.x, hLeft.y)
            close()
        }

        drawPath(
            path = torsoPath,
            color = BioMassCyanGlow.copy(alpha = 0.08f * pulseAlpha)
        )

        drawPath(
            path = torsoPath,
            color = BioMassCyanPrimary.copy(alpha = 0.65f),
            style = Stroke(width = 1.5f)
        )

        // Anillos transversales intermedios de Simpson
        val midLeft = Offset((sLeft.x + hLeft.x) * 0.5f, (sLeft.y + hLeft.y) * 0.5f)
        val midRight = Offset((sRight.x + hRight.x) * 0.5f, (sRight.y + hRight.y) * 0.5f)
        drawLine(
            color = BioMassMeshWireframe.copy(alpha = 0.5f),
            start = midLeft,
            end = midRight,
            strokeWidth = 1.2f
        )
    }

    // 3. Extremidades como cilindros
    drawLimbCylinder(landmarks, JointType.LEFT_SHOULDER, JointType.LEFT_ELBOW, screenWidth, screenHeight, pulseAlpha, 18f)
    drawLimbCylinder(landmarks, JointType.LEFT_ELBOW, JointType.LEFT_WRIST, screenWidth, screenHeight, pulseAlpha, 14f)
    drawLimbCylinder(landmarks, JointType.RIGHT_SHOULDER, JointType.RIGHT_ELBOW, screenWidth, screenHeight, pulseAlpha, 18f)
    drawLimbCylinder(landmarks, JointType.RIGHT_ELBOW, JointType.RIGHT_WRIST, screenWidth, screenHeight, pulseAlpha, 14f)

    drawLimbCylinder(landmarks, JointType.LEFT_HIP, JointType.LEFT_KNEE, screenWidth, screenHeight, pulseAlpha, 26f)
    drawLimbCylinder(landmarks, JointType.LEFT_KNEE, JointType.LEFT_ANKLE, screenWidth, screenHeight, pulseAlpha, 20f)
    drawLimbCylinder(landmarks, JointType.RIGHT_HIP, JointType.RIGHT_KNEE, screenWidth, screenHeight, pulseAlpha, 26f)
    drawLimbCylinder(landmarks, JointType.RIGHT_KNEE, JointType.RIGHT_ANKLE, screenWidth, screenHeight, pulseAlpha, 20f)
}

private fun DrawScope.drawLimbCylinder(
    landmarks: PoseJointLandmarks,
    jointA: JointType,
    jointB: JointType,
    screenWidth: Float,
    screenHeight: Float,
    pulseAlpha: Float,
    radiusPx: Float
) {
    val j1 = landmarks[jointA] ?: return
    val j2 = landmarks[jointB] ?: return
    if (j1.likelihood < 0.4f || j2.likelihood < 0.4f) return

    val p1 = Offset(j1.screenX * screenWidth, j1.screenY * screenHeight)
    val p2 = Offset(j2.screenX * screenWidth, j2.screenY * screenHeight)

    val dx = p2.x - p1.x
    val dy = p2.y - p1.y
    val dist = sqrt(dx * dx + dy * dy)
    if (dist < 8f) return

    val nx = -dy / dist * radiusPx
    val ny = dx / dist * radiusPx

    val p1L = Offset(p1.x + nx, p1.y + ny)
    val p1R = Offset(p1.x - nx, p1.y - ny)
    val p2L = Offset(p2.x + nx * 0.8f, p2.y + ny * 0.8f)
    val p2R = Offset(p2.x - nx * 0.8f, p2.y - ny * 0.8f)

    val limbPath = Path().apply {
        moveTo(p1L.x, p1L.y)
        lineTo(p2L.x, p2L.y)
        lineTo(p2R.x, p2R.y)
        lineTo(p1R.x, p1R.y)
        close()
    }

    drawPath(limbPath, BioMassCyanGlow.copy(alpha = 0.05f * pulseAlpha))
    drawLine(BioMassCyanPrimary.copy(alpha = 0.6f), p1L, p2L, 1.2f)
    drawLine(BioMassCyanPrimary.copy(alpha = 0.6f), p1R, p2R, 1.2f)
}

/**
 * Conecta los puntos del esqueleto ML Kit con líneas de alta visibilidad.
 */
private fun DrawScope.drawSkeletonBones(
    landmarks: PoseJointLandmarks,
    screenWidth: Float,
    screenHeight: Float
) {
    val bones = listOf(
        Pair(JointType.LEFT_SHOULDER, JointType.RIGHT_SHOULDER),
        Pair(JointType.LEFT_SHOULDER, JointType.LEFT_ELBOW),
        Pair(JointType.LEFT_ELBOW, JointType.LEFT_WRIST),
        Pair(JointType.RIGHT_SHOULDER, JointType.RIGHT_ELBOW),
        Pair(JointType.RIGHT_ELBOW, JointType.RIGHT_WRIST),
        Pair(JointType.LEFT_SHOULDER, JointType.LEFT_HIP),
        Pair(JointType.RIGHT_SHOULDER, JointType.RIGHT_HIP),
        Pair(JointType.LEFT_HIP, JointType.RIGHT_HIP),
        Pair(JointType.LEFT_HIP, JointType.LEFT_KNEE),
        Pair(JointType.LEFT_KNEE, JointType.LEFT_ANKLE),
        Pair(JointType.RIGHT_HIP, JointType.RIGHT_KNEE),
        Pair(JointType.RIGHT_KNEE, JointType.RIGHT_ANKLE)
    )

    for ((startType, endType) in bones) {
        val j1 = landmarks[startType]
        val j2 = landmarks[endType]
        if (j1 != null && j2 != null && j1.likelihood > 0.4f && j2.likelihood > 0.4f) {
            val startOffset = Offset(j1.screenX * screenWidth, j1.screenY * screenHeight)
            val endOffset = Offset(j2.screenX * screenWidth, j2.screenY * screenHeight)

            drawLine(
                color = BioMassEmeraldSecondary.copy(alpha = 0.85f),
                start = startOffset,
                end = endOffset,
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Dibuja los nodos articulares esféricos con halo de luz.
 */
private fun DrawScope.drawJointNodes(
    landmarks: PoseJointLandmarks,
    screenWidth: Float,
    screenHeight: Float,
    pulseAlpha: Float
) {
    for ((_, joint) in landmarks.landmarks) {
        if (joint.likelihood > 0.45f) {
            val center = Offset(joint.screenX * screenWidth, joint.screenY * screenHeight)

            drawCircle(
                color = BioMassEmeraldSecondary.copy(alpha = 0.35f * pulseAlpha),
                radius = 7f,
                center = center
            )
            drawCircle(
                color = BioMassJointPulsing,
                radius = 3.5f,
                center = center
            )
        }
    }
}

/**
 * Dibuja el láser horizontal de barrido volumétrico de escaneo continuo.
 */
private fun DrawScope.drawScanningLaser(
    screenWidth: Float,
    screenHeight: Float,
    progress: Float
) {
    val laserY = screenHeight * progress

    val brush = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            BioMassScanLaserGlow,
            BioMassScanLaser,
            BioMassScanLaserGlow,
            Color.Transparent
        ),
        startY = laserY - 20f,
        endY = laserY + 20f
    )

    drawRect(
        brush = brush,
        topLeft = Offset(0f, laserY - 20f),
        size = Size(screenWidth, 40f)
    )

    drawLine(
        color = BioMassCyanPrimary,
        start = Offset(0f, laserY),
        end = Offset(screenWidth, laserY),
        strokeWidth = 2.0f
    )
}
