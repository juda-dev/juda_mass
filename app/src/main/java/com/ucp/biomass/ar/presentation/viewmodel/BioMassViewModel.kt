package com.ucp.biomass.ar.presentation.viewmodel

import android.content.Context
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ucp.biomass.ar.core.util.DispatcherProvider
import com.ucp.biomass.ar.domain.model.BiomassCalculationResult
import com.ucp.biomass.ar.domain.model.JointType
import com.ucp.biomass.ar.domain.model.PoseJointLandmarks
import com.ucp.biomass.ar.domain.model.Somatotype
import com.ucp.biomass.ar.domain.model.TrackingStatus
import com.ucp.biomass.ar.domain.repository.IBodyScannerRepository
import com.ucp.biomass.ar.domain.usecase.CalibrateSomatotypeUseCase
import com.ucp.biomass.ar.domain.usecase.CheckSensorTrackingUseCase
import com.ucp.biomass.ar.domain.usecase.EstimateBiomassUseCase
import com.ucp.biomass.ar.presentation.util.JuDaMassSnapshotGenerator
import kotlin.math.abs
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel principal de JuDa Mass (MVVM Architecture).
 *
 * Conecta los flujos de visión por computadora en tiempo real e implementa
 * estabilización temporal con retención de 2 segundos para lecturas calmas y precisas.
 */
