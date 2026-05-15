package com.mukesh.vidyarthibus.core.location

import kotlin.math.*

object LocationUtils {
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Calculates the distance between two points in meters using Haversine formula.
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c * 1000.0 // distance in meters
    }

    /**
     * Checks if a point is within [radiusMeters] of any point in the [corridor].
     */
    fun isPointInCorridor(
        lat: Double,
        lng: Double,
        corridor: List<com.mukesh.vidyarthibus.domain.model.LatLng>,
        radiusMeters: Double = 500.0
    ): Boolean {
        if (corridor.isEmpty()) return true // Default to true if no corridor defined (flexible for MVP)
        return corridor.any { point ->
            calculateDistance(lat, lng, point.lat, point.lng) <= radiusMeters
        }
    }
}
