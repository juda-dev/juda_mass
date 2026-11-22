package com.ucp.biomass.ar.domain.repository

import androidx.camera.core.ImageProxy
import com.ucp.biomass.ar.domain.model.AnatomicalSegment
import com.ucp.biomass.ar.domain.model.PoseJointLandmarks
import com.ucp.biomass.ar.domain.model.SegmentGeometry
import com.ucp.biomass.ar.domain.model.Somatotype
import com.ucp.biomass.ar.domain.model.TrackingStatus
import kotlinx.coroutines.flow.StateFlow

/**
 * Repositorio del Escáner Biomecánico que abstrae la captura de hardware,
 * la visión por computadora en tiempo real y el modelo de aproximación matemática.
 */
interface IBodyScannerRepository {
    val trackingStatus: StateFlow<TrackingStatus>
    val poseLandmarks: StateFlow<PoseJointLandmarks?>
    val segmentGeometries: StateFlow<Map<AnatomicalSegment, SegmentGeometry>>
    val sensorConfidence: StateFlow<Double>
    val activeSomatotype: StateFlow<Somatotype>
    val calibratedHeight: StateFlow<Double?>

    fun startScanning()
    fun stopScanning()
    fun setSomatotype(somatotype: Somatotype)
    fun setCalibratedHeight(heightM: Double?)
    fun processCameraFrame(imageProxy: ImageProxy)
    fun processManualDepthFallback(depthSampleConfidence: Double)
    fun getLatestCameraFrame(): android.graphics.Bitmap?
}
