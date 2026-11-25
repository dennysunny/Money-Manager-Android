package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import com.example.ui.theme.ExpressiveButtonShape
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.ExpressiveChipShape
import com.example.ui.theme.ExpressiveMenuShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.ui.theme.SFMonoFontFamily
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.PaymentModeStat
import androidx.activity.compose.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.CategorySpend
import com.example.ui.TimeFilter
import com.example.ui.TrendBarData
import com.example.ui.components.CategoryHelper
import com.example.ui.components.PeriodHeader
import com.example.ui.components.rememberAllFitColumnWidth
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.DecimalFormat

@Composable
fun StatisticsScreen(
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
    initialTab: String = "SummaryDashboard",
    includeStatusBarPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    val savingsRate = if (totalIncome > 0) (((totalIncome - totalExpense) / totalIncome) * 100).coerceIn(0.0, 100.0) else 0.0
    var statsTab by remember(initialTab) { mutableStateOf(initialTab) } // "SummaryDashboard", "Category", "MonthlyReport", "PaymentMode"
    var selectedPaymentFilter by remember { mutableStateOf("All") } // "All", "Cash / UPI", "Credit Card"

    val cashStat = remember(paymentModeStats) { paymentModeStats.find { it.mode == "Cash / UPI" } }
    val cardStat = remember(paymentModeStats) { paymentModeStats.find { it.mode == "Credit Card" } }
    val mainListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    var savedStatsScrollIndex by rememberSaveable { mutableIntStateOf(0) }
    var savedStatsScrollOffset by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(selectedCategory) {
        if (selectedCategory == null && (savedStatsScrollIndex > 0 || savedStatsScrollOffset > 0)) {
            mainListState.scrollToItem(savedStatsScrollIndex, savedStatsScrollOffset)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("statistics_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // General Overview Statistics
            PeriodHeader(
                selectedTimeFilter = selectedTimeFilter,
                periodLabel = periodLabel,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                onTimeFilterSelected = onTimeFilterSelected,
                onPrevious = onPreviousPeriod,
                onNext = onNextPeriod,
                onResetToday = onResetToday,
                includeStatusBarPadding = includeStatusBarPadding
            )

            // Switcher Tabs: Summary Dashboard vs Breakdown vs Monthly Report vs Cash / Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = statsTab == "SummaryDashboard",
                    onClick = { statsTab = "SummaryDashboard" },
                    leadingIcon = {
                        Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(13.dp))
                    },
                    label = { Text("Summary Dashboard", fontSize = 11.sp, fontWeight = if (statsTab == "SummaryDashboard") FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                FilterChip(
                    selected = statsTab == "Category",
                    onClick = { statsTab = "Category" },
                    label = { Text("Breakdown", fontSize = 11.sp, fontWeight = if (statsTab == "Category") FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                FilterChip(
                    selected = statsTab == "MonthlyReport",
                    onClick = { statsTab = "MonthlyReport" },
                    leadingIcon = {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(13.dp))
                    },
                    label = { Text("Monthly Report", fontSize = 11.sp, fontWeight = if (statsTab == "MonthlyReport") FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                    )
                )

                FilterChip(
                    selected = statsTab == "PaymentMode",
                    onClick = { statsTab = "PaymentMode" },
                    leadingIcon = {
                        Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(13.dp))
                    },
                    label = { Text("Cash / Cards", fontSize = 11.sp, fontWeight = if (statsTab == "PaymentMode") FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF8B5CF6),
                        selectedLabelColor = Color.White
                    )
                )
            }

            if (statsTab == "SummaryDashboard") {
                SpendingHabitsDashboardView(
                    allTransactions = allTransactions,
                    onEditTransaction = onEditTransaction,
                    onDeleteTransaction = onDeleteTransaction,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (statsTab == "MonthlyReport") {
                MonthlyReportView(
                    allTransactions = allTransactions,
                    onEditTransaction = onEditTransaction,
                    onDeleteTransaction = onDeleteTransaction,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            } else {
                LazyColumn(
                    state = mainListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 190.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                if (statsTab == "Category") {
                    // Visual Donut Chart Card with Liquid Glass
                    item(key = "donut_chart_card") {
                        com.example.ui.components.LiquidGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("donut_chart_card"),
                            shape = ExpressiveCardShape,
                            tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            blurRadius = 20.dp,
                            lensCurvature = 0.42f,
                            specularHighlight = 0.80f,
                            chromaticDispersion = 0.28f,
                            elevation = 8.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Expense Distribution",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                if (categorySpends.isEmpty() || totalExpense <= 0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(140.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No expense data in this period",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.size(180.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Canvas(modifier = Modifier.size(170.dp)) {
                                            val strokeWidth = 26.dp.toPx()
                                            val arcSize = size.width - strokeWidth
                                            var currentAngle = -90f

                                            categorySpends.forEach { item ->
                                                val sweepAngle = item.percentage * 360f
                                                drawArc(
                                                    color = CategoryHelper.getCategoryColor(item.category),
                                                    startAngle = currentAngle,
                                                    sweepAngle = sweepAngle,
                                                    useCenter = false,
                                                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                                    size = Size(arcSize, arcSize),
                                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                                )
                                                currentAngle += sweepAngle
                                            }
                                        }

                                        val donutTotalText = "₹${numberFormat.format(totalExpense)}"
                                        val donutFontSize = when {
                                            donutTotalText.length > 13 -> 9.sp
                                            donutTotalText.length > 11 -> 10.sp
                                            donutTotalText.length > 9 -> 12.sp
                                            else -> 15.sp
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Total Spent",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = donutTotalText,
                                                fontSize = donutFontSize,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = SFMonoFontFamily,
                                                color = ExpenseRed,
                                                textAlign = TextAlign.Center,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }

                                // Quick Payment Mode Glance badge inside chart card
                                if (cashStat != null || cardStat != null) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    val cashGlanceText = "Cash: ₹${numberFormat.format(cashStat?.totalExpense ?: 0.0)}"
                                    val cardGlanceText = "Card: ₹${numberFormat.format(cardStat?.totalExpense ?: 0.0)}"
                                    val glanceStyle = androidx.compose.ui.text.TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = SFMonoFontFamily
                                    )
                                    BoxWithConstraints(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .clickable { statsTab = "PaymentMode" }
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        val glanceColWidth = (maxWidth - 18.dp) / 2
                                        val bothGlanceFit = rememberAllFitColumnWidth(
                                            textsAndStyles = listOf(
                                                cashGlanceText to glanceStyle,
                                                cardGlanceText to glanceStyle
                                            ),
                                            columnWidth = glanceColWidth,
                                            horizontalPaddingPerColumn = 20.dp
                                        )
                                        if (bothGlanceFit) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(cashGlanceText, style = glanceStyle, maxLines = 1, softWrap = false)
                                                }
                                                Box(modifier = Modifier.width(1.dp).height(16.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(cardGlanceText, style = glanceStyle, maxLines = 1, softWrap = false)
                                                }
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(cashGlanceText, style = glanceStyle, maxLines = 1, softWrap = false)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(cardGlanceText, style = glanceStyle, maxLines = 1, softWrap = false)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Overall Period Spending Trend Graph
                    if (overallTrendBars.isNotEmpty()) {
                        item(key = "spending_trend_graph_card") {
                            SpendingTrendBarChart(
                                title = "Spending Trend",
                                subtitle = selectedTimeFilter.label,
                                trendBars = overallTrendBars,
                                totalSpent = totalExpense,
                                barColor = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Quick Stats Insight Badges
                    item(key = "quick_stats_badges") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.TrendingUp,
                                            contentDescription = null,
                                            tint = IncomeGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Savings Rate", style = MaterialTheme.typography.labelMedium)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${"%.1f".format(savingsRate)}%",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = IncomeGreen
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                val topCat = categorySpends.firstOrNull()
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.TrendingDown,
                                            contentDescription = null,
                                            tint = ExpenseRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Top Spending", style = MaterialTheme.typography.labelMedium)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = topCat?.category ?: "None",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Ranked Category Breakdown Header
                    item(key = "category_breakdown_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Category Breakdown (${categorySpends.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tap to view details",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    items(categorySpends, key = { it.category }) { item ->
                        CategorySpendCard(
                            item = item,
                            onClick = {
                                savedStatsScrollIndex = mainListState.firstVisibleItemIndex
                                savedStatsScrollOffset = mainListState.firstVisibleItemScrollOffset
                                onSelectCategory(item.category)
                            }
                        )
                    }
                } else {
                    // ==========================================
                    // CASH VS CREDIT CARD DETAILED VIEW
                    // ==========================================
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Payment Mode Breakdown",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Which expenses were paid with Cash vs. Credit Card",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                // Proportional colored progress bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                ) {
                                    val cashWeight = cashStat?.percentage ?: 0f
                                    val cardWeight = cardStat?.percentage ?: 0f

                                    if (cashWeight > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(cashWeight.coerceAtLeast(0.01f))
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                    if (cardWeight > 0f) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .weight(cardWeight.coerceAtLeast(0.01f))
                                                .background(Color(0xFF8B5CF6))
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // 2 Summary Cards: Cash / UPI vs Credit Card
                                val cashAmtStr = "₹${numberFormat.format(cashStat?.totalExpense ?: 0.0)}"
                                val cardAmtStr = "₹${numberFormat.format(cardStat?.totalExpense ?: 0.0)}"
                                val modeAmtStyle = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SFMonoFontFamily
                                )
                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    val halfCardWidth = (maxWidth - 10.dp) / 2
                                    val bothCardsFit = rememberAllFitColumnWidth(
                                        textsAndStyles = listOf(
                                            cashAmtStr to modeAmtStyle,
                                            cardAmtStr to modeAmtStyle
                                        ),
                                        columnWidth = halfCardWidth,
                                        horizontalPaddingPerColumn = 24.dp
                                    )

                                    val cashCardContent: @Composable (Modifier) -> Unit = { cardModifier ->
                                        Card(
                                            modifier = cardModifier.clickable { selectedPaymentFilter = "Cash / UPI" },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (selectedPaymentFilter == "Cash / UPI") MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Cash / UPI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = cashAmtStr,
                                                    style = modeAmtStyle,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Text(
                                                    text = "${"%.1f".format((cashStat?.percentage ?: 0f) * 100)}% • ${cashStat?.count ?: 0} txs",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    val creditCardContent: @Composable (Modifier) -> Unit = { cardModifier ->
                                        Card(
                                            modifier = cardModifier.clickable { selectedPaymentFilter = "Credit Card" },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (selectedPaymentFilter == "Credit Card") Color(0xFF8B5CF6).copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                                            )
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Credit Card", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = cardAmtStr,
                                                    style = modeAmtStyle,
                                                    color = Color(0xFF8B5CF6),
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Text(
                                                    text = "${"%.1f".format((cardStat?.percentage ?: 0f) * 100)}% • ${cardStat?.count ?: 0} txs",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    if (bothCardsFit) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            cashCardContent(Modifier.weight(1f))
                                            creditCardContent(Modifier.weight(1f))
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            cashCardContent(Modifier.fillMaxWidth())
                                            creditCardContent(Modifier.fillMaxWidth())
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Mode Filter row
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("All", "Cash / UPI", "Credit Card").forEach { m ->
                                val isSelected = selectedPaymentFilter == m
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedPaymentFilter = m },
                                    label = { Text(if (m == "All") "All Expenses" else "$m Only", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (m == "Credit Card") Color(0xFF8B5CF6) else MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    // List of transactions for selected mode
                    val relevantTxs = when (selectedPaymentFilter) {
                        "Cash / UPI" -> cashStat?.transactions ?: emptyList()
                        "Credit Card" -> cardStat?.transactions ?: emptyList()
                        else -> (cashStat?.transactions ?: emptyList()) + (cardStat?.transactions ?: emptyList())
                    }.sortedByDescending { it.date }

                    if (relevantTxs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No expenses found for $selectedPaymentFilter in this period.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                        }
                    } else {
                        items(relevantTxs, key = { it.id }) { tx ->
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
    }

    // Overlay drill-down view when a category is selected, preserving mainListState scroll state!
        if (selectedCategory != null) {
            CategoryDrillDownView(
                categoryName = selectedCategory,
                transactions = categoryTransactions,
                trendBars = categoryTrendBars,
                selectedTimeFilter = selectedTimeFilter,
                periodLabel = periodLabel,
                onBack = { onSelectCategory(null) },
                onTimeFilterSelected = onTimeFilterSelected,
                onPreviousPeriod = onPreviousPeriod,
                onNextPeriod = onNextPeriod,
                onResetToday = onResetToday,
                onEditTransaction = onEditTransaction,
                onDeleteTransaction = onDeleteTransaction,
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            )
        }
    }
}

@Composable
fun CategoryDrillDownView(
    categoryName: String,
    transactions: List<TransactionEntity>,
    trendBars: List<TrendBarData>,
    selectedTimeFilter: TimeFilter,
    periodLabel: String,
    onBack: () -> Unit,
    onTimeFilterSelected: (TimeFilter) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onResetToday: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    val drillDownListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val categoryColor = CategoryHelper.getCategoryColor(categoryName)
    val totalCategorySpent = transactions.sumOf { it.amount }
    val avgTransaction = if (transactions.isNotEmpty()) totalCategorySpent / transactions.size else 0.0

    val subcategoryGroups = remember(transactions) {
        transactions.filter { it.subcategory.isNotBlank() }
            .groupBy { it.subcategory }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val safeDrawingTop = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val effectiveTopPadding = maxOf(statusBarTop, safeDrawingTop, 44.dp)

    Column(modifier = modifier.fillMaxSize()) {
        // Back Navigation Bar in dedicated Surface with guaranteed status bar clearance
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = effectiveTopPadding, bottom = 12.dp, start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("back_from_category_drilldown")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Stats")
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryHelper.getCategoryIcon(categoryName),
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total: ₹${numberFormat.format(totalCategorySpent)} • ${transactions.size} records",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = SFMonoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Time Filter Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TimeFilter.values().forEach { filter ->
                val isSelected = filter == selectedTimeFilter
                FilterChip(
                    selected = isSelected,
                    onClick = { onTimeFilterSelected(filter) },
                    label = { Text(filter.label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        // Period Navigator for Category Drill-Down
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPreviousPeriod, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Period", modifier = Modifier.size(18.dp))
                }
                Text(
                    text = if (selectedTimeFilter == TimeFilter.ALL) "All Time History" else periodLabel,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                if (selectedTimeFilter != TimeFilter.ALL) {
                    IconButton(onClick = onNextPeriod, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Period", modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (selectedTimeFilter != TimeFilter.ALL) {
                Text(
                    text = "Today",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onResetToday() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        LazyColumn(
            state = drillDownListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 190.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Trend Graphical Representation (Bar Graph)
            item(key = "category_trend_chart_card") {
                SpendingTrendBarChart(
                    title = "Spending Trend",
                    subtitle = selectedTimeFilter.label,
                    trendBars = trendBars,
                    totalSpent = totalCategorySpent,
                    barColor = categoryColor
                )
            }

            // Category Details Summary Cards
            item {
                val avgTxText = "₹${numberFormat.format(avgTransaction)}"
                val avgTxStyle = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = SFMonoFontFamily
                )
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val halfCardWidth = (maxWidth - 10.dp) / 2
                    val fitsTwoCols = rememberAllFitColumnWidth(
                        textsAndStyles = listOf(avgTxText to avgTxStyle),
                        columnWidth = halfCardWidth,
                        horizontalPaddingPerColumn = 24.dp
                    )
                    if (fitsTwoCols) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Total Records", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${transactions.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = SFMonoFontFamily, maxLines = 1, softWrap = false)
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Avg Expense", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(avgTxText, style = avgTxStyle, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Total Records", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${transactions.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = SFMonoFontFamily, maxLines = 1, softWrap = false)
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Avg Expense", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(avgTxText, style = avgTxStyle, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }

            // Subcategory Breakdown if any subcategories exist (e.g. Petrol -> Bike vs Car)
            if (subcategoryGroups.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Subcategory Breakdown",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                subcategoryGroups.forEach { (subcat, amt) ->
                                    val pct = if (totalCategorySpent > 0) ((amt / totalCategorySpent) * 100).toInt() else 0
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Column {
                                            Text(subcat, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("₹${numberFormat.format(amt)} ($pct%)", fontSize = 11.sp, color = categoryColor)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Transactions Header
            item {
                Text(
                    text = "All Records under $categoryName",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // List of transactions under this category
            if (transactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No transactions found for this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(transactions, key = { "cat_tx_${it.id}" }) { tx ->
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

@Composable
fun CategorySpendCard(
    item: CategorySpend,
    onClick: () -> Unit = {}
) {
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    val color = CategoryHelper.getCategoryColor(item.category)

    com.example.ui.components.LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("stat_category_${item.category}"),
        shape = ExpressiveCardShape,
        tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
        blurRadius = 14.dp,
        lensCurvature = 0.35f,
        specularHighlight = 0.70f,
        chromaticDispersion = 0.22f,
        borderAlpha = 0.38f,
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            val catAmountText = "₹${numberFormat.format(item.amount)}"
            val catAmountStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = SFMonoFontFamily
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = CategoryHelper.getCategoryIcon(item.category),
                        contentDescription = item.category,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))

                BoxWithConstraints(modifier = Modifier.weight(1f)) {
                    val fitsInline = rememberAllFitColumnWidth(
                        textsAndStyles = listOf(catAmountText to catAmountStyle),
                        columnWidth = (maxWidth - 132.dp).coerceAtLeast(0.dp),
                        horizontalPaddingPerColumn = 0.dp
                    )
                    if (fitsInline) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.category,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.count} items • ${"%.1f".format(item.percentage * 100)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = catAmountText,
                                    style = catAmountStyle,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.count} items • ${"%.1f".format(item.percentage * 100)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Details",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = catAmountText,
                                style = catAmountStyle,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Percentage Bar with rounded ends
            LinearProgressIndicator(
                progress = { item.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpendingTrendBarChart(
    title: String,
    subtitle: String = "",
    trendBars: List<TrendBarData>,
    totalSpent: Double,
    barColor: Color = MaterialTheme.colorScheme.primary,
    onBarClick: ((TrendBarData) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    var activeBar by remember(trendBars) { mutableStateOf(trendBars.maxByOrNull { it.amount }) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spending_trend_graph_card"),
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Title + Period and Total Spent
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
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = barColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (subtitle.isNotEmpty()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Text(
                    text = "₹${numberFormat.format(totalSpent)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = SFMonoFontFamily,
                    color = ExpenseRed,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expressive Tooltip Banner for active bar
            if (activeBar != null && activeBar!!.amount > 0) {
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Text(
                        text = "${activeBar!!.label}: ₹${numberFormat.format(activeBar!!.amount)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SFMonoFontFamily,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            } else {
                Text(
                    text = "Tap any bar to inspect amount",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (trendBars.isEmpty() || totalSpent <= 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No spending recorded in this period",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Horizontal scroll container with rounded-top vertical progress tracks
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    trendBars.forEach { bar ->
                        val isSelected = activeBar?.label == bar.label

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .widthIn(min = 48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    activeBar = bar
                                    onBarClick?.invoke(bar)
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            // Vertical Track Area with guaranteed non-clipping
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(96.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                // Background guide track
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(96.dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
                                )

                                // Active progress track
                                val barHeight = (88 * bar.heightRatio).dp.coerceAtLeast(if (bar.amount > 0) 8.dp else 0.dp)
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else if (bar.amount > 0) barColor.copy(alpha = 0.75f)
                                            else Color.Transparent
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Date Label
                            Text(
                                text = bar.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

