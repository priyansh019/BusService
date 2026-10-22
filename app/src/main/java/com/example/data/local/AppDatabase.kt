package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        StopEntity::class,
        StopAliasEntity::class,
        RouteEntity::class,
        RouteStopEntity::class,
        TripEntity::class,
        StopTimeEntity::class,
        ServiceCalendarEntity::class,
        CalendarExceptionEntity::class,
        FareRuleEntity::class,
        FareStageEntity::class,
        OperatorEntity::class,
        UserEntity::class,
        VehiclePositionEntity::class,
        ServiceAlertEntity::class,
        RouteSubmissionEntity::class,
        VerificationEntity::class,
        AuditLogEntity::class,
        AnalyticsEventEntity::class,
        SavedJourneyEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun busDao(): BusDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bus_discovery.db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
