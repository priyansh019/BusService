package com.example.ui.contribute

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.di.AppContainer
import com.example.domain.model.RouteSubmission
import com.example.domain.model.SubmissionStatus
import com.example.domain.model.SubmissionType
import com.example.domain.model.UserRole
import com.example.ui.components.DemoDatasetBanner
import com.example.ui.theme.TransitBlue
import com.example.ui.theme.TransitNavy
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributeScreen(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val session by container.currentSession.collectAsState()
    val submissions by container.repository.getSubmissionsFlow().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) } // 0: Submit, 1: My Submissions

    // Form inputs
    var submissionType by remember { mutableStateOf(SubmissionType.MISSING_ROUTE) }
    var fromStop by remember { mutableStateOf("Mamura Bus Stop (Sector 66, Noida)") }
    var toStop by remember { mutableStateOf("Dasna Bus Stop") }
    var stopsCsv by remember { mutableStateOf("Mamura, Sector 62, Dasna") }
    var operatorName by remember { mutableStateOf("Local Private Mini-Bus") }
    var proposedFareStr by remember { mutableStateOf("25") }
    var proposedFreqStr by remember { mutableStateOf("40") }
    var evidenceText by remember { mutableStateOf("Frequent white 16-seater mini buses run between Mamura Chowk and Dasna via NH9.") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DemoDatasetBanner()

        // Contributor Reputation Bar
        SurfaceHeader(sessionUser = session.user)

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = TransitNavy
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Submit Report", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_submit_report")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("My Submissions (${submissions.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_my_submissions")
            )
        }

        if (selectedTab == 0) {
            // Submission Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text("Select Report Type:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        SubmissionType.MISSING_ROUTE to "Missing Route",
                        SubmissionType.FARE_CHANGE to "Fare Change",
                        SubmissionType.FREQUENCY_OBSERVATION to "Frequency"
                    ).forEach { (t, label) ->
                        FilterChip(
                            selected = submissionType == t,
                            onClick = { submissionType = t },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fromStop,
                    onValueChange = { fromStop = it },
                    label = { Text("From (Origin)") },
                    modifier = Modifier.fillMaxWidth().testTag("contrib_from_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = toStop,
                    onValueChange = { toStop = it },
                    label = { Text("To (Destination)") },
                    modifier = Modifier.fillMaxWidth().testTag("contrib_to_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = stopsCsv,
                    onValueChange = { stopsCsv = it },
                    label = { Text("Stops in Order (comma separated)") },
                    modifier = Modifier.fillMaxWidth().testTag("contrib_stops_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = proposedFareStr,
                        onValueChange = { proposedFareStr = it },
                        label = { Text("Fare (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("contrib_fare_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = proposedFreqStr,
                        onValueChange = { proposedFreqStr = it },
                        label = { Text("Frequency (mins)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("contrib_freq_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = evidenceText,
                    onValueChange = { evidenceText = it },
                    label = { Text("Observation Evidence & Notes") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("contrib_evidence_input"),
                    maxLines = 4
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFDC2626),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isSubmitting = true
                        errorMessage = null
                        val newSubmission = RouteSubmission(
                            id = "sub_${UUID.randomUUID().toString().take(8)}",
                            submissionType = submissionType,
                            contributorId = session.user.id,
                            contributorName = session.user.name,
                            operatorName = operatorName,
                            fromStopName = fromStop,
                            toStopName = toStop,
                            stopsListCsv = stopsCsv,
                            proposedFare = proposedFareStr.toDoubleOrNull(),
                            proposedFrequencyMinutes = proposedFreqStr.toIntOrNull(),
                            operatingHours = "07:00–19:00",
                            observationDate = "2026-10-22",
                            evidenceText = evidenceText,
                            photoUri = null,
                            status = SubmissionStatus.SUBMITTED,
                            idempotencyKey = "key_${UUID.randomUUID()}"
                        )

                        coroutineScope.launch {
                            val (success, msg) = container.repository.submitContribution(newSubmission, session)
                            isSubmitting = false
                            if (success) {
                                Toast.makeText(context, "Submitted for moderation!", Toast.LENGTH_SHORT).show()
                                selectedTab = 1
                            } else {
                                errorMessage = msg
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("submit_contribution_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = TransitNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit for Moderation")
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        } else {
            // My Submissions Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .testTag("my_submissions_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(submissions) { sub ->
                    SubmissionItemCard(sub = sub)
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun SurfaceHeader(sessionUser: com.example.domain.model.User) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sessionUser.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Role: ${sessionUser.role.name}  ·  Reputation: ${sessionUser.reputationPoints} pts", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun SubmissionItemCard(sub: RouteSubmission) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("submission_card_${sub.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${sub.fromStopName} → ${sub.toStopName}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                SubmissionStatusBadge(status = sub.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (sub.proposedFare != null) {
                Text("Proposed Fare: ₹${sub.proposedFare.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF047857))
            }

            Text("Stops: ${sub.stopsListCsv}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            Text("Evidence: ${sub.evidenceText}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (sub.moderatorNotes != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Moderator: ${sub.moderatorNotes}", fontSize = 11.sp, color = TransitBlue, fontWeight = FontWeight.Medium)
            }

            if (sub.duplicateFlag) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("⚠ Auto-flagged duplicate of ${sub.duplicateOfId}", fontSize = 10.sp, color = Color(0xFFC2410C), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SubmissionStatusBadge(status: SubmissionStatus) {
    val (label, bg, fg) = when (status) {
        SubmissionStatus.APPROVED -> Triple("Approved", Color(0xFFD1FAE5), Color(0xFF047857))
        SubmissionStatus.REJECTED -> Triple("Rejected", Color(0xFFFEE2E2), Color(0xFF991B1B))
        SubmissionStatus.UNDER_REVIEW -> Triple("In Review", Color(0xFFFEF3C7), Color(0xFFB45309))
        SubmissionStatus.AWAITING_SECOND_APPROVAL -> Triple("Needs 2nd Approval", Color(0xFFEDE9FE), Color(0xFF6D28D9))
        SubmissionStatus.CLARIFICATION_REQUESTED -> Triple("Clarification", Color(0xFFFFEDD5), Color(0xFFC2410C))
        else -> Triple("Submitted", Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = label, color = fg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}
