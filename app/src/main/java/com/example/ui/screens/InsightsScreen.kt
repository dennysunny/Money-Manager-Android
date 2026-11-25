package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.ui.theme.SFMonoFontFamily
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.HabitEntity
import com.example.ui.CategoryMoMComparison
import com.example.ui.HabitCorrelationData
import com.example.ui.HabitTrackerState
import com.example.ui.HabitWithStatus
import com.example.ui.InsightsData
import com.example.ui.MonthlySpendComparison
import com.example.ui.components.CategoryHelper
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.rememberAllFitColumnWidth
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpressiveButtonShape
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.ExpressiveDialogShape
import com.example.ui.theme.ExpressivePillButtonShape
import com.example.ui.theme.IncomeGreen
import java.text.DecimalFormat
import kotlin.math.abs

/**
 * Dedicated Room-backed Insights Screen with Material 3 Expressive Design.
 *
 * Implements:
 * 1. Daily Habit Tracker with Room database persistence
 * 2. Correlation of daily habits with spending trends and measurable savings
 * 3. Month-over-Month (MoM) spending comparisons with custom bar charts
 * 4. Top-spending categories with rank, share, and MoM trend deltas
 * 5. Spending velocity & daily pace
 * 6. Responsive, overflow-safe buttons that grow gracefully
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InsightsScreen(
    insightsData: InsightsData,
    habitTrackerState: HabitTrackerState = HabitTrackerState(),
    onToggleHabit: ((Long) -> Unit)? = null,
    onAddCustomHabit: ((String, String, String) -> Unit)? = null,
    onDeleteHabit: ((HabitEntity) -> Unit)? = null,
    onNavigateToCategory: ((String) -> Unit)? = null,
    includeStatusBarPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { DecimalFormat("#,##,###") }
    var selectedFilterSegment by remember { mutableIntStateOf(0) } // 0: Overview, 1: Daily Habits, 2: Top Categories, 3: MoM Trends
    var selectedChartMonthIndex by remember { mutableIntStateOf(insightsData.monthlyComparisons.lastIndex.coerceAtLeast(0)) }
    var showAddHabitDialog by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val isSpendingUp = insightsData.momDeltaAmount > 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .then(if (includeStatusBarPadding) Modifier.statusBarsPadding() else Modifier)
            .testTag("screen_insights"),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = if (includeStatusBarPadding) 8.dp else 4.dp,
            bottom = 120.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Screen Title Header
        item(key = "header_insights") {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "Insights",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Room-backed trend analysis & daily habit intelligence",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Data Count Chip
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = primaryColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoGraph,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${insightsData.totalTransactionsAnalyzed} Txs",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Material 3 Expressive Segmented Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Overview", "Daily Habits", "Top Categories", "MoM Trends").forEachIndexed { index, label ->
                        FilterChip(
                            selected = selectedFilterSegment == index,
                            onClick = { selectedFilterSegment = index },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedFilterSegment == index) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor.copy(alpha = 0.16f),
                                selectedLabelColor = primaryColor
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.defaultMinSize(minHeight = 38.dp)
                        )
                    }
                }
            }
        }

        // 2. DAILY HABIT TRACKER & SPENDING CORRELATION
        if (selectedFilterSegment == 0 || selectedFilterSegment == 1) {
            item(key = "card_daily_habit_tracker") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_daily_habit_tracker"),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF10B981).copy(alpha = 0.16f),
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.TaskAlt,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Daily Habit Tracker",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Correlating recurring routines with savings",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Add Habit Button (overflow-safe, flexible size)
                            OutlinedButton(
                                onClick = { showAddHabitDialog = true },
                                shape = ExpressivePillButtonShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 36.dp)
                                    .testTag("btn_add_habit")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Habit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Today's Adherence Summary Bar
                        val completedCount = habitTrackerState.habits.count { it.isCompletedToday }
                        val totalHabits = habitTrackerState.habits.size
                        val adherencePct = if (totalHabits > 0) (completedCount.toFloat() / totalHabits).coerceIn(0f, 1f) else 0f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Today's Progress: $completedCount of $totalHabits completed",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(adherencePct * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (adherencePct >= 0.7f) IncomeGreen else primaryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { adherencePct },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (adherencePct >= 0.7f) IncomeGreen else primaryColor,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Habits Checklist
                        if (habitTrackerState.habits.isEmpty()) {
                            Text(
                                text = "No habits tracked yet. Tap 'New Habit' to start correlating your daily goals!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                habitTrackerState.habits.forEach { habitStatus ->
                                    HabitCheckItem(
                                        habitStatus = habitStatus,
                                        onToggle = { onToggleHabit?.invoke(habitStatus.habit.id) },
                                        onDelete = { onDeleteHabit?.invoke(habitStatus.habit) }
                                    )
                                }
                            }
                        }

                        // Habit-Spending Correlation Highlights
                        if (habitTrackerState.correlations.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Spending Correlations & Habit Impact",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                habitTrackerState.correlations.take(3).forEach { correlation ->
                                    HabitCorrelationPill(correlation = correlation, numberFormat = numberFormat)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. HERO CARD: Month-over-Month Spending Comparison
        if (selectedFilterSegment == 0 || selectedFilterSegment == 3) {
            item(key = "card_mom_hero") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_mom_hero"),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(end = 8.dp)) {
                                Text(
                                    text = "MONTH-OVER-MONTH SPEND",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "₹${numberFormat.format(insightsData.currentMonthExpense)}",
                                        fontSize = 26.sp,
                                        lineHeight = 30.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = SFMonoFontFamily,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Text(
                                        text = insightsData.currentMonthLabel,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.align(Alignment.Bottom).padding(bottom = 3.dp)
                                    )
                                }
                            }

                            // MoM Delta Badge
                            val deltaBadgeColor = if (isSpendingUp) ExpenseRed else IncomeGreen
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = deltaBadgeColor.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, deltaBadgeColor.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSpendingUp) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = deltaBadgeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    val formattedDelta = String.format("%.1f%%", abs(insightsData.momDeltaPercent))
                                    Text(
                                        text = if (isSpendingUp) "+$formattedDelta" else "-$formattedDelta",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SFMonoFontFamily,
                                        color = deltaBadgeColor,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Comparison Stats Triplet
                        val currentMonthStr = "₹${numberFormat.format(insightsData.currentMonthExpense)}"
                        val prevMonthStr = "₹${numberFormat.format(insightsData.prevMonthExpense)}"
                        val deltaAbs = abs(insightsData.momDeltaAmount)
                        val deltaStr = "${if (isSpendingUp) "+₹" else "-₹"}${numberFormat.format(deltaAbs)}"
                        val momStatStyle = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SFMonoFontFamily
                        )

                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(vertical = 12.dp, horizontal = 10.dp)
                        ) {
                            val singleColumnWidth = (maxWidth - 16.dp) / 3
                            val allThreeFit = rememberAllFitColumnWidth(
                                textsAndStyles = listOf(
                                    currentMonthStr to momStatStyle,
                                    prevMonthStr to momStatStyle,
                                    deltaStr to momStatStyle
                                ),
                                columnWidth = singleColumnWidth,
                                horizontalPaddingPerColumn = 4.dp
                            )

                            if (allThreeFit) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "This Month",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = currentMonthStr,
                                            style = momStatStyle,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "Last Month (${insightsData.prevMonthLabel})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = prevMonthStr,
                                            style = momStatStyle,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .height(24.dp)
                                            .width(1.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (isSpendingUp) "Net Increase" else "Net Savings",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = deltaStr,
                                            style = momStatStyle,
                                            color = if (isSpendingUp) ExpenseRed else IncomeGreen,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column {
                                        Text("This Month", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = currentMonthStr, style = momStatStyle, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, softWrap = false)
                                    }
                                    Column {
                                        Text("Last Month (${insightsData.prevMonthLabel})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = prevMonthStr, style = momStatStyle, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, softWrap = false)
                                    }
                                    Column {
                                        Text(if (isSpendingUp) "Net Increase" else "Net Savings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = deltaStr, style = momStatStyle, color = if (isSpendingUp) ExpenseRed else IncomeGreen, maxLines = 1, softWrap = false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Month-over-Month 6-Month Comparison Bar Chart
        if (selectedFilterSegment == 0 || selectedFilterSegment == 3) {
            item(key = "card_mom_bar_chart") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_mom_chart"),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = primaryColor.copy(alpha = 0.16f),
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            tint = primaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "6-Month Spending Trend",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Tap a bar to inspect that month's Room metrics",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Canvas Chart
                        MonthOverMonthBarChart(
                            months = insightsData.monthlyComparisons,
                            selectedIndex = selectedChartMonthIndex,
                            onMonthSelected = { selectedChartMonthIndex = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(170.dp)
                        )

                        // Selected Month Detail Info Box
                        if (selectedChartMonthIndex in insightsData.monthlyComparisons.indices) {
                            val selectedMonth = insightsData.monthlyComparisons[selectedChartMonthIndex]
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(end = 8.dp)) {
                                        Text(
                                            text = selectedMonth.fullLabel,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Top category: ${selectedMonth.topCategory} • ${selectedMonth.transactionCount} transactions",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${numberFormat.format(selectedMonth.totalExpense)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = SFMonoFontFamily,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.End,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        if (selectedMonth.deltaPercentVsPrev != 0f) {
                                            val isUp = selectedMonth.deltaPercentVsPrev > 0
                                            Text(
                                                text = "${if (isUp) "+" else ""}${String.format("%.1f%%", selectedMonth.deltaPercentVsPrev)} MoM",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = SFMonoFontFamily,
                                                color = if (isUp) ExpenseRed else IncomeGreen,
                                                textAlign = TextAlign.End,
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

        // 5. TOP-SPENDING CATEGORIES SECTION
        if (selectedFilterSegment == 0 || selectedFilterSegment == 2) {
            item(key = "card_top_categories_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Spending Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "${insightsData.topCategories.size} Categories",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item(key = "card_category_breakdown_donut") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("card_category_distribution"),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Category Share & Distribution",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = insightsData.currentMonthLabel,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stacked Proportion Bar
                        CategoryStackedBar(
                            categories = insightsData.topCategories.take(6),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Category List with Rank, Icon, Spend, Share, and MoM Trend
                        insightsData.topCategories.take(5).forEachIndexed { index, cat ->
                            CategoryMoMRow(
                                rank = index + 1,
                                category = cat,
                                numberFormat = numberFormat,
                                onClick = { onNavigateToCategory?.invoke(cat.category) }
                            )
                            if (index < 4 && index < insightsData.topCategories.size - 1) {
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }
                    }
                }
            }
        }

        // 6. SPENDING VELOCITY & BURN RATE
        item(key = "card_spending_velocity") {
            val velocity = insightsData.spendingVelocity
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_spending_velocity"),
                shape = ExpressiveCardShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = primaryColor.copy(alpha = 0.16f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Velocity & Pace",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Day ${velocity.daysElapsed} of ${velocity.totalDaysInMonth}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val progressRatio = (velocity.daysElapsed.toFloat() / velocity.totalDaysInMonth.toFloat()).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = primaryColor,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val dailyAvgStr = "₹${numberFormat.format(velocity.currentMonthDailyAvg.toLong())} / day"
                    val projectedStr = "₹${numberFormat.format(velocity.projectedMonthTotal.toLong())}"
                    val velocityValueStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SFMonoFontFamily
                    )

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val halfWidth = (maxWidth - 10.dp) / 2
                        val bothFit = rememberAllFitColumnWidth(
                            textsAndStyles = listOf(
                                dailyAvgStr to velocityValueStyle,
                                projectedStr to velocityValueStyle
                            ),
                            columnWidth = halfWidth,
                            horizontalPaddingPerColumn = 0.dp
                        )

                        if (bothFit) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Daily Average",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = dailyAvgStr,
                                        style = velocityValueStyle,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Projected Month End",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.End
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = projectedStr,
                                        style = velocityValueStyle,
                                        color = if (velocity.projectedDifference > 0) ExpenseRed else IncomeGreen,
                                        textAlign = TextAlign.End,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column {
                                    Text("Daily Average", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = dailyAvgStr, style = velocityValueStyle, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, softWrap = false)
                                }
                                Column {
                                    Text("Projected Month End", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = projectedStr, style = velocityValueStyle, color = if (velocity.projectedDifference > 0) ExpenseRed else IncomeGreen, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add Custom Habit
    if (showAddHabitDialog) {
        AddCustomHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onAdd = { title, cat, desc ->
                onAddCustomHabit?.invoke(title, cat, desc)
                showAddHabitDialog = false
            }
        )
    }
}

/**
 * Habit Check-In Item
 */
