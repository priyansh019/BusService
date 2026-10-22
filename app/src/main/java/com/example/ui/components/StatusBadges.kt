package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Provenance
import com.example.domain.model.TrackingStatus
import com.example.domain.model.VerificationStatus
import com.example.ui.theme.StatusApproximate
import com.example.ui.theme.StatusApproximateBg
import com.example.ui.theme.StatusCommunityApproved
import com.example.ui.theme.StatusCommunityApprovedBg
import com.example.ui.theme.StatusEstimated
import com.example.ui.theme.StatusEstimatedBg
import com.example.ui.theme.StatusLive
import com.example.ui.theme.StatusLiveBg
import com.example.ui.theme.StatusOfficialVerified
import com.example.ui.theme.StatusOfficialVerifiedBg
import com.example.ui.theme.StatusScheduled
import com.example.ui.theme.StatusScheduledBg
import com.example.ui.theme.StatusStale
import com.example.ui.theme.StatusStaleBg
import com.example.ui.theme.StatusUnavailable
import com.example.ui.theme.StatusUnavailableBg

@Composable
fun ProvenanceBadge(
    provenance: Provenance,
    isStale: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    val (icon, label, textColor, bgColor) = when {
        isStale -> BadgeStyle(Icons.Default.Warning, "STALE", StatusStale, StatusStaleBg)
        provenance.verificationStatus == VerificationStatus.OFFICIAL_VERIFIED -> BadgeStyle(Icons.Default.CheckCircle, "Official", StatusOfficialVerified, StatusOfficialVerifiedBg)
        provenance.verificationStatus == VerificationStatus.COMMUNITY_APPROVED -> BadgeStyle(Icons.Default.Group, "Community-approved", StatusCommunityApproved, StatusCommunityApprovedBg)
        provenance.verificationStatus == VerificationStatus.APPROXIMATE -> BadgeStyle(Icons.Default.LocationOn, "Approximate", StatusApproximate, StatusApproximateBg)
        else -> BadgeStyle(Icons.Default.Info, "Unverified", StatusScheduled, StatusScheduledBg)
    }

    Box(
        modifier = modifier
            .testTag("provenance_badge_${label.lowercase()}")
            .background(bgColor, RoundedCornerShape(12.dp))
            .clickable { showDialog = true }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = "$label data verification badge",
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    if (showDialog) {
        ProvenanceDetailDialog(
            provenance = provenance,
            isStale = isStale,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun TrackingStatusBadge(
    status: TrackingStatus,
    modifier: Modifier = Modifier
) {
    val (icon, label, textColor, bgColor) = when (status) {
        TrackingStatus.LIVE -> BadgeStyle(Icons.Default.NearMe, "LIVE GPS", StatusLive, StatusLiveBg)
        TrackingStatus.ESTIMATED -> BadgeStyle(Icons.Default.AccessTime, "ESTIMATED", StatusEstimated, StatusEstimatedBg)
        TrackingStatus.SCHEDULED -> BadgeStyle(Icons.Default.Schedule, "SCHEDULED", StatusScheduled, StatusScheduledBg)
        TrackingStatus.COMMUNITY_REPORTED -> BadgeStyle(Icons.Default.Group, "REPORTED", StatusCommunityApproved, StatusCommunityApprovedBg)
        TrackingStatus.UNAVAILABLE -> BadgeStyle(Icons.Default.CloudOff, "UNAVAILABLE", StatusUnavailable, StatusUnavailableBg)
    }

    Box(
        modifier = modifier
            .testTag("tracking_badge_${status.name.lowercase()}")
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = "$label tracking status",
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ProvenanceDetailDialog(
    provenance: Provenance,
    isStale: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Data Provenance & Audit")
            }
        },
        text = {
            Column {
                ProvenanceFactRow(label = "Source", value = provenance.source)
                ProvenanceFactRow(label = "Effective Date", value = provenance.effectiveDate)
                ProvenanceFactRow(label = "Last Verified", value = provenance.lastVerifiedAt)
                ProvenanceFactRow(label = "Verification Status", value = provenance.verificationStatus.name.replace("_", " "))
                if (provenance.reviewer != null) {
                    ProvenanceFactRow(label = "Reviewer / Authority", value = provenance.reviewer)
                }
                if (isStale) {
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "⚠ Schedule STALE: Data was last verified more than 90 days ago.",
                        color = StatusStale,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun ProvenanceFactRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Normal)
    }
}

private data class BadgeStyle(
    val icon: ImageVector,
    val label: String,
    val textColor: Color,
    val bgColor: Color
)
