package com.example.util

import java.text.NumberFormat
import java.util.Locale

object Formatters {

    fun formatSecondsToTime(secondsFromMidnight: Int): String {
        val totalDays = secondsFromMidnight / 86400
        val remainingSeconds = secondsFromMidnight % 86400

        val hours24 = remainingSeconds / 3600
        val minutes = (remainingSeconds % 3600) / 60

        val period = if (hours24 < 12) "AM" else "PM"
        val hours12 = when (hours24 % 12) {
            0 -> 12
            else -> hours24 % 12
        }

        val baseTime = String.format(Locale.ENGLISH, "%02d:%02d %s", hours12, minutes, period)
        return if (totalDays > 0) {
            "$baseTime +$totalDays day"
        } else {
            baseTime
        }
    }

    fun formatRupees(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        formatter.maximumFractionDigits = 0
        return formatter.format(amount).replace("INR", "₹").trim()
    }

    fun formatDuration(durationSeconds: Int): String {
        val hours = durationSeconds / 3600
        val minutes = (durationSeconds % 3600) / 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    fun formatDistance(meters: Double): String {
        return if (meters >= 1000) {
            String.format(Locale.ENGLISH, "%.1f km", meters / 1000.0)
        } else {
            "${meters.toInt()} m"
        }
    }
}