@Composable
private fun HabitCheckItem(
    habitStatus: HabitWithStatus,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val habit = habitStatus.habit
    val isCompleted = habitStatus.isCompletedToday
    val primaryColor = MaterialTheme.colorScheme.primary

    val checkBgColor by animateColorAsState(
        targetValue = if (isCompleted) IncomeGreen else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "habitCheckBg"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(
            width = if (isCompleted) 1.5.dp else 1.dp,
            color = if (isCompleted) IncomeGreen.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggle() }
            .testTag("habit_item_${habit.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Interactive Checkbox Circle
                Surface(
                    shape = CircleShape,
                    color = checkBgColor,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = habit.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (habitStatus.currentStreak > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "🔥 ${habitStatus.currentStreak}d",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD97706),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    if (habit.description.isNotBlank()) {
                        Text(
                            text = habit.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 7-day dot trail
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                habitStatus.recent7DaysStatus.forEach { dayCompleted ->
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (dayCompleted) IncomeGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

/**
 * Habit-Spending Correlation Pill
 */
@Composable
private fun HabitCorrelationPill(
    correlation: HabitCorrelationData,
    numberFormat: DecimalFormat
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Savings,
                contentDescription = null,
                tint = IncomeGreen,
                modifier = Modifier
                    .size(16.dp)
                    .padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = correlation.habitTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                    if (correlation.savingsPerDay > 0) {
                        Text(
                            text = "Est. ₹${numberFormat.format(correlation.totalEstimatedSavings.toLong())} saved",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SFMonoFontFamily,
                            color = IncomeGreen,
                            textAlign = TextAlign.End
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = correlation.correlationDescription,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

/**
 * Add Custom Habit Dialog
 */
@Composable
private fun AddCustomHabitDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetCategory by remember { mutableStateOf("Food") }
    var description by remember { mutableStateOf("") }

    val categoryOptions = listOf("Food", "Shopping", "Petrol", "Health", "Bills", "General")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = ExpressiveDialogShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Track New Habit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Habit Name (e.g. Cooked at Home)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Goal Note (e.g. Avoid ordering takeout)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Correlate with Spending Category:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categoryOptions.forEach { cat ->
                        FilterChip(
                            selected = targetCategory == cat,
                            onClick = { targetCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Overflow-safe Dialog Buttons that grow gracefully
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = ExpressiveButtonShape,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAdd(title, targetCategory, description)
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = ExpressiveButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text(
                            text = "Save Habit",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas Month-over-Month Bar Chart
 */
@Composable
private fun MonthOverMonthBarChart(
    months: List<MonthlySpendComparison>,
    selectedIndex: Int,
    onMonthSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (months.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No transactions in database to analyze", color = Color.Gray, fontSize = 12.sp)
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val maxSpend = (months.maxOfOrNull { it.totalExpense } ?: 1.0).coerceAtLeast(100.0)

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {}
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val bottomLabelHeight = 28.dp.toPx()
            val chartTopPadding = 24.dp.toPx()
            val usableHeight = canvasHeight - bottomLabelHeight - chartTopPadding

            val barCount = months.size
            val barSpacing = 12.dp.toPx()
            val totalSpacing = barSpacing * (barCount + 1)
            val barWidth = ((canvasWidth - totalSpacing) / barCount).coerceIn(18.dp.toPx(), 44.dp.toPx())

            val avgSpend = months.map { it.totalExpense }.average()
            val avgY = chartTopPadding + usableHeight - ((avgSpend / maxSpend).toFloat() * usableHeight)
            drawLine(
                color = onSurfaceVariant.copy(alpha = 0.25f),
                start = Offset(0f, avgY.toFloat()),
                end = Offset(canvasWidth, avgY.toFloat()),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            months.forEachIndexed { index, item ->
                val x = barSpacing + index * (barWidth + barSpacing)
                val heightFraction = (item.totalExpense / maxSpend).toFloat().coerceIn(0.04f, 1f)
                val barHeight = usableHeight * heightFraction
                val barTop = chartTopPadding + usableHeight - barHeight

                val isSelected = index == selectedIndex
                val isCurrentMonth = index == months.lastIndex

                val barBrush = if (isSelected) {
                    Brush.verticalGradient(
                        colors = listOf(primaryColor, primaryColor.copy(alpha = 0.75f)),
                        startY = barTop,
                        endY = barTop + barHeight
                    )
                } else if (isCurrentMonth) {
                    Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.90f), primaryColor.copy(alpha = 0.50f)),
                        startY = barTop,
                        endY = barTop + barHeight
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(surfaceVariant.copy(alpha = 0.95f), surfaceVariant.copy(alpha = 0.60f)),
                        startY = barTop,
                        endY = barTop + barHeight
                    )
                }

                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(x, barTop),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                )

                drawRoundRect(
                    color = if (isSelected) primaryColor else Color.Transparent,
                    topLeft = Offset(x, barTop),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = if (isSelected) primaryColor.hashCode() else onSurfaceVariant.hashCode()
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                        isFakeBoldText = isSelected
                    }
                    drawText(
                        item.monthLabel,
                        x + barWidth / 2f,
                        canvasHeight - 6.dp.toPx(),
                        paint
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            months.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            onMonthSelected(index)
                        }
                )
            }
        }
    }
}

/**
 * Category Stacked Horizontal Bar
 */
@Composable
private fun CategoryStackedBar(
    categories: List<CategoryMoMComparison>,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) return

    Row(modifier = modifier) {
        categories.forEach { cat ->
            val color = CategoryHelper.getCategoryColor(cat.category)
            val weight = cat.percentageOfTotal.coerceAtLeast(0.02f)
            Box(
                modifier = Modifier
                    .weight(weight)
                    .fillMaxSize()
                    .background(color)
            )
        }
    }
}

/**
 * Category Row with Rank, Icon, Spend, Share, and MoM Trend
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryMoMRow(
    rank: Int,
    category: CategoryMoMComparison,
    numberFormat: DecimalFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = CategoryHelper.getCategoryColor(category.category)
    val categoryIcon = CategoryHelper.getCategoryIcon(category.category)
    val isDeltaPositive = category.deltaAmount > 0
    val amountStr = "₹${numberFormat.format(category.currentMonthAmount)}"
    val amountStyle = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = SFMonoFontFamily
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "#$rank",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            shape = CircleShape,
            color = categoryColor.copy(alpha = 0.18f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val fitsInline = rememberAllFitColumnWidth(
                textsAndStyles = listOf(amountStr to amountStyle),
                columnWidth = (maxWidth - 110.dp).coerceAtLeast(0.dp),
                horizontalPaddingPerColumn = 0.dp
            )

            val catDetails: @Composable (Modifier) -> Unit = { mod ->
                Column(modifier = mod) {
                    Text(
                        text = category.category,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${String.format("%.1f%%", category.percentageOfTotal * 100)} of expenses • ${category.transactionCount} txs",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val deltaContent: @Composable () -> Unit = {
                if (category.previousMonthAmount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDeltaPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                            contentDescription = null,
                            tint = if (isDeltaPositive) ExpenseRed else IncomeGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${if (isDeltaPositive) "+" else ""}${String.format("%.0f%%", category.deltaPercent)} MoM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SFMonoFontFamily,
                            color = if (isDeltaPositive) ExpenseRed else IncomeGreen,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                } else {
                    Text(
                        text = "New category",
                        fontSize = 10.sp,
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
                    catDetails(Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = amountStr,
                            style = amountStyle,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            softWrap = false
                        )
                        deltaContent()
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    catDetails(Modifier.fillMaxWidth())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        deltaContent()
                        Text(
                            text = amountStr,
                            style = amountStyle,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}
