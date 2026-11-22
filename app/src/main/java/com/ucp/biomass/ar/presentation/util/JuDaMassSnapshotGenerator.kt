package com.ucp.biomass.ar.presentation.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.ucp.biomass.ar.domain.model.BiomassCalculationResult
import com.ucp.biomass.ar.domain.model.Somatotype
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generador de Ficha/Recuerdo Gráfico de Escaneo Biomecánico JuDa Mass.
 *
 * Crea una imagen de alta resolución (1080 x 1720 px) con diseño ejecutivo,
 * incorporando la fotografía real de la persona capturada por la cámara,
 * todos los parámetros calculados por integrales triples, fecha, hora
 * y la firma "by: JuDa Dev".
 */
object JuDaMassSnapshotGenerator {

    fun generateAndSaveSnapshot(
        context: Context,
        result: BiomassCalculationResult,
        somatotype: Somatotype,
        effectiveHeightM: Double,
        personPhoto: Bitmap? = null
    ): Result<String> {
        return runCatching {
            val width = 1080
            val height = 1720
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // 1. Fondo Degradado Obsidiana Profundo (#0A0C10 a #141923)
            val bgPaint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    Color.parseColor("#0A0C10"),
                    Color.parseColor("#141923"),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Marco perimetral fino
            val borderPaint = Paint().apply {
                color = Color.parseColor("#283241")
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }
            val margin = 36f
            canvas.drawRoundRect(
                RectF(margin, margin, width - margin, height - margin),
                28f, 28f, borderPaint
            )

            // 2. Encabezado
            val titlePaint = Paint().apply {
                color = Color.parseColor("#F8FAFC")
                textSize = 58f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("JuDa Mass", 70f, 130f, titlePaint)

            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#4F9CF8") // Zafiro Ártico
                textSize = 28f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("REPORTE BIOMECÁNICO • INTEGRALES TRIPLES", 70f, 175f, subtitlePaint)

            // Fecha y Hora actual formateada en español
            val dateFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy • hh:mm a", Locale("es", "CO"))
            val currentDateStr = dateFormat.format(Date())

            val datePaint = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 24f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }
            canvas.drawText(currentDateStr, 70f, 215f, datePaint)

            // Línea divisoria
            val divPaint = Paint().apply {
                color = Color.parseColor("#1F2735")
                strokeWidth = 2f
            }
            canvas.drawLine(70f, 245f, width - 70f, 245f, divPaint)

            // 3. Fotografía Real de la Persona en el Recuerdo
            val photoBoxTop = 270f
            val photoBoxHeight = 440f
            val photoBox = RectF(70f, photoBoxTop, width - 70f, photoBoxTop + photoBoxHeight)

            if (personPhoto != null && !personPhoto.isRecycled) {
                // Fondo de la tarjeta de foto
                val photoBg = Paint().apply {
                    color = Color.parseColor("#10141D")
                    isAntiAlias = true
                }
                canvas.drawRoundRect(photoBox, 20f, 20f, photoBg)

                // Dibujar foto recortada y centrada
                canvas.save()
                val clipPath = android.graphics.Path().apply {
                    addRoundRect(photoBox, 20f, 20f, android.graphics.Path.Direction.CW)
                }
                canvas.clipPath(clipPath)

                // Calcular escala para rellenar el recuadro respetando aspect ratio
                val srcW = personPhoto.width
                val srcH = personPhoto.height
                val targetAspect = photoBox.width() / photoBox.height()
                val srcAspect = srcW.toFloat() / srcH.toFloat()

                val srcRect: Rect = if (srcAspect > targetAspect) {
                    val cropW = (srcH * targetAspect).toInt()
                    val xOff = (srcW - cropW) / 2
                    Rect(xOff, 0, xOff + cropW, srcH)
                } else {
                    val cropH = (srcW / targetAspect).toInt()
                    val yOff = (srcH - cropH) / 2
                    Rect(0, yOff, srcW, yOff + cropH)
                }

                canvas.drawBitmap(personPhoto, srcRect, photoBox, Paint(Paint.FILTER_BITMAP_FLAG))
                canvas.restore()

                // Borde y distintivo de la foto
                val photoBorder = Paint().apply {
                    color = Color.parseColor("#4F9CF8")
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    isAntiAlias = true
                }
                canvas.drawRoundRect(photoBox, 20f, 20f, photoBorder)

                // Píldora de distintivo sobre la foto
                val tagBox = RectF(90f, photoBoxTop + 20f, 430f, photoBoxTop + 65f)
                val tagBg = Paint().apply {
                    color = Color.parseColor("#CC0A0C10")
                    isAntiAlias = true
                }
                canvas.drawRoundRect(tagBox, 10f, 10f, tagBg)
                val tagTextPaint = Paint().apply {
                    color = Color.parseColor("#4F9CF8")
                    textSize = 20f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("📸 CAPTURA DE ANÁLISIS EN VIVO", 105f, photoBoxTop + 50f, tagTextPaint)
            } else {
                // Si por alguna razón no hay foto, marco gráfico elegante
                val noPhotoBg = Paint().apply {
                    color = Color.parseColor("#13171F")
                    isAntiAlias = true
                }
                canvas.drawRoundRect(photoBox, 20f, 20f, noPhotoBg)
                val placeholderPaint = Paint().apply {
                    color = Color.parseColor("#94A3B8")
                    textSize = 26f
                    typeface = Typeface.DEFAULT
                    isAntiAlias = true
                }
                canvas.drawText("JuDa Mass • Escaneo Óptico Tridimensional", 260f, photoBoxTop + 230f, placeholderPaint)
            }

            // 4. Tarjeta Principal de Masa y Rango
            val massCardTop = photoBoxTop + photoBoxHeight + 25f
            val massCardHeight = 250f
            val cardRect = RectF(70f, massCardTop, width - 70f, massCardTop + massCardHeight)

            val cardPaint = Paint().apply {
                color = Color.parseColor("#13171F")
                isAntiAlias = true
            }
            canvas.drawRoundRect(cardRect, 20f, 20f, cardPaint)
            val cardBorder = Paint().apply {
                color = Color.parseColor("#2E3B4E")
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            canvas.drawRoundRect(cardRect, 20f, 20f, cardBorder)

            val cardLabelPaint = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 21f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.08f
                isAntiAlias = true
            }
            canvas.drawText("PESO REAL ESTIMADO (M = ∭ δ dV)", 105f, massCardTop + 45f, cardLabelPaint)

            // Número gigante de peso
            val massStr = String.format(Locale.US, "%.1f", result.totalMassKg)
            val massBigPaint = Paint().apply {
                color = Color.parseColor("#F8FAFC")
                textSize = 96f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(massStr, 105f, massCardTop + 145f, massBigPaint)

            val newtonsPaint = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 26f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                isAntiAlias = true
            }
            canvas.drawText("(${result.weightNewtons.toInt()} N  •  ${String.format(Locale.US, "%.1f", result.weightLbs)} lbs)", 105f, massCardTop + 195f, newtonsPaint)

            // Rango Estimado pill
            val rangeBox = RectF(width - 480f, massCardTop + 35f, width - 105f, massCardTop + 115f)
            val rangeBg = Paint().apply {
                color = Color.parseColor("#1B2330")
                isAntiAlias = true
            }
            canvas.drawRoundRect(rangeBox, 14f, 14f, rangeBg)
            val rangeBorder = Paint().apply {
                color = Color.parseColor("#4F9CF8")
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            canvas.drawRoundRect(rangeBox, 14f, 14f, rangeBorder)

            val rangeTitlePaint = Paint().apply {
                color = Color.parseColor("#4F9CF8")
                textSize = 17f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText("RANGO ESTIMADO (±5%)", width - 460f, massCardTop + 65f, rangeTitlePaint)

            val rangeValPaint = Paint().apply {
                color = Color.parseColor("#F8FAFC")
                textSize = 28f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
            }
            canvas.drawText(result.weightRangeFormatted, width - 460f, massCardTop + 100f, rangeValPaint)

            // 5. Parámetros Biométricos y Antropométricos (Grilla)
            val gridTop = massCardTop + massCardHeight + 25f
            drawMetricBox(canvas, 70f, gridTop, 450f, 115f, "ESTATURA CALIBRADA", String.format(Locale.US, "%.2f m", effectiveHeightM), "Eje Z Métrico")
            drawMetricBox(canvas, 560f, gridTop, 450f, 115f, "VOLUMEN CORPORAL", String.format(Locale.US, "%.1f Litros", result.totalVolumeLiters), String.format(Locale.US, "%.4f m³", result.totalVolumeM3))

            val row2Top = gridTop + 130f
            drawMetricBox(canvas, 70f, row2Top, 450f, 115f, "SOMATOTIPO", somatotype.displayName, somatotype.tagline)
            drawMetricBox(canvas, 560f, row2Top, 450f, 115f, "DENSIDAD MEDIA (δ̄)", "${result.meanDensityKgM3.toInt()} kg/m³", "Gradiente radial α=${somatotype.radialGradientFactor}")

            val row3Top = row2Top + 130f
            drawMetricBox(canvas, 70f, row3Top, 450f, 115f, "ÍNDICE DE MASA CORPORAL", String.format(Locale.US, "%.1f kg/m²", result.bmi), "Estado: " + when {
                result.bmi < 18.5 -> "Delgado"
                result.bmi < 25.0 -> "Normopeso"
                result.bmi < 30.0 -> "Sobrepeso"
                else -> "Robusto"
            })
            drawMetricBox(canvas, 560f, row3Top, 450f, 115f, "SEGMENTOS ANALIZADOS", "${result.segmentResults.size} Regiones 3D", "Simpson 1/3 + Gauss-Legendre")

            // 6. Caja Teórica y Rigor Matemático
            val mathBox = RectF(70f, row3Top + 145f, width - 70f, row3Top + 295f)
            val mathBg = Paint().apply {
                color = Color.parseColor("#10141D")
                isAntiAlias = true
            }
            canvas.drawRoundRect(mathBox, 16f, 16f, mathBg)
            val mathBorder = Paint().apply {
                color = Color.parseColor("#222C3C")
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                isAntiAlias = true
            }
            canvas.drawRoundRect(mathBox, 16f, 16f, mathBorder)

            val mathTitlePaint = Paint().apply {
                color = Color.parseColor("#E5C07B") // Oro Champán
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.05f
                isAntiAlias = true
            }
            canvas.drawText("FORMULACIÓN MATEMÁTICA (CÁLCULO MULTIVARIABLE)", 100f, row3Top + 185f, mathTitlePaint)

            val mathFormulaPaint = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 22f
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
                isAntiAlias = true
            }
            canvas.drawText("• Tronco y Extremidades: V = ∭ r dz dr dθ (Cilíndricas)", 100f, row3Top + 225f, mathFormulaPaint)
            canvas.drawText("• Cabeza: V = ∭ ρ² sin(φ) dρ dφ dθ (Esféricas Elipsoidales)", 100f, row3Top + 258f, mathFormulaPaint)
            canvas.drawText("• Masa Total: M = ∭ δ(r, θ, z) dV", 100f, row3Top + 290f, mathFormulaPaint)

            // 7. Pie de Página con Firma "by: JuDa Dev"
            val footerY = height - 55f
            val footerInstPaint = Paint().apply {
                color = Color.parseColor("#64748B")
                textSize = 22f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }
            canvas.drawText("Universidad Católica de Pereira • Matemáticas 3", 70f, footerY, footerInstPaint)

            val authorPaint = Paint().apply {
                color = Color.parseColor("#4F9CF8")
                textSize = 26f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val authorText = "by: JuDa Dev"
            val authorWidth = authorPaint.measureText(authorText)
            canvas.drawText(authorText, width - 70f - authorWidth, footerY, authorPaint)

            // Guardar el Bitmap generado en la Galería de Fotos (Pictures/JuDaMass)
            val savedPath = saveBitmapToGallery(context, bitmap, "JuDa_Mass_Recuerdo_${System.currentTimeMillis()}.jpg")

            // Si hay foto de la persona, guardarla también como foto individual limpia
            if (personPhoto != null && !personPhoto.isRecycled) {
                try {
                    saveBitmapToGallery(context, personPhoto, "JuDa_Mass_Foto_${System.currentTimeMillis()}.jpg")
                } catch (_: Exception) {}
            }

            savedPath
        }
    }

    private fun drawMetricBox(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        title: String,
        value: String,
        sub: String
    ) {
        val rect = RectF(x, y, x + w, y + h)
        val bgPaint = Paint().apply {
            color = Color.parseColor("#13171F")
            isAntiAlias = true
        }
        canvas.drawRoundRect(rect, 14f, 14f, bgPaint)
        val borderPaint = Paint().apply {
            color = Color.parseColor("#222C3C")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            isAntiAlias = true
        }
        canvas.drawRoundRect(rect, 14f, 14f, borderPaint)

        val tPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.04f
            isAntiAlias = true
        }
        canvas.drawText(title, x + 20f, y + 36f, tPaint)

        val vPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(value, x + 20f, y + 74f, vPaint)

        val sPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 16f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        canvas.drawText(sub, x + 20f, y + 102f, sPaint)
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap, filename: String): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/JuDaMass")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw IllegalStateException("No se pudo crear entrada en MediaStore")

            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 96, outputStream)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            return "Pictures/JuDaMass/$filename"
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(picturesDir, "JuDaMass")
            if (!appDir.exists()) appDir.mkdirs()

            val file = File(appDir, filename)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 96, out)
            }
            return file.absolutePath
        }
    }
}
