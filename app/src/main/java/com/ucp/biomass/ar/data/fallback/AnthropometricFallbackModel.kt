package com.ucp.biomass.ar.data.fallback

import com.ucp.biomass.ar.core.math.Vector3D
import com.ucp.biomass.ar.core.util.KalmanFilter1D
import com.ucp.biomass.ar.domain.model.AnatomicalSegment
import com.ucp.biomass.ar.domain.model.JointType
import com.ucp.biomass.ar.domain.model.PoseJointLandmarks
import com.ucp.biomass.ar.domain.model.SegmentGeometry
import com.ucp.biomass.ar.domain.model.Somatotype
import kotlin.math.abs
import kotlin.math.max

/**
 * Modelo Antropométrico de Alta Precisión y Estrategia de Rollback Defensiva.
 *
 * Basado en las tablas biomecánicas de De Leva (1996), Hanavan (1964) y
 * los modelos elipsoidales de McConville.
 *
 * Escala todas las geometrías tridimensionales combinando:
 * 1. La estatura calibrada (métrica real ground truth).
 * 2. Las proporciones proyectivas reales medidas en los píxeles de la cámara
 *    (ancho biacromial de hombros, ancho biiliaco de caderas y extremidades).
 * 3. El gradiente radial y factor de profundidad del Somatotipo.
 */
class AnthropometricFallbackModel {

    // Filtros de Kalman para suavizar la profundidad y prevenir ruido en el modo de respaldo
    private val depthFilters = mutableMapOf<AnatomicalSegment, KalmanFilter1D>()

    // Buffer de último estado geométrico de alta confianza (Estrategia de Rollback)
    private var lastValidGeometries: Map<AnatomicalSegment, SegmentGeometry>? = null

