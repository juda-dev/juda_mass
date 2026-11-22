package com.ucp.biomass.ar.presentation.viewmodel

import com.ucp.biomass.ar.domain.model.AnatomicalSegment
import com.ucp.biomass.ar.domain.model.BiomassCalculationResult
import com.ucp.biomass.ar.domain.model.PoseJointLandmarks
import com.ucp.biomass.ar.domain.model.SegmentGeometry
import com.ucp.biomass.ar.domain.model.Somatotype
import com.ucp.biomass.ar.domain.model.TrackingStatus

/**
 * Estado inmutable de la interfaz de usuario de JuDaMass.
 */
data class BioMassUiState(
    val calculationResult: BiomassCalculationResult = BiomassCalculationResult.EMPTY,
    val trackingStatus: TrackingStatus = TrackingStatus.SEARCHING,
    val poseLandmarks: PoseJointLandmarks? = null,
    val segmentGeometries: Map<AnatomicalSegment, SegmentGeometry> = emptyMap(),
    val activeSomatotype: Somatotype = Somatotype.MESOMORPH,
    val suggestedSomatotype: Somatotype? = null,        // Somatotipo inferido por proporciones en cámara
    val sensorConfidence: Double = 0.0,
    val isScanningActive: Boolean = true,
    val hasCameraPermission: Boolean = false,
    val isHudVisible: Boolean = true,                   // Control de visibilidad del panel sobre la cámara
    val isDetailsSheetExpanded: Boolean = false,        // Desglose completo de integrales
    val isSomatotypeGuideVisible: Boolean = false,      // Diálogo explicativo "¿Cuál es mi somatotipo?"
    val isHeightDialogVisible: Boolean = false,         // Diálogo de ajuste preciso de estatura
    val userCalibratedHeightM: Double? = null,          // Calibración manual de estatura por usuario
    val userErrorMessage: String? = null,
    val snapshotMessage: String? = null                 // Mensaje tras guardar el recuerdo
) {
    val isBodyDetected: Boolean
        get() = poseLandmarks != null && segmentGeometries.isNotEmpty() && calculationResult.totalMassKg > 10.0

    val effectiveHeightMeters: Double
        get() = userCalibratedHeightM ?: calculationResult.heightMeters

    val statusBadgeText: String
        get() = when {
            !hasCameraPermission -> "PERMISO DE CÁMARA REQUERIDO"
            !isBodyDetected -> "BUSCANDO PERSONA (A 2 METROS)..."
            trackingStatus == TrackingStatus.ARCORE_DEPTH_ACTIVE -> "CUERPO BLOQUEADO: INTEGRANDO ARCORE DEPTH"
            else -> "CUERPO BLOQUEADO: INTEGRANDO CÁMARA 3D"
        }
}

sealed interface ScannerEvent {
    data class StatusChanged(val status: TrackingStatus) : ScannerEvent
    data class SomatotypeCalibrated(val somatotype: Somatotype) : ScannerEvent
    data class SensorWarning(val message: String) : ScannerEvent
    data class SnapshotSaved(val filePath: String) : ScannerEvent
}
