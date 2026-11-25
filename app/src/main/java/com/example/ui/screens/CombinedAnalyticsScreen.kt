package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DonutSmall
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.data.model.HabitEntity
import com.example.data.model.TransactionEntity
import com.example.ui.CategoryMoMComparison
import com.example.ui.CategorySpend
import com.example.ui.HabitTrackerState
import com.example.ui.InsightsData
import com.example.ui.PaymentModeStat
import com.example.ui.TimeFilter
import com.example.ui.TrendBarData
import com.example.ui.theme.ExpressiveChipShape
import com.example.ui.theme.SFProFontFamily

enum class AnalyticsSubTab(val title: String) {
    INSIGHTS("Insights & Habits"),
    CATEGORIES("Category Stats"),
    MONTHLY_REPORT("Monthly Report"),
    PAYMENT_MODES("Payment Modes")
}

@Composable
fun CombinedAnalyticsScreen(
    insightsData: InsightsData,
    habitTrackerState: HabitTrackerState = HabitTrackerState(),
    onToggleHabit: ((Long) -> Unit)? = null,
    onAddCustomHabit: ((String, String, String) -> Unit)? = null,
    onDeleteHabit: ((HabitEntity) -> Unit)? = null,
    categorySpends: List<CategorySpend>,
    paymentModeStats: List<PaymentModeStat> = emptyList(),
    totalExpense: Double,
    totalIncome: Double,
    selectedTimeFilter: TimeFilter,
    periodLabel: String,
    selectedCategory: String?,
    categoryTransactions: List<TransactionEntity>,
    categoryTrendBars: List<TrendBarData>,
    overallTrendBars: List<TrendBarData> = emptyList(),
    allTransactions: List<TransactionEntity> = emptyList(),
    onSelectCategory: (String?) -> Unit,
    onTimeFilterSelected: (TimeFilter) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onResetToday: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit = {},
    onDeleteTransaction: (TransactionEntity) -> Unit = {},
    initialSubTab: AnalyticsSubTab = AnalyticsSubTab.INSIGHTS,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember(initialSubTab) { mutableStateOf(initialSubTab) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_combined_analytics")
    ) {
        // Edge-to-edge seamless Top Segmented Bar extending behind the status bar
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnalyticsSubTab.values().forEach { tab ->
                        val isSelected = activeSubTab == tab
                        val icon = when (tab) {
                            AnalyticsSubTab.INSIGHTS -> Icons.Default.AutoGraph
                            AnalyticsSubTab.CATEGORIES -> Icons.Default.PieChart
                            AnalyticsSubTab.MONTHLY_REPORT -> Icons.Default.ReceiptLong
                            AnalyticsSubTab.PAYMENT_MODES -> Icons.Default.CreditCard
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { activeSubTab = tab },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontFamily = SFProFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = ExpressiveChipShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("analytics_subtab_${tab.name.lowercase()}")
                        )
                    }
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                )
            }
        }

        // Subtab Content Switcher with smooth fade transition
        AnimatedContent(
            targetState = activeSubTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "AnalyticsTabTransition",
            modifier = Modifier.weight(1f)
        ) { targetTab ->
            when (targetTab) {
                AnalyticsSubTab.INSIGHTS -> {
                    InsightsScreen(
                        insightsData = insightsData,
                        habitTrackerState = habitTrackerState,
                        onToggleHabit = onToggleHabit,
                        onAddCustomHabit = onAddCustomHabit,
                        onDeleteHabit = onDeleteHabit,
                        onNavigateToCategory = { cat ->
                            onSelectCategory(cat)
                            activeSubTab = AnalyticsSubTab.CATEGORIES
                        },
                        includeStatusBarPadding = false
                    )
                }
                AnalyticsSubTab.CATEGORIES -> {
                    StatisticsScreen(
                        categorySpends = categorySpends,
                        paymentModeStats = paymentModeStats,
                        totalExpense = totalExpense,
                        totalIncome = totalIncome,
                        selectedTimeFilter = selectedTimeFilter,
                        periodLabel = periodLabel,
                        selectedCategory = selectedCategory,
                        categoryTransactions = categoryTransactions,
                        categoryTrendBars = categoryTrendBars,
                        overallTrendBars = overallTrendBars,
                        allTransactions = allTransactions,
                        onSelectCategory = onSelectCategory,
                        onTimeFilterSelected = onTimeFilterSelected,
                        onPreviousPeriod = onPreviousPeriod,
                        onNextPeriod = onNextPeriod,
                        onResetToday = onResetToday,
                        onEditTransaction = onEditTransaction,
                        onDeleteTransaction = onDeleteTransaction,
                        initialTab = "Category",
                        includeStatusBarPadding = false
                    )
                }
                AnalyticsSubTab.MONTHLY_REPORT -> {
                    StatisticsScreen(
                        categorySpends = categorySpends,
                        paymentModeStats = paymentModeStats,
                        totalExpense = totalExpense,
                        totalIncome = totalIncome,
                        selectedTimeFilter = selectedTimeFilter,
                        periodLabel = periodLabel,
                        selectedCategory = selectedCategory,
                        categoryTransactions = categoryTransactions,
                        categoryTrendBars = categoryTrendBars,
                        overallTrendBars = overallTrendBars,
                        allTransactions = allTransactions,
                        onSelectCategory = onSelectCategory,
                        onTimeFilterSelected = onTimeFilterSelected,
                        onPreviousPeriod = onPreviousPeriod,
                        onNextPeriod = onNextPeriod,
                        onResetToday = onResetToday,
                        onEditTransaction = onEditTransaction,
                        onDeleteTransaction = onDeleteTransaction,
                        initialTab = "MonthlyReport",
                        includeStatusBarPadding = false
                    )
                }
                AnalyticsSubTab.PAYMENT_MODES -> {
                    StatisticsScreen(
                        categorySpends = categorySpends,
                        paymentModeStats = paymentModeStats,
                        totalExpense = totalExpense,
                        totalIncome = totalIncome,
                        selectedTimeFilter = selectedTimeFilter,
                        periodLabel = periodLabel,
                        selectedCategory = selectedCategory,
                        categoryTransactions = categoryTransactions,
                        categoryTrendBars = categoryTrendBars,
                        overallTrendBars = overallTrendBars,
                        allTransactions = allTransactions,
                        onSelectCategory = onSelectCategory,
                        onTimeFilterSelected = onTimeFilterSelected,
                        onPreviousPeriod = onPreviousPeriod,
                        onNextPeriod = onNextPeriod,
                        onResetToday = onResetToday,
                        onEditTransaction = onEditTransaction,
                        onDeleteTransaction = onDeleteTransaction,
                        initialTab = "PaymentMode",
                        includeStatusBarPadding = false
                    )
                }
            }
        }
    }
}
