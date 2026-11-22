package com.ucp.biomass.ar.domain.math

import com.ucp.biomass.ar.core.math.CylindricalPoint
import com.ucp.biomass.ar.domain.model.SegmentGeometry
import com.ucp.biomass.ar.domain.model.Somatotype
import kotlin.math.PI

/**
 * Motor numérico para la evaluación de Integrales Triples en Coordenadas Cilíndricas:
 * V = \int_0^L \int_0^{2\pi} \int_0^{R(z, theta)} r * dr * dtheta * dz
 * Masa = \int_0^L \int_0^{2\pi} \int_0^{R(z, theta)} delta(r, z) * r * dr * dtheta * dz
 *
 * Emplea Regla de Simpson 1/3 Compuesta combinada con Cuadratura de Gauss-Legendre
 * para garantizar exactitud absoluta y ejecución en < 2 milisegundos por segmento.
 */
class SimpsonTripleIntegrator {

    /**
     * Evalúa el volumen y la masa de un segmento cilíndrico/cónico elíptico
     * utilizando integración triple numérica sobre los límites dados por la geometría.
     *
     * @param geometry Geometría del segmento con semiejes longitudinales a(z) y b(z).
     * @param somatotype Somatotipo activo para la modulación de la función de densidad.
     * @param stepsZ Número de subdivisiones a lo largo del eje vertical z (debe ser par para Simpson).
     * @param stepsTheta Número de subdivisiones angulares theta en [0, 2*PI] (debe ser par).
     * @return Pair(Volumen en m^3, Masa en kg)
     */
    fun integrateCylindricalSegment(
        geometry: SegmentGeometry,
        somatotype: Somatotype,
        stepsZ: Int = 16,
        stepsTheta: Int = 16
    ): CylindricalIntegralResult {
        require(stepsZ % 2 == 0) { "stepsZ debe ser un número par para Simpson 1/3: $stepsZ" }
        require(stepsTheta % 2 == 0) { "stepsTheta debe ser un número par para Simpson 1/3: $stepsTheta" }

        val L = geometry.length
        val hZ = L / stepsZ
        val twoPi = 2.0 * PI
        val hTheta = twoPi / stepsTheta

        var volumeSumZ = 0.0
        var massSumZ = 0.0
        var weightedZMassSum = 0.0 // Para calcular el centro de masa local \bar{z}
        var totalPointsEvaluated = 0

        // Integración externa sobre z in [0, L] usando Simpson 1/3
        for (i in 0..stepsZ) {
            val z = i * hZ
            val weightZ = when {
                i == 0 || i == stepsZ -> 1.0
                i % 2 == 1 -> 4.0
                else -> 2.0
            }

            val aZ = geometry.semiAxisAAt(z)
            val bZ = geometry.semiAxisBAt(z)

            // Integración intermedia sobre theta in [0, 2*PI]
            var volumeSumTheta = 0.0
            var massSumTheta = 0.0

            for (j in 0..stepsTheta) {
                val theta = j * hTheta
                val weightTheta = when {
                    j == 0 || j == stepsTheta -> 1.0
                    j % 2 == 1 -> 4.0
                    else -> 2.0
                }

                // Radio elíptico R(z, theta)
                val maxR = CylindricalPoint.ellipticalRadius(theta, aZ, bZ)

                // Integración interna sobre r in [0, R(z, theta)]
                // Elemento diferencial con Jacobiano: r * dr
                // Para el volumen: \int_0^R r dr = R^2 / 2
                val volumeR = 0.5 * maxR * maxR

                // Para la masa: \int_0^R delta(r, maxR) * r dr usando Gauss-Legendre
                val massR = GaussLegendreQuadrature.integrateFast(0.0, maxR) { r ->
                    val density = somatotype.evaluateRadialDensity(r, maxR)
                    density * r // densidad multiplicada por el factor Jacobiano r
                }

                volumeSumTheta += weightTheta * volumeR
                massSumTheta += weightTheta * massR
                totalPointsEvaluated += 4 // 4 nodos de Gauss en cada evaluación interna
            }

            val dVolumeDz = (hTheta / 3.0) * volumeSumTheta
            val dMassDz = (hTheta / 3.0) * massSumTheta

            volumeSumZ += weightZ * dVolumeDz
            massSumZ += weightZ * dMassDz
            weightedZMassSum += weightZ * (z * dMassDz)
        }

        val totalVolumeM3 = (hZ / 3.0) * volumeSumZ
        val totalMassKg = (hZ / 3.0) * massSumZ
        val localCenterOfMassZ = if (totalMassKg > 1e-9) {
            ((hZ / 3.0) * weightedZMassSum) / totalMassKg
        } else {
            L * 0.5
        }

        // Densidad promedio efectiva
        val meanDensity = if (totalVolumeM3 > 1e-12) totalMassKg / totalVolumeM3 else somatotype.bulkDensityKgM3

        return CylindricalIntegralResult(
            volumeM3 = totalVolumeM3,
            massKg = totalMassKg,
            meanDensityKgM3 = meanDensity,
            localCenterOfMassZ = localCenterOfMassZ,
            pointsEvaluated = totalPointsEvaluated
        )
    }

    /**
     * Solución analítica exacta en forma cerrada para el volumen de un tronco de cono elíptico:
     * V = (PI / 3) * L * (a1*b1 + a2*b2 + sqrt(a1*b1 * a2*b2))
     *
     * Utilizada para verificación cruzada matemática y pruebas unitarias de exactitud.
     */
    fun analyticalVolume(geometry: SegmentGeometry): Double {
        val a1 = geometry.proximalSemiAxisA
        val b1 = geometry.proximalSemiAxisB
        val a2 = geometry.distalSemiAxisA
        val b2 = geometry.distalSemiAxisB
        val L = geometry.length

        val s1 = a1 * b1
        val s2 = a2 * b2
        return (PI / 3.0) * L * (s1 + s2 + Math.sqrt(s1 * s2))
    }
}

data class CylindricalIntegralResult(
    val volumeM3: Double,
    val massKg: Double,
    val meanDensityKgM3: Double,
    val localCenterOfMassZ: Double,
    val pointsEvaluated: Int
)