    /**
     * Construye las geometrías de los segmentos anatómicos a partir de los hitos de pose reales
     * medidos en la cámara.
     */
    fun generateFallbackGeometries(
        landmarks: PoseJointLandmarks,
        somatotype: Somatotype,
        depthConfidence: Double,
        calibratedHeightM: Double? = null
    ): Map<AnatomicalSegment, SegmentGeometry> {
        val heightM = (calibratedHeightM ?: landmarks.estimateHeightMeters()).coerceIn(1.20, 2.30)
        val geometries = mutableMapOf<AnatomicalSegment, SegmentGeometry>()

        // 1. Obtener puntos articulares en pantalla normalizados
        val leftShoulder = landmarks[JointType.LEFT_SHOULDER]
        val rightShoulder = landmarks[JointType.RIGHT_SHOULDER]
        val leftHip = landmarks[JointType.LEFT_HIP]
        val rightHip = landmarks[JointType.RIGHT_HIP]
        val nose = landmarks[JointType.NOSE]
        val leftAnkle = landmarks[JointType.LEFT_ANKLE]
        val rightAnkle = landmarks[JointType.RIGHT_ANKLE]

        // 2. Determinar envergadura vertical en la imagen con corrección de encuadre
        val topY = nose?.screenY ?: 0.20f
        val bottomY = when {
            leftAnkle != null && rightAnkle != null -> (leftAnkle.screenY + rightAnkle.screenY) * 0.5f
            leftAnkle != null -> leftAnkle.screenY
            rightAnkle != null -> rightAnkle.screenY
            leftHip != null && rightHip != null -> {
                // Si solo se ve de cintura para arriba, extrapolamos proporcionalmente el cuerpo completo
                val hipY = (leftHip.screenY + rightHip.screenY) * 0.5f
                topY + (hipY - topY) * 1.95f
            }
            else -> 0.90f
        }
        val personHeightFrac = max(0.25f, abs(bottomY - topY) * 1.10f)

        // 3. Medir ancho de hombros real proyectivo (Biacromial)
        val shoulderFrac = if (leftShoulder != null && rightShoulder != null) {
            abs(leftShoulder.screenX - rightShoulder.screenX)
        } else {
            0.23f * personHeightFrac
        }
        val rawShoulderWidth = (heightM * (shoulderFrac / personHeightFrac).toDouble())
        val shoulderWidth = rawShoulderWidth.coerceIn(0.33, 0.54)

        // 4. Medir ancho de caderas real proyectivo (Biiliaco)
        val hipFrac = if (leftHip != null && rightHip != null) {
            abs(leftHip.screenX - rightHip.screenX)
        } else {
            0.18f * personHeightFrac
        }
        val rawHipWidth = (heightM * (hipFrac / personHeightFrac).toDouble())
        val hipWidth = rawHipWidth.coerceIn(0.25, 0.44)

        // Factor de profundidad sagital b(z) según el Somatotipo
        val torsoDepthRatio = when (somatotype) {
            Somatotype.ECTOMORPH -> 0.66
            Somatotype.MESOMORPH -> 0.74
            Somatotype.ENDOMORPH -> 0.83
        }

        // Posiciones 3D de referencia centradas en la persona
        val shoulder3DLeft = leftShoulder?.position3D ?: Vector3D(-shoulderWidth * 0.5, 0.45 * heightM, 2.0)
        val shoulder3DRight = rightShoulder?.position3D ?: Vector3D(shoulderWidth * 0.5, 0.45 * heightM, 2.0)
        val hip3DLeft = leftHip?.position3D ?: Vector3D(-hipWidth * 0.5, 0.0, 2.0)
        val hip3DRight = rightHip?.position3D ?: Vector3D(hipWidth * 0.5, 0.0, 2.0)
        val nose3D = nose?.position3D ?: Vector3D(0.0, 0.65 * heightM, 2.0)

        val shoulderMid = shoulder3DLeft.lerp(shoulder3DRight, 0.5)
        val hipMid = hip3DLeft.lerp(hip3DRight, 0.5)
        val torsoMid = shoulderMid.lerp(hipMid, 0.52)

        // 1. Cabeza (Coordenadas Esféricas / Elipsoidales)
        val leftEar = landmarks[JointType.LEFT_EAR]
        val rightEar = landmarks[JointType.RIGHT_EAR]
        val headWidth = if (leftEar != null && rightEar != null) {
            val earFrac = abs(leftEar.screenX - rightEar.screenX)
            (heightM * (earFrac / personHeightFrac).toDouble()).coerceIn(0.14, 0.19)
        } else {
            0.155 * (heightM / 1.74)
        }
        val headSemiA = headWidth * 0.5
        val rawHeadSemiB = headSemiA * 1.20
        val smoothedHeadSemiB = smoothDepth(AnatomicalSegment.HEAD, rawHeadSemiB)
        val headHeight = headSemiA * 2.25

        geometries[AnatomicalSegment.HEAD] = SegmentGeometry(
            segment = AnatomicalSegment.HEAD,
            proximalJoint = nose3D - Vector3D(0.0, headHeight * 0.40, 0.0),
            distalJoint = nose3D + Vector3D(0.0, headHeight * 0.60, 0.0),
            proximalSemiAxisA = headSemiA * 0.90,
            proximalSemiAxisB = smoothedHeadSemiB * 0.90,
            distalSemiAxisA = headSemiA,
            distalSemiAxisB = smoothedHeadSemiB,
            depthConfidence = depthConfidence
        )

        // 2. Cuello
        val neckSemiA = shoulderWidth * 0.145
        val neckSemiB = smoothDepth(AnatomicalSegment.NECK, neckSemiA * 0.95)
        geometries[AnatomicalSegment.NECK] = SegmentGeometry(
            segment = AnatomicalSegment.NECK,
            proximalJoint = shoulderMid,
            distalJoint = nose3D - Vector3D(0.0, headHeight * 0.35, 0.0),
            proximalSemiAxisA = neckSemiA * 1.05,
            proximalSemiAxisB = neckSemiB * 1.05,
            distalSemiAxisA = neckSemiA * 0.90,
            distalSemiAxisB = neckSemiB * 0.90,
            depthConfidence = depthConfidence
        )

        // 3. Tórax (Torso Superior)
        val thoraxSemiA1 = (shoulderWidth * 0.5) * 0.88
        val thoraxSemiB1 = smoothDepth(AnatomicalSegment.UPPER_TORSO, thoraxSemiA1 * torsoDepthRatio * 1.06)
        val thoraxSemiA2 = ((shoulderWidth + hipWidth) * 0.25) * 0.86
        val thoraxSemiB2 = thoraxSemiB1 * 0.94
        geometries[AnatomicalSegment.UPPER_TORSO] = SegmentGeometry(
            segment = AnatomicalSegment.UPPER_TORSO,
            proximalJoint = shoulderMid,
            distalJoint = torsoMid,
            proximalSemiAxisA = thoraxSemiA1,
            proximalSemiAxisB = thoraxSemiB1,
            distalSemiAxisA = thoraxSemiA2,
            distalSemiAxisB = thoraxSemiB2,
            depthConfidence = depthConfidence
        )

        // 4. Abdomen y Pelvis (Torso Inferior)
        val abdomenSemiA1 = thoraxSemiA2
        val abdomenSemiB1 = smoothDepth(AnatomicalSegment.LOWER_TORSO, (hipWidth * 0.5) * torsoDepthRatio * 1.02)
        val abdomenSemiA2 = (hipWidth * 0.5) * 0.92
        val abdomenSemiB2 = abdomenSemiB1 * 1.02
        geometries[AnatomicalSegment.LOWER_TORSO] = SegmentGeometry(
            segment = AnatomicalSegment.LOWER_TORSO,
            proximalJoint = torsoMid,
            distalJoint = hipMid,
            proximalSemiAxisA = abdomenSemiA1,
            proximalSemiAxisB = abdomenSemiB1,
            distalSemiAxisA = abdomenSemiA2,
            distalSemiAxisB = abdomenSemiB2,
            depthConfidence = depthConfidence
        )

        // Brazos y Piernas medidos proporcionalmente a hombros, caderas y estatura
        buildArm(geometries, landmarks, somatotype, isLeft = true, depthConfidence, heightM, shoulderWidth)
        buildArm(geometries, landmarks, somatotype, isLeft = false, depthConfidence, heightM, shoulderWidth)

        buildLeg(geometries, landmarks, somatotype, isLeft = true, depthConfidence, heightM, hipWidth)
        buildLeg(geometries, landmarks, somatotype, isLeft = false, depthConfidence, heightM, hipWidth)

        if (depthConfidence >= 0.35) {
            lastValidGeometries = geometries
        }

        return geometries
    }

