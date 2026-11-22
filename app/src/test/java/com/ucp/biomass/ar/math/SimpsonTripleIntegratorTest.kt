package com.ucp.biomass.ar.math

import com.ucp.biomass.ar.core.math.Vector3D
import com.ucp.biomass.ar.domain.math.SimpsonTripleIntegrator
import com.ucp.biomass.ar.domain.model.AnatomicalSegment
import com.ucp.biomass.ar.domain.model.SegmentGeometry
import com.ucp.biomass.ar.domain.model.Somatotype
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class SimpsonTripleIntegratorTest {

    private val integrator = SimpsonTripleIntegrator()
    private val epsilonPercent = 0.01 // 1% de tolerancia de error para cuadraturas numéricas discretizadas

    @Test
    fun testCylindricalTripleIntegralOnStandardCylinder() {
        // Cilindro perfecto: radio r = 0.15 m, longitud L = 0.80 m
        // Volumen analítico exacto: V = PI * r^2 * L = PI * 0.15^2 * 0.80 = 0.0565486 m^3
        val radius = 0.15
        val length = 0.80
        val analyticalV = PI * radius * radius * length

        val cylinderGeom = SegmentGeometry(
            segment = AnatomicalSegment.UPPER_TORSO,
            proximalJoint = Vector3D(0.0, 0.0, 0.0),
            distalJoint = Vector3D(0.0, length, 0.0),
            proximalSemiAxisA = radius,
            proximalSemiAxisB = radius,
            distalSemiAxisA = radius,
            distalSemiAxisB = radius
        )

        val result = integrator.integrateCylindricalSegment(
            geometry = cylinderGeom,
            somatotype = Somatotype.MESOMORPH,
            stepsZ = 16,
            stepsTheta = 16
        )

        val relativeError = Math.abs(result.volumeM3 - analyticalV) / analyticalV
        assertTrue("El error relativo de la integral triple cilíndrica debe ser < 1%, fue $relativeError", relativeError < epsilonPercent)

        // Verificar el centro de masa local: para un cilindro uniforme debe estar exactamente en L/2 = 0.40 m
        assertEquals(length * 0.5, result.localCenterOfMassZ, 0.01)

        // Masa = Volumen * Densidad promedio
        assertTrue("La masa calculada debe ser positiva y coherente con la densidad", result.massKg > 0.0)
    }

    @Test
    fun testEllipticalTruncatedConeIntegral() {
        // Tronco de cono elíptico (aproximación anatómica del fémur o muslo):
        // a1 = 0.09, b1 = 0.08, a2 = 0.06, b2 = 0.05, L = 0.45
        val geom = SegmentGeometry(
            segment = AnatomicalSegment.LEFT_THIGH,
            proximalJoint = Vector3D(-0.10, 0.0, 0.0),
            distalJoint = Vector3D(-0.10, -0.45, 0.0),
            proximalSemiAxisA = 0.09,
            proximalSemiAxisB = 0.08,
            distalSemiAxisA = 0.06,
            distalSemiAxisB = 0.05
        )

        val numericalResult = integrator.integrateCylindricalSegment(
            geometry = geom,
            somatotype = Somatotype.MESOMORPH
        )

        val analyticalV = integrator.analyticalVolume(geom)
        val relativeError = Math.abs(numericalResult.volumeM3 - analyticalV) / analyticalV

        assertTrue(
            "La integral numérica triple debe converger a la solución analítica cerrada con error < 1%, fue $relativeError",
            relativeError < epsilonPercent
        )
    }
}
