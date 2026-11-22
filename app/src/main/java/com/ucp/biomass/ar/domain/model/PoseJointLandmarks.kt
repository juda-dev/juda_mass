package com.ucp.biomass.ar.domain.model

import com.ucp.biomass.ar.core.math.Vector3D

/**
 * Representa una articulación o hito corporal detectado por ML Kit Pose Detection.
 */
data class JointLandmark(
    val type: JointType,
    val position3D: Vector3D,     // Coordenadas métricas relativas estimadas (X, Y, Z)
    val screenX: Float,           // Coordenada horizontal normalizada [0.0 - 1.0]
    val screenY: Float,           // Coordenada vertical normalizada [0.0 - 1.0]
    val likelihood: Float         // Certeza del detector [0.0 - 1.0]
)

enum class JointType {
    NOSE,
    LEFT_EYE, RIGHT_EYE,
    LEFT_EAR, RIGHT_EAR,
    LEFT_SHOULDER, RIGHT_SHOULDER,
    LEFT_ELBOW, RIGHT_ELBOW,
    LEFT_WRIST, RIGHT_WRIST,
    LEFT_HIP, RIGHT_HIP,
    LEFT_KNEE, RIGHT_KNEE,
    LEFT_ANKLE, RIGHT_ANKLE,
    LEFT_HEEL, RIGHT_HEEL,
    LEFT_FOOT_INDEX, RIGHT_FOOT_INDEX
}

/**
 * Colección completa de hitos corporales en un cuadro de video.
 */
data class PoseJointLandmarks(
    val landmarks: Map<JointType, JointLandmark>,
    val timestampMs: Long = System.currentTimeMillis()
) {
    operator fun get(type: JointType): JointLandmark? = landmarks[type]

    fun hasJoints(vararg types: JointType): Boolean =
        types.all { landmarks[it]?.let { j -> j.likelihood > 0.45f } == true }

    /**
     * Calcula la altura del sujeto sumando las longitudes de los segmentos verticales clave:
     * Tobillo a rodilla + rodilla a cadera + cadera a hombro + hombro a corona de la cabeza.
     */
    fun estimateHeightMeters(): Double {
        val leftAnkle = landmarks[JointType.LEFT_ANKLE]?.position3D
        val rightAnkle = landmarks[JointType.RIGHT_ANKLE]?.position3D
        val leftKnee = landmarks[JointType.LEFT_KNEE]?.position3D
        val rightKnee = landmarks[JointType.RIGHT_KNEE]?.position3D
        val leftHip = landmarks[JointType.LEFT_HIP]?.position3D
        val rightHip = landmarks[JointType.RIGHT_HIP]?.position3D
        val leftShoulder = landmarks[JointType.LEFT_SHOULDER]?.position3D
        val rightShoulder = landmarks[JointType.RIGHT_SHOULDER]?.position3D
        val nose = landmarks[JointType.NOSE]?.position3D

        if (nose == null || (leftHip == null && rightHip == null)) return 1.70 // Altura estándar por defecto

        val hip = when {
            leftHip != null && rightHip != null -> leftHip.lerp(rightHip, 0.5)
            leftHip != null -> leftHip
            else -> rightHip!!
        }

        val shoulder = when {
            leftShoulder != null && rightShoulder != null -> leftShoulder.lerp(rightShoulder, 0.5)
            leftShoulder != null -> leftShoulder
            else -> nose
        }

        val knee = when {
            leftKnee != null && rightKnee != null -> leftKnee.lerp(rightKnee, 0.5)
            leftKnee != null -> leftKnee
            else -> rightKnee
        }

        val ankle = when {
            leftAnkle != null && rightAnkle != null -> leftAnkle.lerp(rightAnkle, 0.5)
            leftAnkle != null -> leftAnkle
            else -> rightAnkle
        }

        val torsoLen = hip.distanceTo(shoulder)
        val headLen = shoulder.distanceTo(nose) * 1.35
        val legLen = if (knee != null && ankle != null) {
            hip.distanceTo(knee) + knee.distanceTo(ankle) + 0.08
        } else {
            torsoLen * 1.8 // Proporción áurea estándar corporal
        }

        val totalHeight = torsoLen + headLen + legLen
        return totalHeight.coerceIn(1.20, 2.30)
    }
}
