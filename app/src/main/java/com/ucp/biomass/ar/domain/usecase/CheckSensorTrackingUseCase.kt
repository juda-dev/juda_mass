package com.ucp.biomass.ar.domain.usecase

import com.ucp.biomass.ar.domain.model.TrackingStatus
import com.ucp.biomass.ar.domain.repository.IBodyScannerRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Caso de uso para monitorear el estado de salud y certidumbre del rastreo de sensores.
 */
class CheckSensorTrackingUseCase(
    private val repository: IBodyScannerRepository
) {
    val trackingStatus: StateFlow<TrackingStatus> = repository.trackingStatus
    val sensorConfidence: StateFlow<Double> = repository.sensorConfidence

    fun isTrackingDegraded(): Boolean = repository.trackingStatus.value.isDegraded
}
