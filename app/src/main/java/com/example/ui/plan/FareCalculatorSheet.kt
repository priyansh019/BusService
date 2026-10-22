package com.example.ui.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.fare.SavingsCalculator
import com.example.ui.theme.TransitBlue
import com.example.ui.theme.TransitNavy

@Composable
fun FareCalculatorContent(
    initialBusFare: Double? = 25.0,
    initialAlternative: Double? = 50.0,
    modifier: Modifier = Modifier
) {
    var busFareStr by remember { mutableStateOf(initialBusFare?.toInt()?.toString() ?: "25") }
    var altCostStr by remember { mutableStateOf(initialAlternative?.toInt()?.toString() ?: "50") }
    var workingDaysStr by remember { mutableStateOf("22") }

    val busFare = busFareStr.toDoubleOrNull()
    val altCost = altCostStr.toDoubleOrNull()
    val workingDays = workingDaysStr.toIntOrNull() ?: 22

    val result = SavingsCalculator.calculate(busFare, altCost, workingDays)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("fare_calculator_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    tint = TransitNavy,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fare & Commute Savings Calculator",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TransitNavy
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bus Fare input
            OutlinedTextField(
                value = busFareStr,
                onValueChange = { busFareStr = it },
                label = { Text("Bus One-Way Fare (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("calc_bus_fare_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Alternative (Auto / Cab) cost input
            OutlinedTextField(
                value = altCostStr,
                onValueChange = { altCostStr = it },
                label = { Text("Alternative Mode One-Way (Auto / Cab) (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("calc_alt_fare_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Working Days input
            OutlinedTextField(
                value = workingDaysStr,
                onValueChange = { workingDaysStr = it },
                label = { Text("Working Days per Month") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("calc_working_days_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (result != null) {
                // Calculated Results Card
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("calc_result_box"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD1FAE5))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF047857))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Monthly Commuter Savings",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "₹${result.monthlySavings.toInt()} / month",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF047857),
                            modifier = Modifier.testTag("monthly_savings_amount")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "One-way: ₹${result.oneWayBusFare.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF065F46)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Round trip: ₹${result.roundTripBusFare.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF065F46)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Daily save: ₹${result.dailySavings.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF065F46)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = result.assumptionText,
                            fontSize = 10.sp,
                            color = Color(0xFF047857),
                            lineHeight = 14.sp
                        )
                    }
                }
            } else {
                // Refusal when fare is unknown or blank
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                        .testTag("calc_refusal_notice"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cannot compute savings: Valid bus fare and alternative cost are required.",
                        fontSize = 11.sp,
                        color = Color(0xFF92400E)
                    )
                }
            }
        }
    }
}
