package com.example.ui.saved

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.di.AppContainer
import com.example.domain.model.SavedJourney
import com.example.ui.components.DemoDatasetBanner
import com.example.ui.theme.TransitNavy
import kotlinx.coroutines.launch

@Composable
fun SavedJourneysScreen(
    container: AppContainer,
    onNavigateToPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val savedJourneys by container.repository.getSavedJourneysFlow().collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DemoDatasetBanner()

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Saved Journeys & Commutes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TransitNavy
            )
            Text(
                text = "Tracked daily routes with real-time alert notifications",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        if (savedJourneys.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text("No saved journeys yet.", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "Plan a journey and tap the bookmark icon to save your commute.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("saved_journeys_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(savedJourneys) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("saved_journey_${item.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.originStopId} → ${item.destinationStopId}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Departure: ${item.departureTimeFormatted} · Line: ${item.routeSummary}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                Text("Fare: ${item.fareFormatted}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TransitNavy)
                            }

                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        container.repository.removeSavedJourney(item.id)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}
