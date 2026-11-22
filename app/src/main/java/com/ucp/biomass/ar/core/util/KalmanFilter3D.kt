package com.ucp.biomass.ar.core.util

import com.ucp.biomass.ar.core.math.Vector3D

/**
 * Filtro de Kalman 1D simple para suavizado temporal y mitigación de ruido sensor.
 */
class KalmanFilter1D(
    private val processNoiseQ: Double = 1e-4,
    private val measurementNoiseR: Double = 1e-2,
    initialEstimate: Double = 0.0,
    initialErrorCovariance: Double = 1.0
) {
    var estimate: Double = initialEstimate
        private set
    private var errorCovariance: Double = initialErrorCovariance

    fun update(measurement: Double): Double {
        // 1. Predicción
        val predictedCovariance = errorCovariance + processNoiseQ

        // 2. Ganancia de Kalman
        val kalmanGain = predictedCovariance / (predictedCovariance + measurementNoiseR)

        // 3. Actualización de estimación
        estimate += kalmanGain * (measurement - estimate)

        // 4. Actualización de covarianza de error
        errorCovariance = (1.0 - kalmanGain) * predictedCovariance

        return estimate
    }

    fun reset(value: Double) {
        estimate = value
        errorCovariance = 1.0
    }
}

/**
 * Filtro de Kalman tridimensional desacoplado para coordenadas espaciales R^3 (x, y, z).
 */
class KalmanFilter3D(
    processNoiseQ: Double = 1e-4,
    measurementNoiseR: Double = 1e-2,
    initialPoint: Vector3D = Vector3D.ZERO
) {
    private val filterX = KalmanFilter1D(processNoiseQ, measurementNoiseR, initialPoint.x)
    private val filterY = KalmanFilter1D(processNoiseQ, measurementNoiseR, initialPoint.y)
    private val filterZ = KalmanFilter1D(processNoiseQ, measurementNoiseR, initialPoint.z)

    fun update(measurement: Vector3D): Vector3D =
        Vector3D(
            x = filterX.update(measurement.x),
            y = filterY.update(measurement.y),
            z = filterZ.update(measurement.z)
        )

    fun reset(point: Vector3D) {
        filterX.reset(point.x)
        filterY.reset(point.y)
        filterZ.reset(point.z)
    }

    val currentEstimate: Vector3D
        get() = Vector3D(filterX.estimate, filterY.estimate, filterZ.estimate)
}
