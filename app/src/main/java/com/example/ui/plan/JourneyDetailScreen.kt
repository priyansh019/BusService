package com.example.ui.plan

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.di.AppContainer
import com.example.domain.model.SavedJourney
import com.example.domain.planner.Journey
import com.example.domain.planner.JourneyLeg
import com.example.domain.planner.PlannerPreferences
import com.example.ui.components.ProvenanceBadge
import com.example.ui.components.SchematicMapView
import com.example.ui.components.TrackingStatusBadge
import com.example.ui.theme.TransitBlue
import com.example.ui.theme.TransitNavy
import com.example.util.Formatters
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyDetailScreen(
    journeyId: String,
    container: AppContainer,
    onBack: () -> Unit,
    onReportIssue: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var journey by remember { mutableStateOf<Journey?>(null) }
    var isSaved by remember { mutableStateOf(false) }

    LaunchedEffect(journeyId) {
        // Re-execute plan to find matching journey id
        val results = container.repository.planJourney(
            origin = "mamura",
            destination = "dasna_college",
            departureSeconds = 28800,
            preferences = PlannerPreferences()
        )
        journey = results.firstOrNull { it.id == journeyId } ?: results.firstOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journey Detail", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val j = journey ?: return@IconButton
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Bus Journey: ${Formatters.formatSecondsToTime(j.departureSeconds)} to ${Formatters.formatSecondsToTime(j.arrivalSeconds)}, Fare: ${j.totalFare.displayText} via Bus Discovery"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Journey"))
                        },
                        modifier = Modifier.testTag("share_journey_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }

                    IconButton(
                        onClick = {
                            val j = journey ?: return@IconButton
                            coroutineScope.launch {
                                container.repository.saveJourney(
                                    SavedJourney(
                                        id = "save_${UUID.randomUUID().toString().take(8)}",
                                        originStopId = "mamura",
                                        destinationStopId = "dasna_college",
                                        departureTimeFormatted = Formatters.formatSecondsToTime(j.departureSeconds),
                                        routeSummary = j.legs.filterIsInstance<JourneyLeg.TransitLeg>().joinToString(" + ") { it.routeShortName },
                                        fareFormatted = j.totalFare.displayText
                                    )
                                )
                                isSaved = true
                                Toast.makeText(context, "Journey saved to your profile!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("save_journey_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Save",
                            tint = if (isSaved) TransitBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = TransitNavy
                )
            )
        }
    ) { paddingValues ->
        val j = journey
        if (j == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Loading journey details...")
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
            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("journey_summary_header"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${Formatters.formatSecondsToTime(j.departureSeconds)} → ${Formatters.formatSecondsToTime(j.arrivalSeconds)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TransitNavy
                            )
                            Text(
                                text = "Total Duration: ${Formatters.formatDuration(j.totalDurationSeconds)}  ·  ${if (j.transfers == 0) "Direct" else "${j.transfers} transfer"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Text(
                            text = j.totalFare.displayText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (j.totalFare.isComplete) Color(0xFF047857) else Color(0xFFD97706)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProvenanceBadge(
                            provenance = j.legs.filterIsInstance<JourneyLeg.TransitLeg>().firstOrNull()?.provenance ?: com.example.domain.model.Provenance("Official Roster", "2026-10-22", "2026-10-22", j.overallVerificationStatus),
                            isStale = j.isStale
                        )
                        TrackingStatusBadge(status = j.overallTrackingStatus)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Schematic Map Card (Canvas route renderer)
            val firstTransit = j.legs.filterIsInstance<JourneyLeg.TransitLeg>().firstOrNull()
            if (firstTransit != null) {
                SchematicMapView(
                    boardStopName = firstTransit.boardStopName,
                    alightStopName = firstTransit.alightStopName,
                    intermediateStops = firstTransit.intermediateStops,
                    hasLiveVehicle = firstTransit.trackingStatus == com.example.domain.model.TrackingStatus.LIVE
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Navigate to Boarding Point button
            Button(
                onClick = {
                    val firstBoard = firstTransit?.boardStopName ?: "Mamura Bus Stop"
                    val gmmIntentUri = Uri.parse("geo:28.6015,77.3685?q=Mamura+Bus+Stop+Sector+66+Noida")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    try {
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        // Fallback copy coordinates
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Coordinates", "28.6015, 77.3685"))
                        Toast.makeText(context, "Boarding point coordinates (28.6015, 77.3685) copied to clipboard", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("navigate_boarding_point_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TransitNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Navigate to Boarding Point")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Leg Timeline
            Text("Journey Timeline & Legs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
            Spacer(modifier = Modifier.height(8.dp))

            j.legs.forEachIndexed { index, leg ->
                LegTimelineCard(leg = leg)
                if (index < j.legs.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Assumptions & Transparency Panel
            Card(
                modifier = Modifier.fillMaxWidth().testTag("assumptions_panel"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TransitNavy, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Engine Assumptions & Accuracy", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    j.assumptions.forEach {
                        Text("• $it", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (j.uncertainty.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        j.uncertainty.forEach {
                            Text("⚠ $it", fontSize = 11.sp, color = Color(0xFFC2410C), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Report issue button
            OutlinedButton(
                onClick = { onReportIssue(firstTransit?.routeId ?: "L2") },
                modifier = Modifier.fillMaxWidth().testTag("report_issue_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Report an Issue on this Service", color = Color(0xFFDC2626))
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun LegTimelineCard(leg: JourneyLeg) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            when (leg) {
                is JourneyLeg.WalkLeg -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Walk to ${leg.toStopName}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${Formatters.formatDistance(leg.distanceMeters)} · ~${leg.durationSeconds / 60} mins",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = leg.assumptionText,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                is JourneyLeg.TransitLeg -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(TransitBlue, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(leg.routeShortName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(leg.routeLongName, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(leg.operatorName, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                        Text(
                            text = if (leg.isFareComplete && leg.fareAmount != null) "₹${leg.fareAmount.toInt()}" else "Fare n/a",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (leg.isFareComplete) Color(0xFF047857) else Color(0xFFD97706)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Boarding Stop
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF059669), RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(leg.boardStopName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(Formatters.formatSecondsToTime(leg.scheduledDepartureSeconds), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Intermediate Stops Expander
                    if (leg.intermediateStops.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .padding(vertical = 4.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${leg.intermediateStops.size} intermediate stops",
                                fontSize = 11.sp,
                                color = TransitBlue,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = TransitBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        if (expanded) {
                            Column(modifier = Modifier.padding(start = 24.dp, bottom = 6.dp)) {
                                leg.intermediateStops.forEach { s ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(s.stopName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(Formatters.formatSecondsToTime(s.arrivalSeconds), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    }

                    // Alighting Stop
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFDC2626), RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(leg.alightStopName, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(Formatters.formatSecondsToTime(leg.scheduledArrivalSeconds), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fare rule: ${leg.fareDescription}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
