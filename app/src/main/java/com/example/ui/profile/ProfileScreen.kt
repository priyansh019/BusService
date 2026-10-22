package com.example.ui.profile

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.domain.model.User
import com.example.domain.model.UserRole
import com.example.ui.components.DemoDatasetBanner
import com.example.ui.theme.TransitBlue
import com.example.ui.theme.TransitLightBlue
import com.example.ui.theme.TransitNavy
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    container: AppContainer,
    onNavigateToModerator: () -> Unit,
    onNavigateToOperator: () -> Unit,
    onNavigateToDriver: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToSelfTest: () -> Unit,
    onNavigateToDevJson: () -> Unit,
    onStartTour: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val session by container.currentSession.collectAsState()
    val currentLang by container.currentLanguage.collectAsState()

    var availableUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedUserToSwitch by remember { mutableStateOf<User?>(null) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        availableUsers = container.repository.getUsers()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        DemoDatasetBanner(alwaysVisible = true)

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Profile & Demo Roles",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TransitNavy
            )
            Text(
                text = "Switch between pilot roles to test permissions and consoles",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Current Session Card
            Card(
                modifier = Modifier.fillMaxWidth().testTag("current_user_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(TransitLightBlue, RoundedCornerShape(22.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = TransitNavy)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(session.user.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Active Role: ${session.user.role.name}", fontSize = 12.sp, color = TransitBlue, fontWeight = FontWeight.SemiBold)
                        if (session.user.operatorId != null) {
                            Text("Tenant: ${session.user.operatorId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Role-Specific Action Button (Appears based on active role)
            when (session.user.role) {
                UserRole.MODERATOR -> {
                    Button(
                        onClick = onNavigateToModerator,
                        modifier = Modifier.fillMaxWidth().testTag("open_moderator_console_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Gavel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Moderation Queue")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                UserRole.OPERATOR -> {
                    Button(
                        onClick = onNavigateToOperator,
                        modifier = Modifier.fillMaxWidth().testTag("open_operator_console_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = TransitNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Business, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Operator Console")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                UserRole.DRIVER -> {
                    Button(
                        onClick = onNavigateToDriver,
                        modifier = Modifier.fillMaxWidth().testTag("open_driver_console_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.DirectionsBus, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Driver Console")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                UserRole.ADMIN -> {
                    Button(
                        onClick = onNavigateToAdmin,
                        modifier = Modifier.fillMaxWidth().testTag("open_admin_console_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = TransitNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Administrator Console")
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
                else -> {}
            }

            // Language Switcher
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Language, contentDescription = null, tint = TransitNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Language / भाषा", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = currentLang == "en",
                            onClick = { container.setLanguage("en") },
                            label = { Text("English", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = currentLang == "hi",
                            onClick = { container.setLanguage("hi") },
                            label = { Text("हिंदी", fontSize = 11.sp) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Demo Role Switcher with explicit PINs
            Text("Switch Demo Role (Local Only)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
            Text("PINs are displayed below for immediate reviewer access without login services", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableUsers.forEach { user ->
                    val isCurrent = user.id == session.user.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedUserToSwitch = user
                                enteredPin = user.pin // auto-prefill for smooth in-person event walkthrough
                                pinError = null
                            }
                            .testTag("role_item_${user.role.name.lowercase()}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) TransitLightBlue else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Role: ${user.role.name} · PIN: ${user.pin}", fontSize = 11.sp, color = TransitBlue, fontWeight = FontWeight.SemiBold)
                            }
                            if (isCurrent) {
                                Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Event & Developer Tools Section
            Text("Event & Testing Tools", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TransitNavy)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(onClick = onStartTour, modifier = Modifier.fillMaxWidth().testTag("launch_tour_btn")) {
                        Icon(Icons.Default.Explore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Guided Demo Tour (6 Steps)")
                    }

                    TextButton(onClick = onNavigateToSelfTest, modifier = Modifier.fillMaxWidth().testTag("launch_selftest_btn")) {
                        Icon(Icons.Default.Rule, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Self-Test Suite (P1–P14 Acceptance Checks)")
                    }

                    TextButton(onClick = onNavigateToDevJson, modifier = Modifier.fillMaxWidth().testTag("launch_devjson_btn")) {
                        Icon(Icons.Default.Code, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Developer JSON View (Appendix A Spec)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Role PIN Dialog
    val target = selectedUserToSwitch
    if (target != null) {
        AlertDialog(
            onDismissRequest = { selectedUserToSwitch = null },
            title = { Text("Switch to ${target.role.name}") },
            text = {
                Column {
                    Text("User: ${target.name}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Demo PIN: ${target.pin}", fontSize = 12.sp, color = TransitBlue, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = { enteredPin = it },
                        label = { Text("Enter PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth().testTag("role_pin_input")
                    )
                    if (pinError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pinError ?: "", color = Color(0xFFDC2626), fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val (success, msg) = container.switchRole(target, enteredPin)
                        if (success) {
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            selectedUserToSwitch = null
                        } else {
                            pinError = msg
                        }
                    },
                    modifier = Modifier.testTag("role_pin_confirm_btn")
                ) {
                    Text("Confirm Switch")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedUserToSwitch = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
