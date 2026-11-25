package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.ui.theme.ExpressiveButtonShape
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.SFMonoFontFamily
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IncomeGreen
import java.text.DecimalFormat

@Composable
fun SalaryScreen(
    currentAmount: Double,
    currentEmployer: String,
    currentCreditDay: Int,
    currentWallet: String,
    currentAutoCredit: Boolean,
    lastCreditedMonth: String,
    onSaveSalarySettings: (amount: Double, employer: String, day: Int, wallet: String, autoCredit: Boolean) -> Unit,
    onCreditSalaryNow: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }

    var amountInput by remember(currentAmount) {
        mutableStateOf(if (currentAmount > 0) currentAmount.toInt().toString() else "")
    }
    var employerInput by remember(currentEmployer) { mutableStateOf(currentEmployer) }
    var creditDayInput by remember(currentCreditDay) { mutableStateOf(currentCreditDay.toString()) }
    var selectedWallet by remember(currentWallet) { mutableStateOf(currentWallet) }
    var autoCreditEnabled by remember(currentAutoCredit) { mutableStateOf(currentAutoCredit) }

    val walletOptions = listOf("Bank Account", "Cash", "UPI", "Savings", "Cheque")

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val safeDrawingTop = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val effectiveTopPadding = maxOf(statusBarTop, safeDrawingTop, 36.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("salary_screen")
    ) {
        // Top App Bar with back navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = effectiveTopPadding, bottom = 8.dp, start = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "Income & Salary Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Set regular income, salary credit date & auto-posting",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Salary Overview Card
            item(key = "salary_hero_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = IncomeGreen.copy(alpha = 0.12f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(IncomeGreen.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = IncomeGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (employerInput.isNotBlank()) employerInput else "Monthly Salary",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Credit day: ${creditDayInput.ifBlank { "1" }}th of every month",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (autoCreditEnabled) IncomeGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (autoCreditEnabled) "Auto-Credit ON" else "Manual",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (autoCreditEnabled) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val displayAmt = amountInput.toDoubleOrNull() ?: currentAmount
                        Text(
                            text = "₹${numberFormat.format(displayAmt)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = SFMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick "Credit Now" Button
                        Button(
                            onClick = {
                                if (displayAmt <= 0) {
                                    Toast.makeText(context, "Please enter a valid salary amount first", Toast.LENGTH_SHORT).show()
                                } else {
                                    onSaveSalarySettings(
                                        displayAmt,
                                        employerInput.trim(),
                                        creditDayInput.toIntOrNull()?.coerceIn(1, 31) ?: 1,
                                        selectedWallet,
                                        autoCreditEnabled
                                    )
                                    onCreditSalaryNow()
                                    Toast.makeText(context, "₹${numberFormat.format(displayAmt)} credited to ledger for this month!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = ExpressiveButtonShape,
                            colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Post / Credit Salary to Ledger Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Edit Configuration Form
            item(key = "salary_form_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Salary & Credit Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configure your recurring monthly salary information",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Monthly Amount Input
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { amountInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Monthly Salary Amount") },
                            placeholder = { Text("e.g. 75000") },
                            leadingIcon = {
                                Text(
                                    "₹",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen,
                                    modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("salary_amount_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Employer / Source Name
                        OutlinedTextField(
                            value = employerInput,
                            onValueChange = { employerInput = it },
                            label = { Text("Employer / Company / Source") },
                            placeholder = { Text("e.g. Tech Corp, Freelance, Clinic") },
                            leadingIcon = {
                                Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("salary_employer_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Credit Day (1 to 31)
                        OutlinedTextField(
                            value = creditDayInput,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(2)
                                val num = clean.toIntOrNull()
                                if (clean.isEmpty() || (num != null && num in 1..31)) {
                                    creditDayInput = clean
                                }
                            },
                            label = { Text("Salary Credit Date (Day of Month: 1 - 31)") },
                            placeholder = { Text("e.g. 1 or 25 or 30") },
                            leadingIcon = {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            },
                            supportingText = {
                                val d = creditDayInput.toIntOrNull() ?: 1
                                Text("Salary will be scheduled on the ${d}${getDaySuffix(d)} of each month")
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("salary_day_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Credit Day Quick Selector Pills
                        Text(
                            text = "Quick Select Credit Day:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1, 5, 7, 10, 15, 20, 25, 28, 30, 31).forEach { day ->
                                val isSelected = creditDayInput == day.toString()
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { creditDayInput = day.toString() },
                                    label = { Text("${day}${getDaySuffix(day)}", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Payment Wallet / Receiving Account
                        Text(
                            text = "Receiving Wallet / Account:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            walletOptions.forEach { w ->
                                val isSelected = selectedWallet == w
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedWallet = w },
                                    leadingIcon = {
                                        if (w == "Cash") {
                                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                                        } else {
                                            Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(14.dp))
                                        }
                                    },
                                    label = { Text(w, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Auto-credit Toggle Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Credit on Salary Day",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Automatically logs income transaction when the date arrives without manual entry",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoCreditEnabled,
                                onCheckedChange = { autoCreditEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = IncomeGreen
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Save Settings Button
                        Button(
                            onClick = {
                                val parsedAmt = amountInput.toDoubleOrNull() ?: 0.0
                                val parsedDay = creditDayInput.toIntOrNull()?.coerceIn(1, 31) ?: 1
                                onSaveSalarySettings(
                                    parsedAmt,
                                    employerInput.trim(),
                                    parsedDay,
                                    selectedWallet,
                                    autoCreditEnabled
                                )
                                Toast.makeText(context, "Salary settings saved successfully!", Toast.LENGTH_SHORT).show()
                                onBack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_save_salary"),
                            shape = ExpressiveButtonShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Salary Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun getDaySuffix(day: Int): String {
    if (day in 11..13) return "th"
    return when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}
