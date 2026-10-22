package com.example.domain.planner

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeoUtils {
    const val WALKING_SPEED_METERS_PER_SEC = 1.25 // 4.5 km/h = 75 m/min
    const val DETOUR_FACTOR = 1.3

    fun haversineDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun walkingDistanceMeters(straightLineMeters: Double): Double {
        return straightLineMeters * DETOUR_FACTOR
    }

    fun walkingDurationSeconds(straightLineMeters: Double): Int {
        val actualWalkMeters = walkingDistanceMeters(straightLineMeters)
        return (actualWalkMeters / WALKING_SPEED_METERS_PER_SEC).toInt()
    }
}
