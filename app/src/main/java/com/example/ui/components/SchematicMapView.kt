package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.planner.IntermediateStopInfo
import com.example.ui.theme.TransitBlue
import com.example.ui.theme.TransitNavy

data class SchematicStop(
    val name: String,
    val isBoarding: Boolean = false,
    val isAlighting: Boolean = false,
    val isPassThrough: Boolean = false,
    val hasBusNow: Boolean = false
)

@Composable
fun SchematicMapView(
    boardStopName: String,
    alightStopName: String,
    intermediateStops: List<IntermediateStopInfo> = emptyList(),
    hasLiveVehicle: Boolean = false,
    modifier: Modifier = Modifier
) {
    val stops = mutableListOf<SchematicStop>()
    stops.add(SchematicStop(boardStopName, isBoarding = true))
    intermediateStops.forEach {
        val isPass = it.stopName.contains("Masuri", ignoreCase = true)
        stops.add(SchematicStop(it.stopName, isPassThrough = isPass))
    }
    stops.add(SchematicStop(alightStopName, isAlighting = true))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("schematic_map_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Schematic Route Map",
                    tint = TransitNavy,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Route Schematic & Waypoints",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                if (hasLiveVehicle) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFD1FAE5), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = "Live Bus",
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Live on line", fontSize = 10.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas drawing vertical transit diagram
            val stopHeight = 44.dp
            val canvasHeight = (stopHeight * stops.size).coerceAtLeast(120.dp)

            Box(modifier = Modifier.fillMaxWidth().height(canvasHeight)) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val xPos = 24.dp.toPx()
                    val totalH = size.height
                    val stepY = totalH / stops.size

                    // Draw route line
                    drawLine(
                        color = TransitBlue,
                        start = Offset(xPos, stepY / 2),
                        end = Offset(xPos, totalH - (stepY / 2)),
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round
                    )

                    // Draw stop points
                    stops.forEachIndexed { index, stop ->
                        val centerY = (stepY * index) + (stepY / 2)
                        when {
                            stop.isBoarding -> {
                                drawCircle(Color(0xFF059669), radius = 10.dp.toPx(), center = Offset(xPos, centerY))
                                drawCircle(Color.White, radius = 5.dp.toPx(), center = Offset(xPos, centerY))
                            }
                            stop.isAlighting -> {
                                drawCircle(Color(0xFFDC2626), radius = 10.dp.toPx(), center = Offset(xPos, centerY))
                                drawCircle(Color.White, radius = 5.dp.toPx(), center = Offset(xPos, centerY))
                            }
                            stop.isPassThrough -> {
                                drawCircle(Color(0xFF94A3B8), radius = 7.dp.toPx(), center = Offset(xPos, centerY))
                                drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(xPos, centerY))
                            }
                            else -> {
                                drawCircle(TransitNavy, radius = 7.dp.toPx(), center = Offset(xPos, centerY))
                                drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(xPos, centerY))
                            }
                        }
                    }
                }

                // Stop text labels
                Column(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(start = 48.dp)
                ) {
                    stops.forEach { stop ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stop.name,
                                        fontSize = 13.sp,
                                        fontWeight = if (stop.isBoarding || stop.isAlighting) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            stop.isBoarding -> Color(0xFF059669)
                                            stop.isAlighting -> Color(0xFFDC2626)
                                            stop.isPassThrough -> Color(0xFF64748B)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (stop.isPassThrough) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(Pass-through only)",
                                            fontSize = 11.sp,
                                            color = Color(0xFFEA580C),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                if (stop.isBoarding) {
                                    Text("Boarding stop", fontSize = 11.sp, color = Color(0xFF059669))
                                } else if (stop.isAlighting) {
                                    Text("Alighting stop", fontSize = 11.sp, color = Color(0xFFDC2626))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
