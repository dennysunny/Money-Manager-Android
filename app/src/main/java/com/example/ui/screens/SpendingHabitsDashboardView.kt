package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.ui.theme.SFMonoFontFamily
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.ExpressiveChipShape
import com.example.ui.theme.ExpressivePillButtonShape
import com.example.ui.theme.IncomeGreen
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Summary Spending Habits & Trends Dashboard
 * Deeply analyzes:
 * 1. Financial Health & Habit Score (0-100)
 * 2. 50/30/20 Rule: Needs vs Wants vs Savings Breakdown
 * 3. Weekend vs Weekday Habit & Surge Ratio
 * 4. Day-of-Week Spending Heat & Distribution
 * 5. Time-of-Day Impulse Rhythm (Morning, Afternoon, Evening, Night)
 * 6. Spending Velocity, Daily Burn Rate & Month-End Forecast
 * 7. "Latte Factor" Micro-Transactions (small frequent leaks)
 * 8. Top Outlier Spends & Largest Transactions
 * 9. Month-over-Month Category Trend Shifts
 * 10. Digital vs Cash / Credit Card Dependency Risk Index
 * 11. Actionable Smart Habit Takeaways & Badges
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpendingHabitsDashboardView(
    allTransactions: List<TransactionEntity>,
    onEditTransaction: (TransactionEntity) -> Unit = {},
    onDeleteTransaction: (TransactionEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { DecimalFormat("#,##,##0") }
    val decimalFormat = remember { DecimalFormat("#,##,##0.00") }

    // Dashboard Time Scope Filter
    var selectedScope by remember { mutableStateOf("This Month") }
    val scopeOptions = listOf("This Month", "Last 30 Days", "Quarter (3M)", "Year-to-Date", "All Time")

    // Filter transactions based on selected scope
    val now = remember { System.currentTimeMillis() }
    val cal = remember { Calendar.getInstance() }

    val currentScopeTransactions = remember(allTransactions, selectedScope) {
        val c = Calendar.getInstance()
        when (selectedScope) {
            "This Month" -> {
                c.set(Calendar.DAY_OF_MONTH, 1)
                c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
                val start = c.timeInMillis
                allTransactions.filter { it.date >= start }
            }
            "Last 30 Days" -> {
                val start = now - (30L * 24 * 60 * 60 * 1000)
                allTransactions.filter { it.date >= start }
            }
            "Quarter (3M)" -> {
                val start = now - (90L * 24 * 60 * 60 * 1000)
                allTransactions.filter { it.date >= start }
            }
            "Year-to-Date" -> {
                c.set(Calendar.DAY_OF_YEAR, 1)
                c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
                val start = c.timeInMillis
                allTransactions.filter { it.date >= start }
            }
            else -> allTransactions
        }
    }

    // Previous Scope Transactions (for MoM comparison)
    val previousScopeTransactions = remember(allTransactions, selectedScope) {
        val c = Calendar.getInstance()
        when (selectedScope) {
            "This Month" -> {
                // Previous calendar month
                c.set(Calendar.DAY_OF_MONTH, 1)
                c.add(Calendar.MONTH, -1)
                c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0); c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
                val prevStart = c.timeInMillis
                c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
                c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59); c.set(Calendar.MILLISECOND, 999)
                val prevEnd = c.timeInMillis
                allTransactions.filter { it.date in prevStart..prevEnd }
            }
            "Last 30 Days" -> {
                val prevEnd = now - (30L * 24 * 60 * 60 * 1000)
                val prevStart = now - (60L * 24 * 60 * 60 * 1000)
                allTransactions.filter { it.date in prevStart..prevEnd }
            }
            else -> emptyList()
        }
    }

    // Expense & Income Aggregates
    val expenseTxs = remember(currentScopeTransactions) {
        currentScopeTransactions.filter { it.type.equals("Expense", ignoreCase = true) }
    }
    val incomeTxs = remember(currentScopeTransactions) {
        currentScopeTransactions.filter { it.type.equals("Income", ignoreCase = true) }
    }

    val totalExpense = remember(expenseTxs) { expenseTxs.sumOf { it.amount } }
    val totalIncome = remember(incomeTxs) { incomeTxs.sumOf { it.amount } }
    val netBalance = totalIncome - totalExpense
    val savingsRate = if (totalIncome > 0) ((netBalance / totalIncome) * 100).coerceIn(0.0, 100.0) else 0.0

    // Prev period expense
    val prevExpense = remember(previousScopeTransactions) {
        previousScopeTransactions.filter { it.type.equals("Expense", ignoreCase = true) }.sumOf { it.amount }
    }
    val momExpenseDeltaPct = if (prevExpense > 0) {
        ((totalExpense - prevExpense) / prevExpense) * 100.0
    } else null

    // 1. 50/30/20 Rule: Needs vs Wants vs Savings
    val needsCategories = setOf(
        "Petrol", "Grocery Home", "Home Expenses", "Flat Expenses", "Medical",
        "Bills", "Health", "Car / Bike Expenses", "Mobile Recharge", "Transportation",
        "Baby", "School", "Maternity"
    )
    val savingsCategories = setOf("EMI", "SIP", "Investment", "Debt", "Savings")

    val needsExpense = remember(expenseTxs) {
        expenseTxs.filter { tx -> needsCategories.any { tx.category.contains(it, ignoreCase = true) } }.sumOf { it.amount }
    }
    val savingsExpense = remember(expenseTxs) {
        expenseTxs.filter { tx -> savingsCategories.any { tx.category.contains(it, ignoreCase = true) } }.sumOf { it.amount }
    }
    val wantsExpense = remember(totalExpense, needsExpense, savingsExpense) {
        (totalExpense - needsExpense - savingsExpense).coerceAtLeast(0.0)
    }

    val needsPct = if (totalExpense > 0) ((needsExpense / totalExpense) * 100).toFloat() else 0f
    val wantsPct = if (totalExpense > 0) ((wantsExpense / totalExpense) * 100).toFloat() else 0f
    val savingsPct = if (totalExpense > 0) ((savingsExpense / totalExpense) * 100).toFloat() else 0f

    // 2. Weekend vs Weekday Spending Habits
    val (weekdaySpend, weekendSpend, weekdayCount, weekendCount) = remember(expenseTxs) {
        var wDaySum = 0.0
        var wEndSum = 0.0
        val dayCal = Calendar.getInstance()
        val distinctWeekdayDates = mutableSetOf<String>()
        val distinctWeekendDates = mutableSetOf<String>()
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (tx in expenseTxs) {
            dayCal.timeInMillis = tx.date
            val dow = dayCal.get(Calendar.DAY_OF_WEEK)
            val dStr = dateFmt.format(Date(tx.date))
            if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) {
                wEndSum += tx.amount
                distinctWeekendDates.add(dStr)
            } else {
                wDaySum += tx.amount
                distinctWeekdayDates.add(dStr)
            }
        }
        val wDays = distinctWeekdayDates.size.coerceAtLeast(1)
        val wEnds = distinctWeekendDates.size.coerceAtLeast(1)
        Quad(wDaySum, wEndSum, wDays, wEnds)
    }

    val avgWeekdayDaily = weekdaySpend / weekdayCount
    val avgWeekendDaily = weekendSpend / weekendCount
    val weekendSurgeMultiplier = if (avgWeekdayDaily > 0) (avgWeekendDaily / avgWeekdayDaily) else 1.0

    // 3. Day of Week Breakdown (Mon to Sun)
    val dayOfWeekStats = remember(expenseTxs) {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val totals = DoubleArray(7) { 0.0 }
        val counts = IntArray(7) { 0 }
        val dayCal = Calendar.getInstance()

        for (tx in expenseTxs) {
            dayCal.timeInMillis = tx.date
            val dow = dayCal.get(Calendar.DAY_OF_WEEK)
            // Convert Calendar DOW (Sun=1, Mon=2..Sat=7) to index 0=Mon..6=Sun
            val idx = when (dow) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            totals[idx] += tx.amount
            counts[idx]++
        }
        val maxTotal = totals.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
        days.mapIndexed { i, label ->
            DayOfWeekStat(
                day = label,
                amount = totals[i],
                count = counts[i],
                ratio = (totals[i] / maxTotal).toFloat()
            )
        }
    }
    val peakDayStat = remember(dayOfWeekStats) { dayOfWeekStats.maxByOrNull { it.amount } }

    // 4. Time of Day Rhythm (Morning, Afternoon, Evening, Night)
    val timeOfDayStats = remember(expenseTxs) {
        var morning = 0.0
        var afternoon = 0.0
        var evening = 0.0
        var night = 0.0
        val dayCal = Calendar.getInstance()

        for (tx in expenseTxs) {
            dayCal.timeInMillis = tx.date
            val hour = dayCal.get(Calendar.HOUR_OF_DAY)
            when (hour) {
                in 6..11 -> morning += tx.amount
                in 12..16 -> afternoon += tx.amount
                in 17..20 -> evening += tx.amount
                else -> night += tx.amount
            }
        }
        val total = (morning + afternoon + evening + night).coerceAtLeast(1.0)
        listOf(
            TimeWindowStat("Morning", "6 AM - 12 PM", morning, (morning / total).toFloat(), Icons.Default.WbSunny, Color(0xFFF59E0B)),
            TimeWindowStat("Afternoon", "12 PM - 5 PM", afternoon, (afternoon / total).toFloat(), Icons.Default.AccessTime, Color(0xFF3B82F6)),
            TimeWindowStat("Evening", "5 PM - 9 PM", evening, (evening / total).toFloat(), Icons.Default.Weekend, Color(0xFF8B5CF6)),
            TimeWindowStat("Night", "9 PM - 6 AM", night, (night / total).toFloat(), Icons.Default.NightlightRound, Color(0xFFEC4899))
        )
    }
    val peakTimeWindow = remember(timeOfDayStats) { timeOfDayStats.maxByOrNull { it.amount } }

    // 5. Velocity, Burn Rate & No-Spend Days
    val (dailyBurnRate, elapsedDays, noSpendDaysCount, maxConsecutiveNoSpend) = remember(expenseTxs, selectedScope) {
        val dayCal = Calendar.getInstance()
        val daysWithSpend = mutableSetOf<String>()
        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (tx in expenseTxs) {
            daysWithSpend.add(dateFmt.format(Date(tx.date)))
        }

        val totalDaysInScope = when (selectedScope) {
            "This Month" -> dayCal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
            "Last 30 Days" -> 30
            "Quarter (3M)" -> 90
            "Year-to-Date" -> dayCal.get(Calendar.DAY_OF_YEAR).coerceAtLeast(1)
            else -> 30
        }

        val burnRate = totalExpense / totalDaysInScope.coerceAtLeast(1)
        val noSpend = (totalDaysInScope - daysWithSpend.size).coerceAtLeast(0)
        Quad(burnRate, totalDaysInScope, noSpend, (noSpend / 2).coerceAtLeast(1))
    }

    val daysInCurrentMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
    val projectedMonthEndSpend = dailyBurnRate * daysInCurrentMonth

    // 6. "Latte Factor" Micro-Transactions (small frequent spends <= ₹300)
    val microTxs = remember(expenseTxs) { expenseTxs.filter { it.amount <= 300.0 } }
    val microSpendTotal = remember(microTxs) { microTxs.sumOf { it.amount } }
    val microSpendCount = microTxs.size
    val microSpendRatio = if (totalExpense > 0) ((microSpendTotal / totalExpense) * 100).toFloat() else 0f

    // 7. Top Outlier Spends (Largest single transactions)
    val topOutliers = remember(expenseTxs) {
        expenseTxs.sortedByDescending { it.amount }.take(4)
    }

    // 8. Payment Mode Dependency & Credit Card Risk Ratio
    val (cashTotal, upiTotal, cardTotal) = remember(expenseTxs) {
        var cash = 0.0
        var upi = 0.0
        var card = 0.0
        for (tx in expenseTxs) {
            val w = tx.wallet.lowercase()
            when {
                w.contains("card") || w.contains("credit") -> card += tx.amount
                w.contains("upi") || w.contains("gpay") || w.contains("phonepe") || w.contains("bank") -> upi += tx.amount
                else -> cash += tx.amount
            }
        }
        Triple(cash, upi, card)
    }
    val cardDependencyPct = if (totalExpense > 0) ((cardTotal / totalExpense) * 100).toFloat() else 0f

    // 9. Financial Habit & Health Score (0–100)
    val habitScore = remember(savingsRate, wantsPct, microSpendRatio, cardDependencyPct, totalExpense) {
        if (totalExpense <= 0) return@remember 90
        var score = 70.0

        // Savings component (+15 if >20%, -10 if 0%)
        if (savingsRate >= 25) score += 15
        else if (savingsRate >= 15) score += 10
        else if (savingsRate >= 5) score += 5
        else score -= 10

        // Wants discipline (ideal <=30%)
        if (wantsPct <= 30) score += 10
        else if (wantsPct <= 45) score += 0
        else score -= 10

        // Micro-spend control (ideal <=15%)
        if (microSpendRatio <= 15) score += 5
        else score -= 5

        // Card dependency (ideal <=50%)
        if (cardDependencyPct <= 40) score += 5
        else if (cardDependencyPct > 70) score -= 8

        score.coerceIn(20.0, 98.0).toInt()
    }

    val habitScoreLabel = when {
        habitScore >= 85 -> "Excellent Discipline"
        habitScore >= 70 -> "Healthy Habits"
        habitScore >= 55 -> "Moderate Balance"
        else -> "High Burn Rate"
    }

    val habitScoreColor = when {
        habitScore >= 85 -> IncomeGreen
        habitScore >= 70 -> Color(0xFF38BDF8)
        habitScore >= 55 -> Color(0xFFF59E0B)
        else -> ExpenseRed
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("spending_habits_dashboard_view"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 190.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Scope Filter Header
        item(key = "scope_filter") {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    scopeOptions.forEach { scope ->
                        val isSelected = scope == selectedScope
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedScope = scope },
                            label = { Text(scope, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            shape = ExpressiveChipShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("scope_chip_${scope.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }
        }

        // HERO CARD: Financial Habit & Health Score with Liquid Glass
        item(key = "habit_score_hero") {
            com.example.ui.components.LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_habit_health_score"),
                shape = ExpressiveCardShape,
                tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.60f),
                blurRadius = 22.dp,
                lensCurvature = 0.45f,
                specularHighlight = 0.85f,
                chromaticDispersion = 0.32f,
                elevation = 12.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = habitScoreColor.copy(alpha = 0.18f),
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Psychology,
                                            contentDescription = null,
                                            tint = habitScoreColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Spending Habit Score",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = habitScoreLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = habitScoreColor
                            )
                        }

                        // Circular Score Badge
                        Box(
                            modifier = Modifier.size(76.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(70.dp)) {
                                val stroke = 7.dp.toPx()
                                val arcSize = size.width - stroke
                                drawArc(
                                    color = Color.White.copy(alpha = 0.12f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = Offset(stroke / 2, stroke / 2),
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(stroke, cap = StrokeCap.Round)
                                )
                                drawArc(
                                    color = habitScoreColor,
                                    startAngle = -90f,
                                    sweepAngle = (habitScore / 100f) * 360f,
                                    useCenter = false,
                                    topLeft = Offset(stroke / 2, stroke / 2),
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(stroke, cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$habitScore",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = habitScoreColor
                                )
                                Text(
                                    text = "/100",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Mini Metric Pills: Savings, Needs, Micro-Spend, Burn
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreSubPill(
                            label = "Savings Rate",
                            value = "${savingsRate.toInt()}%",
                            color = if (savingsRate >= 20) IncomeGreen else Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        ScoreSubPill(
                            label = "Needs Ratio",
                            value = "${needsPct.toInt()}%",
                            color = if (needsPct <= 50) Color(0xFF38BDF8) else ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                        ScoreSubPill(
                            label = "Micro-Spends",
                            value = "${microSpendRatio.toInt()}%",
                            color = if (microSpendRatio <= 15) IncomeGreen else Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        ScoreSubPill(
                            label = "Zero-Spend",
                            value = "${noSpendDaysCount}d",
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. THE 50/30/20 RULE: Needs vs Wants vs Savings with Liquid Glass
        item(key = "card_50_30_20") {
            com.example.ui.components.LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_50_30_20_analysis"),
                shape = ExpressiveCardShape,
                tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
                blurRadius = 16.dp,
                lensCurvature = 0.38f,
                specularHighlight = 0.72f,
                chromaticDispersion = 0.24f,
                borderAlpha = 0.40f,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "50/30/20 Rule Analysis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = ExpressiveChipShape,
                            color = if (wantsPct <= 35) IncomeGreen.copy(alpha = 0.18f) else ExpenseRed.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = if (wantsPct <= 35) "Disciplined" else "Wants Heavy",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (wantsPct <= 35) IncomeGreen else ExpenseRed,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Multi-Segment Colored Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            if (needsPct > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(needsPct.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(Color(0xFF38BDF8))
                                )
                            }
                            if (wantsPct > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(wantsPct.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(Color(0xFFF97316))
                                )
                            }
                            if (savingsPct > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(savingsPct.coerceAtLeast(0.01f))
                                        .fillMaxHeight()
                                        .background(IncomeGreen)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3-Column details
                    val needsAmtStr = "₹${numberFormat.format(needsExpense)}"
                    val wantsAmtStr = "₹${numberFormat.format(wantsExpense)}"
                    val savingsAmtStr = "₹${numberFormat.format(savingsExpense)}"
                    val ruleAmtStyle = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SFMonoFontFamily
                    )

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val thirdWidth = maxWidth / 3
                        val allThreeRuleFit = rememberAllFitColumnWidth(
                            textsAndStyles = listOf(
                                needsAmtStr to ruleAmtStyle,
                                wantsAmtStr to ruleAmtStyle,
                                savingsAmtStr to ruleAmtStyle
                            ),
                            columnWidth = thirdWidth,
                            horizontalPaddingPerColumn = 6.dp
                        )

                        if (allThreeRuleFit) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                BudgetRuleColumn(
                                    title = "Needs",
                                    target = "Target ≤50%",
                                    amount = needsAmtStr,
                                    pct = "${needsPct.toInt()}%",
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.weight(1f)
                                )
                                BudgetRuleColumn(
                                    title = "Wants",
                                    target = "Target ≤30%",
                                    amount = wantsAmtStr,
                                    pct = "${wantsPct.toInt()}%",
                                    color = Color(0xFFF97316),
                                    modifier = Modifier.weight(1f)
                                )
                                BudgetRuleColumn(
                                    title = "Savings / Debt",
                                    target = "Target ≥20%",
                                    amount = savingsAmtStr,
                                    pct = "${savingsPct.toInt()}%",
                                    color = IncomeGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BudgetRuleColumn(
                                    title = "Needs",
                                    target = "Target ≤50%",
                                    amount = needsAmtStr,
                                    pct = "${needsPct.toInt()}%",
                                    color = Color(0xFF38BDF8)
                                )
                                BudgetRuleColumn(
                                    title = "Wants",
                                    target = "Target ≤30%",
                                    amount = wantsAmtStr,
                                    pct = "${wantsPct.toInt()}%",
                                    color = Color(0xFFF97316)
                                )
                                BudgetRuleColumn(
                                    title = "Savings / Debt",
                                    target = "Target ≥20%",
                                    amount = savingsAmtStr,
                                    pct = "${savingsPct.toInt()}%",
                                    color = IncomeGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (wantsPct > 35) {
                            "💡 Tip: Discretionary 'Wants' account for ${wantsPct.toInt()}% of your budget. Trimming non-essential dining and shopping by 15% would free up ₹${numberFormat.format(wantsExpense * 0.15)} for savings."
                        } else {
                            "✨ Great job! Your essential needs are well-controlled at ${needsPct.toInt()}% and discretionary wants are strictly inside the recommended 30% boundary."
                        },
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. WEEKEND VS. WEEKDAY HABIT RHYTHM with Liquid Glass
        item(key = "card_weekend_weekday") {
            com.example.ui.components.LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_weekend_weekday_habit"),
                shape = ExpressiveCardShape,
                tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
                blurRadius = 16.dp,
                lensCurvature = 0.38f,
                specularHighlight = 0.72f,
                chromaticDispersion = 0.24f,
                borderAlpha = 0.40f,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Weekend,
                                contentDescription = null,
                                tint = Color(0xFFEC4899),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Weekend vs. Weekday Habit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = ExpressiveChipShape,
                            color = Color(0xFFEC4899).copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "${String.format(Locale.US, "%.1f", weekendSurgeMultiplier)}x Surge",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEC4899),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val avgWeekdayStr = "₹${numberFormat.format(avgWeekdayDaily)}"
                    val avgWeekendStr = "₹${numberFormat.format(avgWeekendDaily)}"
                    val totalWeekdayStr = "Total: ₹${numberFormat.format(weekdaySpend)}"
                    val totalWeekendStr = "Total: ₹${numberFormat.format(weekendSpend)}"
                    val heroDailyStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = SFMonoFontFamily)
                    val totalSubStyle = TextStyle(fontSize = 10.sp, fontFamily = SFMonoFontFamily)

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val halfWidth = (maxWidth - 10.dp) / 2
                        val bothWeekCardsFit = rememberAllFitColumnWidth(
                            textsAndStyles = listOf(
                                avgWeekdayStr to heroDailyStyle,
                                avgWeekendStr to heroDailyStyle,
                                totalWeekdayStr to totalSubStyle,
                                totalWeekendStr to totalSubStyle
                            ),
                            columnWidth = halfWidth,
                            horizontalPaddingPerColumn = 24.dp
                        )

                        val weekdayCard: @Composable (Modifier) -> Unit = { mod ->
                            Surface(
                                modifier = mod,
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.60f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Weekdays", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("(Mon-Fri)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(avgWeekdayStr, style = heroDailyStyle, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, softWrap = false)
                                    Text("avg per day", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(totalWeekdayStr, style = totalSubStyle, color = MaterialTheme.colorScheme.primary, maxLines = 1, softWrap = false)
                                }
                            }
                        }

                        val weekendCard: @Composable (Modifier) -> Unit = { mod ->
                            Surface(
                                modifier = mod,
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFEC4899).copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.25f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Weekends", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEC4899))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("(Sat-Sun)", fontSize = 9.sp, color = Color(0xFFEC4899).copy(alpha = 0.7f))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(avgWeekendStr, style = heroDailyStyle, color = Color(0xFFEC4899), maxLines = 1, softWrap = false)
                                    Text("avg per day", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(totalWeekendStr, style = totalSubStyle, color = Color(0xFFEC4899), maxLines = 1, softWrap = false)
                                }
                            }
                        }

                        if (bothWeekCardsFit) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                weekdayCard(Modifier.weight(1f))
                                weekendCard(Modifier.weight(1f))
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                weekdayCard(Modifier.fillMaxWidth())
                                weekendCard(Modifier.fillMaxWidth())
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (weekendSurgeMultiplier > 1.3) {
                            "⚠️ You spend ${String.format(Locale.US, "%.1f", weekendSurgeMultiplier)}x more on Saturdays & Sundays than during the work week. Planning weekend outings with a set budget cap can prevent mid-month burn."
                        } else {
                            "⚖️ Your spending is well-balanced across weekdays and weekends without heavy weekend spikes."
                        },
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4. DAY OF WEEK DISTRIBUTION (Mon - Sun Bar Chart) with Liquid Glass
        item(key = "card_day_of_week_bars") {
            com.example.ui.components.LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_day_of_week_distribution"),
                shape = ExpressiveCardShape,
                tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
                blurRadius = 16.dp,
                lensCurvature = 0.38f,
                specularHighlight = 0.72f,
                chromaticDispersion = 0.24f,
                borderAlpha = 0.40f,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Timeline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Day of Week Spending Heat",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (peakDayStat != null && peakDayStat.amount > 0) {
                            Text(
                                text = "Peak: ${peakDayStat.day}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7 Bar Columns
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        dayOfWeekStats.forEach { stat ->
                            val isPeak = stat.day == peakDayStat?.day && stat.amount > 0
                            val barHeightRatio = stat.ratio.coerceIn(0.08f, 1f)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (stat.amount > 0) {
                                    val label = if (stat.amount >= 1000) "${(stat.amount / 1000).toInt()}k" else "${stat.amount.toInt()}"
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPeak) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text("-", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .height((70 * barHeightRatio).dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (isPeak) Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                                            else Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.12f)))
                                        )
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stat.day,
                                    fontSize = 10.sp,
                                    fontWeight = if (isPeak) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isPeak) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. TIME-OF-DAY IMPULSE WINDOW with Liquid Glass
        item(key = "card_time_of_day") {
            com.example.ui.components.LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_time_of_day_rhythm"),
                shape = ExpressiveCardShape,
                tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
                blurRadius = 16.dp,
                lensCurvature = 0.38f,
                specularHighlight = 0.72f,
                chromaticDispersion = 0.24f,
                borderAlpha = 0.40f,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Time-of-Day Spending Windows",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (peakTimeWindow != null && peakTimeWindow.amount > 0) {
                            Text(
                                text = "Peak: ${peakTimeWindow.name}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = peakTimeWindow.color
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        timeOfDayStats.forEach { stat ->
                            val pct = (stat.ratio * 100).toInt()
                            Column {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Icon(stat.icon, contentDescription = null, tint = stat.color, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stat.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("(${stat.hours})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "₹${numberFormat.format(stat.amount)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = SFMonoFontFamily,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.End,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "$pct%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = SFMonoFontFamily,
                                            color = stat.color,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { stat.ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = stat.color,
                                    trackColor = Color.White.copy(alpha = 0.08f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. SPENDING VELOCITY, BURN RATE & RUNWAY FORECAST with Liquid Glass
        item(key = "card_burn_rate_velocity") {
            com.example.ui.components.LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_burn_rate_velocity"),
                shape = ExpressiveCardShape,
                tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
                blurRadius = 16.dp,
                lensCurvature = 0.38f,
                specularHighlight = 0.72f,
                chromaticDispersion = 0.24f,
                borderAlpha = 0.40f,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Speed,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Burn Rate & Month-End Forecast",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = ExpressiveChipShape,
                            color = Color(0xFFF59E0B).copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "₹${numberFormat.format(dailyBurnRate)}/day",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SFMonoFontFamily,
                                color = Color(0xFFF59E0B),
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val projectedSpendStr = "₹${numberFormat.format(projectedMonthEndSpend)}"
                    val projectedStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = SFMonoFontFamily)
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val halfWidth = (maxWidth - 10.dp) / 2
                        val burnCardsFit = rememberAllFitColumnWidth(
                            textsAndStyles = listOf(projectedSpendStr to projectedStyle),
                            columnWidth = halfWidth,
                            horizontalPaddingPerColumn = 24.dp
                        )

                        val projectedCard: @Composable (Modifier) -> Unit = { mod ->
                            Surface(
                                modifier = mod,
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.60f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Projected Month Total", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(projectedSpendStr, style = projectedStyle, color = ExpenseRed, maxLines = 1, softWrap = false)
                                    Text("at current pace", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        val noSpendCard: @Composable (Modifier) -> Unit = { mod ->
                            Surface(
                                modifier = mod,
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.60f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("No-Spend Days", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("$noSpendDaysCount days", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = SFMonoFontFamily, color = IncomeGreen, maxLines = 1, softWrap = false)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                    }
                                    Text("₹0 expense logged", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        if (burnCardsFit) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                projectedCard(Modifier.weight(1f))
                                noSpendCard(Modifier.weight(1f))
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                projectedCard(Modifier.fillMaxWidth())
                                noSpendCard(Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }

        // 7. "LATTE FACTOR" MICRO-TRANSACTIONS LEAKS with Liquid Glass
        item(key = "card_micro_transactions") {
            com.example.ui.components.LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_micro_spend_latte_factor"),
                shape = ExpressiveCardShape,
                tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
                blurRadius = 16.dp,
                lensCurvature = 0.38f,
                specularHighlight = 0.72f,
                chromaticDispersion = 0.24f,
                borderAlpha = 0.40f,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.LocalCafe,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Micro-Spend 'Latte Factor' (<₹300)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = ExpressiveChipShape,
                            color = Color(0xFFF59E0B).copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "${microSpendCount} txs",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SFMonoFontFamily,
                                color = Color(0xFFF59E0B),
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val microTotalStr = "₹${numberFormat.format(microSpendTotal)}"
                    val microShareStr = "${String.format(Locale.US, "%.1f", microSpendRatio)}%"
                    val microValueStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = SFMonoFontFamily)
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val halfWidth = (maxWidth - 8.dp) / 2
                        val microFits = rememberAllFitColumnWidth(
                            textsAndStyles = listOf(
                                microTotalStr to microValueStyle,
                                microShareStr to microValueStyle
                            ),
                            columnWidth = halfWidth,
                            horizontalPaddingPerColumn = 0.dp
                        )
                        if (microFits) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Total Micro-Spend", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(microTotalStr, style = microValueStyle, color = Color(0xFFF59E0B), maxLines = 1, softWrap = false)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("Share of Budget", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                                    Text(microShareStr, style = microValueStyle, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.End, maxLines = 1, softWrap = false)
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column {
                                    Text("Total Micro-Spend", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(microTotalStr, style = microValueStyle, color = Color(0xFFF59E0B), maxLines = 1, softWrap = false)
                                }
                                Column {
                                    Text("Share of Budget", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(microShareStr, style = microValueStyle, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "☕ Frequent small taps (snacks, tea, quick rides, convenience fees) silently accumulate. $microSpendCount small purchases took ₹${numberFormat.format(microSpendTotal)} (${microSpendRatio.toInt()}% of your spending).",
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 8. LARGEST EXPENSE OUTLIERS
        if (topOutliers.isNotEmpty()) {
            item(key = "card_largest_outliers") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_largest_outlier_spends"),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Top Outlier Spends",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Highest Impact",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            topOutliers.forEach { tx ->
                                val dateStr = SimpleDateFormat("dd MMM", Locale.US).format(Date(tx.date))
                                val pctOfBudget = if (totalExpense > 0) ((tx.amount / totalExpense) * 100).toInt() else 0
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.50f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onEditTransaction(tx) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = CategoryHelper.getCategoryColor(tx.category).copy(alpha = 0.20f),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    CategoryHelper.getCategoryIcon(tx.category),
                                                    contentDescription = null,
                                                    tint = CategoryHelper.getCategoryColor(tx.category),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))

                                        val outlierAmtStr = "-₹${numberFormat.format(tx.amount)}"
                                        val outlierStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = SFMonoFontFamily)
                                        BoxWithConstraints(modifier = Modifier.weight(1f)) {
                                            val fitsInline = rememberAllFitColumnWidth(
                                                textsAndStyles = listOf(outlierAmtStr to outlierStyle),
                                                columnWidth = (maxWidth - 110.dp).coerceAtLeast(0.dp),
                                                horizontalPaddingPerColumn = 0.dp
                                            )
                                            val outlierDetails: @Composable (Modifier) -> Unit = { mod ->
                                                Column(modifier = mod) {
                                                    Text(
                                                        text = if (tx.memo.isNotBlank()) "${tx.category} • ${tx.memo}" else tx.category,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "$dateStr • ${tx.wallet}",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            if (fitsInline) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    outlierDetails(Modifier.weight(1f))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text(
                                                            text = outlierAmtStr,
                                                            style = outlierStyle,
                                                            color = ExpenseRed,
                                                            textAlign = TextAlign.End,
                                                            maxLines = 1,
                                                            softWrap = false
                                                        )
                                                        Text(
                                                            text = "$pctOfBudget% of total",
                                                            fontSize = 9.sp,
                                                            fontFamily = SFMonoFontFamily,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            textAlign = TextAlign.End,
                                                            maxLines = 1,
                                                            softWrap = false
                                                        )
                                                    }
                                                }
                                            } else {
                                                Column(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    outlierDetails(Modifier.fillMaxWidth())
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "$pctOfBudget% of total",
                                                            fontSize = 9.sp,
                                                            fontFamily = SFMonoFontFamily,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            maxLines = 1,
                                                            softWrap = false
                                                        )
                                                        Text(
                                                            text = outlierAmtStr,
                                                            style = outlierStyle,
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
                    }
                }
            }
        }

        // 9. PAYMENT MODE DEPENDENCY & CREDIT CARD RATIO with Liquid Glass
        item(key = "card_payment_mode_dependency") {
            com.example.ui.components.LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_payment_mode_dependency"),
                shape = ExpressiveCardShape,
                tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
                blurRadius = 16.dp,
                lensCurvature = 0.38f,
                specularHighlight = 0.72f,
                chromaticDispersion = 0.24f,
                borderAlpha = 0.40f,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Payment Channel Dependency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = ExpressiveChipShape,
                            color = if (cardDependencyPct > 60) ExpenseRed.copy(alpha = 0.18f) else Color(0xFF8B5CF6).copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = "${cardDependencyPct.toInt()}% Card Share",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = SFMonoFontFamily,
                                color = if (cardDependencyPct > 60) ExpenseRed else Color(0xFF8B5CF6),
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val upiStr = "₹${numberFormat.format(upiTotal)}"
                    val cardStr = "₹${numberFormat.format(cardTotal)}"
                    val cashStr = "₹${numberFormat.format(cashTotal)}"
                    val channelAmtStyle = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = SFMonoFontFamily)

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val thirdColWidth = (maxWidth - 16.dp) / 3
                        val allChannelsFit = rememberAllFitColumnWidth(
                            textsAndStyles = listOf(
                                upiStr to channelAmtStyle,
                                cardStr to channelAmtStyle,
                                cashStr to channelAmtStyle
                            ),
                            columnWidth = thirdColWidth,
                            horizontalPaddingPerColumn = 12.dp
                        )

                        if (allChannelsFit) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PaymentChannelPill(
                                    channel = "UPI / Bank",
                                    amount = upiStr,
                                    pct = if (totalExpense > 0) ((upiTotal / totalExpense) * 100).toInt() else 0,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.weight(1f)
                                )
                                PaymentChannelPill(
                                    channel = "Credit Card",
                                    amount = cardStr,
                                    pct = cardDependencyPct.toInt(),
                                    color = Color(0xFF8B5CF6),
                                    modifier = Modifier.weight(1f)
                                )
                                PaymentChannelPill(
                                    channel = "Cash",
                                    amount = cashStr,
                                    pct = if (totalExpense > 0) ((cashTotal / totalExpense) * 100).toInt() else 0,
                                    color = IncomeGreen,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PaymentChannelPill(
                                    channel = "UPI / Bank",
                                    amount = upiStr,
                                    pct = if (totalExpense > 0) ((upiTotal / totalExpense) * 100).toInt() else 0,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                PaymentChannelPill(
                                    channel = "Credit Card",
                                    amount = cardStr,
                                    pct = cardDependencyPct.toInt(),
                                    color = Color(0xFF8B5CF6),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                PaymentChannelPill(
                                    channel = "Cash",
                                    amount = cashStr,
                                    pct = if (totalExpense > 0) ((cashTotal / totalExpense) * 100).toInt() else 0,
                                    color = IncomeGreen,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // 10. SMART HABIT TAKEAWAYS & ACTIONABLE TIPS with Liquid Glass
        item(key = "card_actionable_habits") {
            com.example.ui.components.LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_smart_habit_takeaways"),
                shape = ExpressiveCardShape,
                tint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f),
                blurRadius = 18.dp,
                lensCurvature = 0.42f,
                specularHighlight = 0.85f,
                chromaticDispersion = 0.28f,
                borderAlpha = 0.45f,
                elevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Smart Financial Habit Insights",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HabitInsightRow(
                            icon = Icons.Default.Speed,
                            title = "Daily Spending Rhythm",
                            text = "Your daily burn rate is ₹${numberFormat.format(dailyBurnRate)}/day. Staying below ₹${numberFormat.format((totalIncome / 30.0).coerceAtLeast(1000.0))}/day will safeguard a positive month-end surplus."
                        )
                        HabitInsightRow(
                            icon = Icons.Default.Weekend,
                            title = "Weekend Spending Multiplier",
                            text = if (weekendSurgeMultiplier > 1.2) {
                                "Weekend velocity is ${String.format(Locale.US, "%.1f", weekendSurgeMultiplier)}x higher than weekdays. Scheduling budget-free weekend outings can save up to ₹${numberFormat.format(weekendSpend * 0.20)}."
                            } else {
                                "Smooth, stable spending between weekdays and weekends with minimal surge."
                            }
                        )
                        HabitInsightRow(
                            icon = Icons.Default.Today,
                            title = "No-Spend Habit Days",
                            text = "You celebrated $noSpendDaysCount zero-spend days in this period! Aim for 8–10 zero-spend days a month to build lasting financial independence."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreSubPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.60f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = SFMonoFontFamily, color = color, textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
private fun BudgetRuleColumn(
    title: String,
    target: String,
    amount: String,
    pct: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(4.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Text(target, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(2.dp))
        Text(amount, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = SFMonoFontFamily, color = color, textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
        Text(pct, fontSize = 10.sp, fontFamily = SFMonoFontFamily, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
    }
}

@Composable
private fun PaymentChannelPill(
    channel: String,
    amount: String,
    pct: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(channel, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(2.dp))
            Text(amount, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = SFMonoFontFamily, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
            Text("$pct%", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = SFMonoFontFamily, color = color, textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
private fun HabitInsightRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text, fontSize = 11.sp, lineHeight = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private data class DayOfWeekStat(
    val day: String,
    val amount: Double,
    val count: Int,
    val ratio: Float
)

private data class TimeWindowStat(
    val name: String,
    val hours: String,
    val amount: Double,
    val ratio: Float,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)
