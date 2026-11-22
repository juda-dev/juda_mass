package com.ucp.biomass.ar.math

import com.ucp.biomass.ar.domain.math.GaussLegendreQuadrature
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class GaussLegendreQuadratureTest {

    private val epsilon = 1e-4

    @Test
    fun testExactPolynomialIntegration() {
        // \int_0^2 x^3 dx = [x^4 / 4]_0^2 = 16 / 4 = 4.0
        val result = GaussLegendreQuadrature.integrate(0.0, 2.0) { x -> x * x * x }
        assertEquals(4.0, result, epsilon)

        // \int_1^3 (3x^2 + 2x + 1) dx = [x^3 + x^2 + x]_1^3 = (27 + 9 + 3) - (1 + 1 + 1) = 39 - 3 = 36.0
        val resultPoly = GaussLegendreQuadrature.integrate(1.0, 3.0) { x -> 3.0 * x * x + 2.0 * x + 1.0 }
        assertEquals(36.0, resultPoly, epsilon)
    }

    @Test
    fun testTrigonometricIntegration() {
        // \int_0^PI sin(x) dx = [-cos(x)]_0^PI = -(-1) - (-1) = 2.0
        val resultSin = GaussLegendreQuadrature.integrate(0.0, PI) { x -> sin(x) }
        assertEquals(2.0, resultSin, epsilon)
    }

    @Test
    fun testFastQuadraturePerformance() {
        // Fast 4-point quadrature para funciones cuadráticas
        val result = GaussLegendreQuadrature.integrateFast(0.0, 1.0) { x -> x * x }
        assertEquals(1.0 / 3.0, result, epsilon)
    }
}
