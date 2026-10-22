package com.example.data.sim

import com.example.data.local.BusDao
import com.example.data.local.VehiclePositionEntity
import com.example.domain.model.TrackingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LiveVehicleSimulator(
    private val dao: BusDao,
    private val scope: CoroutineScope
) {
    private var simulationJob: Job? = null

    // Geometry points between Anand Vihar and Dasna along NH9
    private val x1RouteWaypoints = listOf(
        Pair(28.6475, 77.3150), // Anand Vihar
        Pair(28.6550, 77.3450),
        Pair(28.6650, 77.3800),
        Pair(28.6710, 77.4120), // Ghaziabad Adda
        Pair(28.6780, 77.4600),
        Pair(28.6850, 77.5300), // Masuri
        Pair(28.6880, 77.5450)  // Dasna
    )

    private var currentWaypointIndex = 0

    fun start() {
        if (simulationJob?.isActive == true) return

        simulationJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                try {
                    val pt = x1RouteWaypoints[currentWaypointIndex]
                    val nowSec = System.currentTimeMillis() / 1000

                    val livePos = VehiclePositionEntity(
                        id = "pos_x1",
                        tripId = "X1_0830",
                        vehicleId = "UP-14-BT-1024",
                        lat = pt.first,
                        lng = pt.second,
                        timestampSeconds = nowSec,
                        speedKmh = 42.0,
                        heading = 82.0,
                        status = TrackingStatus.LIVE
                    )
                    dao.insertVehiclePosition(livePos)

                    currentWaypointIndex = (currentWaypointIndex + 1) % x1RouteWaypoints.size
                } catch (e: Exception) {
                    // Ignore errors during simulation
                }
                delay(3000L) // 3 seconds per tick
            }
        }
    }

    fun stop() {
        simulationJob?.cancel()
        simulationJob = null
    }
}
