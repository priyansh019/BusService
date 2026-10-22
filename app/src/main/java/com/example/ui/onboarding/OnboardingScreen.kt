package com.example.ui.onboarding

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TransitBlue
import com.example.ui.theme.TransitNavy

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconBg: Color
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = listOf(
        OnboardingPage(
            title = "Unified Bus Discovery",
            description = "Discover unmapped feeder minibuses, state express routes, and city buses across the Noida-Ghaziabad-Dasna-Hapur pilot corridor.",
            icon = Icons.Default.DirectionsBus,
            iconBg = TransitNavy
        ),
        OnboardingPage(
            title = "Strict Data Honesty",
            description = "Demo dataset illustrative notice: Provenance is tracked on every fact. Missing fares are never fabricated, stale data is clearly flagged, and approximate locations are disclosed.",
            icon = Icons.Default.Info,
            iconBg = Color(0xFFD97706)
        ),
        OnboardingPage(
            title = "Offline & In-Person Ready",
            description = "The entire journey planner, schematic transit maps, and local role switcher work 100% offline with zero internet required.",
            icon = Icons.Default.NearMe,
            iconBg = Color(0xFF047857)
        )
    )

    var pageIndex by remember { mutableStateOf(0) }
    val page = pages[pageIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onComplete, modifier = Modifier.testTag("skip_onboarding_btn")) {
                Text("Skip", color = MaterialTheme.colorScheme.outline)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(96.dp)
                .background(page.iconBg, RoundedCornerShape(48.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TransitNavy,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // Indicator dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pages.forEachIndexed { idx, _ ->
                Box(
                    modifier = Modifier
                        .size(if (idx == pageIndex) 10.dp else 6.dp)
                        .background(
                            if (idx == pageIndex) TransitBlue else Color(0xFFCBD5E1),
                            RoundedCornerShape(5.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (pageIndex < pages.size - 1) {
                    pageIndex++
                } else {
                    onComplete()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("onboarding_next_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = TransitNavy),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (pageIndex < pages.size - 1) "Continue" else "Get Started", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
