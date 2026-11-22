package com.ucp.biomass.ar.domain.model

/**
 * Sistema de coordenadas utilizado para la integración triple de un segmento.
 */
enum class CoordinateSystemType {
    CYLINDRICAL,  // V = \iiint r dz dr dtheta
    SPHERICAL     // V = \iiint rho^2 sin(phi) drho dphi dtheta
}

/**
 * Segmentos anatómicos del cuerpo humano según la extensión biomecánica de Hanavan y De Leva.
 */
enum class AnatomicalSegment(
    val displayName: String,
    val coordinateSystem: CoordinateSystemType,
    val relativeDensityMultiplier: Double, // Ajuste de densidad relativo a la densidad base
    val massPercentageReference: Double   // Porcentaje aproximado de la masa corporal total
) {
    HEAD(
        displayName = "Cabeza",
        coordinateSystem = CoordinateSystemType.SPHERICAL,
        relativeDensityMultiplier = 1.03, // Cráneo óseo denso + encéfalo hidratado
        massPercentageReference = 7.0
    ),
    NECK(
        displayName = "Cuello",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 1.01,
        massPercentageReference = 1.5
    ),
    UPPER_TORSO(
        displayName = "Tórax",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 0.98, // Presencia de pulmones con aire reduce densidad promedio
        massPercentageReference = 16.5
    ),
    LOWER_TORSO(
        displayName = "Abdomen y Pelvis",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 1.02, // Vísceras hidratadas + huesos ilíacos densos
        massPercentageReference = 26.5
    ),
    LEFT_UPPER_ARM(
        displayName = "Brazo Superior Izq.",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 1.05,
        massPercentageReference = 2.8
    ),
    RIGHT_UPPER_ARM(
        displayName = "Brazo Superior Der.",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 1.05,
        massPercentageReference = 2.8
    ),
    LEFT_FOREARM(
        displayName = "Antebrazo Izq.",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 1.07,
        massPercentageReference = 1.6
    ),
    RIGHT_FOREARM(
        displayName = "Antebrazo Der.",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 1.07,
        massPercentageReference = 1.6
    ),
    LEFT_HAND(
        displayName = "Mano Izq.",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 1.08,
        massPercentageReference = 0.6
    ),
    RIGHT_HAND(
        displayName = "Mano Der.",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 1.08,
        massPercentageReference = 0.6
    ),
    LEFT_THIGH(
        displayName = "Muslo Izq.",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 1.05,
        massPercentageReference = 10.0
    ),
    RIGHT_THIGH(
        displayName = "Muslo Der.",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 1.05,
        massPercentageReference = 10.0
    ),
    LEFT_SHANK(
        displayName = "Pierna/Pantorrilla Izq.",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 1.08,
        massPercentageReference = 4.3
    ),
    RIGHT_SHANK(
        displayName = "Pierna/Pantorrilla Der.",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 1.08,
        massPercentageReference = 4.3
    ),
    LEFT_FOOT(
        displayName = "Pie Izq.",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 1.07,
        massPercentageReference = 1.4
    ),
    RIGHT_FOOT(
        displayName = "Pie Der.",
        coordinateSystem = CoordinateSystemType.CYLINDRICAL,
        relativeDensityMultiplier = 1.07,
        massPercentageReference = 1.4
    )
}
