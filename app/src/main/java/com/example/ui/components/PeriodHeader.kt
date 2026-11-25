package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TimeFilter
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpressiveChipShape
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SFMonoFontFamily
import java.text.DecimalFormat
import java.util.Locale

@Composable
fun PeriodHeader(
    selectedTimeFilter: TimeFilter,
    periodLabel: String,
    totalIncome: Double,
    totalExpense: Double,
    onTimeFilterSelected: (TimeFilter) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onResetToday: () -> Unit,
    onOpenCalendar: (() -> Unit)? = null,
    includeStatusBarPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        PeriodTimeFilterBar(
            selectedTimeFilter = selectedTimeFilter,
            onTimeFilterSelected = onTimeFilterSelected,
            includeStatusBarPadding = includeStatusBarPadding
        )
        PeriodOverviewCard(
            selectedTimeFilter = selectedTimeFilter,
            periodLabel = periodLabel,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            onPrevious = onPrevious,
            onNext = onNext,
            onResetToday = onResetToday,
            onOpenCalendar = onOpenCalendar,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PeriodTimeFilterBar(
    selectedTimeFilter: TimeFilter,
    onTimeFilterSelected: (TimeFilter) -> Unit,
    includeStatusBarPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val safeDrawingTop = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val effectiveTopPadding = if (includeStatusBarPadding) maxOf(statusBarTop, safeDrawingTop, 36.dp) else 0.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (includeStatusBarPadding) {
                    Modifier.background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.09f),
                                Color.Transparent
                            )
                        )
                    )
                } else {
                    Modifier
                }
            )
            .padding(top = effectiveTopPadding)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeFilter.values().forEach { filter ->
            val isSelected = filter == selectedTimeFilter
            FilterChip(
                selected = isSelected,
                onClick = { onTimeFilterSelected(filter) },
                label = {
                    Text(
                        text = filter.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        softWrap = false
                    )
                },
                shape = ExpressiveChipShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
            )
        }
    }
}

@Composable
fun PeriodOverviewCard(
    selectedTimeFilter: TimeFilter,
    periodLabel: String,
    totalIncome: Double,
    totalExpense: Double,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onResetToday: () -> Unit,
    onOpenCalendar: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    val incomeFormatted = "₹${numberFormat.format(totalIncome)}"
    val expenseFormatted = "₹${numberFormat.format(totalExpense)}"

    val totalFlow = totalIncome + totalExpense
    val incomePct = if (totalFlow > 0.0) (totalIncome / totalFlow) * 100.0 else 0.0
    val expensePct = if (totalFlow > 0.0) (totalExpense / totalFlow) * 100.0 else 0.0

    val ratioPrimaryText = when {
        totalIncome > 0.0 && totalExpense > 0.0 -> "${String.format(Locale.US, "%.1f", totalExpense / totalIncome)}×"
        totalExpense > 0.0 && totalIncome == 0.0 -> "100%"
        totalIncome > 0.0 && totalExpense == 0.0 -> "0.0×"
        else -> "0.0×"
    }
    val ratioSubText = when {
        totalFlow <= 0.0 -> "No Data"
        totalIncome == 0.0 -> "Expense"
        else -> "vs Inc"
    }

    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val trackRingColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Compact Period Navigator (only active when not ALL)
        if (selectedTimeFilter != TimeFilter.ALL) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("prev_period_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Period",
                        tint = secondaryTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedTimeFilter == TimeFilter.DAY && onOpenCalendar != null) {
                        IconButton(
                            onClick = onOpenCalendar,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("calendar_picker_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Open Calendar",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                    }

                    Text(
                        text = periodLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor,
                        modifier = if (selectedTimeFilter == TimeFilter.DAY && onOpenCalendar != null) {
                            Modifier.clickable { onOpenCalendar() }
                        } else Modifier
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onResetToday,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("today_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Today",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onNext,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("next_period_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Period",
                        tint = secondaryTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Single Compact Liquid Glass Ratio & Income/Expense Card (no duplicate data)
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("summary_card"),
            shape = RoundedCornerShape(22.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            blurRadius = 20.dp,
            lensCurvature = 0.42f,
            specularHighlight = 0.80f,
            chromaticDispersion = 0.28f,
            elevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expense vs Income Ratio Donut Ring
                Box(
                    modifier = Modifier.size(62.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(62.dp)) {
                        val strokeWidth = 8.dp.toPx()
                        val inset = strokeWidth / 2f
                        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val topLeft = Offset(inset, inset)

                        if (totalFlow <= 0.0) {
                            drawArc(
                                color = trackRingColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth)
                            )
                        } else {
                            val incomeSweep = ((totalIncome / totalFlow) * 360.0).toFloat()
                            val expenseSweep = 360f - incomeSweep

                            if (incomeSweep > 0f) {
                                drawArc(
                                    color = IncomeGreen,
                                    startAngle = -90f,
                                    sweepAngle = incomeSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                            }
                            if (expenseSweep > 0f) {
                                drawArc(
                                    color = ExpenseRed,
                                    startAngle = -90f + incomeSweep,
                                    sweepAngle = expenseSweep,
                                    useCenter = false,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = ratioPrimaryText,
                            fontFamily = SFMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            lineHeight = 14.sp,
                            color = primaryTextColor,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = ratioSubText,
                            fontSize = 8.5.sp,
                            lineHeight = 9.5.sp,
                            color = secondaryTextColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .background(dividerColor)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RatioLegendRow(
                        dotColor = IncomeGreen,
                        label = "Income",
                        amountText = incomeFormatted,
                        percentText = "(${String.format(Locale.US, "%.1f", incomePct)}%)",
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )

                    HorizontalDivider(
                        thickness = 1.dp,
                        color = dividerColor
                    )

                    RatioLegendRow(
                        dotColor = ExpenseRed,
                        label = "Expense",
                        amountText = expenseFormatted,
                        percentText = "(${String.format(Locale.US, "%.1f", expensePct)}%)",
                        primaryTextColor = primaryTextColor,
                        secondaryTextColor = secondaryTextColor
                    )
                }
            }
        }
    }
}

@Composable
private fun RatioLegendRow(
    dotColor: Color,
    label: String,
    amountText: String,
    percentText: String,
    primaryTextColor: Color,
    secondaryTextColor: Color
) {
    val amountStyle = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = SFMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.5.sp,
        letterSpacing = (-0.2).sp
    )
    val percentStyle = MaterialTheme.typography.bodySmall.copy(
        fontFamily = SFMonoFontFamily,
        fontSize = 11.sp,
        letterSpacing = (-0.2).sp
    )

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val fitsInline = rememberAllFitColumnWidth(
            textsAndStyles = listOf(
                "$amountText $percentText" to amountStyle
            ),
            columnWidth = (maxWidth - 68.dp).coerceAtLeast(1.dp),
            horizontalPaddingPerColumn = 2.dp
        )

        if (fitsInline) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = label,
                        fontSize = 12.5.sp,
                        color = primaryTextColor.copy(alpha = 0.9f),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = amountText,
                        style = amountStyle,
                        color = primaryTextColor,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = percentText,
                        style = percentStyle,
                        color = secondaryTextColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Text(
                            text = label,
                            fontSize = 12.5.sp,
                            color = primaryTextColor.copy(alpha = 0.9f),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Text(
                        text = percentText,
                        style = percentStyle,
                        color = secondaryTextColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Text(
                    text = amountText,
                    style = amountStyle,
                    color = primaryTextColor,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(start = 15.dp)
                )
            }
        }
    }
}
