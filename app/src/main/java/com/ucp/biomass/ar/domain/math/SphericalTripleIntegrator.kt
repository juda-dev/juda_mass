package com.ucp.biomass.ar.domain.math

import com.ucp.biomass.ar.domain.model.Somatotype
import kotlin.math.PI
import kotlin.math.sin

/**
 * Motor numérico para la evaluación de Integrales Triples en Coordenadas Esféricas
 * y Elipsoidales escaladas para la cabeza y cráneo humano:
 *
 * V = \int_0^{2\pi} d\theta \int_0^{\pi} d\phi \int_0^1 (a * b * c * \rho^2 * \sin(\phi)) d\rho
 * Masa = \int_0^{2\pi} d\theta \int_0^{\pi} d\phi \int_0^1 (\delta(\rho) * a * b * c * \rho^2 * \sin(\phi)) d\rho
 *
 * Jacobiano elipsoidal: J = a * b * c * \rho^2 * \sin(\phi)
 */
class SphericalTripleIntegrator {

    /**
     * Evalúa el volumen y la masa de la cabeza modelada como un elipsoide en coordenadas esféricas.
     *
     * @param semiAxisA Semieje lateral (ancho de sien a sien / 2).
     * @param semiAxisB Semieje sagital (profundidad de nariz a nuca / 2 vía Depth API).
     * @param semiAxisC Semieje vertical (altura de barbilla a coronilla / 2).
     * @param somatotype Somatotipo activo.
     * @param stepsPhi Pasos para el ángulo cenital phi en [0, PI] (debe ser par).
     * @param stepsTheta Pasos para el ángulo azimutal theta en [0, 2*PI] (debe ser par).
     */
    fun integrateHeadEllipsoid(
        semiAxisA: Double,
        semiAxisB: Double,
        semiAxisC: Double,
        somatotype: Somatotype,
        stepsPhi: Int = 16,
        stepsTheta: Int = 16
    ): SphericalIntegralResult {
        require(stepsPhi % 2 == 0) { "stepsPhi debe ser par: $stepsPhi" }
        require(stepsTheta % 2 == 0) { "stepsTheta debe ser par: $stepsTheta" }

        val hPhi = PI / stepsPhi
        val twoPi = 2.0 * PI
        val hTheta = twoPi / stepsTheta
        val abc = semiAxisA * semiAxisB * semiAxisC

        // Integración radial interna en \rho in [0, 1] usando Gauss-Legendre
        // Para el volumen sin densidad: \int_0^1 \rho^2 d\rho = 1/3
        val radialVolumeIntegral = 1.0 / 3.0

        // Para la masa: \int_0^1 \delta(\rho) * \rho^2 d\rho
        val radialMassIntegral = GaussLegendreQuadrature.integrate(0.0, 1.0) { rho ->
            // Densidad radial craneal modulada por el somatotipo:
            // Núcleo encefálico interior (rho <= 0.85) vs corteza craneal ósea externa (rho > 0.85)
            val headDensity = if (rho > 0.85) {
                somatotype.coreBoneDensityKgM3 * 0.70 + somatotype.bulkDensityKgM3 * 0.30
            } else {
                somatotype.bulkDensityKgM3 * 0.98
            }
            headDensity * rho * rho
        }

        var volumeSumPhi = 0.0
        var massSumPhi = 0.0

        for (i in 0..stepsPhi) {
            val phi = i * hPhi
            val weightPhi = when {
                i == 0 || i == stepsPhi -> 1.0
                i % 2 == 1 -> 4.0
                else -> 2.0
            }

            val sinPhi = sin(phi)
            val dVPhi = sinPhi * radialVolumeIntegral
            val dMPhi = sinPhi * radialMassIntegral

            volumeSumPhi += weightPhi * dVPhi
            massSumPhi += weightPhi * dMPhi
        }

        val integralOverPhiVolume = (hPhi / 3.0) * volumeSumPhi
        val integralOverPhiMass = (hPhi / 3.0) * massSumPhi

        // Integración sobre theta in [0, 2*PI]
        var volumeSumTheta = 0.0
        var massSumTheta = 0.0

        for (j in 0..stepsTheta) {
            val weightTheta = when {
                j == 0 || j == stepsTheta -> 1.0
                j % 2 == 1 -> 4.0
                else -> 2.0
            }
            volumeSumTheta += weightTheta * integralOverPhiVolume
            massSumTheta += weightTheta * integralOverPhiMass
        }

        val totalVolumeM3 = abc * (hTheta / 3.0) * volumeSumTheta
        val totalMassKg = abc * (hTheta / 3.0) * massSumTheta
        val meanDensity = if (totalVolumeM3 > 1e-12) totalMassKg / totalVolumeM3 else somatotype.bulkDensityKgM3

        return SphericalIntegralResult(
            volumeM3 = totalVolumeM3,
            massKg = totalMassKg,
            meanDensityKgM3 = meanDensity,
            pointsEvaluated = stepsPhi * stepsTheta * 8
        )
    }

    /**
     * Solución analítica exacta en forma cerrada: V = (4/3) * PI * a * b * c
     */
    fun analyticalVolume(semiAxisA: Double, semiAxisB: Double, semiAxisC: Double): Double =
        (4.0 / 3.0) * PI * semiAxisA * semiAxisB * semiAxisC
}

data class SphericalIntegralResult(
    val volumeM3: Double,
    val massKg: Double,
    val meanDensityKgM3: Double,
    val pointsEvaluated: Int
)
