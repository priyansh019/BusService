package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.domain.model.SubmissionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BusDao {
    // Stops
    @Query("SELECT * FROM stops ORDER BY name ASC")
    fun getAllStopsFlow(): Flow<List<StopEntity>>

    @Query("SELECT * FROM stops ORDER BY name ASC")
    suspend fun getAllStops(): List<StopEntity>

    @Query("SELECT * FROM stops WHERE id = :id")
    suspend fun getStopById(id: String): StopEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStops(stops: List<StopEntity>)

    // Stop Aliases
    @Query("SELECT * FROM stop_aliases")
    suspend fun getAllStopAliases(): List<StopAliasEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopAliases(aliases: List<StopAliasEntity>)

    // Routes
    @Query("SELECT * FROM routes ORDER BY shortName ASC")
    fun getAllRoutesFlow(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes ORDER BY shortName ASC")
    suspend fun getAllRoutes(): List<RouteEntity>

    @Query("SELECT * FROM routes WHERE id = :id")
    suspend fun getRouteById(id: String): RouteEntity?

    @Query("SELECT * FROM routes WHERE operatorId = :operatorId")
    suspend fun getRoutesByOperator(operatorId: String): List<RouteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutes(routes: List<RouteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    // Route Stops
    @Query("SELECT * FROM route_stops WHERE routeId = :routeId ORDER BY sequence ASC")
    suspend fun getRouteStops(routeId: String): List<RouteStopEntity>

    @Query("SELECT * FROM route_stops ORDER BY sequence ASC")
    suspend fun getAllRouteStops(): List<RouteStopEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouteStops(routeStops: List<RouteStopEntity>)

    // Trips
    @Query("SELECT * FROM trips WHERE routeId = :routeId")
    suspend fun getTripsByRoute(routeId: String): List<TripEntity>

    @Query("SELECT * FROM trips")
    suspend fun getAllTrips(): List<TripEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<TripEntity>)

    // Stop Times
    @Query("SELECT * FROM stop_times WHERE tripId = :tripId ORDER BY sequence ASC")
    suspend fun getStopTimesForTrip(tripId: String): List<StopTimeEntity>

    @Query("SELECT * FROM stop_times ORDER BY sequence ASC")
    suspend fun getAllStopTimes(): List<StopTimeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStopTimes(stopTimes: List<StopTimeEntity>)

    // Service Calendars
    @Query("SELECT * FROM service_calendars")
    suspend fun getAllCalendars(): List<ServiceCalendarEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendars(calendars: List<ServiceCalendarEntity>)

    // Calendar Exceptions
    @Query("SELECT * FROM calendar_exceptions")
    suspend fun getAllCalendarExceptions(): List<CalendarExceptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarExceptions(exceptions: List<CalendarExceptionEntity>)

    // Fare Rules
    @Query("SELECT * FROM fare_rules")
    suspend fun getAllFareRules(): List<FareRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFareRules(fareRules: List<FareRuleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFareRule(fareRule: FareRuleEntity)

    // Fare Stages
    @Query("SELECT * FROM fare_stages")
    suspend fun getAllFareStages(): List<FareStageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFareStages(stages: List<FareStageEntity>)

    // Operators
    @Query("SELECT * FROM operators")
    suspend fun getAllOperators(): List<OperatorEntity>

    @Query("SELECT * FROM operators WHERE id = :id")
    suspend fun getOperatorById(id: String): OperatorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperators(operators: List<OperatorEntity>)

    // Users
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    // Vehicle Positions
    @Query("SELECT * FROM vehicle_positions WHERE tripId = :tripId")
    suspend fun getPositionForTrip(tripId: String): VehiclePositionEntity?

    @Query("SELECT * FROM vehicle_positions")
    fun getAllVehiclePositionsFlow(): Flow<List<VehiclePositionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehiclePosition(position: VehiclePositionEntity)

    @Query("DELETE FROM vehicle_positions WHERE timestampSeconds < :cutoffSeconds")
    suspend fun purgeOldPositions(cutoffSeconds: Long): Int

    @Query("DELETE FROM vehicle_positions WHERE tripId = :tripId")
    suspend fun deletePositionsForTrip(tripId: String)

    // Service Alerts
    @Query("SELECT * FROM service_alerts")
    fun getAllAlertsFlow(): Flow<List<ServiceAlertEntity>>

    @Query("SELECT * FROM service_alerts")
    suspend fun getAllAlerts(): List<ServiceAlertEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<ServiceAlertEntity>)

    // Route Submissions
    @Query("SELECT * FROM route_submissions ORDER BY submittedAt DESC")
    fun getAllSubmissionsFlow(): Flow<List<RouteSubmissionEntity>>

    @Query("SELECT * FROM route_submissions ORDER BY submittedAt DESC")
    suspend fun getAllSubmissions(): List<RouteSubmissionEntity>

    @Query("SELECT * FROM route_submissions WHERE id = :id")
    suspend fun getSubmissionById(id: String): RouteSubmissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: RouteSubmissionEntity)

    @Update
    suspend fun updateSubmission(submission: RouteSubmissionEntity)

    // Verifications (Immutable Audit Records)
    @Query("SELECT * FROM verifications ORDER BY verifiedAt DESC")
    suspend fun getAllVerifications(): List<VerificationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerification(verification: VerificationEntity)

    // Audit Log
    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC")
    fun getAllAuditLogsFlow(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC")
    suspend fun getAllAuditLogs(): List<AuditLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // Analytics Events
    @Query("SELECT * FROM analytics_events ORDER BY timestamp DESC")
    suspend fun getAllAnalyticsEvents(): List<AnalyticsEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyticsEvent(event: AnalyticsEventEntity)

    // Saved Journeys
    @Query("SELECT * FROM saved_journeys ORDER BY savedAt DESC")
    fun getAllSavedJourneysFlow(): Flow<List<SavedJourneyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedJourney(journey: SavedJourneyEntity)

    @Query("DELETE FROM saved_journeys WHERE id = :id")
    suspend fun deleteSavedJourney(id: String)

    // App Settings
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getAppSettingsFlow(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getAppSettings(): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAppSettings(settings: AppSettingsEntity)

    // Clear all for demo reset
    @Query("DELETE FROM stops") suspend fun clearStops()
    @Query("DELETE FROM stop_aliases") suspend fun clearStopAliases()
    @Query("DELETE FROM routes") suspend fun clearRoutes()
    @Query("DELETE FROM route_stops") suspend fun clearRouteStops()
    @Query("DELETE FROM trips") suspend fun clearTrips()
    @Query("DELETE FROM stop_times") suspend fun clearStopTimes()
    @Query("DELETE FROM service_calendars") suspend fun clearCalendars()
    @Query("DELETE FROM calendar_exceptions") suspend fun clearCalendarExceptions()
    @Query("DELETE FROM fare_rules") suspend fun clearFareRules()
    @Query("DELETE FROM fare_stages") suspend fun clearFareStages()
    @Query("DELETE FROM operators") suspend fun clearOperators()
    @Query("DELETE FROM users") suspend fun clearUsers()
    @Query("DELETE FROM vehicle_positions") suspend fun clearVehiclePositions()
    @Query("DELETE FROM service_alerts") suspend fun clearAlerts()
    @Query("DELETE FROM route_submissions") suspend fun clearSubmissions()
    @Query("DELETE FROM verifications") suspend fun clearVerifications()
    @Query("DELETE FROM audit_log") suspend fun clearAuditLog()
    @Query("DELETE FROM analytics_events") suspend fun clearAnalytics()
    @Query("DELETE FROM saved_journeys") suspend fun clearSavedJourneys()
}
