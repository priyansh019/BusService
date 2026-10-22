package com.example.domain.repository

import com.example.domain.model.AppSettings
import com.example.domain.model.AuditLogEntry
import com.example.domain.model.Operator
import com.example.domain.model.Route
import com.example.domain.model.RouteSubmission
import com.example.domain.model.SavedJourney
import com.example.domain.model.ServiceAlert
import com.example.domain.model.Session
import com.example.domain.model.Stop
import com.example.domain.model.SubmissionStatus
import com.example.domain.model.User
import com.example.domain.model.VehiclePosition
import com.example.domain.model.Verification
import com.example.domain.planner.Journey
import com.example.domain.planner.PlannerPreferences
import kotlinx.coroutines.flow.Flow

interface BusDiscoveryRepository {
    fun getStopsFlow(): Flow<List<Stop>>
    suspend fun getStops(): List<Stop>
    suspend fun getStopById(id: String): Stop?

    fun getRoutesFlow(): Flow<List<Route>>
    suspend fun getRoutes(): List<Route>
    suspend fun getRouteById(id: String): Route?

    fun getSubmissionsFlow(): Flow<List<RouteSubmission>>
    fun getAlertsFlow(): Flow<List<ServiceAlert>>
    fun getLivePositionsFlow(): Flow<List<VehiclePosition>>
    fun getSavedJourneysFlow(): Flow<List<SavedJourney>>
    fun getAuditLogsFlow(): Flow<List<AuditLogEntry>>
    fun getAppSettingsFlow(): Flow<AppSettings?>

    suspend fun getUsers(): List<User>
    suspend fun getUserById(id: String): User?
    suspend fun getOperators(): List<Operator>

    suspend fun planJourney(
        origin: String,
        destination: String,
        departureSeconds: Int,
        preferences: PlannerPreferences
    ): List<Journey>

    suspend fun submitContribution(
        submission: RouteSubmission,
        session: Session
    ): Pair<Boolean, String>

    suspend fun reviewSubmission(
        submissionId: String,
        decision: String, // APPROVED, REJECTED, CLARIFICATION_REQUESTED, MERGED
        notes: String,
        session: Session
    ): Pair<Boolean, String>

    suspend fun updateOperatorRoute(
        route: Route,
        session: Session
    ): Pair<Boolean, String>

    suspend fun recordDriverPosition(
        tripId: String,
        lat: Double,
        lng: Double,
        speedKmh: Double,
        heading: Double,
        session: Session
    ): Pair<Boolean, String>

    suspend fun deleteDriverLocationData(tripId: String, session: Session): Boolean
    suspend fun saveJourney(journey: SavedJourney)
    suspend fun removeSavedJourney(id: String)
    suspend fun updateAppSettings(settings: AppSettings)
    suspend fun resetDemoData()
}
