package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(
    allTransactions: List<TransactionEntity>,
    onAddTransactionForDate: (Long) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var calendarMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    val dayFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.US) }
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }

    // Map epoch day key "yyyy-MM-dd" -> List<TransactionEntity>
    val dayKeyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val transactionsByDayKey = remember(allTransactions) {
        allTransactions.groupBy { dayKeyFormat.format(Date(it.date)) }
    }

    val selectedDayKey = dayKeyFormat.format(selectedDate.time)
    val selectedDayTransactions = transactionsByDayKey[selectedDayKey] ?: emptyList()
    val selectedDayExpense = selectedDayTransactions.filter { it.type.equals("Expense", ignoreCase = true) }.sumOf { it.amount }
    val selectedDayIncome = selectedDayTransactions.filter { it.type.equals("Income", ignoreCase = true) }.sumOf { it.amount }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val safeDrawingTop = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val effectiveTopPadding = maxOf(statusBarTop, safeDrawingTop, 36.dp)

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen")
    ) {
        // Month Navigation Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = effectiveTopPadding, bottom = 8.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newCal = (calendarMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                    calendarMonth = newCal
                },
                modifier = Modifier.testTag("calendar_prev_month")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = monthYearFormat.format(calendarMonth.time),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        val today = Calendar.getInstance()
                        calendarMonth = today
                        selectedDate = today
                    },
                    modifier = Modifier.size(28.dp).testTag("calendar_today_button")
                ) {
                    Icon(Icons.Default.Today, contentDescription = "Today", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }

            IconButton(
                onClick = {
                    val newCal = (calendarMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                    calendarMonth = newCal
                },
                modifier = Modifier.testTag("calendar_next_month")
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
            }
        }

        // Calendar Grid Card with Liquid Glass
        com.example.ui.components.LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("calendar_grid_card"),
            shape = RoundedCornerShape(22.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            blurRadius = 20.dp,
            lensCurvature = 0.42f,
            specularHighlight = 0.80f,
            chromaticDispersion = 0.28f,
            elevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Day of Week Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Build days matrix for the month
                val daysMatrix = remember(calendarMonth) {
                    val cal = (calendarMonth.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sunday
                    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

                    val grid = mutableListOf<List<CalendarDayItem>>()
                    var currentDay = 1
                    var week = mutableListOf<CalendarDayItem>()

                    // Leading empty days
                    for (i in 0 until firstDayOfWeek) {
                        week.add(CalendarDayItem(dayNumber = 0, isCurrentMonth = false, dateKey = ""))
                    }

                    while (currentDay <= maxDays) {
                        val dayCal = (cal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, currentDay) }
                        val dateKey = dayKeyFormat.format(dayCal.time)
                        week.add(CalendarDayItem(dayNumber = currentDay, isCurrentMonth = true, dateKey = dateKey))

                        if (week.size == 7) {
                            grid.add(week)
                            week = mutableListOf()
                        }
                        currentDay++
                    }

                    // Trailing empty days to complete row
                    if (week.isNotEmpty()) {
                        while (week.size < 7) {
                            week.add(CalendarDayItem(dayNumber = 0, isCurrentMonth = false, dateKey = ""))
                        }
                        grid.add(week)
                    }
                    grid
                }

                // Render Calendar Rows
                daysMatrix.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        week.forEach { item ->
                            val isSelected = item.isCurrentMonth && item.dateKey == selectedDayKey
                            val dayTransactions = if (item.isCurrentMonth) transactionsByDayKey[item.dateKey] ?: emptyList() else emptyList()
                            val hasExpense = dayTransactions.any { it.type.equals("Expense", ignoreCase = true) }
                            val hasIncome = dayTransactions.any { it.type.equals("Income", ignoreCase = true) }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable(enabled = item.isCurrentMonth) {
                                        val newSel = (calendarMonth.clone() as Calendar).apply {
                                            set(Calendar.DAY_OF_MONTH, item.dayNumber)
                                        }
                                        selectedDate = newSel
                                    }
                                    .testTag(if (item.isCurrentMonth) "cal_day_${item.dayNumber}" else "cal_empty"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.isCurrentMonth) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "${item.dayNumber}",
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )

                                        // Status dots
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (hasIncome) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color.White else IncomeGreen)
                                                )
                                            }
                                            if (hasExpense) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color.White else ExpenseRed)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Day Details Header & Add Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = dayFormat.format(selectedDate.time),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Expense: -₹${numberFormat.format(selectedDayExpense)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = SFMonoFontFamily,
                        color = ExpenseRed,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Income: +₹${numberFormat.format(selectedDayIncome)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = SFMonoFontFamily,
                        color = IncomeGreen,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Button(
                onClick = { onAddTransactionForDate(selectedDate.timeInMillis) },
                shape = ExpressiveButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_for_day_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Record", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected Day Transactions List
        if (selectedDayTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No records for this day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 190.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedDayTransactions, key = { it.id }) { tx ->
                    TransactionCard(
                        transaction = tx,
                        onEdit = { onEditTransaction(tx) },
                        onDelete = { onDeleteTransaction(tx) }
                    )
                }
            }
        }
    }
}

