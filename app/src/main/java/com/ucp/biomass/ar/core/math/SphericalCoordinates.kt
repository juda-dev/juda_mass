package com.ucp.biomass.ar.core.math

import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Representación y transformaciones en Coordenadas Esféricas (rho, phi, theta).
 *
 * Convención matemática estándar:
 * rho: distancia radial desde el origen [0, infinity)
 * phi: ángulo cenital o colatitud [0, PI] medido desde el eje z positivo
 * theta: ángulo azimutal [0, 2*PI) en el plano xy
 *
 * Ecuaciones de transformación:
 * x = rho * sin(phi) * cos(theta)
 * y = rho * sin(phi) * sin(theta)
 * z = rho * cos(phi)
 *
 * Determinante Jacobiano:
 * J = det [ dx/drho   dx/dphi               dx/dtheta ]
 *         [ dy/drho   dy/dphi               dy/dtheta ]
 *         [ dz/drho   dz/dphi               dz/dtheta ]
 *
 *   = rho^2 * sin(phi)
 *
 * Elemento diferencial de volumen: dV = rho^2 * sin(phi) * drho * dphi * dtheta
 */
data class SphericalPoint(
    val rho: Double,
    val phi: Double,   // [0, PI]
    val theta: Double  // [0, 2*PI)
) {
    init {
        require(rho >= 0.0) { "El radio rho debe ser no negativo: rho = $rho" }
        require(phi in -1e-6..(Math.PI + 1e-6)) { "El ángulo cenital phi debe estar en [0, PI]: phi = $phi" }
    }

    /**
     * Convierte el punto esférico a coordenadas cartesianas R^3.
     */
    fun toCartesian(): Vector3D =
        Vector3D(
            x = rho * sin(phi) * cos(theta),
            y = rho * sin(phi) * sin(theta),
            z = rho * cos(phi)
        )

    /**
     * Retorna el factor del Jacobiano en este punto: J = rho^2 * sin(phi).
     */
    fun jacobian(): Double = rho * rho * sin(phi)

    companion object {
        /**
         * Transforma un punto cartesiano (x, y, z) a esférico (rho, phi, theta).
         */
        fun fromCartesian(v: Vector3D): SphericalPoint {
            val rho = v.norm()
            if (rho < 1e-12) return SphericalPoint(0.0, 0.0, 0.0)

            val phi = acos((v.z / rho).coerceIn(-1.0, 1.0))
            var theta = atan2(v.y, v.x)
            if (theta < 0.0) theta += 2.0 * Math.PI

            return SphericalPoint(rho = rho, phi = phi, theta = theta)
        }

        /**
         * Jacobiano para un elipsoide con semiejes (a, b, c):
         * J_ellipsoid = a * b * c * rho^2 * sin(phi)
         */
        fun ellipsoidalJacobian(rho: Double, phi: Double, a: Double, b: Double, c: Double): Double =
            a * b * c * rho * rho * sin(phi)
    }
}
