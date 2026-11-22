package com.ucp.biomass.ar.domain.math

import com.ucp.biomass.ar.core.math.Vector3D
import com.ucp.biomass.ar.domain.model.AnatomicalSegment
import com.ucp.biomass.ar.domain.model.BiomassCalculationResult
import com.ucp.biomass.ar.domain.model.CoordinateSystemType
import com.ucp.biomass.ar.domain.model.SegmentGeometry
import com.ucp.biomass.ar.domain.model.SegmentIntegrationResult
import com.ucp.biomass.ar.domain.model.Somatotype
import com.ucp.biomass.ar.domain.model.TrackingStatus
import kotlin.math.max

/**
 * Motor Matemático Principal de BioMass AR (Cálculo Multivariable - Matemáticas 3).
 *
 * Responsable de coordinar la resolución numérica y analítica de las Integrales Triples
 * para todos los segmentos corporales:
 *
 * Masa Total = \iiint_{V_{Cuerpo}} \delta(\vec{r}) dV
 *            = \iiint_{V_{Cabeza}} \delta(\rho,\phi,\theta) \rho^2 \sin(\phi) d\rho d\phi d\theta
 *            + \sum_{i \in Tronco,Extremidades} \iiint_{V_i} \delta(r,\theta,z) r dr d\theta dz
 *
 * Peso Terrestre: P = Masa * g (donde g = 9.80665 m/s^2)
 * Baricentro (Centro de Masa): \vec{R}_{CoM} = (1 / MasaTotal) * \sum (M_i * \vec{r}_i)
 */
