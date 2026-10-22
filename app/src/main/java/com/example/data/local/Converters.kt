package com.example.data.local

import androidx.room.TypeConverter
import com.example.domain.model.AlertSeverity
import com.example.domain.model.FareRuleType
import com.example.domain.model.StopType
import com.example.domain.model.SubmissionStatus
import com.example.domain.model.SubmissionType
import com.example.domain.model.TrackingStatus
import com.example.domain.model.UserRole
import com.example.domain.model.VerificationStatus

class Converters {
    @TypeConverter fun fromVerificationStatus(value: VerificationStatus?): String = value?.name ?: VerificationStatus.UNVERIFIED.name
    @TypeConverter fun toVerificationStatus(value: String?): VerificationStatus = value?.let { enumValueOf<VerificationStatus>(it) } ?: VerificationStatus.UNVERIFIED

    @TypeConverter fun fromTrackingStatus(value: TrackingStatus?): String = value?.name ?: TrackingStatus.UNAVAILABLE.name
    @TypeConverter fun toTrackingStatus(value: String?): TrackingStatus = value?.let { enumValueOf<TrackingStatus>(it) } ?: TrackingStatus.UNAVAILABLE

    @TypeConverter fun fromFareRuleType(value: FareRuleType?): String = value?.name ?: FareRuleType.FIXED.name
    @TypeConverter fun toFareRuleType(value: String?): FareRuleType = value?.let { enumValueOf<FareRuleType>(it) } ?: FareRuleType.FIXED

    @TypeConverter fun fromUserRole(value: UserRole?): String = value?.name ?: UserRole.PASSENGER.name
    @TypeConverter fun toUserRole(value: String?): UserRole = value?.let { enumValueOf<UserRole>(it) } ?: UserRole.PASSENGER

    @TypeConverter fun fromStopType(value: StopType?): String = value?.name ?: StopType.ROADSIDE.name
    @TypeConverter fun toStopType(value: String?): StopType = value?.let { enumValueOf<StopType>(it) } ?: StopType.ROADSIDE

    @TypeConverter fun fromAlertSeverity(value: AlertSeverity?): String = value?.name ?: AlertSeverity.INFO.name
    @TypeConverter fun toAlertSeverity(value: String?): AlertSeverity = value?.let { enumValueOf<AlertSeverity>(it) } ?: AlertSeverity.INFO

    @TypeConverter fun fromSubmissionType(value: SubmissionType?): String = value?.name ?: SubmissionType.MISSING_ROUTE.name
    @TypeConverter fun toSubmissionType(value: String?): SubmissionType = value?.let { enumValueOf<SubmissionType>(it) } ?: SubmissionType.MISSING_ROUTE

    @TypeConverter fun fromSubmissionStatus(value: SubmissionStatus?): String = value?.name ?: SubmissionStatus.SUBMITTED.name
    @TypeConverter fun toSubmissionStatus(value: String?): SubmissionStatus = value?.let { enumValueOf<SubmissionStatus>(it) } ?: SubmissionStatus.SUBMITTED
}
