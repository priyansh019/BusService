package com.example.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.di.AppContainer
import com.example.domain.model.Stop
import com.example.domain.planner.Journey
import com.example.domain.planner.JourneyLeg
import com.example.domain.planner.PlannerPreferences
import com.example.domain.planner.RankingPriority
import com.example.ui.components.DemoDatasetBanner
import com.example.ui.components.ProvenanceBadge
import com.example.ui.components.TrackingStatusBadge
import com.example.ui.theme.TransitBlue
import com.example.ui.theme.TransitNavy
import com.example.util.Formatters
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlanScreen(
    container: AppContainer,
    onNavigateToJourney: (String) -> Unit,
    onNavigateToFareCalculator: (Double?, Double?) -> Unit,
    onNavigateToAlerts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val stops by container.repository.getStopsFlow().collectAsState(initial = emptyList())
    val alerts by container.repository.getAlertsFlow().collectAsState(initial = emptyList())

    var originQuery by remember { mutableStateOf("mamura") }
    var destQuery by remember { mutableStateOf("dasna_college") }
    var originExpanded by remember { mutableStateOf(false) }
    var destExpanded by remember { mutableStateOf(false) }

    var departureSeconds by remember { mutableStateOf(28800) } // Default 08:00 AM (28800 s)
    var priority by remember { mutableStateOf(RankingPriority.RECOMMENDED) }
    var maxTransfers by remember { mutableStateOf(2) }
    var maxWalkMeters by remember { mutableStateOf(1200.0) }

    var journeys by remember { mutableStateOf<List<Journey>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }

    fun executeSearch() {
        if (originQuery.isBlank() || destQuery.isBlank()) return
        isLoading = true
        coroutineScope.launch {
            val results = container.repository.planJourney(
                origin = originQuery,
                destination = destQuery,
                departureSeconds = departureSeconds,
                preferences = PlannerPreferences(
                    priority = priority,
                    maxTransfers = maxTransfers,
                    maxWalkMeters = maxWalkMeters
                )
            )
            journeys = results
            isLoading = false
        }
    }

    LaunchedEffect(originQuery, destQuery, priority, maxTransfers, maxWalkMeters, departureSeconds) {
        executeSearch()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DemoDatasetBanner()

        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = null,
                tint = TransitNavy,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Bus Discovery",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TransitNavy
                )
                Text(
                    text = "Noida – Ghaziabad – Dasna Corridor",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = onNavigateToAlerts,
                modifier = Modifier.testTag("alerts_button")
            ) {
                BadgedBox(
                    badge = {
                        if (alerts.isNotEmpty()) {
                            Badge { Text("${alerts.size}") }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = if (alerts.isNotEmpty()) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Search Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Origin Dropdown Field
                ExposedDropdownMenuBox(
                    expanded = originExpanded,
                    onExpandedChange = { originExpanded = it }
                ) {
                    OutlinedTextField(
                        value = stops.firstOrNull { it.id == originQuery }?.name ?: originQuery,
                        onValueChange = { originQuery = it },
                        label = { Text("From (Origin)") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF059669))
                        },
                        trailingIcon = {
                            IconButton(onClick = { originQuery = "mamura" }) {
                                Icon(Icons.Default.MyLocation, contentDescription = "Use My Location", tint = TransitBlue)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("origin_input"),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = originExpanded,
                        onDismissRequest = { originExpanded = false }
                    ) {
                        stops.forEach { stop ->
                            DropdownMenuItem(
                                text = { Text("${stop.name} (${stop.locality})") },
                                onClick = {
                                    originQuery = stop.id
                                    originExpanded = false
                                }
                            )
                        }
                    }
                }

                // Swap Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = {
                            val temp = originQuery
                            originQuery = destQuery
                            destQuery = temp
                        },
                        modifier = Modifier.testTag("swap_stops_button")
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = "Swap Origin and Destination", tint = TransitBlue)
                    }
                }

                // Destination Dropdown Field
                ExposedDropdownMenuBox(
                    expanded = destExpanded,
                    onExpandedChange = { destExpanded = it }
                ) {
                    OutlinedTextField(
                        value = stops.firstOrNull { it.id == destQuery }?.name ?: destQuery,
                        onValueChange = { destQuery = it },
                        label = { Text("To (Destination)") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFDC2626))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("destination_input"),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = destExpanded,
                        onDismissRequest = { destExpanded = false }
                    ) {
                        stops.forEach { stop ->
                            DropdownMenuItem(
                                text = { Text("${stop.name} (${stop.locality})") },
                                onClick = {
                                    destQuery = stop.id
                                    destExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Time picker & Quick preset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Depart: ${Formatters.formatSecondsToTime(departureSeconds)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    TextButton(
                        onClick = {
                            departureSeconds = if (departureSeconds == 28800) 81000 else 28800 // toggle between 08:00 AM and 22:30 PM (P5 testing)
                        },
                        modifier = Modifier.testTag("toggle_time_button")
                    ) {
                        Text(if (departureSeconds == 28800) "Switch to 22:30" else "Switch to 08:00", fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = { showFilters = !showFilters },
                        modifier = Modifier.testTag("toggle_filters_button")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showFilters) "Hide" else "Filters", fontSize = 11.sp)
                    }
                }

                // Expandable Filters
                if (showFilters) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sort & Priority:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RankingPriority.values().forEach { r ->
                            FilterChip(
                                selected = priority == r,
                                onClick = { priority = r },
                                label = { Text(r.name.replace("_", " ").lowercase().capitalize(), fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_priority_${r.name.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Max Transfers:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0, 1, 2).forEach { t ->
                            FilterChip(
                                selected = maxTransfers == t,
                                onClick = { maxTransfers = t },
                                label = { Text(if (t == 0) "Direct (0)" else "$t Transfers", fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_transfers_$t")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Max Walking Distance:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(300.0, 600.0, 1200.0).forEach { w ->
                            FilterChip(
                                selected = maxWalkMeters == w,
                                onClick = { maxWalkMeters = w },
                                label = { Text("${w.toInt()}m", fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_walk_${w.toInt()}")
                            )
                        }
                    }
                }

                // Demo Preset Quick Link
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable {
                            originQuery = "mamura"
                            destQuery = "dasna_college"
                            departureSeconds = 28800
                            maxWalkMeters = 1200.0
                            priority = RankingPriority.RECOMMENDED
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Preset: Mamura → Dasna College (Weekday 08:00 AM)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TransitNavy
                        )
                    }
                }
            }
        }

        // Search Results Header & List
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when {
                isLoading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = TransitNavy)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Finding routes with verified timetables...", fontSize = 12.sp)
                    }
                }
                journeys.isEmpty() -> {
                    // Explanatory empty states per prompt §7.1 and scenarios P4, P5
                    val emptyMessage = when {
                        departureSeconds > 75600 -> "No service after 21:00 — last service from Mamura 20:30, next 06:30 tomorrow."
                        maxWalkMeters < 910.0 && (destQuery == "dasna_college" || originQuery == "dasna_college") ->
                            "Walking limit too low — nearest valid stop to Dasna College is ≈ 910 m away."
                        maxTransfers == 0 && (destQuery.contains("dasna") && originQuery.contains("mamura")) ->
                            "No direct official service between these stops. Increase max transfers or check community proposals."
                        else -> "No bus journey found matching your constraints. Try adjusting walking limit or transfer settings."
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
                            .testTag("empty_state_view"),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = emptyMessage,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                maxWalkMeters = 1200.0
                                maxTransfers = 2
                                departureSeconds = 28800
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TransitNavy)
                        ) {
                            Text("Reset to standard commute")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("journey_results_list"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(journeys) { journey ->
                            JourneyCard(
                                journey = journey,
                                onClick = { onNavigateToJourney(journey.id) },
                                onCalculateSavings = {
                                    onNavigateToFareCalculator(journey.totalFare.amount, 50.0)
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun JourneyCard(
    journey: Journey,
    onClick: () -> Unit,
    onCalculateSavings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("journey_card_${journey.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Badges row: Provenance & Tracking
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProvenanceBadge(
                    provenance = journey.legs.firstOrNull { it is JourneyLeg.TransitLeg }?.let { (it as JourneyLeg.TransitLeg).provenance } ?: com.example.domain.model.Provenance("Demo", "2026-10-22", "2026-10-22", journey.overallVerificationStatus),
                    isStale = journey.isStale
                )
                Spacer(modifier = Modifier.width(8.dp))
                TrackingStatusBadge(status = journey.overallTrackingStatus)

                Spacer(modifier = Modifier.weight(1f))

                // Fare Display
                Text(
                    text = journey.totalFare.displayText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (journey.totalFare.isComplete) Color(0xFF047857) else Color(0xFFD97706),
                    modifier = Modifier.testTag("journey_fare_text")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Timing row: Departure -> Arrival (Duration)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Formatters.formatSecondsToTime(journey.departureSeconds),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = " → ",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = Formatters.formatSecondsToTime(journey.arrivalSeconds),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${Formatters.formatDuration(journey.totalDurationSeconds)})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Legs line summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                journey.legs.forEachIndexed { idx, leg ->
                    when (leg) {
                        is JourneyLeg.TransitLeg -> {
                            Box(
                                modifier = Modifier
                                    .background(TransitBlue, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = leg.routeShortName,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        is JourneyLeg.WalkLeg -> {
                            Text(
                                text = "Walk ${Formatters.formatDistance(leg.distanceMeters)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    if (idx < journey.legs.size - 1) {
                        Text(
                            text = " + ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = if (journey.transfers == 0) "Direct" else "${journey.transfers} transfer",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (journey.transfers == 0) Color(0xFF047857) else TransitNavy
                )
            }

            // Split breakdown: Walk / Wait / Ride
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Walk: ${journey.walkTimeSeconds / 60}m  ·  Wait: ${journey.waitTimeSeconds / 60}m  ·  Ride: ${journey.rideTimeSeconds / 60}m",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                if (journey.totalFare.isComplete && journey.totalFare.amount != null) {
                    Text(
                        text = "Calc Savings",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TransitBlue,
                        modifier = Modifier
                            .clickable { onCalculateSavings() }
                            .testTag("calc_savings_link")
                    )
                }
            }

            if (journey.hasAlert) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp))
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = journey.alertDescription ?: "Service alert on route",
                        fontSize = 11.sp,
                        color = Color(0xFF991B1B),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
