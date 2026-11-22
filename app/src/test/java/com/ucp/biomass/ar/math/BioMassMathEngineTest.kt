package com.ucp.biomass.ar.math

import com.ucp.biomass.ar.data.fallback.AnthropometricFallbackModel
import com.ucp.biomass.ar.domain.math.BioMassMathEngine
import com.ucp.biomass.ar.domain.model.Somatotype
import com.ucp.biomass.ar.domain.model.TrackingStatus
import com.ucp.biomass.ar.data.repository.BodyScannerRepositoryImpl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BioMassMathEngineTest {

    private val mathEngine = BioMassMathEngine()
    private val fallbackModel = AnthropometricFallbackModel()

    @Test
    fun testFullBodyMassEstimationWithTripleIntegrals() {
        // Generar postura de referencia para un adulto estándar de 1.75m
        val dummyRepo = BodyScannerRepositoryImpl(
            context = io.mockk.mockk(relaxed = true),
            arCoreHelper = io.mockk.mockk(relaxed = true),
            mlKitHelper = io.mockk.mockk(relaxed = true),
            fusionEngine = io.mockk.mockk(relaxed = true),
            fallbackModel = fallbackModel,
            dispatchers = io.mockk.mockk(relaxed = true)
        )
        val baselinePose = dummyRepo.createBaselineCalibrationPose()

        // Generar geometrías anatómicas
        val geometries = fallbackModel.generateFallbackGeometries(
            landmarks = baselinePose,
            somatotype = Somatotype.MESOMORPH,
            depthConfidence = 0.85
        )

        val resultMeso = mathEngine.computeBiomass(
            geometries = geometries,
            somatotype = Somatotype.MESOMORPH,
            trackingStatus = TrackingStatus.ARCORE_DEPTH_ACTIVE,
            sensorConfidence = 0.85,
            subjectHeightMeters = 1.75
        )

        // Verificaciones Fisiológicas Rigurosas:
        // Para un adulto de 1.75m, la masa debe ubicarse en el rango humano normal [60kg, 90kg]
        assertTrue(
            "La masa estimada para Mesomorfo (1.75m) debe estar en un rango biomecánico real [60 - 90 kg], fue ${resultMeso.totalMassKg}",
            resultMeso.totalMassKg in 60.0..90.0
        )

        // Verificar el peso en Newtons P = M * g
        val expectedWeightN = resultMeso.totalMassKg * BioMassMathEngine.GRAVITATIONAL_ACCELERATION
        assertEquals(expectedWeightN, resultMeso.weightNewtons, 1e-3)

        // Verificar el Centro de Masa (Baricentro corporal): debe estar cerca del ombligo / pelvis
        assertTrue("El centro de masa global no debe ser nulo", resultMeso.centerOfMass.norm() > 0.1)

        // Evaluar ahora para Ectomorfo con la misma osamenta base:
        val resultEcto = mathEngine.computeBiomass(
            geometries = geometries,
            somatotype = Somatotype.ECTOMORPH,
            trackingStatus = TrackingStatus.ARCORE_DEPTH_ACTIVE,
            sensorConfidence = 0.85,
            subjectHeightMeters = 1.75
        )

        // Como el tejido mesomórfico tiene mayor densidad muscular y ósea (delta_meso > delta_ecto),
        // la integral triple debe producir una masa mayor para el Mesomorfo:
        assertTrue(
            "La masa mesomórfica (${resultMeso.totalMassKg} kg) debe superar a la ectomórfica (${resultEcto.totalMassKg} kg) por mayor densidad muscular/ósea",
            resultMeso.totalMassKg > resultEcto.totalMassKg
        )
    }

    @Test
    fun testDefensiveHandlingOfEmptyGeometries() {
        val emptyResult = mathEngine.computeBiomass(
            geometries = emptyMap(),
            somatotype = Somatotype.MESOMORPH,
            trackingStatus = TrackingStatus.SEARCHING,
            sensorConfidence = 0.0,
            subjectHeightMeters = 1.75
        )

        assertEquals(0.0, emptyResult.totalMassKg, 1e-6)
        assertEquals(0.0, emptyResult.weightNewtons, 1e-6)
        assertEquals(0.0, emptyResult.totalVolumeM3, 1e-6)
    }
}
