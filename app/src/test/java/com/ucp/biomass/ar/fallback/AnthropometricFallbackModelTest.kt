package com.ucp.biomass.ar.fallback

import com.ucp.biomass.ar.core.math.Vector3D
import com.ucp.biomass.ar.data.fallback.AnthropometricFallbackModel
import com.ucp.biomass.ar.domain.model.AnatomicalSegment
import com.ucp.biomass.ar.domain.model.JointLandmark
import com.ucp.biomass.ar.domain.model.JointType
import com.ucp.biomass.ar.domain.model.PoseJointLandmarks
import com.ucp.biomass.ar.domain.model.Somatotype
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnthropometricFallbackModelTest {

    private val fallbackModel = AnthropometricFallbackModel()

    @Test
    fun testFallbackGenerationAndRollbackBuffering() {
        val landmarksMap = mutableMapOf<JointType, JointLandmark>()
        fun addJoint(type: JointType, x: Double, y: Double, z: Double) {
            landmarksMap[type] = JointLandmark(
                type = type,
                position3D = Vector3D(x, y, z),
                screenX = 0.5f,
                screenY = 0.5f,
                likelihood = 0.9f
            )
        }

        addJoint(JointType.NOSE, 0.0, 0.65, 2.0)
        addJoint(JointType.LEFT_SHOULDER, -0.20, 0.45, 2.0)
        addJoint(JointType.RIGHT_SHOULDER, 0.20, 0.45, 2.0)
        addJoint(JointType.LEFT_HIP, -0.12, 0.0, 2.0)
        addJoint(JointType.RIGHT_HIP, 0.12, 0.0, 2.0)
        addJoint(JointType.LEFT_KNEE, -0.12, -0.42, 2.0)
        addJoint(JointType.RIGHT_KNEE, 0.12, -0.42, 2.0)
        addJoint(JointType.LEFT_ANKLE, -0.12, -0.80, 2.0)
        addJoint(JointType.RIGHT_ANKLE, 0.12, -0.80, 2.0)

        val pose = PoseJointLandmarks(landmarksMap)

        // 1. Generar geometrías en modo de respaldo con confianza suficiente para almacenarse en buffer
        val geometries = fallbackModel.generateFallbackGeometries(
            landmarks = pose,
            somatotype = Somatotype.MESOMORPH,
            depthConfidence = 0.60
        )

        assertFalse("Las geometrías generadas no deben estar vacías", geometries.isEmpty())
        assertTrue("Debe contener el segmento de la Cabeza", geometries.containsKey(AnatomicalSegment.HEAD))
        assertTrue("Debe contener el segmento del Tórax", geometries.containsKey(AnatomicalSegment.UPPER_TORSO))
        assertTrue("Debe contener el segmento del Muslo Izq", geometries.containsKey(AnatomicalSegment.LEFT_THIGH))

        // 2. Estrategia de Rollback Defensivo: Si los hitos de repente son nulos (pérdida total de visión)
        val rollbackGeometries = fallbackModel.rollbackOrFallback(
            landmarks = null,
            somatotype = Somatotype.MESOMORPH,
            depthConfidence = 0.0
        )

        assertFalse("El rollback defensivo debe retornar las últimas geometrías válidas guardadas en buffer", rollbackGeometries.isEmpty())
        assertNotNull(rollbackGeometries[AnatomicalSegment.UPPER_TORSO])
    }
}
