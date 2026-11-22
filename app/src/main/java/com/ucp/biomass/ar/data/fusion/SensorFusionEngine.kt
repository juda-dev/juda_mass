package com.ucp.biomass.ar.data.fusion

import com.google.ar.core.Frame
import com.ucp.biomass.ar.core.math.Vector3D
import com.ucp.biomass.ar.data.arcore.ArCoreSessionHelper
import com.ucp.biomass.ar.data.fallback.AnthropometricFallbackModel
import com.ucp.biomass.ar.domain.model.AnatomicalSegment
import com.ucp.biomass.ar.domain.model.JointType
import com.ucp.biomass.ar.domain.model.PoseJointLandmarks
import com.ucp.biomass.ar.domain.model.SegmentGeometry
import com.ucp.biomass.ar.domain.model.Somatotype
import com.ucp.biomass.ar.domain.model.TrackingStatus
import kotlin.math.abs
import kotlin.math.max

/**
 * Motor de Fusión Sensorial.
 *
 * Cruza los datos cinemáticos y de límites verticales z provistos por ML Kit
 * con los mapas métricos de profundidad 3D y radios r provistos por ARCore Depth API.
 * Gestiona la conmutación defensiva al modelo antropométrico de respaldo.
 */
class SensorFusionEngine(
    private val fallbackModel: AnthropometricFallbackModel = AnthropometricFallbackModel()
) {
    /**
     * Procesa un cuadro de video combinando ARCore y ML Kit en hilos de background.
     */
    fun fuseSensors(
        landmarks: PoseJointLandmarks?,
        arFrame: Frame?,
        arHelper: ArCoreSessionHelper?,
        somatotype: Somatotype
    ): SensorFusionResult {
        if (landmarks == null || landmarks.landmarks.isEmpty()) {
            val fallback = fallbackModel.rollbackOrFallback(null, somatotype, 0.0)
            return SensorFusionResult(
                geometries = fallback,
                trackingStatus = TrackingStatus.SEARCHING,
                overallConfidence = 0.0
            )
        }

        // Determinar disponibilidad y salud de ARCore Depth
        val isArCoreUsable = arFrame != null && arHelper != null && arHelper.isDepthSupported

        if (!isArCoreUsable) {
            // ARCore Depth no disponible o dispositivo sin soporte: activar Modelo Antropométrico
            val fallbackGeometries = fallbackModel.generateFallbackGeometries(
                landmarks = landmarks,
                somatotype = somatotype,
                depthConfidence = 0.50
            )
            return SensorFusionResult(
                geometries = fallbackGeometries,
                trackingStatus = TrackingStatus.FALLBACK_ANTHROPOMETRIC,
                overallConfidence = 0.50
            )
        }

        // Si ARCore Depth está disponible, muestrear la profundidad real en las articulaciones
        var depthSamplesCount = 0
        var depthConfidenceSum = 0.0
        val samplePoints = listOf(
            landmarks[JointType.NOSE],
            landmarks[JointType.LEFT_SHOULDER],
            landmarks[JointType.RIGHT_SHOULDER],
            landmarks[JointType.LEFT_HIP],
            landmarks[JointType.RIGHT_HIP],
            landmarks[JointType.LEFT_KNEE],
            landmarks[JointType.RIGHT_KNEE]
        ).filterNotNull()

        val sampledDepths = mutableListOf<Double>()

        for (joint in samplePoints) {
            val sample = arHelper.sampleDepthAtNormalizedPoint(arFrame!!, joint.screenX, joint.screenY)
            if (sample != null) {
                sampledDepths.add(sample.depthMeters)
                depthConfidenceSum += sample.confidence
                depthSamplesCount++
            }
        }

        val averageConfidence = if (depthSamplesCount > 0) depthConfidenceSum / depthSamplesCount else 0.0

        // Si la certidumbre de profundidad es deficiente (< 0.35), hacer fallback
        if (averageConfidence < 0.35 || sampledDepths.isEmpty()) {
            val fallbackGeometries = fallbackModel.generateFallbackGeometries(
                landmarks = landmarks,
                somatotype = somatotype,
                depthConfidence = averageConfidence
            )
            return SensorFusionResult(
                geometries = fallbackGeometries,
                trackingStatus = TrackingStatus.FALLBACK_ANTHROPOMETRIC,
                overallConfidence = averageConfidence
            )
        }

        // Fusión exitosa: refinar geometrías con la profundidad física medida por ARCore
        val baseGeometries = fallbackModel.generateFallbackGeometries(
            landmarks = landmarks,
            somatotype = somatotype,
            depthConfidence = averageConfidence
        )

        val refinedGeometries = mutableMapOf<AnatomicalSegment, SegmentGeometry>()
        for ((segment, geom) in baseGeometries) {
            // Muestrear profundidad en la articulación proximal y distal
            val proxScreenU = ((geom.proximalJoint.x / geom.proximalJoint.z) + 0.5).toFloat().coerceIn(0f, 1f)
            val proxScreenV = ((-geom.proximalJoint.y / geom.proximalJoint.z) + 0.5).toFloat().coerceIn(0f, 1f)
            val proxSample = arHelper.sampleDepthAtNormalizedPoint(arFrame!!, proxScreenU, proxScreenV)

            val distScreenU = ((geom.distalJoint.x / geom.distalJoint.z) + 0.5).toFloat().coerceIn(0f, 1f)
            val distScreenV = ((-geom.distalJoint.y / geom.distalJoint.z) + 0.5).toFloat().coerceIn(0f, 1f)
            val distSample = arHelper.sampleDepthAtNormalizedPoint(arFrame, distScreenU, distScreenV)

            // Ajustar semiejes sagitales (profundidad B) con factor medido
            val depthFactor = when {
                proxSample != null && distSample != null -> ((proxSample.depthMeters + distSample.depthMeters) * 0.5) / 1.8
                proxSample != null -> proxSample.depthMeters / 1.8
                else -> 1.0
            }.coerceIn(0.75, 1.35)

            refinedGeometries[segment] = geom.copy(
                proximalSemiAxisB = geom.proximalSemiAxisB * depthFactor,
                distalSemiAxisB = geom.distalSemiAxisB * depthFactor,
                depthConfidence = averageConfidence
            )
        }

        return SensorFusionResult(
            geometries = refinedGeometries,
            trackingStatus = TrackingStatus.ARCORE_DEPTH_ACTIVE,
            overallConfidence = averageConfidence
        )
    }
}

data class SensorFusionResult(
    val geometries: Map<AnatomicalSegment, SegmentGeometry>,
    val trackingStatus: TrackingStatus,
    val overallConfidence: Double
)
