package com.ucp.biomass.ar.domain.model

/**
 * Estados del rastreo de sensores y visión por computadora.
 */
enum class TrackingStatus(val isDegraded: Boolean) {
    SEARCHING(isDegraded = true),
    CALIBRATING(isDegraded = true),
    ARCORE_DEPTH_ACTIVE(isDegraded = false),
    FALLBACK_ANTHROPOMETRIC(isDegraded = true),
    LOST(isDegraded = true)
}
