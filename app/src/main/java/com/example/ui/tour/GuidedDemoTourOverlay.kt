package com.example.ui.tour

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.TransitBlue
import com.example.ui.theme.TransitNavy

data class TourStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val actionHint: String
)

@Composable
fun GuidedDemoTourOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        TourStep(1, "Search Mamura → Dasna College", "Notice the 700m straight-line distance to the engineering college landmark is automatically resolved with the 1.3 detour factor to a 910m walking leg (~12 mins).", "Tap 'Search' or use the college commute preset."),
        TourStep(2, "Compare Cheapest vs Fastest", "Cheapest (L2 + X1) costs ₹35 with full fare completeness. Fastest (L2 + X2) arrives 5 mins earlier at 09:25, but fare is '₹15 + unknown' and data quality is STALE.", "Sort by 'Cheapest' vs 'Fastest'."),
        TourStep(3, "Inspect Leg & Masuri Pass-Through", "Tap the journey to inspect intermediate stops. Notice X1 passes through Masuri without offering boarding or alighting, strictly enforcing the gazetted express rules.", "Open Journey Detail to see the schematic route map."),
        TourStep(4, "Submit Missing Route (D1)", "Switch role to Contributor in Profile. Open Contribute and submit the direct white mini-bus from Mamura to Dasna charging flat ₹25.", "Submit with idempotency key and observation text."),
        TourStep(5, "Approve as Moderator", "Switch role to Moderator. Open the Review Queue, compare proposal vs verified data, and click 'Approve'. This writes an immutable verification audit record.", "Confirm approval in the moderation dialog."),
        TourStep(6, "Re-search & Commute Savings", "Return to Plan. Re-running Mamura → Dasna now surfaces the newly discovered ₹25 direct mini-bus! Open the Fare Calculator to see ₹1,100/month savings vs ₹50 auto.", "Celebrate the successful community-driven transit flow!")
    )

    var currentStepIndex by remember { mutableStateOf(0) }
    val step = steps[currentStepIndex]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable { /* prevent click-through */ },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("tour_overlay_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = TransitNavy,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Demo Tour: Step ${step.stepNumber} of ${steps.size}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TransitNavy
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close Tour")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = step.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = step.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFDBEAFE), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Action: ${step.actionHint}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E3A8A)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        TextButton(onClick = { currentStepIndex-- }) {
                            Text("Previous")
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (currentStepIndex < steps.size - 1) {
                        Button(
                            onClick = { currentStepIndex++ },
                            colors = ButtonDefaults.buttonColors(containerColor = TransitNavy),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Next Step")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.NavigateNext, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Finish Tour")
                        }
                    }
                }
            }
        }
    }
}
