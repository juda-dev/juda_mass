package com.ucp.biomass.ar.domain.usecase

import com.ucp.biomass.ar.domain.math.BioMassMathEngine
import com.ucp.biomass.ar.domain.model.AnatomicalSegment
import com.ucp.biomass.ar.domain.model.BiomassCalculationResult
import com.ucp.biomass.ar.domain.model.SegmentGeometry
import com.ucp.biomass.ar.domain.model.Somatotype
import com.ucp.biomass.ar.domain.model.TrackingStatus

/**
 * Caso de uso principal: Ejecuta la evaluación de las Integrales Triples sobre los datos
 * geométricos y sensados del cuerpo humano en tiempo real.
 */
class EstimateBiomassUseCase(
    private val mathEngine: BioMassMathEngine
) {
    operator fun invoke(
        geometries: Map<AnatomicalSegment, SegmentGeometry>,
        somatotype: Somatotype,
        trackingStatus: TrackingStatus,
        sensorConfidence: Double,
        heightMeters: Double
    ): BiomassCalculationResult {
        if (geometries.isEmpty()) {
            return BiomassCalculationResult.EMPTY.copy(
                somatotype = somatotype,
                trackingStatus = trackingStatus
            )
        }
        return mathEngine.computeBiomass(
            geometries = geometries,
            somatotype = somatotype,
            trackingStatus = trackingStatus,
            sensorConfidence = sensorConfidence,
            subjectHeightMeters = heightMeters
        )
    }
}