    fun rollbackOrFallback(
        landmarks: PoseJointLandmarks?,
        somatotype: Somatotype,
        depthConfidence: Double,
        calibratedHeightM: Double? = null
    ): Map<AnatomicalSegment, SegmentGeometry> {
        return if (landmarks != null && landmarks.landmarks.isNotEmpty()) {
            generateFallbackGeometries(landmarks, somatotype, depthConfidence, calibratedHeightM)
        } else {
            lastValidGeometries ?: emptyMap()
        }
    }

    private fun buildArm(
        map: MutableMap<AnatomicalSegment, SegmentGeometry>,
        landmarks: PoseJointLandmarks,
        somatotype: Somatotype,
        isLeft: Boolean,
        depthConfidence: Double,
        heightM: Double,
        shoulderWidth: Double
    ) {
        val shoulderType = if (isLeft) JointType.LEFT_SHOULDER else JointType.RIGHT_SHOULDER
        val elbowType = if (isLeft) JointType.LEFT_ELBOW else JointType.RIGHT_ELBOW
        val wristType = if (isLeft) JointType.LEFT_WRIST else JointType.RIGHT_WRIST

        val shoulder = landmarks[shoulderType]?.position3D ?: Vector3D(if (isLeft) -shoulderWidth * 0.5 else shoulderWidth * 0.5, 0.45 * heightM, 2.0)
        val elbow = landmarks[elbowType]?.position3D ?: (shoulder + Vector3D(if (isLeft) -0.15 else 0.15, -0.28, 0.0))
        val wrist = landmarks[wristType]?.position3D ?: (elbow + Vector3D(0.0, -0.26, 0.0))

        val somatotypeArmMultiplier = when (somatotype) {
            Somatotype.ECTOMORPH -> 0.90
            Somatotype.MESOMORPH -> 1.05
            Somatotype.ENDOMORPH -> 1.08
        }
        val armScale = somatotypeArmMultiplier * (heightM / 1.74) * (shoulderWidth / 0.43)

        val upperArmSegment = if (isLeft) AnatomicalSegment.LEFT_UPPER_ARM else AnatomicalSegment.RIGHT_UPPER_ARM
        val upperArmSemiA1 = 0.046 * armScale
        val upperArmSemiB1 = smoothDepth(upperArmSegment, upperArmSemiA1 * 0.93)
        val upperArmSemiA2 = 0.036 * armScale
        val upperArmSemiB2 = upperArmSemiB1 * 0.84

        map[upperArmSegment] = SegmentGeometry(
            segment = upperArmSegment,
            proximalJoint = shoulder,
            distalJoint = elbow,
            proximalSemiAxisA = upperArmSemiA1,
            proximalSemiAxisB = upperArmSemiB1,
            distalSemiAxisA = upperArmSemiA2,
            distalSemiAxisB = upperArmSemiB2,
            depthConfidence = depthConfidence
        )

        val forearmSegment = if (isLeft) AnatomicalSegment.LEFT_FOREARM else AnatomicalSegment.RIGHT_FOREARM
        val forearmSemiA1 = upperArmSemiA2
        val forearmSemiB1 = upperArmSemiB2
        val forearmSemiA2 = 0.029 * armScale
        val forearmSemiB2 = smoothDepth(forearmSegment, forearmSemiA2 * 0.88)

        map[forearmSegment] = SegmentGeometry(
            segment = forearmSegment,
            proximalJoint = elbow,
            distalJoint = wrist,
            proximalSemiAxisA = forearmSemiA1,
            proximalSemiAxisB = forearmSemiB1,
            distalSemiAxisA = forearmSemiA2,
            distalSemiAxisB = forearmSemiB2,
            depthConfidence = depthConfidence
        )

        val handSegment = if (isLeft) AnatomicalSegment.LEFT_HAND else AnatomicalSegment.RIGHT_HAND
        val handDistal = wrist + (wrist - elbow).normalized() * 0.15
        map[handSegment] = SegmentGeometry(
            segment = handSegment,
            proximalJoint = wrist,
            distalJoint = handDistal,
            proximalSemiAxisA = 0.034 * armScale,
            proximalSemiAxisB = smoothDepth(handSegment, 0.017 * armScale),
            distalSemiAxisA = 0.028 * armScale,
            distalSemiAxisB = 0.014 * armScale,
            depthConfidence = depthConfidence
        )
    }

