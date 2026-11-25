package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.ui.theme.SFMonoFontFamily
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.components.CategoryHelper
import com.example.ui.components.rememberAllFitColumnWidth
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpressiveButtonShape
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.ExpressiveChipShape
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CategoryMonthlySummary(
    val category: String,
    val totalAmount: Double,
    val count: Int,
    val percentageOfTotal: Float,
    val transactions: List<TransactionEntity>
)

data class WeeklySummary(
    val weekLabel: String,
    val dateRange: String,
    val income: Double,
    val expense: Double,
    val netSavings: Double
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthlyReportView(
    allTransactions: List<TransactionEntity>,
    onEditTransaction: (TransactionEntity) -> Unit = {},
    onDeleteTransaction: (TransactionEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    val monthTitleFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    val dayFormat = remember { SimpleDateFormat("dd MMM", Locale.US) }
    val fullDateFormat = remember { SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.US) }

    // Month navigation state
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }

    val reportCalendar = remember(monthOffset) {
        Calendar.getInstance().apply {
            add(Calendar.MONTH, monthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val monthStartMillis = reportCalendar.timeInMillis
    val maxDayInMonth = reportCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthEndMillis = remember(reportCalendar) {
        val endCal = reportCalendar.clone() as Calendar
        endCal.set(Calendar.DAY_OF_MONTH, maxDayInMonth)
        endCal.set(Calendar.HOUR_OF_DAY, 23)
        endCal.set(Calendar.MINUTE, 59)
        endCal.set(Calendar.SECOND, 59)
        endCal.set(Calendar.MILLISECOND, 999)
        endCal.timeInMillis
    }

    // Filter transactions for this month
    val monthTransactions = remember(allTransactions, monthStartMillis, monthEndMillis) {
        allTransactions.filter { it.date in monthStartMillis..monthEndMillis }
    }

    val incomeTransactions = remember(monthTransactions) {
        monthTransactions.filter { it.type.equals("Income", ignoreCase = true) }
    }
    val expenseTransactions = remember(monthTransactions) {
        monthTransactions.filter { it.type.equals("Expense", ignoreCase = true) }
    }

    val totalIncome = remember(incomeTransactions) { incomeTransactions.sumOf { it.amount } }
    val totalExpense = remember(expenseTransactions) { expenseTransactions.sumOf { it.amount } }
    val totalSavings = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) ((totalSavings / totalIncome) * 100) else if (totalExpense > 0) -100.0 else 0.0

    // Group expenses by category
    val expenseCategoriesSummary = remember(expenseTransactions, totalExpense) {
        val grouped = expenseTransactions.groupBy { it.category }
        grouped.map { (cat, txs) ->
            val sum = txs.sumOf { it.amount }
            val pct = if (totalExpense > 0) (sum / totalExpense).toFloat() else 0f
            CategoryMonthlySummary(
                category = cat,
                totalAmount = sum,
                count = txs.size,
                percentageOfTotal = pct,
                transactions = txs.sortedByDescending { it.date }
            )
        }.sortedByDescending { it.totalAmount }
    }

    // Group income by category
    val incomeCategoriesSummary = remember(incomeTransactions, totalIncome) {
        val grouped = incomeTransactions.groupBy { it.category }
        grouped.map { (cat, txs) ->
            val sum = txs.sumOf { it.amount }
            val pct = if (totalIncome > 0) (sum / totalIncome).toFloat() else 0f
            CategoryMonthlySummary(
                category = cat,
                totalAmount = sum,
                count = txs.size,
                percentageOfTotal = pct,
                transactions = txs.sortedByDescending { it.date }
            )
        }.sortedByDescending { it.totalAmount }
    }

    // Weekly breakdowns
    val weeklySummaries = remember(monthTransactions, reportCalendar, maxDayInMonth) {
        val weeks = mutableListOf<WeeklySummary>()
        val weekRanges = listOf(
            Pair(1, minOf(7, maxDayInMonth)),
            Pair(8, minOf(14, maxDayInMonth)),
            Pair(15, minOf(21, maxDayInMonth)),
            Pair(22, maxDayInMonth)
        )
        weekRanges.forEachIndexed { idx, (startDay, endDay) ->
            val wCalStart = reportCalendar.clone() as Calendar
            wCalStart.set(Calendar.DAY_OF_MONTH, startDay)
            wCalStart.set(Calendar.HOUR_OF_DAY, 0)
            wCalStart.set(Calendar.MINUTE, 0)
            wCalStart.set(Calendar.SECOND, 0)

            val wCalEnd = reportCalendar.clone() as Calendar
            wCalEnd.set(Calendar.DAY_OF_MONTH, endDay)
            wCalEnd.set(Calendar.HOUR_OF_DAY, 23)
            wCalEnd.set(Calendar.MINUTE, 59)
            wCalEnd.set(Calendar.SECOND, 59)

            val wTxs = monthTransactions.filter { it.date in wCalStart.timeInMillis..wCalEnd.timeInMillis }
            val wInc = wTxs.filter { it.type.equals("Income", ignoreCase = true) }.sumOf { it.amount }
            val wExp = wTxs.filter { it.type.equals("Expense", ignoreCase = true) }.sumOf { it.amount }
            weeks.add(
                WeeklySummary(
                    weekLabel = "Week ${idx + 1}",
                    dateRange = "$startDay - $endDay ${SimpleDateFormat("MMM", Locale.US).format(reportCalendar.time)}",
                    income = wInc,
                    expense = wExp,
                    netSavings = wInc - wExp
                )
            )
        }
        weeks
    }

    // Key Highlights
    val largestExpense = remember(expenseTransactions) {
        expenseTransactions.maxByOrNull { it.amount }
    }
    val highestSpendDay = remember(expenseTransactions) {
        val dayGrouped = expenseTransactions.groupBy {
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            cal.get(Calendar.DAY_OF_MONTH)
        }
        dayGrouped.maxByOrNull { it.value.sumOf { tx -> tx.amount } }?.let { (day, txs) ->
            Pair(day, txs.sumOf { it.amount })
        }
    }
    val avgDailySpend = remember(totalExpense, maxDayInMonth) {
        if (maxDayInMonth > 0) totalExpense / maxDayInMonth else 0.0
    }

    var expandedCategory by remember { mutableStateOf<String?>(null) }
    var reportTab by remember { mutableStateOf("Expenses") } // "Expenses" or "Income"

    val monthLabel = monthTitleFormat.format(reportCalendar.time)

    fun generateReportText(): String {
        val sb = StringBuilder()
        sb.appendLine("📊 Monthly Financial Report: $monthLabel")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("💰 Total Income: ₹${numberFormat.format(totalIncome)}")
        sb.appendLine("💳 Total Expenses: ₹${numberFormat.format(totalExpense)}")
        sb.appendLine("${if (totalSavings >= 0) "🟢" else "🔴"} Total Savings: ₹${numberFormat.format(totalSavings)} (${String.format(Locale.US, "%.1f", savingsRate)}%)")
        sb.appendLine("📅 Daily Average: ₹${numberFormat.format(avgDailySpend)}/day")
        sb.appendLine()
        sb.appendLine("🔻 Top Expense Categories:")
        expenseCategoriesSummary.take(5).forEachIndexed { i, cat ->
            sb.appendLine("${i + 1}. ${cat.category}: ₹${numberFormat.format(cat.totalAmount)} (${String.format(Locale.US, "%.1f", cat.percentageOfTotal * 100)}%)")
        }
        if (incomeCategoriesSummary.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("🔺 Income Breakdown:")
            incomeCategoriesSummary.forEachIndexed { i, cat ->
                sb.appendLine("${i + 1}. ${cat.category}: ₹${numberFormat.format(cat.totalAmount)}")
            }
        }
        sb.appendLine()
        sb.appendLine("Generated by Money Manager")
        return sb.toString()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("monthly_report_view")
    ) {
        // Month Selector Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { monthOffset-- },
                    modifier = Modifier.size(40.dp).testTag("prev_month_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { monthOffset = 0 }
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = monthLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (monthOffset != 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = ExpressiveChipShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Today",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { monthOffset++ },
                    modifier = Modifier.size(40.dp).testTag("next_month_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 190.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Savings Summary Card
            item(key = "hero_summary_card") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("monthly_report_hero_card"),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (totalSavings >= 0) IncomeGreen.copy(alpha = 0.3f) else ExpenseRed.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(end = 8.dp)) {
                                Text(
                                    text = "Total Monthly Savings",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${if (totalSavings >= 0) "+" else ""}₹${numberFormat.format(totalSavings)}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = SFMonoFontFamily,
                                    color = if (totalSavings >= 0) IncomeGreen else ExpenseRed,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Surface(
                                shape = ExpressiveChipShape,
                                color = if (totalSavings >= 0) IncomeGreen.copy(alpha = 0.16f) else ExpenseRed.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, if (totalSavings >= 0) IncomeGreen.copy(alpha = 0.4f) else ExpenseRed.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (totalSavings >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = if (totalSavings >= 0) IncomeGreen else ExpenseRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", savingsRate)}% Saved",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SFMonoFontFamily,
                                        color = if (totalSavings >= 0) IncomeGreen else ExpenseRed,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Income vs Expense Mini Stats
                        val miniIncStr = "+₹${numberFormat.format(totalIncome)}"
                        val miniExpStr = "-₹${numberFormat.format(totalExpense)}"
                        val miniAvgStr = "₹${numberFormat.format(avgDailySpend)}"
                        val miniStatStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = SFMonoFontFamily
                        )

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val colWidth = (maxWidth - 12.dp) / 3
                            val allThreeFit = rememberAllFitColumnWidth(
                                textsAndStyles = listOf(
                                    miniIncStr to miniStatStyle,
                                    miniExpStr to miniStatStyle,
                                    miniAvgStr to miniStatStyle
                                ),
                                columnWidth = colWidth,
                                horizontalPaddingPerColumn = 4.dp
                            )

                            if (allThreeFit) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Total Income",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = miniIncStr,
                                            style = miniStatStyle,
                                            color = IncomeGreen,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Text(
                                            text = "${incomeTransactions.size} credits",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Total Expenses",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = miniExpStr,
                                            style = miniStatStyle,
                                            color = ExpenseRed,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Text(
                                            text = "${expenseTransactions.size} debits",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Avg Daily Spend",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.End
                                        )
                                        Text(
                                            text = miniAvgStr,
                                            style = miniStatStyle,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.End,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Text(
                                            text = "$maxDayInMonth days",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column {
                                        Text("Total Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = miniIncStr, style = miniStatStyle, color = IncomeGreen, maxLines = 1, softWrap = false)
                                        Text("${incomeTransactions.size} credits", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column {
                                        Text("Total Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = miniExpStr, style = miniStatStyle, color = ExpenseRed, maxLines = 1, softWrap = false)
                                        Text("${expenseTransactions.size} debits", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Column {
                                        Text("Avg Daily Spend", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = miniAvgStr, style = miniStatStyle, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, softWrap = false)
                                        Text("$maxDayInMonth days", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Visual Expense vs Savings Ratio Bar
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Cash Flow Allocation",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (totalIncome > 0) "${(totalExpense / totalIncome * 100).toInt()}% Spent • ${savingsRate.toInt()}% Saved" else "100% Spent",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { if (totalIncome > 0) (totalExpense / totalIncome).toFloat().coerceIn(0f, 1f) else 1f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ExpenseRed,
                                trackColor = IncomeGreen.copy(alpha = 0.45f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Share / Copy Report Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val text = generateReportText()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Monthly Financial Report", text))
                                    Toast.makeText(context, "Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                shape = ExpressiveButtonShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Summary", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    val text = generateReportText()
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, text)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Monthly Report"))
                                },
                                shape = ExpressiveButtonShape,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share Report", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Key Monthly Highlights Card
            item(key = "highlights_card") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Monthly Highlights & Milestones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val topCatAmtStr = expenseCategoriesSummary.firstOrNull()?.let { "₹${numberFormat.format(it.totalAmount)}" } ?: "₹0"
                        val peakDayAmtStr = highestSpendDay?.let { "₹${numberFormat.format(it.second)}" } ?: "₹0"
                        val highlightAmtStyle = TextStyle(
                            fontSize = 11.sp,
                            fontFamily = SFMonoFontFamily,
                            fontWeight = FontWeight.SemiBold
                        )

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val halfWidth = (maxWidth - 10.dp) / 2
                            val bothHighlightsFit = rememberAllFitColumnWidth(
                                textsAndStyles = listOf(
                                    topCatAmtStr to highlightAmtStyle,
                                    peakDayAmtStr to highlightAmtStyle
                                ),
                                columnWidth = halfWidth,
                                horizontalPaddingPerColumn = 20.dp
                            )

                            val topCatHighlight: @Composable (Modifier) -> Unit = { mod ->
                                Surface(
                                    modifier = mod,
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Top Expense Category", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = expenseCategoriesSummary.firstOrNull()?.category ?: "None",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = topCatAmtStr,
                                            style = highlightAmtStyle,
                                            color = ExpenseRed,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            val peakDayHighlight: @Composable (Modifier) -> Unit = { mod ->
                                Surface(
                                    modifier = mod,
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Peak Spending Day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = highestSpendDay?.let { "Day ${it.first} of $monthLabel" } ?: "None",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = peakDayAmtStr,
                                            style = highlightAmtStyle,
                                            color = ExpenseRed,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            if (bothHighlightsFit) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    topCatHighlight(Modifier.weight(1f))
                                    peakDayHighlight(Modifier.weight(1f))
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    topCatHighlight(Modifier.fillMaxWidth())
                                    peakDayHighlight(Modifier.fillMaxWidth())
                                }
                            }
                        }

                        if (largestExpense != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                val largestAmtStr = "-₹${numberFormat.format(largestExpense.amount)}"
                                val largestAmtStyle = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SFMonoFontFamily
                                )
                                BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                    val fitsInline = rememberAllFitColumnWidth(
                                        textsAndStyles = listOf(largestAmtStr to largestAmtStyle),
                                        columnWidth = (maxWidth - 130.dp).coerceAtLeast(0.dp),
                                        horizontalPaddingPerColumn = 0.dp
                                    )
                                    val largestDetails: @Composable (Modifier) -> Unit = { mod ->
                                        Column(modifier = mod) {
                                            Text("Single Largest Transaction", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = if (largestExpense.memo.isNotBlank()) "${largestExpense.category} • ${largestExpense.memo}" else largestExpense.category,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = dayFormat.format(Date(largestExpense.date)) + " via " + largestExpense.wallet,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (fitsInline) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            largestDetails(Modifier.weight(1f))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = largestAmtStr,
                                                style = largestAmtStyle,
                                                color = ExpenseRed,
                                                textAlign = TextAlign.End,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            largestDetails(Modifier.fillMaxWidth())
                                            Text(
                                                text = largestAmtStr,
                                                style = largestAmtStyle,
                                                color = ExpenseRed,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Weekly Savings Trend
            item(key = "weekly_savings_breakdown") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Weekly Cash Flow & Savings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        weeklySummaries.forEach { week ->
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.padding(end = 8.dp)) {
                                    Text(text = week.weekLabel, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = week.dateRange, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                FlowRow(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "+₹${numberFormat.format(week.income)}",
                                            fontSize = 11.sp,
                                            color = IncomeGreen,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = SFMonoFontFamily,
                                            textAlign = TextAlign.End,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Text(
                                            text = "-₹${numberFormat.format(week.expense)}",
                                            fontSize = 11.sp,
                                            color = ExpenseRed,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = SFMonoFontFamily,
                                            textAlign = TextAlign.End,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    Surface(
                                        shape = ExpressiveChipShape,
                                        color = if (week.netSavings >= 0) IncomeGreen.copy(alpha = 0.12f) else ExpenseRed.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "${if (week.netSavings >= 0) "+" else ""}₹${numberFormat.format(week.netSavings)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = SFMonoFontFamily,
                                            color = if (week.netSavings >= 0) IncomeGreen else ExpenseRed,
                                            textAlign = TextAlign.End,
                                            maxLines = 1,
                                            softWrap = false,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Switcher Tab: Expenses vs Income Categories
            item(key = "breakdown_toggle") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = reportTab == "Expenses",
                        onClick = { reportTab = "Expenses" },
                        label = { Text("Expense Categories (${expenseCategoriesSummary.size})", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExpenseRed.copy(alpha = 0.18f),
                            selectedLabelColor = ExpenseRed
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = reportTab == "Income",
                        onClick = { reportTab = "Income" },
                        label = { Text("Income Categories (${incomeCategoriesSummary.size})", fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IncomeGreen.copy(alpha = 0.18f),
                            selectedLabelColor = IncomeGreen
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Summary List based on active tab
            val currentList = if (reportTab == "Expenses") expenseCategoriesSummary else incomeCategoriesSummary
            val isExpense = reportTab == "Expenses"

            if (currentList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No ${reportTab.lowercase()} recorded in $monthLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(currentList, key = { it.category }) { catSummary ->
                    val isExpanded = expandedCategory == catSummary.category
                    val catColor = CategoryHelper.getCategoryColor(catSummary.category)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedCategory = if (isExpanded) null else catSummary.category }
                            .testTag("report_category_${catSummary.category}"),
                        shape = ExpressiveCardShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            val catAmtStr = "${if (isExpense) "-₹" else "+₹"}${numberFormat.format(catSummary.totalAmount)}"
                            val catAmtStyle = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = SFMonoFontFamily
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(catColor.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = CategoryHelper.getCategoryIcon(catSummary.category),
                                        contentDescription = catSummary.category,
                                        tint = catColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))

                                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                                    val fitsInline = rememberAllFitColumnWidth(
                                        textsAndStyles = listOf(catAmtStr to catAmtStyle),
                                        columnWidth = (maxWidth - 140.dp).coerceAtLeast(0.dp),
                                        horizontalPaddingPerColumn = 0.dp
                                    )
                                    val catTitleCol: @Composable (Modifier) -> Unit = { mod ->
                                        Column(modifier = mod) {
                                            Text(
                                                text = catSummary.category,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${catSummary.count} transaction${if (catSummary.count > 1) "s" else ""} • ${String.format(Locale.US, "%.1f", catSummary.percentageOfTotal * 100)}% of total",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (fitsInline) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            catTitleCol(Modifier.weight(1f))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = catAmtStr,
                                                    style = catAmtStyle,
                                                    color = if (isExpense) ExpenseRed else IncomeGreen,
                                                    textAlign = TextAlign.End,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                catTitleCol(Modifier.weight(1f))
                                                Icon(
                                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = catAmtStr,
                                                style = catAmtStyle,
                                                color = if (isExpense) ExpenseRed else IncomeGreen,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Proportional progress bar for this category
                            LinearProgressIndicator(
                                progress = { catSummary.percentageOfTotal.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = catColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            // Expanded Transactions List
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Transactions in $monthLabel",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    catSummary.transactions.forEach { tx ->
                                        val txAmtStr = "${if (tx.type.equals("Expense", true)) "-₹" else "+₹"}${numberFormat.format(tx.amount)}"
                                        val txAmtStyle = TextStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            fontFamily = SFMonoFontFamily
                                        )
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onEditTransaction(tx) },
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        ) {
                                            BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                                val txFitsInline = rememberAllFitColumnWidth(
                                                    textsAndStyles = listOf(txAmtStr to txAmtStyle),
                                                    columnWidth = (maxWidth - 150.dp).coerceAtLeast(0.dp),
                                                    horizontalPaddingPerColumn = 0.dp
                                                )
                                                val txInfoCol: @Composable (Modifier) -> Unit = { mod ->
                                                    Column(modifier = mod) {
                                                        Text(
                                                            text = if (tx.memo.isNotBlank()) tx.memo else tx.category,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 12.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = "${fullDateFormat.format(Date(tx.date))} • ${tx.wallet}",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                if (txFitsInline) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        txInfoCol(Modifier.weight(1f))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = txAmtStr,
                                                                style = txAmtStyle,
                                                                color = if (tx.type.equals("Expense", true)) ExpenseRed else IncomeGreen,
                                                                textAlign = TextAlign.End,
                                                                maxLines = 1,
                                                                softWrap = false
                                                            )
                                                            IconButton(
                                                                onClick = { onDeleteTransaction(tx) },
                                                                modifier = Modifier.size(28.dp).padding(start = 4.dp)
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.DeleteOutline,
                                                                    contentDescription = "Delete",
                                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                                    modifier = Modifier.size(15.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    Column(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        txInfoCol(Modifier.fillMaxWidth())
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = txAmtStr,
                                                                style = txAmtStyle,
                                                                color = if (tx.type.equals("Expense", true)) ExpenseRed else IncomeGreen,
                                                                maxLines = 1,
                                                                softWrap = false
                                                            )
                                                            IconButton(
                                                                onClick = { onDeleteTransaction(tx) },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(
                                                                    Icons.Default.DeleteOutline,
                                                                    contentDescription = "Delete",
                                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                                    modifier = Modifier.size(15.dp)
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
                    }
                }
            }
        }
    }
}
