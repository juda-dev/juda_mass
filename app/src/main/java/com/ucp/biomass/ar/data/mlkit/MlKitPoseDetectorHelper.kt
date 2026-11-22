package com.ucp.biomass.ar.data.mlkit

import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.Image
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.ucp.biomass.ar.core.math.Vector3D
import com.ucp.biomass.ar.domain.model.JointLandmark
import com.ucp.biomass.ar.domain.model.JointType
import com.ucp.biomass.ar.domain.model.PoseJointLandmarks
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Encapsula el detector de postura corporal de Google ML Kit en modo streaming preciso.
 * Mantiene en caché el último fotograma fotográfico de la persona para los recuerdos.
 */
class MlKitPoseDetectorHelper {

    private val options = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
        .build()

    private val detector: PoseDetector = PoseDetection.getClient(options)

    @Volatile
    var latestFrameBitmap: Bitmap? = null
        private set

    private var lastBitmapCaptureTimeMs = 0L

    /**
     * Procesa un cuadro de CameraX en tiempo real liberando el ImageProxy de forma segura.
     */
    @OptIn(ExperimentalGetImage::class)
    fun processImageProxy(imageProxy: ImageProxy, onResult: (PoseJointLandmarks?) -> Unit) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)

        val isRotated = rotationDegrees == 90 || rotationDegrees == 270
        val effectiveWidth = if (isRotated) mediaImage.height else mediaImage.width
        val effectiveHeight = if (isRotated) mediaImage.width else mediaImage.height

        // Capturar fotograma bitmap a intervalos controlados (máx 2 veces por segundo) para recuerdos
        val now = System.currentTimeMillis()
        if (now - lastBitmapCaptureTimeMs > 450L) {
            lastBitmapCaptureTimeMs = now
            try {
                val rawBitmap = imageProxy.toBitmap()
                if (rotationDegrees != 0) {
                    val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                    latestFrameBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                } else {
                    latestFrameBitmap = rawBitmap
                }
            } catch (_: Exception) {}
        }

        detector.process(inputImage)
            .addOnSuccessListener { pose ->
                val landmarksMap = mapPoseToDomain(pose, effectiveWidth, effectiveHeight)
                val hasCoreBody = landmarksMap.containsKey(JointType.LEFT_SHOULDER) ||
                                  landmarksMap.containsKey(JointType.RIGHT_SHOULDER) ||
                                  landmarksMap.containsKey(JointType.LEFT_HIP) ||
                                  landmarksMap.containsKey(JointType.RIGHT_HIP)

                if (landmarksMap.size >= 6 && hasCoreBody) {
                    onResult(PoseJointLandmarks(landmarksMap))
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    suspend fun detectPose(inputImage: InputImage): PoseJointLandmarks? =
        suspendCancellableCoroutine { continuation ->
            detector.process(inputImage)
                .addOnSuccessListener { pose ->
                    val landmarksMap = mapPoseToDomain(pose, inputImage.width, inputImage.height)
                    if (landmarksMap.isNotEmpty()) {
                        continuation.resume(PoseJointLandmarks(landmarksMap))
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }

    suspend fun detectPoseFromBitmap(bitmap: Bitmap, rotationDegrees: Int = 0): PoseJointLandmarks? {
        val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
        return detectPose(inputImage)
    }

    private fun mapPoseToDomain(pose: Pose, imageWidth: Int, imageHeight: Int): Map<JointType, JointLandmark> {
        val allLandmarks = pose.allPoseLandmarks
        if (allLandmarks.isEmpty()) return emptyMap()

        val result = mutableMapOf<JointType, JointLandmark>()

        for (landmark in allLandmarks) {
            val type = when (landmark.landmarkType) {
                PoseLandmark.NOSE -> JointType.NOSE
                PoseLandmark.LEFT_EYE -> JointType.LEFT_EYE
                PoseLandmark.RIGHT_EYE -> JointType.RIGHT_EYE
                PoseLandmark.LEFT_EAR -> JointType.LEFT_EAR
                PoseLandmark.RIGHT_EAR -> JointType.RIGHT_EAR
                PoseLandmark.LEFT_SHOULDER -> JointType.LEFT_SHOULDER
                PoseLandmark.RIGHT_SHOULDER -> JointType.RIGHT_SHOULDER
                PoseLandmark.LEFT_ELBOW -> JointType.LEFT_ELBOW
                PoseLandmark.RIGHT_ELBOW -> JointType.RIGHT_ELBOW
                PoseLandmark.LEFT_WRIST -> JointType.LEFT_WRIST
                PoseLandmark.RIGHT_WRIST -> JointType.RIGHT_WRIST
                PoseLandmark.LEFT_HIP -> JointType.LEFT_HIP
                PoseLandmark.RIGHT_HIP -> JointType.RIGHT_HIP
                PoseLandmark.LEFT_KNEE -> JointType.LEFT_KNEE
                PoseLandmark.RIGHT_KNEE -> JointType.RIGHT_KNEE
                PoseLandmark.LEFT_ANKLE -> JointType.LEFT_ANKLE
                PoseLandmark.RIGHT_ANKLE -> JointType.RIGHT_ANKLE
                PoseLandmark.LEFT_HEEL -> JointType.LEFT_HEEL
                PoseLandmark.RIGHT_HEEL -> JointType.RIGHT_HEEL
                PoseLandmark.LEFT_FOOT_INDEX -> JointType.LEFT_FOOT_INDEX
                PoseLandmark.RIGHT_FOOT_INDEX -> JointType.RIGHT_FOOT_INDEX
                else -> null
            } ?: continue

            val screenX = if (imageWidth > 0) (landmark.position.x / imageWidth).coerceIn(0f, 1f) else 0.5f
            val screenY = if (imageHeight > 0) (landmark.position.y / imageHeight).coerceIn(0f, 1f) else 0.5f

            val pos3D = landmark.position3D
            val vec3D = Vector3D(
                x = (screenX.toDouble() - 0.5) * 1.5,
                y = -(screenY.toDouble() - 0.5) * 2.0,
                z = pos3D.z * 0.001 + 2.0
            )

            result[type] = JointLandmark(
                type = type,
                position3D = vec3D,
                screenX = screenX,
                screenY = screenY,
                likelihood = landmark.inFrameLikelihood
            )
        }

        return result
    }

    fun close() {
        try {
            detector.close()
            latestFrameBitmap?.recycle()
            latestFrameBitmap = null
        } catch (_: Exception) {}
    }
}