    private fun buildLeg(
        map: MutableMap<AnatomicalSegment, SegmentGeometry>,
        landmarks: PoseJointLandmarks,
        somatotype: Somatotype,
        isLeft: Boolean,
        depthConfidence: Double,
        heightM: Double,
        hipWidth: Double
    ) {
        val hipType = if (isLeft) JointType.LEFT_HIP else JointType.RIGHT_HIP
        val kneeType = if (isLeft) JointType.LEFT_KNEE else JointType.RIGHT_KNEE
        val ankleType = if (isLeft) JointType.LEFT_ANKLE else JointType.RIGHT_ANKLE

        val hip = landmarks[hipType]?.position3D ?: Vector3D(if (isLeft) -hipWidth * 0.5 else hipWidth * 0.5, 0.0, 2.0)
        val knee = landmarks[kneeType]?.position3D ?: (hip + Vector3D(0.0, -0.42 * heightM * 0.5, 0.0))
        val ankle = landmarks[ankleType]?.position3D ?: (knee + Vector3D(0.0, -0.40 * heightM * 0.5, 0.0))

        val somatotypeLegMultiplier = when (somatotype) {
            Somatotype.ECTOMORPH -> 0.90
            Somatotype.MESOMORPH -> 1.05
            Somatotype.ENDOMORPH -> 1.14
        }
        val legScale = somatotypeLegMultiplier * (heightM / 1.74) * (hipWidth / 0.33)

        val thighSegment = if (isLeft) AnatomicalSegment.LEFT_THIGH else AnatomicalSegment.RIGHT_THIGH
        val thighSemiA1 = 0.078 * legScale
        val thighSemiB1 = smoothDepth(thighSegment, thighSemiA1 * 0.92)
        val thighSemiA2 = 0.055 * legScale
        val thighSemiB2 = thighSemiB1 * 0.82

        map[thighSegment] = SegmentGeometry(
            segment = thighSegment,
            proximalJoint = hip,
            distalJoint = knee,
            proximalSemiAxisA = thighSemiA1,
            proximalSemiAxisB = thighSemiB1,
            distalSemiAxisA = thighSemiA2,
            distalSemiAxisB = thighSemiB2,
            depthConfidence = depthConfidence
        )

        val shankSegment = if (isLeft) AnatomicalSegment.LEFT_SHANK else AnatomicalSegment.RIGHT_SHANK
        val shankSemiA1 = 0.052 * legScale
        val shankSemiB1 = smoothDepth(shankSegment, shankSemiA1 * 0.92)
        val shankSemiA2 = 0.035 * legScale
        val shankSemiB2 = shankSemiB1 * 0.78

        map[shankSegment] = SegmentGeometry(
            segment = shankSegment,
            proximalJoint = knee,
            distalJoint = ankle,
            proximalSemiAxisA = shankSemiA1,
            proximalSemiAxisB = shankSemiB1,
            distalSemiAxisA = shankSemiA2,
            distalSemiAxisB = shankSemiB2,
            depthConfidence = depthConfidence
        )

        val footSegment = if (isLeft) AnatomicalSegment.LEFT_FOOT else AnatomicalSegment.RIGHT_FOOT
        val footDistal = ankle + Vector3D(0.0, -0.05, 0.16)
        map[footSegment] = SegmentGeometry(
            segment = footSegment,
            proximalJoint = ankle,
            distalJoint = footDistal,
            proximalSemiAxisA = 0.038 * legScale,
            proximalSemiAxisB = smoothDepth(footSegment, 0.028 * legScale),
            distalSemiAxisA = 0.034 * legScale,
            distalSemiAxisB = 0.020 * legScale,
            depthConfidence = depthConfidence
        )
    }

    private fun smoothDepth(segment: AnatomicalSegment, rawDepth: Double): Double {
        val filter = depthFilters.getOrPut(segment) {
            KalmanFilter1D(
                processNoiseQ = 1e-4,
                measurementNoiseR = 5e-3,
                initialEstimate = rawDepth
            )
        }
        return filter.update(rawDepth)
    }
}

