package com.ucp.biomass.ar.domain.math

/**
 * Cuadratura de Gauss-Legendre de alta precisión para integración numérica.
 *
 * Transforma el intervalo arbitrario [a, b] al intervalo canónico [-1, 1]:
 * \int_a^b f(t) dt = ((b - a) / 2) * \sum_{i=1}^N w_i * f( ((b - a)/2) * x_i + (a + b)/2 )
 *
 * Exacta para polinomios de grado hasta 2N - 1.
 */
object GaussLegendreQuadrature {

    // Nodos y pesos para N = 8 (precisión suficiente para funciones de densidad continuas)
    @PublishedApi
    internal val NODES_8 = doubleArrayOf(
        -0.9602898564975363,
        -0.7966664774136267,
        -0.5255324099163290,
        -0.1834346424956498,
         0.1834346424956498,
         0.5255324099163290,
         0.7966664774136267,
         0.9602898564975363
    )

    @PublishedApi
    internal val WEIGHTS_8 = doubleArrayOf(
        0.1012285362903763,
        0.2223810344533745,
        0.3137066458778873,
        0.3626837833783620,
        0.3626837833783620,
        0.3137066458778873,
        0.2223810344533745,
        0.1012285362903763
    )

    // Nodos y pesos para N = 4 (evaluaciones ultra-rápidas a 60 FPS)
    @PublishedApi
    internal val NODES_4 = doubleArrayOf(
        -0.8611363115940526,
        -0.3399810435848563,
         0.3399810435848563,
         0.8611363115940526
    )

    @PublishedApi
    internal val WEIGHTS_4 = doubleArrayOf(
        0.3478548451374538,
        0.6521451548625461,
        0.6521451548625461,
        0.3478548451374538
    )

    /**
     * Integra f(x) en el intervalo [a, b] usando 8 puntos de Gauss-Legendre.
     */
    inline fun integrate(a: Double, b: Double, crossinline f: (Double) -> Double): Double {
        if (a == b) return 0.0
        val halfLength = (b - a) * 0.5
        val midpoint = (a + b) * 0.5
        var sum = 0.0
        for (i in 0 until 8) {
            val x = halfLength * NODES_8[i] + midpoint
            sum += WEIGHTS_8[i] * f(x)
        }
        return halfLength * sum
    }

    /**
     * Integra f(x) en el intervalo [a, b] usando 4 puntos (modo de alta velocidad para móvil).
     */
    inline fun integrateFast(a: Double, b: Double, crossinline f: (Double) -> Double): Double {
        if (a == b) return 0.0
        val halfLength = (b - a) * 0.5
        val midpoint = (a + b) * 0.5
        var sum = 0.0
        for (i in 0 until 4) {
            val x = halfLength * NODES_4[i] + midpoint
            sum += WEIGHTS_4[i] * f(x)
        }
        return halfLength * sum
    }
}
