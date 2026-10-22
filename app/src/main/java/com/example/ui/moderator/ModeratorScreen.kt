package com.example.ui.moderator

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.di.AppContainer
import com.example.domain.model.RouteSubmission
import com.example.domain.model.SubmissionStatus
import com.example.ui.components.DemoDatasetBanner
import com.example.ui.contribute.SubmissionStatusBadge
import com.example.ui.theme.TransitBlue
import com.example.ui.theme.TransitNavy
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorScreen(
    container: AppContainer,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val session by container.currentSession.collectAsState()
    val submissions by container.repository.getSubmissionsFlow().collectAsState(initial = emptyList())

    var activeSubmission by remember { mutableStateOf<RouteSubmission?>(null) }
    var actionType by remember { mutableStateOf<String?>(null) } // "APPROVE", "REJECT", "CLARIFY", "MERGE"
    var actionNotes by remember { mutableStateOf("") }

    val pending = submissions.filter {
        it.status == SubmissionStatus.SUBMITTED || it.status == SubmissionStatus.AWAITING_SECOND_APPROVAL
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moderation Review Queue", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            DemoDatasetBanner()

            // Header stats
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Gavel, contentDescription = null, tint = TransitNavy, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Moderator: ${session.user.name}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Pending decisions in queue: ${pending.size}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            if (pending.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("All pending submissions have been reviewed!", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .testTag("moderator_queue_list"),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pending) { sub ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mod_item_${sub.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                Text("Proposed by: ${sub.contributorName} (${sub.observationDate})", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

                                if (sub.proposedFare != null) {
                                    Text("Fare: ₹${sub.proposedFare.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                                }

                                Text("Stops: ${sub.stopsListCsv}", fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Evidence: \"${sub.evidenceText}\"", fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                if (sub.duplicateFlag) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("⚠ Duplicate flag: matches verified route ${sub.duplicateOfId}", color = Color(0xFFC2410C), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Moderation action buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            activeSubmission = sub
                                            actionType = "APPROVE"
                                            actionNotes = "Verified against field observations. Approving and publishing corridor service."
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                        modifier = Modifier.testTag("mod_approve_btn_${sub.id}")
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve", fontSize = 12.sp)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            activeSubmission = sub
                                            actionType = "REJECT"
                                            actionNotes = "Insufficient evidence or conflicting timetable."
                                        },
                                        modifier = Modifier.testTag("mod_reject_btn_${sub.id}")
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reject", color = Color(0xFFDC2626), fontSize = 12.sp)
                                    }

                                    if (sub.duplicateFlag) {
                                        OutlinedButton(
                                            onClick = {
                                                activeSubmission = sub
                                                actionType = "MERGE"
                                                actionNotes = "Merged duplicate report into verified route ${sub.duplicateOfId}."
                                            }
                                        ) {
                                            Icon(Icons.Default.MergeType, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Merge", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(40.dp)) }
                }
            }
        }
    }

    // Action Confirmation Dialog
    val currentSub = activeSubmission
    val currentAction = actionType
    if (currentSub != null && currentAction != null) {
        AlertDialog(
            onDismissRequest = {
                activeSubmission = null
                actionType = null
            },
            title = { Text("$currentAction Submission") },
            text = {
                Column {
                    Text("Target: ${currentSub.fromStopName} → ${currentSub.toStopName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = actionNotes,
                        onValueChange = { actionNotes = it },
                        label = { Text("Reviewer Notes & Justification") },
                        modifier = Modifier.fillMaxWidth().testTag("mod_notes_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val decisionCode = when (currentAction) {
                            "APPROVE" -> "APPROVED"
                            "REJECT" -> "REJECTED"
                            "MERGE" -> "MERGED"
                            else -> "CLARIFICATION_REQUESTED"
                        }

                        coroutineScope.launch {
                            val (success, msg) = container.repository.reviewSubmission(
                                submissionId = currentSub.id,
                                decision = decisionCode,
                                notes = actionNotes,
                                session = session
                            )
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            activeSubmission = null
                            actionType = null
                        }
                    },
                    modifier = Modifier.testTag("mod_confirm_dialog_btn")
                ) {
                    Text("Confirm Decision")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    activeSubmission = null
                    actionType = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