class BioMassMathEngine(
    private val simpsonIntegrator: SimpsonTripleIntegrator = SimpsonTripleIntegrator(),
    private val sphericalIntegrator: SphericalTripleIntegrator = SphericalTripleIntegrator()
) {
    companion object {
        const val GRAVITATIONAL_ACCELERATION = 9.80665 // m/s^2 (Estándar ISO 80000-3)
        const val KG_TO_LBS = 2.20462262185
    }

    /**
     * Ejecuta el cálculo integral tridimensional sobre la geometría anatómica detectada.
     *
     * @param geometries Mapa de geometrías de cada segmento extraídas por los sensores.
     * @param somatotype Somatotipo seleccionado por el usuario.
     * @param trackingStatus Estado actual del rastreo de sensores.
     * @param sensorConfidence Nivel de confianza ponderado de profundidad y pose.
     * @param subjectHeightMeters Estatura métrica estimada del sujeto.
     */
    fun computeBiomass(
        geometries: Map<AnatomicalSegment, SegmentGeometry>,
        somatotype: Somatotype,
        trackingStatus: TrackingStatus,
        sensorConfidence: Double,
        subjectHeightMeters: Double
    ): BiomassCalculationResult {
        val startTime = System.nanoTime()

        var totalVolumeM3 = 0.0
        var totalMassKg = 0.0
        var weightedCoMSum = Vector3D.ZERO
        var totalPointsEvaluated = 0

        val segmentResults = mutableMapOf<AnatomicalSegment, SegmentIntegrationResult>()

        for ((segment, geometry) in geometries) {
            val result: SegmentIntegrationResult = when (segment.coordinateSystem) {
                CoordinateSystemType.SPHERICAL -> {
                    // Integración en coordenadas esféricas / elipsoidales para la cabeza
                    val a = geometry.meanRadiusAt(geometry.length * 0.5)
                    val b = (geometry.proximalSemiAxisB + geometry.distalSemiAxisB) * 0.5
                    val c = geometry.length * 0.5 // Semieje vertical

                    val sphResult = sphericalIntegrator.integrateHeadEllipsoid(
                        semiAxisA = max(0.06, a),
                        semiAxisB = max(0.07, b),
                        semiAxisC = max(0.08, c),
                        somatotype = somatotype
                    )

                    totalPointsEvaluated += sphResult.pointsEvaluated

                    SegmentIntegrationResult(
                        segment = segment,
                        volumeM3 = sphResult.volumeM3,
                        massKg = sphResult.massKg * segment.relativeDensityMultiplier,
                        meanDensityKgM3 = sphResult.meanDensityKgM3 * segment.relativeDensityMultiplier,
                        centerOfMass = geometry.center,
                        coordinateSystem = CoordinateSystemType.SPHERICAL,
                        depthConfidence = geometry.depthConfidence
                    )
                }
                CoordinateSystemType.CYLINDRICAL -> {
                    // Integración en coordenadas cilíndricas para tronco y extremidades
                    val cylResult = simpsonIntegrator.integrateCylindricalSegment(
                        geometry = geometry,
                        somatotype = somatotype,
                        stepsZ = 16,
                        stepsTheta = 16
                    )

                    totalPointsEvaluated += cylResult.pointsEvaluated

                    // Posición global del centro de masa del segmento:
                    // P_proximal + longitudinalAxis * localCenterOfMassZ
                    val globalCoM = geometry.proximalJoint + (geometry.longitudinalAxis * cylResult.localCenterOfMassZ)

                    SegmentIntegrationResult(
                        segment = segment,
                        volumeM3 = cylResult.volumeM3,
                        massKg = cylResult.massKg * segment.relativeDensityMultiplier,
                        meanDensityKgM3 = cylResult.meanDensityKgM3 * segment.relativeDensityMultiplier,
                        centerOfMass = globalCoM,
                        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
                        depthConfidence = geometry.depthConfidence
                    )
                }
            }

            segmentResults[segment] = result
            totalVolumeM3 += result.volumeM3
            totalMassKg += result.massKg
            weightedCoMSum = weightedCoMSum + (result.centerOfMass * result.massKg)
        }

        // Programación defensiva: verificación de valores físicos válidos
        val sanitizedMass = if (totalMassKg.isFinite() && totalMassKg > 0.0) totalMassKg else 0.0
        val sanitizedVolume = if (totalVolumeM3.isFinite() && totalVolumeM3 > 0.0) totalVolumeM3 else 0.0

        val weightN = sanitizedMass * GRAVITATIONAL_ACCELERATION
        val weightLbs = sanitizedMass * KG_TO_LBS

        val meanDensity = if (sanitizedVolume > 1e-9) sanitizedMass / sanitizedVolume else somatotype.bulkDensityKgM3
        val globalCenterOfMass = if (sanitizedMass > 1e-9) weightedCoMSum / sanitizedMass else Vector3D.ZERO

        val height = subjectHeightMeters.coerceIn(1.0, 2.5)
        val bmi = if (sanitizedMass > 0.0) sanitizedMass / (height * height) else 0.0

        // Estimación biomecánica de porcentaje de grasa basada en la densidad volumétrica calculada
        // Ecuación de Siri: %Grasa = (495 / Densidad) - 450 (adaptada a kg/m^3 dividiendo densidad entre 1000)
        val densityGPerCm3 = meanDensity / 1000.0
        val calculatedBodyFat = if (densityGPerCm3 in 0.9..1.2) {
            ((4.95 / densityGPerCm3) - 4.50) * 100.0
        } else {
            somatotype.typicalBodyFatPercentage
        }
        val clampedFat = calculatedBodyFat.coerceIn(5.0, 50.0)

        val latencyMs = (System.nanoTime() - startTime) / 1_000_000

        return BiomassCalculationResult(
            totalMassKg = sanitizedMass,
            weightNewtons = weightN,
            weightLbs = weightLbs,
            totalVolumeM3 = sanitizedVolume,
            meanDensityKgM3 = meanDensity,
            centerOfMass = globalCenterOfMass,
            heightMeters = height,
            bmi = bmi,
            estimatedBodyFatPercentage = clampedFat,
            somatotype = somatotype,
            segmentResults = segmentResults,
            trackingStatus = trackingStatus,
            sensorConfidence = sensorConfidence.coerceIn(0.0, 1.0),
            integrationPointsEvaluated = totalPointsEvaluated,
            computationLatencyMs = latencyMs,
            isBodyDetected = sanitizedMass > 15.0 && segmentResults.isNotEmpty()
        )
    }
}