class BioMassViewModel(
    private val repository: IBodyScannerRepository,
    private val estimateBiomassUseCase: EstimateBiomassUseCase,
    private val calibrateSomatotypeUseCase: CalibrateSomatotypeUseCase,
    private val checkSensorTrackingUseCase: CheckSensorTrackingUseCase,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    companion object {
        private const val STABILIZATION_HOLD_MS = 2000L // Retención de 2 segundos para lecturas calmas y estables
        private const val TRACKING_LOST_GRACE_PERIOD_MS = 1500L // Período de gracia defensivo ante cuadros caídos
    }

    private val _uiState = MutableStateFlow(BioMassUiState())
    val uiState: StateFlow<BioMassUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ScannerEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<ScannerEvent> = _events.asSharedFlow()

    private val actionChannel = Channel<ViewModelAction>(Channel.BUFFERED)
    val actions = actionChannel.receiveAsFlow()

    // Control de retención y estabilidad temporal (2 segundos)
    private var lastStableUpdateTimeMs = 0L
    private var lastValidDetectionTimeMs = 0L
    private var lockedCalculationResult: BiomassCalculationResult? = null
    private var lockedLandmarks: PoseJointLandmarks? = null
    private var lockedGeometries: Map<com.ucp.biomass.ar.domain.model.AnatomicalSegment, com.ucp.biomass.ar.domain.model.SegmentGeometry> = emptyMap()
    private var lockedStatus: TrackingStatus = TrackingStatus.SEARCHING
    private var lockedConfidence: Double = 0.0

    init {
        observeSensorDataStreams()
    }

    private fun observeSensorDataStreams() {
        viewModelScope.launch(dispatchers.default) {
            combine(
                repository.trackingStatus,
                repository.poseLandmarks,
                repository.segmentGeometries,
                repository.sensorConfidence,
                repository.activeSomatotype
            ) { status, landmarks, geometries, confidence, somatotype ->
                val now = System.currentTimeMillis()
                val heightM = _uiState.value.userCalibratedHeightM ?: landmarks?.estimateHeightMeters() ?: 1.74

                val isPersonDetected = landmarks != null && geometries.isNotEmpty()
                if (isPersonDetected) {
                    lastValidDetectionTimeMs = now
                    lockedLandmarks = landmarks
                    lockedGeometries = geometries
                    lockedStatus = status
                    lockedConfidence = confidence
                }

                val isWithinGracePeriod = (now - lastValidDetectionTimeMs) < TRACKING_LOST_GRACE_PERIOD_MS && lockedCalculationResult != null

                val calculationResult = when {
                    isPersonDetected -> {
                        val rawResult = estimateBiomassUseCase(
                            geometries = geometries,
                            somatotype = somatotype,
                            trackingStatus = status,
                            sensorConfidence = confidence,
                            heightMeters = heightM
                        )

                        // Retener valores durante 2 segundos para permitir lectura cómoda y eliminar parpadeos
                        if (lockedCalculationResult == null || (now - lastStableUpdateTimeMs) >= STABILIZATION_HOLD_MS) {
                            lastStableUpdateTimeMs = now
                            lockedCalculationResult = rawResult
                            rawResult
                        } else {
                            lockedCalculationResult!!
                        }
                    }
                    isWithinGracePeriod -> {
                        lockedCalculationResult!!
                    }
                    else -> {
                        lockedCalculationResult = null
                        lastStableUpdateTimeMs = 0L
                        lockedLandmarks = null
                        lockedGeometries = emptyMap()
                        lockedStatus = TrackingStatus.SEARCHING
                        lockedConfidence = 0.0
                        BiomassCalculationResult.EMPTY.copy(
                            somatotype = somatotype,
                            trackingStatus = TrackingStatus.SEARCHING
                        )
                    }
                }

                val effectiveLandmarks = if (isPersonDetected) landmarks else if (isWithinGracePeriod) lockedLandmarks else null
                val effectiveGeometries = if (isPersonDetected) geometries else if (isWithinGracePeriod) lockedGeometries else emptyMap()
                val effectiveStatus = if (isPersonDetected) status else if (isWithinGracePeriod) lockedStatus else TrackingStatus.SEARCHING
                val effectiveConfidence = if (isPersonDetected) confidence else if (isWithinGracePeriod) lockedConfidence else 0.0

                val suggested = if (effectiveLandmarks != null) {
                    computeSuggestedSomatotype(effectiveLandmarks, heightM)
                } else null

                _uiState.update { currentState ->
                    currentState.copy(
                        calculationResult = calculationResult,
                        trackingStatus = effectiveStatus,
                        poseLandmarks = effectiveLandmarks,
                        segmentGeometries = effectiveGeometries,
                        activeSomatotype = somatotype,
                        suggestedSomatotype = suggested,
                        sensorConfidence = effectiveConfidence
                    )
                }
            }.collect {}
        }
    }

    private fun computeSuggestedSomatotype(landmarks: PoseJointLandmarks, heightM: Double): Somatotype? {
        val ls = landmarks[JointType.LEFT_SHOULDER]
        val rs = landmarks[JointType.RIGHT_SHOULDER]
        val lh = landmarks[JointType.LEFT_HIP]
        val rh = landmarks[JointType.RIGHT_HIP]
        val nose = landmarks[JointType.NOSE]
        val leftAnkle = landmarks[JointType.LEFT_ANKLE]
        val rightAnkle = landmarks[JointType.RIGHT_ANKLE]
        if (ls == null || rs == null || lh == null || rh == null) return null

        val topY = nose?.screenY ?: 0.20f
        val bottomY = when {
            leftAnkle != null && rightAnkle != null -> (leftAnkle.screenY + rightAnkle.screenY) * 0.5f
            leftAnkle != null -> leftAnkle.screenY
            rightAnkle != null -> rightAnkle.screenY
            else -> 0.90f
        }
        val personHeightFrac = kotlin.math.max(0.25f, kotlin.math.abs(bottomY - topY) * 1.10f)

        val shoulderFrac = kotlin.math.abs(ls.screenX - rs.screenX)
        val hipFrac = kotlin.math.abs(lh.screenX - rh.screenX)

        val shoulderW = (heightM * (shoulderFrac / personHeightFrac).toDouble()).coerceIn(0.33, 0.54)
        val hipW = (heightM * (hipFrac / personHeightFrac).toDouble()).coerceIn(0.25, 0.44)

        return Somatotype.suggestFromProportions(shoulderW, hipW, heightM)
    }

    fun processCameraFrame(imageProxy: ImageProxy) {
        repository.processCameraFrame(imageProxy)
    }

    fun onCameraPermissionGranted() {
        _uiState.update { it.copy(hasCameraPermission = true) }
        startScanning()
    }

    fun startScanning() {
        _uiState.update { it.copy(isScanningActive = true) }
        repository.startScanning()
    }

    fun stopScanning() {
        _uiState.update { it.copy(isScanningActive = false) }
        repository.stopScanning()
    }

    fun selectSomatotype(somatotype: Somatotype) {
        calibrateSomatotypeUseCase(somatotype)
        // Forzar actualización inmediata al cambiar el somatotipo manualmente
        lockedCalculationResult = null
        lastStableUpdateTimeMs = 0L
        viewModelScope.launch {
            _events.emit(ScannerEvent.SomatotypeCalibrated(somatotype))
        }
    }

    fun toggleHudVisibility() {
        _uiState.update { it.copy(isHudVisible = !it.isHudVisible) }
    }

    fun toggleDetailsSheet() {
        _uiState.update { it.copy(isDetailsSheetExpanded = !it.isDetailsSheetExpanded) }
    }

    fun openSomatotypeGuide() {
        _uiState.update { it.copy(isSomatotypeGuideVisible = true) }
    }

    fun closeSomatotypeGuide() {
        _uiState.update { it.copy(isSomatotypeGuideVisible = false) }
    }

    fun openHeightDialog() {
        _uiState.update { it.copy(isHeightDialogVisible = true) }
    }

    fun closeHeightDialog() {
        _uiState.update { it.copy(isHeightDialogVisible = false) }
    }

    fun setCalibratedHeight(heightM: Double?) {
        _uiState.update { it.copy(userCalibratedHeightM = heightM) }
        repository.setCalibratedHeight(heightM)
        // Forzar recalibración inmediata
        lockedCalculationResult = null
        lastStableUpdateTimeMs = 0L
    }

    /**
     * Genera y guarda la imagen/recuerdo con la fotografía real de la persona,
     * todos los datos calculados, fecha y hora, y la firma "by: JuDa Dev".
     */
    fun captureSnapshot(context: Context) {
        val currentState = _uiState.value
        if (!currentState.isBodyDetected) {
            _uiState.update { it.copy(snapshotMessage = "⚠️ Primero enfoque a una persona para generar el recuerdo") }
            return
        }

        val cameraPhoto = repository.getLatestCameraFrame()

        viewModelScope.launch(dispatchers.default) {
            val saveResult = JuDaMassSnapshotGenerator.generateAndSaveSnapshot(
                context = context,
                result = currentState.calculationResult,
                somatotype = currentState.activeSomatotype,
                effectiveHeightM = currentState.effectiveHeightMeters,
                personPhoto = cameraPhoto
            )

            withContext(dispatchers.main) {
                saveResult.fold(
                    onSuccess = { path ->
                        _uiState.update { it.copy(snapshotMessage = "✨ Recuerdo guardado con éxito en Galería ($path)") }
                        _events.emit(ScannerEvent.SnapshotSaved(path))
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(snapshotMessage = "Error al guardar recuerdo: ${error.localizedMessage}") }
                    }
                )
            }
        }
    }

    fun clearSnapshotMessage() {
        _uiState.update { it.copy(snapshotMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopScanning()
    }
}

sealed interface ViewModelAction {
    data object RequestCameraPermission : ViewModelAction
    data object ResetCalibration : ViewModelAction
}
