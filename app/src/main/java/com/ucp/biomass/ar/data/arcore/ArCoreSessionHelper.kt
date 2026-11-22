package com.ucp.biomass.ar.data.arcore

import android.content.Context
import android.media.Image
import com.google.ar.core.ArCoreApk
import com.google.ar.core.CameraIntrinsics
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.ucp.biomass.ar.core.math.Vector3D
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Gestor del ciclo de vida y lectura de profundidad de Google ARCore Depth API.
 */
class ArCoreSessionHelper(private val context: Context) {

    var session: Session? = null
        private set
    var isDepthSupported: Boolean = false
        private set

    /**
     * Inicializa la sesión ARCore y configura el modo de profundidad automática.
     * Si el dispositivo no tiene soporte ARCore, maneja la excepción defensivamente.
     */
    fun createSession(): Boolean {
        return try {
            val availability = ArCoreApk.getInstance().checkAvailability(context)
            if (!availability.isSupported) {
                isDepthSupported = false
                return false
            }

            val newSession = Session(context)
            val config = Config(newSession)

            // Configuración de la Depth API
            isDepthSupported = newSession.isDepthModeSupported(Config.DepthMode.AUTOMATIC)
            if (isDepthSupported) {
                config.depthMode = Config.DepthMode.AUTOMATIC
            } else {
                config.depthMode = Config.DepthMode.DISABLED
            }

            config.focusMode = Config.FocusMode.AUTO
            newSession.configure(config)
            session = newSession
            true
        } catch (e: UnavailableException) {
            isDepthSupported = false
            false
        } catch (e: Exception) {
            isDepthSupported = false
            false
        }
    }

    /**
     * Extrae la distancia métrica en metros Z y la confianza en un punto normalizado (u, v).
     */
    fun sampleDepthAtNormalizedPoint(
        frame: Frame,
        u: Float,
        v: Float
    ): DepthSample? {
        var depthImage: Image? = null
        var confidenceImage: Image? = null
        return try {
            depthImage = frame.acquireDepthImage16Bits()
            val width = depthImage.width
            val height = depthImage.height

            val pixelX = (u * (width - 1)).toInt().coerceIn(0, width - 1)
            val pixelY = (v * (height - 1)).toInt().coerceIn(0, height - 1)

            val planes = depthImage.planes
            val buffer = planes[0].buffer.order(ByteOrder.LITTLE_ENDIAN)
            val rowStride = planes[0].rowStride
            val pixelStride = planes[0].pixelStride

            val index = pixelY * rowStride + pixelX * pixelStride
            val depthMillimeters = buffer.getShort(index).toInt() and 0xFFFF
            val depthMeters = depthMillimeters / 1000.0

            // Leer confianza si está disponible
            var confidenceVal = 0.85
            try {
                confidenceImage = frame.acquireRawDepthConfidenceImage()
                val confPlanes = confidenceImage.planes
                val confBuffer = confPlanes[0].buffer
                val confStride = confPlanes[0].rowStride
                val confIndex = pixelY * confStride + pixelX * confPlanes[0].pixelStride
                val rawConfidence = confBuffer.get(confIndex).toInt() and 0xFF
                confidenceVal = (rawConfidence / 255.0).coerceIn(0.0, 1.0)
            } catch (_: Exception) {
                // Si la imagen de confianza no está disponible en este frame, usar estimación
            }

            if (depthMeters in 0.3..6.0) {
                DepthSample(depthMeters = depthMeters, confidence = confidenceVal)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            depthImage?.close()
            confidenceImage?.close()
        }
    }

    /**
     * Desproyecta un punto en pantalla (pixelX, pixelY, Z_metros) a un vector 3D en el espacio de la cámara.
     */
    fun unprojectPoint(
        pixelX: Float,
        pixelY: Float,
        depthMeters: Double,
        intrinsics: CameraIntrinsics
    ): Vector3D {
        val focalLength = intrinsics.focalLength
        val principalPoint = intrinsics.principalPoint

        val fx = focalLength[0].toDouble()
        val fy = focalLength[1].toDouble()
        val cx = principalPoint[0].toDouble()
        val cy = principalPoint[1].toDouble()

        val x = (pixelX.toDouble() - cx) * depthMeters / fx
        val y = -(pixelY.toDouble() - cy) * depthMeters / fy // Coordenadas espaciales estándar
        return Vector3D(x, y, depthMeters)
    }

    fun pause() {
        try {
            session?.pause()
        } catch (_: Exception) {}
    }

    fun resume() {
        try {
            session?.resume()
        } catch (e: CameraNotAvailableException) {
            // Manejado en capas superiores
        } catch (_: Exception) {}
    }

    fun destroy() {
        try {
            session?.close()
            session = null
        } catch (_: Exception) {}
    }
}

data class DepthSample(
    val depthMeters: Double,
    val confidence: Double
)
