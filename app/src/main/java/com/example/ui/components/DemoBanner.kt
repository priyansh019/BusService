package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.example.ui.theme.TransitLightAmber

@Composable
fun DemoDatasetBanner(
    modifier: Modifier = Modifier,
    alwaysVisible: Boolean = false
) {
    var isDismissed by remember { mutableStateOf(false) }

    if (isDismissed && !alwaysVisible) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("demo_dataset_banner")
            .background(TransitLightAmber)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Demo notice",
            tint = Color(0xFFB45309),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Demo dataset — illustrative routes/fares/times, not real services.",
            fontSize = 11.sp,
            color = Color(0xFF92400E),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        if (!alwaysVisible) {
            IconButton(
                onClick = { isDismissed = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss notice",
                    tint = Color(0xFF92400E),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
