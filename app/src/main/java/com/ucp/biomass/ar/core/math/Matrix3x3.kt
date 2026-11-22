package com.ucp.biomass.ar.core.math

import kotlin.math.cos
import kotlin.math.sin

/**
 * Matriz 3x3 para transformaciones espaciales en R^3, cálculo de rotaciones de Rodrigues
 * y evaluación de determinantes Jacobianas para integrales múltiples.
 */
data class Matrix3x3(
    val m00: Double, val m01: Double, val m02: Double,
    val m10: Double, val m11: Double, val m12: Double,
    val m20: Double, val m21: Double, val m22: Double
) {
    operator fun times(v: Vector3D): Vector3D =
        Vector3D(
            m00 * v.x + m01 * v.y + m02 * v.z,
            m10 * v.x + m11 * v.y + m12 * v.z,
            m20 * v.x + m21 * v.y + m22 * v.z
        )

    operator fun times(other: Matrix3x3): Matrix3x3 =
        Matrix3x3(
            m00 * other.m00 + m01 * other.m10 + m02 * other.m20,
            m00 * other.m01 + m01 * other.m11 + m02 * other.m21,
            m00 * other.m02 + m01 * other.m12 + m02 * other.m22,

            m10 * other.m00 + m11 * other.m10 + m12 * other.m20,
            m10 * other.m01 + m11 * other.m11 + m12 * other.m21,
            m10 * other.m02 + m11 * other.m12 + m12 * other.m22,

            m20 * other.m00 + m21 * other.m10 + m22 * other.m20,
            m20 * other.m01 + m21 * other.m11 + m22 * other.m21,
            m20 * other.m02 + m21 * other.m12 + m22 * other.m22
        )

    fun transpose(): Matrix3x3 =
        Matrix3x3(
            m00, m10, m20,
            m01, m11, m21,
            m02, m12, m22
        )

    /**
     * Determinante de la matriz 3x3:
     * det(M) = m00*(m11*m22 - m12*m21) - m01*(m10*m22 - m12*m20) + m02*(m10*m21 - m11*m20)
     */
    fun determinant(): Double =
        m00 * (m11 * m22 - m12 * m21) -
        m01 * (m10 * m22 - m12 * m20) +
        m02 * (m10 * m21 - m11 * m20)

    companion object {
        val IDENTITY = Matrix3x3(
            1.0, 0.0, 0.0,
            0.0, 1.0, 0.0,
            0.0, 0.0, 1.0
        )

        /**
         * Crea una matriz de rotación utilizando la fórmula de rotación de Rodrigues
         * para rotar un ángulo [theta] (en radianes) alrededor de un [axis] unitario.
         */
        fun rotationAroundAxis(axis: Vector3D, theta: Double): Matrix3x3 {
            val u = axis.normalized()
            val cosT = cos(theta)
            val sinT = sin(theta)
            val oneMinusCos = 1.0 - cosT

            return Matrix3x3(
                cosT + u.x * u.x * oneMinusCos,
                u.x * u.y * oneMinusCos - u.z * sinT,
                u.x * u.z * oneMinusCos + u.y * sinT,

                u.y * u.x * oneMinusCos + u.z * sinT,
                cosT + u.y * u.y * oneMinusCos,
                u.y * u.z * oneMinusCos - u.x * sinT,

                u.z * u.x * oneMinusCos - u.y * sinT,
                u.z * u.y * oneMinusCos + u.x * sinT,
                cosT + u.z * u.z * oneMinusCos
            )
        }

        /**
         * Construye la matriz de rotación ortonormal que alinea el vector director [source]
         * con el vector objetivo [target]. Fundamental para orientar cilindros anatómicos.
         */
        fun alignVectors(source: Vector3D, target: Vector3D): Matrix3x3 {
            val v1 = source.normalized()
            val v2 = target.normalized()
            val dot = (v1 dot v2).coerceIn(-1.0, 1.0)

            if (dot > 0.999999) return IDENTITY
            if (dot < -0.999999) {
                // Vectores opuestos: rotar 180 grados en cualquier eje ortogonal
                val ortho = if (Math.abs(v1.x) < 0.9) Vector3D.UNIT_X else Vector3D.UNIT_Y
                val rotAxis = (v1 cross ortho).normalized()
                return rotationAroundAxis(rotAxis, Math.PI)
            }

            val cross = (v1 cross v2).normalized()
            val angle = kotlin.math.acos(dot)
            return rotationAroundAxis(cross, angle)
        }
    }
}
