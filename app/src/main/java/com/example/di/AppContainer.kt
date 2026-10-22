package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.BusDao
import com.example.data.repository.BusDiscoveryRepositoryImpl
import com.example.data.sim.LiveVehicleSimulator
import com.example.domain.model.Session
import com.example.domain.model.User
import com.example.domain.model.UserRole
import com.example.domain.repository.BusDiscoveryRepository
import com.example.domain.time.DemoClock
import com.example.domain.time.DemoClockImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppContainer(val context: Context) {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    val busDao: BusDao by lazy {
        database.busDao()
    }

    val demoClock: DemoClock by lazy {
        DemoClockImpl()
    }

    val repository: BusDiscoveryRepository by lazy {
        BusDiscoveryRepositoryImpl(busDao, demoClock)
    }

    val liveSimulator: LiveVehicleSimulator by lazy {
        LiveVehicleSimulator(busDao, appScope)
    }

    // Default session: Passenger (no login required)
    private val defaultPassengerUser = User(
        id = "user_passenger",
        name = "Anita Sharma",
        role = UserRole.PASSENGER,
        operatorId = null,
        assignedTripId = null,
        pin = "0000",
        reputationPoints = 10
    )

    private val _currentSession = MutableStateFlow(Session(defaultPassengerUser))
    val currentSession: StateFlow<Session> = _currentSession.asStateFlow()

    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    init {
        // Initialize seed data if database is empty
        appScope.launch(Dispatchers.IO) {
            val stops = busDao.getAllStops()
            if (stops.isEmpty()) {
                repository.resetDemoData()
            }
            liveSimulator.start()
        }
    }

    fun switchRole(user: User, enteredPin: String): Pair<Boolean, String> {
        if (enteredPin != user.pin) {
            return Pair(false, "Incorrect PIN for demo role ${user.role.name}. Expected PIN: ${user.pin}")
        }
        _currentSession.value = Session(user)
        return Pair(true, "Switched role to ${user.role.name} (${user.name})")
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }
}
