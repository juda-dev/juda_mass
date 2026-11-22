package com.ucp.biomass.ar.core.math

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Representa un vector tridimensional en el espacio euclidiano R^3.
 * Provee operaciones de álgebra lineal fundamentales para la transformación
 * de sistemas de coordenadas y la orientación de segmentos anatómicos.
 */
data class Vector3D(
    val x: Double,
    val y: Double,
    val z: Double
) {
    constructor(x: Float, y: Float, z: Float) : this(x.toDouble(), y.toDouble(), z.toDouble())

    operator fun plus(other: Vector3D): Vector3D =
        Vector3D(x + other.x, y + other.y, z + other.z)

    operator fun minus(other: Vector3D): Vector3D =
        Vector3D(x - other.x, y - other.y, z - other.z)

    operator fun times(scalar: Double): Vector3D =
        Vector3D(x * scalar, y * scalar, z * scalar)

    operator fun times(scalar: Float): Vector3D =
        Vector3D(x * scalar.toDouble(), y * scalar.toDouble(), z * scalar.toDouble())

    operator fun div(scalar: Double): Vector3D {
        require(scalar != 0.0) { "División por cero en Vector3D" }
        return Vector3D(x / scalar, y / scalar, z / scalar)
    }

    operator fun unaryMinus(): Vector3D =
        Vector3D(-x, -y, -z)

    /**
     * Producto punto (escalar) entre vectores: u · v = u_x*v_x + u_y*v_y + u_z*v_z
     */
    infix fun dot(other: Vector3D): Double =
        x * other.x + y * other.y + z * other.z

    /**
     * Producto cruz (vectorial): u x v
     */
    infix fun cross(other: Vector3D): Vector3D =
        Vector3D(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )

    /**
     * Norma euclidiana (magnitud): ||v|| = sqrt(x^2 + y^2 + z^2)
     */
    fun norm(): Double = sqrt(x * x + y * y + z * z)

    /**
     * Cuadrado de la norma euclidiana: ||v||^2
     */
    fun normSq(): Double = x * x + y * y + z * z

    /**
     * Vector unitario en la misma dirección (v / ||v||).
     * Si la norma es casi cero, retorna el vector cero para prevenir NaN.
     */
    fun normalized(): Vector3D {
        val n = norm()
        return if (n > 1e-12) this / n else ZERO
    }

    /**
     * Distancia euclidiana hacia otro punto/vector.
     */
    fun distanceTo(other: Vector3D): Double = (this - other).norm()

    /**
     * Ángulo en radianes entre dos vectores: theta = acos((u · v) / (||u|| * ||v||))
     */
    fun angleWith(other: Vector3D): Double {
        val nProd = this.norm() * other.norm()
        if (nProd < 1e-12) return 0.0
        val cosTheta = (this dot other) / nProd
        return acos(cosTheta.coerceIn(-1.0, 1.0))
    }

    /**
     * Interpolación lineal (Lerp) entre este vector y otro.
     */
    fun lerp(other: Vector3D, t: Double): Vector3D {
        val clampedT = t.coerceIn(0.0, 1.0)
        return this + (other - this) * clampedT
    }

    companion object {
        val ZERO = Vector3D(0.0, 0.0, 0.0)
        val UNIT_X = Vector3D(1.0, 0.0, 0.0)
        val UNIT_Y = Vector3D(0.0, 1.0, 0.0)
        val UNIT_Z = Vector3D(0.0, 0.0, 1.0)
    }
}
