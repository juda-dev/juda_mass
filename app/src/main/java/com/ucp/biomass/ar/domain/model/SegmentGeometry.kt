package com.ucp.biomass.ar.domain.model

import com.ucp.biomass.ar.core.math.Matrix3x3
import com.ucp.biomass.ar.core.math.Vector3D
import kotlin.math.max

/**
 * Define los parámetros geométricos tridimensionales de un segmento corporal
 * extraídos del cruce entre ML Kit (límites verticales / z) y ARCore Depth (radios r / profundidad).
 */
data class SegmentGeometry(
    val segment: AnatomicalSegment,
    val proximalJoint: Vector3D,    // Punto articular proximal (origen del eje z local)
    val distalJoint: Vector3D,      // Punto articular distal (extremo del eje z local)
    val proximalSemiAxisA: Double,  // Semieje transversal proximal (ancho / 2)
    val proximalSemiAxisB: Double,  // Semieje sagital proximal (profundidad / 2 vía Depth API)
    val distalSemiAxisA: Double,    // Semieje transversal distal (ancho / 2)
    val distalSemiAxisB: Double,    // Semieje sagital distal (profundidad / 2 vía Depth API)
    val depthConfidence: Double = 1.0 // Nivel de certeza de profundidad [0.0 - 1.0]
) {
    /**
     * Longitud longitudinal del segmento: L = ||P_distal - P_proximal||
     */
    val length: Double = max(0.01, proximalJoint.distanceTo(distalJoint))

    /**
     * Vector unitario a lo largo del eje longitudinal del segmento.
     */
    val longitudinalAxis: Vector3D = (distalJoint - proximalJoint).normalized()

    /**
     * Matriz de rotación que transforma un vector del sistema de referencia local (donde z es longitudinal)
     * al sistema de referencia global de la cámara AR.
     */
    val localToGlobalMatrix: Matrix3x3 = Matrix3x3.alignVectors(Vector3D.UNIT_Z, longitudinalAxis)

    /**
     * Matriz inversa (global a local) para proyectar puntos de la nube hacia el cilindro local.
     */
    val globalToLocalMatrix: Matrix3x3 = localToGlobalMatrix.transpose()

    /**
     * Centro geométrico (baricentro volumétrico aproximado).
     */
    val center: Vector3D = proximalJoint.lerp(distalJoint, 0.5)

    /**
     * Radio transversal interpolado a una altura z local in [0, length].
     */
    fun semiAxisAAt(z: Double): Double {
        val t = (z / length).coerceIn(0.0, 1.0)
        return proximalSemiAxisA + (distalSemiAxisA - proximalSemiAxisA) * t
    }

    /**
     * Radio sagital (profundidad) interpolado a una altura z local in [0, length].
     */
    fun semiAxisBAt(z: Double): Double {
        val t = (z / length).coerceIn(0.0, 1.0)
        return proximalSemiAxisB + (distalSemiAxisB - proximalSemiAxisB) * t
    }

    /**
     * Radio promedio en la sección transversal z.
     */
    fun meanRadiusAt(z: Double): Double = (semiAxisAAt(z) + semiAxisBAt(z)) * 0.5
}
