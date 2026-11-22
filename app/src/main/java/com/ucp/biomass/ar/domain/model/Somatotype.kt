package com.ucp.biomass.ar.domain.model

/**
 * Clasificación de Somatotipos según la antropometría clínica de Sheldon y Heath-Carter.
 *
 * Cada somatotipo modula la constante de densidad tisular delta(r, theta, z)
 * utilizada dentro de las integrales triples de volumen:
 *   Masa = \iiint_V delta(r, theta, z) * dV
 *
 * Incorpora descripciones morfológicas claras, la prueba clínica de la muñeca
 * y un clasificador de ratios biométricos para que cualquier usuario sepa
 * con certeza qué somatotipo le corresponde.
 */
enum class Somatotype(
    val displayName: String,
    val tagline: String,
    val description: String,
    val wristTestGuide: String,
    val keyTraits: List<String>,
    val bulkDensityKgM3: Double,      // Densidad promedio global
    val coreBoneDensityKgM3: Double,  // Densidad del núcleo óseo
    val muscleDensityKgM3: Double,    // Densidad del manto muscular
    val surfaceFatDensityKgM3: Double,// Densidad de la capa subcutánea
    val radialGradientFactor: Double, // Factor alfa del gradiente radial de densidad
    val typicalBodyFatPercentage: Double // Grasa corporal de referencia (%)
) {
    /**
     * Ectomorfo:
     * - Predominio de linealidad, osamenta fina, extremidades alargadas.
     * - Dificultad para ganar grasa y masa muscular; metabolismo acelerado.
     * - Prueba de la muñeca: El pulgar y el dedo medio se solapan con facilidad.
     */
    ECTOMORPH(
        displayName = "Ectomorfo",
        tagline = "Complexión Delgada / Estructura Fina",
        description = "Estructura ósea estrecha, articulaciones delgadas, extremidades largas y bajo nivel de grasa subcutánea.",
        wristTestGuide = "Si rodeas tu muñeca contraria con pulgar y dedo medio, se solapan con facilidad.",
        keyTraits = listOf(
            "Hombros y caja torácica estrechos",
            "Muñecas y tobillos finos",
            "Dificultad natural para ganar peso",
            "Masa magra compacta pero lineal"
        ),
        bulkDensityKgM3 = 1035.0,
        coreBoneDensityKgM3 = 1750.0,
        muscleDensityKgM3 = 1060.0,
        surfaceFatDensityKgM3 = 920.0,
        radialGradientFactor = 0.12,
        typicalBodyFatPercentage = 11.5
    ),

    /**
     * Mesomorfo:
     * - Predominio muscular y óseo equilibrado, silueta en 'V' o atlética.
     * - Osamenta media a densa, buena definición muscular natural.
     * - Prueba de la muñeca: El pulgar y el dedo medio se tocan exactamente en las puntas.
     */
    MESOMORPH(
        displayName = "Mesomorfo",
        tagline = "Complexión Atlética / Equilibrada",
        description = "Estructura osteomuscular media a fuerte, hombros anchos respecto a la cintura y excelente tono muscular.",
        wristTestGuide = "Si rodeas tu muñeca contraria, el pulgar y el dedo medio apenas se tocan en la punta.",
        keyTraits = listOf(
            "Hombros anchos y cintura proporcionada",
            "Estructura ósea sólida y articulaciones medias",
            "Facilidad para ganar músculo y fuerza",
            "Mayor densidad corporal del espectro humano"
        ),
        bulkDensityKgM3 = 1070.0,
        coreBoneDensityKgM3 = 1850.0,
        muscleDensityKgM3 = 1065.0,
        surfaceFatDensityKgM3 = 920.0,
        radialGradientFactor = 0.15,
        typicalBodyFatPercentage = 14.5
    ),

    /**
     * Endomorfo:
     * - Predominio de volumen, osamenta ancha y mayor densidad de tejidos blandos.
     * - Caja torácica y caderas amplias, tendencia natural al almacenamiento de volumen.
     * - Prueba de la muñeca: El pulgar y el dedo medio no se tocan (queda un espacio visible).
     */
    ENDOMORPH(
        displayName = "Endomorfo",
        tagline = "Complexión Robusta / Voluminosa",
        description = "Estructura ósea ancha y densa, hombros y caderas amplios, con mayor distribución de volumen en tronco y muslos.",
        wristTestGuide = "Si rodeas tu muñeca contraria, tus dedos no alcanzan a tocarse.",
        keyTraits = listOf(
            "Caderas y hombros anchos y sólidos",
            "Huesos y articulaciones gruesas",
            "Mayor volumen en torso, cintura y piernas",
            "Excelente soporte de masa y fuerza"
        ),
        bulkDensityKgM3 = 1015.0,
        coreBoneDensityKgM3 = 1700.0,
        muscleDensityKgM3 = 1055.0,
        surfaceFatDensityKgM3 = 920.0,
        radialGradientFactor = 0.18,
        typicalBodyFatPercentage = 24.0
    );

    /**
     * Evalúa la función de densidad heterogénea radial delta(r, R) para un corte de radio R:
     * delta(r, R) = bulkDensityKgM3 * [ 1.0 + alpha * (1.0 - 2.0 * (r / R)^2) ]
     *
     * Demostración analítica de conservación de masa:
     * \int_0^R delta(r) * r dr = bulkDensityKgM3 * (R^2 / 2)
     */
    fun evaluateRadialDensity(r: Double, maxRadius: Double): Double {
        if (maxRadius <= 1e-6) return bulkDensityKgM3
        val normalizedRadius = (r / maxRadius).coerceIn(0.0, 1.0)
        val gradient = 1.0 - 2.0 * (normalizedRadius * normalizedRadius)
        return bulkDensityKgM3 * (1.0 + radialGradientFactor * gradient)
    }

    companion object {
        /**
         * Recomienda un somatotipo a partir de las proporciones morfométricas medidas por la cámara:
         * @param shoulderWidthMeters Ancho biacromial (entre hombros) en metros.
         * @param hipWidthMeters Ancho biiliaco (entre caderas) en metros.
         * @param heightMeters Estatura métrica.
         */
        fun suggestFromProportions(
            shoulderWidthMeters: Double,
            hipWidthMeters: Double,
            heightMeters: Double
        ): Somatotype {
            if (heightMeters <= 0.5) return MESOMORPH
            val frameRatio = (shoulderWidthMeters + hipWidthMeters) / (2.0 * heightMeters)
            val shoulderToHipRatio = if (hipWidthMeters > 0.05) shoulderWidthMeters / hipWidthMeters else 1.2

            return when {
                frameRatio < 0.225 -> ECTOMORPH
                frameRatio > 0.260 || shoulderToHipRatio < 1.08 -> ENDOMORPH
                else -> MESOMORPH
            }
        }
    }
}
