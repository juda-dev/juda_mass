package com.ucp.biomass.ar.presentation.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de Diseño "Obsidian & Titanium Sapphire"
 *
 * Concebida para transmitir la estética de instrumental médico de precisión,
 * relojería de alta gama y diseño industrial escandinavo.
 * Elimina los colores de neón estridentes estilo "cyberpunk / clásico IA"
 * y los reemplaza por tonos armoniosos, orgánicos y elegantes.
 */

// Superficies Profundas (Obsidiana y Titanio)
val BioMassVoidBlack = Color(0xFF0A0C10)         // Negro obsidiana cálido y natural
val BioMassSurfaceDark = Color(0xFF13171F)        // Grafito mate profundo
val BioMassSurfaceVariant = Color(0xFF1B212B)     // Titanio intermedio
val BioMassSurfaceElevated = Color(0xFF232A37)    // Pizarra platino elevado
val BioMassGlassOverlay = Color(0xEB13171F)       // Superficie translúcida con acabado de vidrio esmerilado

// Bordes y Delineados Ópticos Finos (Cristal y Platino)
val BioMassBorderOutline = Color(0xFF283241)      // Borde estructural sutil
val BioMassBorderSubtle = Color(0x33FFFFFF)       // Micro-borde translúcido (0.5dp)
val BioMassBorderGlow = Color(0x404F9CF8)         // Resplandor de enfoque en zafiro

// Acentos Principales Armoniosos (No Neón)
val BioMassSapphirePrimary = Color(0xFF4F9CF8)    // Zafiro ártico elegante (color insignia de precisión)
val BioMassSapphireMuted = Color(0xFF3B7ED1)      // Zafiro profundo contenido
val BioMassSapphireGlow = Color(0x2E4F9CF8)       // Halo de luz azulada tenue

// Estados y Telemetría Biológica
val BioMassJadeSuccess = Color(0xFF2DD4BF)        // Verde jade / menta natural (estado de bloqueo y salud)
val BioMassJadeGlow = Color(0x2E2DD4BF)          // Halo suave menta
val BioMassAmberOchre = Color(0xFFF59E0B)         // Ámbar ocre cálido (calibración y advertencias sutiles)
val BioMassAmberGlow = Color(0x2EF59E0B)          // Halo ámbar contenido
val BioMassChampagneGold = Color(0xFFE5C07B)      // Oro champán refinado para métricas destacadas
val BioMassRoseAccent = Color(0xFFFB7185)         // Rosa tenue para alertas

// Jerarquía Tipográfica de Alto Contraste y Calidez
val BioMassTextPrimary = Color(0xFFF8FAFC)        // Blanco platino puro
val BioMassTextSecondary = Color(0xFF94A3B8)      // Niebla de pizarra (lectura óptima sin fatiga)
val BioMassTextTertiary = Color(0xFF64748B)       // Grafito sutil para metadatos
val BioMassTextInverse = Color(0xFF0A0C10)        // Texto oscuro sobre fondos claros

// Malla Holográfica AR y Trazado Biométrico
val BioMassMeshDelicate = Color(0x664F9CF8)       // Alambre 3D fino y suave
val BioMassMeshHighlight = Color(0x992DD4BF)      // Anillos transversales activos en jade
val BioMassJointAnchor = Color(0xFF38BDF8)        // Articulaciones detectadas
val BioMassScanLaserLine = Color(0x994F9CF8)      // Barrido volumétrico sutil
val BioMassScanLaserAura = Color(0x204F9CF8)      // Halo del barrido

// Compatibilidad retroactiva para componentes existentes
val BioMassCyanPrimary = BioMassSapphirePrimary
val BioMassCyanGlow = BioMassSapphireGlow
val BioMassCyanDim = BioMassSapphireMuted
val BioMassEmeraldSecondary = BioMassJadeSuccess
val BioMassEmeraldGlow = BioMassJadeGlow
val BioMassAmberWarning = BioMassAmberOchre
val BioMassCrimsonError = BioMassRoseAccent
val BioMassMeshWireframe = BioMassMeshDelicate
val BioMassJointPulsing = BioMassJointAnchor
val BioMassScanLaser = BioMassScanLaserLine
val BioMassScanLaserGlow = BioMassScanLaserAura

