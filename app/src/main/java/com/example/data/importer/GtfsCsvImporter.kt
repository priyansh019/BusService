package com.example.data.importer

import com.example.data.local.BusDao
import com.example.data.local.FareRuleEntity
import com.example.data.local.RouteEntity
import com.example.data.local.RouteStopEntity
import com.example.data.local.StopEntity
import com.example.data.local.StopTimeEntity
import com.example.data.local.TripEntity
import com.example.domain.model.FareRuleType
import com.example.domain.model.StopType
import com.example.domain.model.VerificationStatus

data class ImportReport(
    val success: Boolean,
    val stopsCount: Int = 0,
    val routesCount: Int = 0,
    val tripsCount: Int = 0,
    val stopTimesCount: Int = 0,
    val errors: List<String> = emptyList(),
    val summary: String = ""
)

object GtfsCsvImporter {

    suspend fun importCsvData(
        operatorId: String,
        csvText: String,
        dao: BusDao
    ): ImportReport {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.isEmpty()) {
            return ImportReport(false, errors = listOf("CSV file is empty."))
        }

        val errors = mutableListOf<String>()
        var routesAdded = 0
        var stopsAdded = 0

        // Format: type,id,name,lat,lng or type,routeId,shortName,longName
        for ((idx, line) in lines.withIndex()) {
            val parts = line.split(",").map { it.trim() }
            if (parts.size < 4) {
                errors.add("Line ${idx + 1}: Malformed row (insufficient columns).")
                continue
            }
            when (parts[0].uppercase()) {
                "STOP" -> {
                    try {
                        val stop = StopEntity(
                            id = parts[1],
                            name = parts[2],
                            hindiName = if (parts.size > 5) parts[5] else "",
                            lat = parts[3].toDouble(),
                            lng = parts[4].toDouble(),
                            type = StopType.ROADSIDE,
                            isLandmark = false,
                            approximate = false,
                            locality = "Imported",
                            source = "CSV Operator Import",
                            effectiveDate = "2026-10-22",
                            lastVerifiedAt = "2026-10-22",
                            verificationStatus = VerificationStatus.OFFICIAL_VERIFIED,
                            reviewer = "Operator Console"
                        )
                        dao.insertStops(listOf(stop))
                        stopsAdded++
                    } catch (e: Exception) {
                        errors.add("Line ${idx + 1}: Invalid stop coordinates (${e.message})")
                    }
                }
                "ROUTE" -> {
                    val route = RouteEntity(
                        id = parts[1],
                        operatorId = operatorId, // Tenant-scoped enforcement
                        shortName = parts[2],
                        longName = parts[3],
                        direction = 0,
                        variantOf = null,
                        frequencyMinutes = 30,
                        firstDepartureSeconds = 21600,
                        lastDepartureSeconds = 72000,
                        source = "Operator Import",
                        effectiveDate = "2026-10-22",
                        lastVerifiedAt = "2026-10-22",
                        verificationStatus = VerificationStatus.OFFICIAL_VERIFIED,
                        reviewer = "Operator"
                    )
                    dao.insertRoute(route)
                    routesAdded++
                }
                else -> {
                    // Header or unknown
                }
            }
        }

        return ImportReport(
            success = errors.isEmpty(),
            stopsCount = stopsAdded,
            routesCount = routesAdded,
            errors = errors,
            summary = "Processed ${lines.size} rows: $routesAdded routes, $stopsAdded stops created."
        )
    }

    suspend fun importSampleFeed(operatorId: String, dao: BusDao): ImportReport {
        val sampleCsv = """
            STOP,sample_sec12,Noida Sector 12,28.5980,77.3400,नोएडा सेक्टर 12
            STOP,sample_sec22,Noida Sector 22,28.5995,77.3480,नोएडा सेक्टर 22
            ROUTE,L3,L3,Mamura to Sector 12 Local Feeder
        """.trimIndent()
        return importCsvData(operatorId, sampleCsv, dao)
    }
}
