package com.example.ui.selftest

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TransitNavy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperJsonScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val sampleAppendixAJson = """
{
  "journey_id": "J_X1_mamura_dasna_0800",
  "request": {
    "origin": { "stop_id": "mamura", "name": "Mamura Bus Stop (Sector 66, Noida)" },
    "destination": { "stop_id": "dasna_college", "name": "Demo Engineering College, Dasna" },
    "departure_time": "2026-10-22T08:00:00+05:30",
    "preferences": {
      "priority": "RECOMMENDED",
      "max_transfers": 2,
      "max_walk_meters": 1200
    }
  },
  "response": {
    "total_fare": {
      "amount": 35.0,
      "currency": "INR",
      "complete": true,
      "status": "OFFICIAL_VERIFIED"
    },
    "duration_seconds": 5400,
    "transfers": 1,
    "legs": [
      {
        "type": "TRANSIT",
        "route_id": "L2",
        "route_short_name": "L2",
        "operator": "Demo City Bus Services",
        "board_stop": "mamura",
        "alight_stop": "gzb_adda",
        "scheduled_departure": "08:00:00",
        "scheduled_arrival": "08:40:00",
        "fare": { "amount": 15.0, "type": "FIXED", "complete": true }
      },
      {
        "type": "TRANSFER_WAIT",
        "stop_id": "gzb_adda",
        "duration_seconds": 900
      },
      {
        "type": "TRANSIT",
        "route_id": "X1",
        "route_short_name": "X1",
        "operator": "Demo State Roadways",
        "board_stop": "gzb_adda",
        "alight_stop": "dasna",
        "scheduled_departure": "08:55:00",
        "scheduled_arrival": "09:30:00",
        "tracking_status": "LIVE",
        "fare": { "amount": 20.0, "type": "STAGE", "complete": true }
      },
      {
        "type": "WALK",
        "from": "dasna",
        "to": "dasna_college",
        "distance_meters": 910.0,
        "duration_seconds": 728,
        "assumption": "Straight-line 700m × 1.3 detour @ 4.5 km/h"
      }
    ],
    "provenance": {
      "source": "UPSRTC Gazetted Timetable & Feeder Schedule",
      "effective_date": "2026-08-01",
      "last_verified": "2026-10-15",
      "verification_status": "OFFICIAL_VERIFIED"
    }
  }
}
    """.trimIndent()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer JSON View", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Planner JSON", sampleAppendixAJson))
                            Toast.makeText(context, "Appendix A JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("copy_json_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy JSON")
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
                .background(Color(0xFF0F172A))
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = sampleAppendixAJson,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFF38BDF8),
                lineHeight = 16.sp,
                modifier = Modifier.testTag("json_payload_text")
            )
        }
    }
}
