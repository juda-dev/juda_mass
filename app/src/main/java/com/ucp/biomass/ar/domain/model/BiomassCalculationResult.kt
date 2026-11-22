package com.ucp.biomass.ar.domain.model

import com.ucp.biomass.ar.core.math.Vector3D
import java.util.Locale

/**
 * Resultado individual de la evaluación de la integral triple para un segmento corporal:
 * - Volumen = \iiint dV
 * - Masa = \iiint delta dV
 * - Densidad media = Masa / Volumen
 * - Baricentro del segmento = (1/Masa) \iiint r * delta dV
 */
data class SegmentIntegrationResult(
    val segment: AnatomicalSegment,
    val volumeM3: Double,
    val massKg: Double,
    val meanDensityKgM3: Double,
    val centerOfMass: Vector3D,
    val coordinateSystem: CoordinateSystemType,
    val depthConfidence: Double
) {
    val volumeLiters: Double get() = volumeM3 * 1000.0
}

/**
 * Resultado consolidado de la estimación de BioMass AR para todo el cuerpo humano.
 */
data class BiomassCalculationResult(
    val totalMassKg: Double,
    val weightNewtons: Double,
    val weightLbs: Double,
    val totalVolumeM3: Double,
    val meanDensityKgM3: Double,
    val centerOfMass: Vector3D,
    val heightMeters: Double,
    val bmi: Double,
    val estimatedBodyFatPercentage: Double,
    val somatotype: Somatotype,
    val segmentResults: Map<AnatomicalSegment, SegmentIntegrationResult>,
    val trackingStatus: TrackingStatus,
    val sensorConfidence: Double,
    val integrationPointsEvaluated: Int,
    val computationLatencyMs: Long,
    val isBodyDetected: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    val totalVolumeLiters: Double get() = totalVolumeM3 * 1000.0

    // Rango de aproximación realista (+- 5% de margen óptico/antropométrico)
    val weightRangeMinKg: Double
        get() = if (isBodyDetected) (totalMassKg * 0.95).coerceAtLeast(10.0) else 0.0

    val weightRangeMaxKg: Double
        get() = if (isBodyDetected) (totalMassKg * 1.05).coerceAtLeast(10.0) else 0.0

    val weightRangeFormatted: String
        get() = if (isBodyDetected && totalMassKg > 0.0) {
            String.format(Locale.US, "%.1f - %.1f kg", weightRangeMinKg, weightRangeMaxKg)
        } else {
            "-- kg"
        }

    val meanMassFormatted: String
        get() = if (isBodyDetected && totalMassKg > 0.0) {
            String.format(Locale.US, "%.1f", totalMassKg)
        } else {
            "--"
        }

    companion object {
        val EMPTY = BiomassCalculationResult(
            totalMassKg = 0.0,
            weightNewtons = 0.0,
            weightLbs = 0.0,
            totalVolumeM3 = 0.0,
            meanDensityKgM3 = 1050.0,
            centerOfMass = Vector3D.ZERO,
            heightMeters = 1.70,
            bmi = 0.0,
            estimatedBodyFatPercentage = 15.0,
            somatotype = Somatotype.MESOMORPH,
            segmentResults = emptyMap(),
            trackingStatus = TrackingStatus.SEARCHING,
            sensorConfidence = 0.0,
            integrationPointsEvaluated = 0,
            computationLatencyMs = 0L,
            isBodyDetected = false
        )
    }
}