/**
 * Calendar Picker Dialog shown when in Day view and tapping the calendar icon.
 */
@Composable
fun CalendarPickerDialog(
    selectedDate: Calendar,
    allTransactions: List<TransactionEntity>,
    onDateSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    var calendarMonth by remember { mutableStateOf((selectedDate.clone() as Calendar)) }
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    val dayKeyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    val transactionsByDayKey = remember(allTransactions) {
        allTransactions.groupBy { dayKeyFormat.format(Date(it.date)) }
    }
    val currentSelectedKey = dayKeyFormat.format(selectedDate.time)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = ExpressiveCardShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val newCal = (calendarMonth.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
                            calendarMonth = newCal
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = monthYearFormat.format(calendarMonth.time),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                val today = Calendar.getInstance()
                                calendarMonth = today
                                onDateSelected(today)
                                onDismiss()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Today, contentDescription = "Today", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }

                    IconButton(
                        onClick = {
                            val newCal = (calendarMonth.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                            calendarMonth = newCal
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Day of Week Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                val daysMatrix = remember(calendarMonth) {
                    val cal = (calendarMonth.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_MONTH, 1)
                    }
                    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
                    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

                    val grid = mutableListOf<List<CalendarDayItem>>()
                    var currentDay = 1
                    var week = mutableListOf<CalendarDayItem>()

                    for (i in 0 until firstDayOfWeek) {
                        week.add(CalendarDayItem(dayNumber = 0, isCurrentMonth = false, dateKey = ""))
                    }

                    while (currentDay <= maxDays) {
                        val dayCal = (cal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, currentDay) }
                        val dateKey = dayKeyFormat.format(dayCal.time)
                        week.add(CalendarDayItem(dayNumber = currentDay, isCurrentMonth = true, dateKey = dateKey))

                        if (week.size == 7) {
                            grid.add(week)
                            week = mutableListOf()
                        }
                        currentDay++
                    }

                    if (week.isNotEmpty()) {
                        while (week.size < 7) {
                            week.add(CalendarDayItem(dayNumber = 0, isCurrentMonth = false, dateKey = ""))
                        }
                        grid.add(week)
                    }
                    grid
                }

                daysMatrix.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        week.forEach { item ->
                            val isSelected = item.isCurrentMonth && item.dateKey == currentSelectedKey
                            val dayTransactions = if (item.isCurrentMonth) transactionsByDayKey[item.dateKey] ?: emptyList() else emptyList()
                            val hasExpense = dayTransactions.any { it.type.equals("Expense", ignoreCase = true) }
                            val hasIncome = dayTransactions.any { it.type.equals("Income", ignoreCase = true) }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable(enabled = item.isCurrentMonth) {
                                        val newSel = (calendarMonth.clone() as Calendar).apply {
                                            set(Calendar.DAY_OF_MONTH, item.dayNumber)
                                        }
                                        onDateSelected(newSel)
                                        onDismiss()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.isCurrentMonth) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = item.dayNumber.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (hasExpense || hasIncome) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                if (hasExpense) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Color.White else ExpenseRed)
                                                    )
                                                }
                                                if (hasIncome) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) Color.White else IncomeGreen)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.TextButton(onClick = onDismiss) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private data class CalendarDayItem(
    val dayNumber: Int,
    val isCurrentMonth: Boolean,
    val dateKey: String
)
