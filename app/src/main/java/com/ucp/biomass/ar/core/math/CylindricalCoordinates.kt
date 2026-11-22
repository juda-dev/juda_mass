package com.ucp.biomass.ar.core.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Representación y transformaciones en Coordenadas Cilíndricas (r, theta, z).
 *
 * Base Matemática:
 * x = r * cos(theta)
 * y = r * sin(theta)
 * z = z
 *
 * Determinante Jacobiano:
 * J = det [ dx/dr     dx/dtheta  dx/dz ]
 *         [ dy/dr     dy/dtheta  dy/dz ]
 *         [ dz/dr     dz/dtheta  dz/dz ]
 *   = det [ cos(theta)  -r*sin(theta)  0 ]
 *         [ sin(theta)   r*cos(theta)  0 ]
 *         [ 0            0             1 ]
 *   = cos(theta)*(r*cos(theta)) - (-r*sin(theta))*(sin(theta))
 *   = r * (cos^2(theta) + sin^2(theta)) = r
 *
 * Elemento diferencial de volumen: dV = r * dr * dtheta * dz
 */
data class CylindricalPoint(
    val r: Double,
    val theta: Double, // en radianes [0, 2*PI)
    val z: Double
) {
    init {
        require(r >= 0.0) { "El radio r en coordenadas cilíndricas debe ser no negativo: r = $r" }
    }

    /**
     * Convierte el punto cilíndrico a coordenadas cartesianas R^3.
     */
    fun toCartesian(): Vector3D =
        Vector3D(
            x = r * cos(theta),
            y = r * sin(theta),
            z = z
        )

    /**
     * Retorna el factor del Jacobiano en este punto: J = r.
     */
    fun jacobian(): Double = r

    companion object {
        /**
         * Transforma un punto cartesiano (x, y, z) a cilíndrico (r, theta, z).
         */
        fun fromCartesian(v: Vector3D): CylindricalPoint {
            val r = sqrt(v.x * v.x + v.y * v.y)
            var theta = atan2(v.y, v.x)
            if (theta < 0.0) theta += 2.0 * Math.PI
            return CylindricalPoint(r = r, theta = theta, z = v.z)
        }

        /**
         * Calcula el radio r(theta) para una sección transversal elíptica
         * con semieje mayor a (ancho) y semieje menor b (profundidad):
         *
         * r(theta) = (a * b) / sqrt((b * cos(theta))^2 + (a * sin(theta))^2)
         */
        fun ellipticalRadius(theta: Double, semiAxisA: Double, semiAxisB: Double): Double {
            require(semiAxisA > 0.0 && semiAxisB > 0.0) { "Los semiejes deben ser positivos" }
            val cosT = cos(theta)
            val sinT = sin(theta)
            val denomSq = (semiAxisB * cosT) * (semiAxisB * cosT) + (semiAxisA * sinT) * (semiAxisA * sinT)
            return (semiAxisA * semiAxisB) / sqrt(denomSq)
        }
    }
}
