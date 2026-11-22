package com.ucp.biomass.ar.math

import com.ucp.biomass.ar.domain.math.SphericalTripleIntegrator
import com.ucp.biomass.ar.domain.model.Somatotype
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI

class SphericalTripleIntegratorTest {

    private val integrator = SphericalTripleIntegrator()
    private val epsilonPercent = 0.01

    @Test
    fun testSphericalTripleIntegralOnSphere() {
        // Esfera con radio R = 0.12 m
        // Volumen analítico: V = (4/3) * PI * R^3 = 4/3 * PI * 0.12^3 = 0.0072382 m^3
        val r = 0.12
        val analyticalV = (4.0 / 3.0) * PI * r * r * r

        val result = integrator.integrateHeadEllipsoid(
            semiAxisA = r,
            semiAxisB = r,
            semiAxisC = r,
            somatotype = Somatotype.MESOMORPH,
            stepsPhi = 16,
            stepsTheta = 16
        )

        val relativeError = Math.abs(result.volumeM3 - analyticalV) / analyticalV
        assertTrue(
            "El volumen de la integral triple esférica debe coincidir con (4/3)*PI*R^3 (error < 1%), fue $relativeError",
            relativeError < epsilonPercent
        )

        // Masa calculada con densidad craneal heterogénea debe ser consistente (~7-8 kg para este volumen)
        assertTrue("La masa de la cabeza debe ser coherente", result.massKg in 6.0..10.0)
    }

    @Test
    fun testEllipsoidHeadIntegral() {
        // Elipsoide con semiejes: a = 0.08 (ancho), b = 0.10 (profundidad), c = 0.11 (altura)
        val a = 0.08
        val b = 0.10
        val c = 0.11
        val analyticalV = integrator.analyticalVolume(a, b, c)

        val result = integrator.integrateHeadEllipsoid(a, b, c, Somatotype.ECTOMORPH)
        val relativeError = Math.abs(result.volumeM3 - analyticalV) / analyticalV

        assertTrue(
            "El volumen elipsoidal de la integral triple debe converger a (4/3)*PI*abc con error < 1%",
            relativeError < epsilonPercent
        )
    }
}
