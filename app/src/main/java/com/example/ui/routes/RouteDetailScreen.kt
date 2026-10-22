package com.example.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.di.AppContainer
import com.example.domain.model.Route
import com.example.domain.model.RouteStop
import com.example.domain.model.Stop
import com.example.data.repository.toDomain
import com.example.ui.components.ProvenanceBadge
import com.example.ui.theme.TransitBlue
import com.example.ui.theme.TransitLightBlue
import com.example.ui.theme.TransitNavy
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteDetailScreen(
    routeId: String,
    container: AppContainer,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var route by remember { mutableStateOf<Route?>(null) }
    var routeStops by remember { mutableStateOf<List<RouteStop>>(emptyList()) }
    var stopsMap by remember { mutableStateOf<Map<String, Stop>>(emptyMap()) }

    LaunchedEffect(routeId) {
        route = container.repository.getRouteById(routeId)
        val allStops = container.repository.getStops().associateBy { it.id }
        stopsMap = allStops
        val rStops = container.database.busDao().getRouteStops(routeId)
        routeStops = rStops.map { it.toDomain() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(route?.shortName ?: "Line Details", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = TransitNavy
                )
            )
        }
    ) { paddingValues ->
        val r = route
        if (r == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Loading route...")
            }
            return@Scaffold
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(TransitNavy, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(r.shortName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(r.longName, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (r.frequencyMinutes != null) "Frequency: ~${r.frequencyMinutes} min headway" else "Fixed departures schedule",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    ProvenanceBadge(provenance = r.provenance)

                    if (r.firstDepartureSeconds != null && r.lastDepartureSeconds != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = TransitNavy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Operating hours: ${Formatters.formatSecondsToTime(r.firstDepartureSeconds)} to ${Formatters.formatSecondsToTime(r.lastDepartureSeconds)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ordered Stop Sequence
            Text("Ordered Stops & Boarding Rules", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth().testTag("stops_sequence_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    routeStops.forEachIndexed { idx, rs ->
                        val stop = stopsMap[rs.stopId]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(TransitLightBlue, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${rs.sequence}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stop?.name ?: rs.stopId, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Boarding allowed
                                    Icon(
                                        imageVector = if (rs.boardingAllowed) Icons.Default.Check else Icons.Default.Block,
                                        contentDescription = null,
                                        tint = if (rs.boardingAllowed) Color(0xFF047857) else Color(0xFFDC2626),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (rs.boardingAllowed) " Board" else " No board",
                                        fontSize = 11.sp,
                                        color = if (rs.boardingAllowed) Color(0xFF047857) else Color(0xFFDC2626)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Alighting allowed
                                    Icon(
                                        imageVector = if (rs.alightingAllowed) Icons.Default.Check else Icons.Default.Block,
                                        contentDescription = null,
                                        tint = if (rs.alightingAllowed) Color(0xFF047857) else Color(0xFFDC2626),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (rs.alightingAllowed) " Alight" else " No alight",
                                        fontSize = 11.sp,
                                        color = if (rs.alightingAllowed) Color(0xFF047857) else Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
