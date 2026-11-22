package com.ucp.biomass.ar.data.repository

import android.content.Context
import androidx.camera.core.ImageProxy
import com.ucp.biomass.ar.core.math.Vector3D
import com.ucp.biomass.ar.core.util.DispatcherProvider
import com.ucp.biomass.ar.data.arcore.ArCoreSessionHelper
import com.ucp.biomass.ar.data.fallback.AnthropometricFallbackModel
import com.ucp.biomass.ar.data.fusion.SensorFusionEngine
import com.ucp.biomass.ar.data.mlkit.MlKitPoseDetectorHelper
import com.ucp.biomass.ar.domain.model.AnatomicalSegment
import com.ucp.biomass.ar.domain.model.JointLandmark
import com.ucp.biomass.ar.domain.model.JointType
import com.ucp.biomass.ar.domain.model.PoseJointLandmarks
import com.ucp.biomass.ar.domain.model.SegmentGeometry
import com.ucp.biomass.ar.domain.model.Somatotype
import com.ucp.biomass.ar.domain.model.TrackingStatus
import com.ucp.biomass.ar.domain.repository.IBodyScannerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Implementación de producción de IBodyScannerRepository.
 * Gestiona el análisis continuo de cuadros de cámara en hilos de background.
 */
class BodyScannerRepositoryImpl(
    private val context: Context,
    private val arCoreHelper: ArCoreSessionHelper,
    private val mlKitHelper: MlKitPoseDetectorHelper,
    private val fusionEngine: SensorFusionEngine,
    private val fallbackModel: AnthropometricFallbackModel,
    private val dispatchers: DispatcherProvider
) : IBodyScannerRepository {

    private val repositoryScope = CoroutineScope(dispatchers.default + Job())

    private val _trackingStatus = MutableStateFlow(TrackingStatus.SEARCHING)
    override val trackingStatus: StateFlow<TrackingStatus> = _trackingStatus.asStateFlow()

    private val _poseLandmarks = MutableStateFlow<PoseJointLandmarks?>(null)
    override val poseLandmarks: StateFlow<PoseJointLandmarks?> = _poseLandmarks.asStateFlow()

    private val _segmentGeometries = MutableStateFlow<Map<AnatomicalSegment, SegmentGeometry>>(emptyMap())
    override val segmentGeometries: StateFlow<Map<AnatomicalSegment, SegmentGeometry>> = _segmentGeometries.asStateFlow()

    private val _sensorConfidence = MutableStateFlow(0.0)
    override val sensorConfidence: StateFlow<Double> = _sensorConfidence.asStateFlow()

    private val _activeSomatotype = MutableStateFlow(Somatotype.MESOMORPH)
    override val activeSomatotype: StateFlow<Somatotype> = _activeSomatotype.asStateFlow()

    private val _calibratedHeight = MutableStateFlow<Double?>(null)
    override val calibratedHeight: StateFlow<Double?> = _calibratedHeight.asStateFlow()

    private var lastValidDetectionTimeMs = 0L

    override fun startScanning() {
        _trackingStatus.value = TrackingStatus.SEARCHING
        repositoryScope.launch {
            withContext(dispatchers.main) {
                arCoreHelper.createSession()
            }
        }
    }

    override fun stopScanning() {
        _trackingStatus.value = TrackingStatus.SEARCHING
        _poseLandmarks.value = null
        _segmentGeometries.value = emptyMap()
        _sensorConfidence.value = 0.0
        lastValidDetectionTimeMs = 0L
        arCoreHelper.pause()
    }

    override fun setSomatotype(somatotype: Somatotype) {
        _activeSomatotype.value = somatotype
        val currentLandmarks = _poseLandmarks.value
        if (currentLandmarks != null) {
            repositoryScope.launch {
                val updatedGeom = fallbackModel.generateFallbackGeometries(
                    landmarks = currentLandmarks,
                    somatotype = somatotype,
                    depthConfidence = _sensorConfidence.value,
                    calibratedHeightM = _calibratedHeight.value
                )
                _segmentGeometries.value = updatedGeom
            }
        }
    }

    override fun setCalibratedHeight(heightM: Double?) {
        _calibratedHeight.value = heightM
        val currentLandmarks = _poseLandmarks.value
        if (currentLandmarks != null) {
            repositoryScope.launch {
                val updatedGeom = fallbackModel.generateFallbackGeometries(
                    landmarks = currentLandmarks,
                    somatotype = _activeSomatotype.value,
                    depthConfidence = _sensorConfidence.value,
                    calibratedHeightM = heightM
                )
                _segmentGeometries.value = updatedGeom
            }
        }
    }

    /**
     * Procesa cada cuadro capturado por CameraX con ML Kit Pose Detection.
     * Incorpora período de gracia defensivo para absorber cuadros caídos o desenfoques breves.
     */
    override fun processCameraFrame(imageProxy: ImageProxy) {
        mlKitHelper.processImageProxy(imageProxy) { detectedLandmarks ->
            repositoryScope.launch {
                val now = System.currentTimeMillis()
                if (detectedLandmarks != null && detectedLandmarks.landmarks.isNotEmpty()) {
                    lastValidDetectionTimeMs = now
                    _poseLandmarks.value = detectedLandmarks
                    _trackingStatus.value = if (arCoreHelper.isDepthSupported) {
                        TrackingStatus.ARCORE_DEPTH_ACTIVE
                    } else {
                        TrackingStatus.FALLBACK_ANTHROPOMETRIC
                    }
                    _sensorConfidence.value = 0.88

                    // Construir las geometrías a partir de los puntos reales medidos en pantalla
                    val geometries = fallbackModel.generateFallbackGeometries(
                        landmarks = detectedLandmarks,
                        somatotype = _activeSomatotype.value,
                        depthConfidence = _sensorConfidence.value,
                        calibratedHeightM = _calibratedHeight.value
                    )
                    _segmentGeometries.value = geometries
                } else {
                    // Estrategia de Rollback: si la pérdida de detección es menor a 1500ms, mantenemos el estado
                    if (now - lastValidDetectionTimeMs > 1500L) {
                        _poseLandmarks.value = null
                        _segmentGeometries.value = emptyMap()
                        _trackingStatus.value = TrackingStatus.SEARCHING
                        _sensorConfidence.value = 0.0
                    }
                }
            }
        }
    }

    override fun processManualDepthFallback(depthSampleConfidence: Double) {
        _sensorConfidence.value = depthSampleConfidence
    }

    override fun getLatestCameraFrame(): android.graphics.Bitmap? = mlKitHelper.latestFrameBitmap

    /**
     * Postura sintética de prueba para tests unitarios.
     */
    fun createBaselineCalibrationPose(): PoseJointLandmarks {
        val map = mutableMapOf<JointType, JointLandmark>()
        fun add(type: JointType, x: Double, y: Double, z: Double, sx: Float, sy: Float) {
            map[type] = JointLandmark(
                type = type,
                position3D = Vector3D(x, y, z),
                screenX = sx,
                screenY = sy,
                likelihood = 0.95f
            )
        }

        add(JointType.NOSE, 0.0, 0.65, 2.0, 0.50f, 0.16f)
        add(JointType.LEFT_EAR, -0.08, 0.66, 2.0, 0.44f, 0.15f)
        add(JointType.RIGHT_EAR, 0.08, 0.66, 2.0, 0.56f, 0.15f)
        add(JointType.LEFT_SHOULDER, -0.22, 0.46, 2.0, 0.35f, 0.26f)
        add(JointType.RIGHT_SHOULDER, 0.22, 0.46, 2.0, 0.65f, 0.26f)
        add(JointType.LEFT_ELBOW, -0.32, 0.20, 2.0, 0.28f, 0.40f)
        add(JointType.RIGHT_ELBOW, 0.32, 0.20, 2.0, 0.72f, 0.40f)
        add(JointType.LEFT_WRIST, -0.36, -0.06, 2.0, 0.24f, 0.54f)
        add(JointType.RIGHT_WRIST, 0.36, -0.06, 2.0, 0.76f, 0.54f)
        add(JointType.LEFT_HIP, -0.13, 0.0, 2.0, 0.42f, 0.52f)
        add(JointType.RIGHT_HIP, 0.13, 0.0, 2.0, 0.58f, 0.52f)
        add(JointType.LEFT_KNEE, -0.14, -0.42, 2.0, 0.41f, 0.72f)
        add(JointType.RIGHT_KNEE, 0.14, -0.42, 2.0, 0.59f, 0.72f)
        add(JointType.LEFT_ANKLE, -0.14, -0.82, 2.0, 0.40f, 0.90f)
        add(JointType.RIGHT_ANKLE, 0.14, -0.82, 2.0, 0.60f, 0.90f)

        return PoseJointLandmarks(map)
    }

    fun updateLandmarks(landmarks: PoseJointLandmarks?) {
        _poseLandmarks.value = landmarks
        if (landmarks != null) {
            val geometries = fallbackModel.generateFallbackGeometries(
                landmarks = landmarks,
                somatotype = _activeSomatotype.value,
                depthConfidence = 0.88,
                calibratedHeightM = _calibratedHeight.value
            )
            _segmentGeometries.value = geometries
            _trackingStatus.value = TrackingStatus.ARCORE_DEPTH_ACTIVE
            _sensorConfidence.value = 0.88
        } else {
            _segmentGeometries.value = emptyMap()
            _trackingStatus.value = TrackingStatus.SEARCHING
            _sensorConfidence.value = 0.0
        }
    }
}
