package com.example.domain.time

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

enum class ClockMode {
    FIXED_WEEKDAY_MORNING, // Fixed 2026-10-22 08:00:00 IST (Thursday)
    REAL_TIME,
    CUSTOM
}

interface DemoClock {
    val zoneId: ZoneId
    val clockMode: StateFlow<ClockMode>
    fun now(): ZonedDateTime
    fun nowLocalDateTime(): LocalDateTime = now().toLocalDateTime()
    fun today(): LocalDate = now().toLocalDate()
    fun nowSecondsFromMidnight(): Int {
        val time = now().toLocalTime()
        return time.toSecondOfDay()
    }
    fun setMode(mode: ClockMode)
    fun setCustomDateTime(dateTime: LocalDateTime)
}

class DemoClockImpl(
    override val zoneId: ZoneId = ZoneId.of("Asia/Kolkata")
) : DemoClock {
    private val defaultDemoDateTime: LocalDateTime = LocalDateTime.of(2026, 10, 22, 8, 0, 0)
    private var customDateTime: LocalDateTime = defaultDemoDateTime

    private val _clockMode = MutableStateFlow(ClockMode.FIXED_WEEKDAY_MORNING)
    override val clockMode: StateFlow<ClockMode> = _clockMode.asStateFlow()

    override fun now(): ZonedDateTime {
        return when (_clockMode.value) {
            ClockMode.FIXED_WEEKDAY_MORNING -> defaultDemoDateTime.atZone(zoneId)
            ClockMode.REAL_TIME -> ZonedDateTime.now(zoneId)
            ClockMode.CUSTOM -> customDateTime.atZone(zoneId)
        }
    }

    override fun setMode(mode: ClockMode) {
        _clockMode.value = mode
    }

    override fun setCustomDateTime(dateTime: LocalDateTime) {
        customDateTime = dateTime
        _clockMode.value = ClockMode.CUSTOM
    }
}
