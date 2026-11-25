package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.BudgetProgress
import com.example.ui.TimeFilter
import com.example.ui.components.CategoryHelper
import com.example.ui.components.PeriodOverviewCard
import com.example.ui.components.PeriodTimeFilterBar
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpressiveButtonShape
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.ExpressiveChipShape
import com.example.ui.theme.ExpressiveDialogShape
import com.example.ui.theme.ExpressivePillButtonShape
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SFMonoFontFamily
import com.example.ui.theme.WarningAmber
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetsScreen(
    budgetProgressList: List<BudgetProgress>,
    totalExpense: Double,
    totalIncome: Double,
    selectedTimeFilter: TimeFilter,
    periodLabel: String,
    onTimeFilterSelected: (TimeFilter) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onResetToday: () -> Unit,
    onSaveBudget: (id: Long, category: String, limit: Double, period: String) -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit,
    onEditTransaction: ((TransactionEntity) -> Unit)? = null,
    onDeleteTransaction: ((TransactionEntity) -> Unit)? = null,
    allCategories: List<CategoryEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetEntity?>(null) }
    var selectedBudgetCategory by remember { mutableStateOf<String?>(null) }
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }

    val totalBudgetLimit = budgetProgressList.sumOf { it.effectiveLimit }
    val totalBudgetSpent = budgetProgressList.sumOf { it.spent }
    val overBudgetCount = budgetProgressList.count { it.isOverBudget }
    val budgetListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("budgets_screen")
        ) {
            // Pinned Time Filter Pills
            PeriodTimeFilterBar(
                selectedTimeFilter = selectedTimeFilter,
                onTimeFilterSelected = onTimeFilterSelected
            )

            LazyColumn(
                state = budgetListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 190.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "period_overview_card") {
                    PeriodOverviewCard(
                        selectedTimeFilter = selectedTimeFilter,
                        periodLabel = periodLabel,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        onPrevious = onPreviousPeriod,
                        onNext = onNextPeriod,
                        onResetToday = onResetToday
                    )
                }

                // Budget Overview Card with Liquid Glass
                item(key = "budget_overview_card") {
                    com.example.ui.components.LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("budget_overview_card"),
                        shape = RoundedCornerShape(22.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        blurRadius = 20.dp,
                        lensCurvature = 0.42f,
                        specularHighlight = 0.80f,
                        chromaticDispersion = 0.28f,
                        elevation = 8.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (selectedTimeFilter == TimeFilter.YEAR) "Yearly Budgets" else "Category Budgets",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = periodLabel,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Button(
                                    onClick = {
                                        editingBudget = null
                                        showAddDialog = true
                                    },
                                    shape = ExpressivePillButtonShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("add_budget_top_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val limitStr = "₹${numberFormat.format(totalBudgetLimit)}"
                            val spentStr = "₹${numberFormat.format(totalBudgetSpent)}"
                            val statusStr = if (overBudgetCount > 0) "$overBudgetCount Over Limit" else "Within Budget"
                            val overviewAmountStyle = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = SFMonoFontFamily
                            )
                            val overviewStatusStyle = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            )
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val fitsInThreeCols = com.example.ui.components.rememberAllFitColumnWidth(
                                    textsAndStyles = listOf(
                                        limitStr to overviewAmountStyle,
                                        spentStr to overviewAmountStyle,
                                        statusStr to overviewStatusStyle
                                    ),
                                    columnWidth = (maxWidth - 16.dp) / 3,
                                    horizontalPaddingPerColumn = 2.dp
                                )
                                if (fitsInThreeCols) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Total Limit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(limitStr, style = overviewAmountStyle, maxLines = 1, softWrap = false)
                                        }
                                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Total Spent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(spentStr, style = overviewAmountStyle, color = ExpenseRed, maxLines = 1, softWrap = false, textAlign = TextAlign.Center)
                                        }
                                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                            Text("Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = statusStr,
                                                style = overviewStatusStyle,
                                                color = if (overBudgetCount > 0) ExpenseRed else IncomeGreen,
                                                maxLines = 1,
                                                softWrap = false,
                                                textAlign = TextAlign.End
                                            )
                                        }
                                    }
                                } else {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column {
                                            Text("Total Limit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(limitStr, style = overviewAmountStyle, maxLines = 1, softWrap = false)
                                        }
                                        Column {
                                            Text("Total Spent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(spentStr, style = overviewAmountStyle, color = ExpenseRed, maxLines = 1, softWrap = false)
                                        }
                                        Column {
                                            Text("Status", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = statusStr,
                                                style = overviewStatusStyle,
                                                color = if (overBudgetCount > 0) ExpenseRed else IncomeGreen,
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

                // Section Label
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Category Budgets (${budgetProgressList.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Budget Items
                items(budgetProgressList, key = { it.budget.id }) { item ->
                    BudgetProgressCard(
                        item = item,
                        onClick = { selectedBudgetCategory = item.budget.category },
                        onEdit = { editingBudget = item.budget },
                        onDelete = { onDeleteBudget(item.budget) }
                    )
                }
            }
        }

        // Category budget drill-down view overlaid on top so LazyColumn is not destroyed, preserving scroll position!
        if (selectedBudgetCategory != null) {
            val selectedItem = budgetProgressList.find { it.budget.category == selectedBudgetCategory }
            if (selectedItem != null) {
                BudgetTransactionsDrillDownView(
                    budgetProgress = selectedItem,
                    periodLabel = periodLabel,
                    onBack = { selectedBudgetCategory = null },
                    onEditBudget = { editingBudget = selectedItem.budget },
                    onEditTransaction = onEditTransaction,
                    onDeleteTransaction = onDeleteTransaction,
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                )
            }
        }

        // Add or Edit Budget Dialog
        if (showAddDialog || editingBudget != null) {
            AddEditBudgetDialog(
                budgetToEdit = editingBudget,
                categoriesList = allCategories,
                onDismiss = {
                    showAddDialog = false
                    editingBudget = null
                },
                onSave = { id, cat, limit, period ->
                    onSaveBudget(id, cat, limit, period)
                    showAddDialog = false
                    editingBudget = null
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetProgressCard(
    item: BudgetProgress,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    val categoryColor = CategoryHelper.getCategoryColor(item.budget.category)
    val pctUsed = (item.progress * 100).toInt()

    val progressColor = when {
        item.isOverBudget -> ExpenseRed
        item.progress > 0.8f -> WarningAmber
        else -> IncomeGreen
    }

    com.example.ui.components.LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("budget_card_${item.budget.category}"),
        shape = ExpressiveCardShape,
        tint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f),
        blurRadius = 14.dp,
        lensCurvature = 0.35f,
        specularHighlight = 0.70f,
        chromaticDispersion = 0.22f,
        borderAlpha = 0.38f,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Row: Category Icon, Name, Overspent Badge and Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryHelper.getCategoryIcon(item.budget.category),
                            contentDescription = item.budget.category,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = item.budget.category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        val limitLabel = if (item.effectiveLimit != item.budget.amountLimit) {
                            "${item.budget.period}: ₹${numberFormat.format(item.budget.amountLimit)} • Limit: ₹${numberFormat.format(item.effectiveLimit)}"
                        } else {
                            "${item.budget.period} limit: ₹${numberFormat.format(item.budget.amountLimit)}"
                        }
                        Text(
                            text = limitLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = SFMonoFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (item.isOverBudget) {
                        Surface(
                            shape = ExpressiveChipShape,
                            color = ExpenseRed.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Overspent",
                                    color = ExpenseRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_budget_${item.budget.id}")
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Budget",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_budget_${item.budget.id}")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "View Transactions",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { item.progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Spent vs Limit and Remaining (pushes remaining to next line intact if space is tight)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Spent: ₹${numberFormat.format(item.spent)} ($pctUsed%)",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = SFMonoFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )

                Text(
                    text = if (item.isOverBudget) {
                        val over = item.spent - item.effectiveLimit
                        "+₹${numberFormat.format(over)} over"
                    } else {
                        "₹${numberFormat.format(item.remaining)} left"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = SFMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isOverBudget) ExpenseRed else IncomeGreen,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetTransactionsDrillDownView(
    budgetProgress: BudgetProgress,
    periodLabel: String,
    onBack: () -> Unit,
    onEditBudget: () -> Unit,
    onEditTransaction: ((TransactionEntity) -> Unit)? = null,
    onDeleteTransaction: ((TransactionEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.US) }
    val categoryColor = CategoryHelper.getCategoryColor(budgetProgress.budget.category)
    val pctUsed = (budgetProgress.progress * 100).toInt()

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val safeDrawingTop = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val effectiveTopPadding = maxOf(statusBarTop, safeDrawingTop, 44.dp)

    Column(modifier = modifier.fillMaxSize().testTag("budget_drilldown_view")) {
        // Back navigation header in dedicated Surface with guaranteed status bar clearance
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = effectiveTopPadding, bottom = 12.dp, start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("back_from_budget_drilldown")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Budgets")
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = CategoryHelper.getCategoryIcon(budgetProgress.budget.category),
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "${budgetProgress.budget.category} Budget",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$periodLabel • ${budgetProgress.transactions.size} records",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onEditBudget,
                    shape = ExpressivePillButtonShape,
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("edit_budget_drilldown_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 190.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Budget Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val effLimitStr = "₹${numberFormat.format(budgetProgress.effectiveLimit)}"
                        val spentAmtStr = "₹${numberFormat.format(budgetProgress.spent)}"
                        val remAmtStr = if (budgetProgress.isOverBudget) "+₹${numberFormat.format(budgetProgress.spent - budgetProgress.effectiveLimit)}" else "₹${numberFormat.format(budgetProgress.remaining)}"
                        val drillTitleStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = SFMonoFontFamily)
                        val drillRemStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFamily = SFMonoFontFamily)

                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val fitsInThreeCols = com.example.ui.components.rememberAllFitColumnWidth(
                                textsAndStyles = listOf(
                                    effLimitStr to drillTitleStyle,
                                    spentAmtStr to drillTitleStyle,
                                    remAmtStr to drillRemStyle
                                ),
                                columnWidth = (maxWidth - 12.dp) / 3,
                                horizontalPaddingPerColumn = 2.dp
                            )
                            if (fitsInThreeCols) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (budgetProgress.effectiveLimit != budgetProgress.budget.amountLimit) "Limit ($periodLabel)" else "Budget Limit",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(effLimitStr, style = drillTitleStyle, maxLines = 1, softWrap = false)
                                        if (budgetProgress.effectiveLimit != budgetProgress.budget.amountLimit) {
                                            Text(
                                                text = "Base: ₹${numberFormat.format(budgetProgress.budget.amountLimit)} / ${budgetProgress.budget.period.lowercase()}",
                                                fontSize = 10.sp,
                                                fontFamily = SFMonoFontFamily,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Spent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(spentAmtStr, style = drillTitleStyle, color = ExpenseRed, maxLines = 1, softWrap = false, textAlign = TextAlign.Center)
                                        Text(
                                            text = "$pctUsed% used",
                                            fontSize = 10.sp,
                                            fontFamily = SFMonoFontFamily,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                        Text(if (budgetProgress.isOverBudget) "Overspent" else "Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = remAmtStr,
                                            style = drillRemStyle,
                                            color = if (budgetProgress.isOverBudget) ExpenseRed else IncomeGreen,
                                            maxLines = 1,
                                            softWrap = false,
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = if (budgetProgress.effectiveLimit != budgetProgress.budget.amountLimit) "Limit ($periodLabel)" else "Budget Limit",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(effLimitStr, style = drillTitleStyle, maxLines = 1, softWrap = false)
                                        if (budgetProgress.effectiveLimit != budgetProgress.budget.amountLimit) {
                                            Text(
                                                text = "Base: ₹${numberFormat.format(budgetProgress.budget.amountLimit)} / ${budgetProgress.budget.period.lowercase()}",
                                                fontSize = 10.sp,
                                                fontFamily = SFMonoFontFamily,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Column {
                                        Text("Spent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(spentAmtStr, style = drillTitleStyle, color = ExpenseRed, maxLines = 1, softWrap = false)
                                        Text(
                                            text = "$pctUsed% used",
                                            fontSize = 10.sp,
                                            fontFamily = SFMonoFontFamily,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column {
                                        Text(if (budgetProgress.isOverBudget) "Overspent" else "Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = remAmtStr,
                                            style = drillRemStyle,
                                            color = if (budgetProgress.isOverBudget) ExpenseRed else IncomeGreen,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { budgetProgress.progress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = if (budgetProgress.isOverBudget) ExpenseRed else if (budgetProgress.progress > 0.8f) WarningAmber else IncomeGreen,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    }
                }
            }

            // Budget Spending Trend Graph Card
            item(key = "budget_trend_graph_card") {
                BudgetTrendingGraphCard(
                    budgetProgress = budgetProgress,
                    periodLabel = periodLabel
                )
            }

            item {
                Text(
                    text = "Transactions (${budgetProgress.transactions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (budgetProgress.transactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                "No transactions recorded for ${budgetProgress.budget.category} in $periodLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(budgetProgress.transactions, key = { it.id }) { tx ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onEditTransaction?.invoke(tx) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(categoryColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CategoryHelper.getCategoryIcon(tx.category),
                                    contentDescription = null,
                                    tint = categoryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(end = 8.dp)) {
                                    Text(
                                        text = if (tx.subcategory.isNotBlank()) "${tx.category} • ${tx.subcategory}" else tx.category,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (tx.memo.isNotBlank()) {
                                        Text(text = tx.memo, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = dateFormat.format(Date(tx.date)),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${tx.wallet}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "-₹${numberFormat.format(tx.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        fontFamily = SFMonoFontFamily,
                                        color = ExpenseRed,
                                        maxLines = 1,
                                        softWrap = false
                                    )

                                    if (onDeleteTransaction != null) {
                                        IconButton(
                                            onClick = { onDeleteTransaction(tx) },
                                            modifier = Modifier.size(32.dp).padding(start = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                modifier = Modifier.size(16.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetTrendingGraphCard(
    budgetProgress: BudgetProgress,
    periodLabel: String,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    val categoryColor = CategoryHelper.getCategoryColor(budgetProgress.budget.category)
    val transactions = remember(budgetProgress.transactions) {
        budgetProgress.transactions.sortedBy { it.date }
    }

    val effectiveLimit = budgetProgress.effectiveLimit
    val totalSpent = budgetProgress.spent

    val (points, maxGraphVal) = remember(transactions, effectiveLimit) {
        if (transactions.isEmpty()) {
            Pair(emptyList<Pair<Float, Double>>(), effectiveLimit.coerceAtLeast(100.0))
        } else {
            val minDate = transactions.first().date
            val maxDate = maxOf(transactions.last().date, minDate + 86400000L)
            val duration = (maxDate - minDate).coerceAtLeast(86400000L).toDouble()

            var cumulative = 0.0
            val pts = mutableListOf<Pair<Float, Double>>()
            pts.add(Pair(0f, 0.0))
            transactions.forEach { tx ->
                cumulative += tx.amount
                val normalizedX = ((tx.date - minDate) / duration).toFloat().coerceIn(0f, 1f)
                pts.add(Pair(normalizedX, cumulative))
            }
            val peak = maxOf(cumulative, effectiveLimit) * 1.15
            Pair(pts, peak)
        }
    }

    val daysInPeriod = when (budgetProgress.budget.period.lowercase()) {
        "yearly" -> 365
        "quarterly" -> 90
        "weekly" -> 7
        else -> 30
    }
    val daysElapsed = remember(transactions) {
        if (transactions.isEmpty()) 1
        else {
            val minD = transactions.first().date
            val maxD = transactions.last().date
            val diff = ((maxD - minD) / (1000 * 60 * 60 * 24)).toInt() + 1
            diff.coerceIn(1, daysInPeriod)
        }
    }
    val dailyBurn = if (daysElapsed > 0) totalSpent / daysElapsed else 0.0
    val targetDailyBurn = effectiveLimit / daysInPeriod
    val isPacingFast = dailyBurn > targetDailyBurn
    val projectedPeriodSpend = dailyBurn * daysInPeriod

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("budget_trending_graph_card"),
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Budget Trending & Burn Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cumulative spend vs budget limit line",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = ExpressiveChipShape,
                    color = if (budgetProgress.isOverBudget) ExpenseRed.copy(alpha = 0.15f)
                    else if (isPacingFast) WarningAmber.copy(alpha = 0.15f)
                    else IncomeGreen.copy(alpha = 0.15f),
                    border = BorderStroke(
                        1.dp,
                        if (budgetProgress.isOverBudget) ExpenseRed.copy(alpha = 0.4f)
                        else if (isPacingFast) WarningAmber.copy(alpha = 0.4f)
                        else IncomeGreen.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (budgetProgress.isOverBudget) "Over Limit"
                        else if (isPacingFast) "Exceeding Pace"
                        else "On Pace",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (budgetProgress.isOverBudget) ExpenseRed
                        else if (isPacingFast) WarningAmber
                        else IncomeGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Line & Area Chart
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No spending recorded yet to calculate trend",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val strokeWidth = 3.dp.toPx()

                        // Budget Limit horizontal dashed line
                        val limitY = canvasHeight * (1f - (effectiveLimit / maxGraphVal).toFloat().coerceIn(0.05f, 0.95f))
                        drawLine(
                            color = ExpenseRed.copy(alpha = 0.75f),
                            start = Offset(0f, limitY),
                            end = Offset(canvasWidth, limitY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                        )

                        // Ideal Pace dashed line (0, height) to (width, limitY)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.35f),
                            start = Offset(0f, canvasHeight),
                            end = Offset(canvasWidth, limitY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                        )

                        // Cumulative curve
                        val path = Path()
                        val fillPath = Path()
                        fillPath.moveTo(0f, canvasHeight)

                        points.forEachIndexed { i, (normX, cumSpent) ->
                            val x = normX * canvasWidth
                            val y = canvasHeight * (1f - (cumSpent / maxGraphVal).toFloat().coerceIn(0f, 1f))
                            if (i == 0) {
                                path.moveTo(x, y)
                                fillPath.lineTo(x, y)
                            } else {
                                path.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                        }

                        val lastX = (points.lastOrNull()?.first ?: 1f) * canvasWidth
                        fillPath.lineTo(lastX, canvasHeight)
                        fillPath.close()

                        // Draw filled gradient area
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(categoryColor.copy(alpha = 0.35f), Color.Transparent),
                                startY = 0f,
                                endY = canvasHeight
                            )
                        )

                        // Draw line
                        drawPath(
                            path = path,
                            color = categoryColor,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Draw dots for transactions
                        points.drop(1).forEach { (normX, cumSpent) ->
                            val dotX = normX * canvasWidth
                            val dotY = canvasHeight * (1f - (cumSpent / maxGraphVal).toFloat().coerceIn(0f, 1f))
                            drawCircle(
                                color = categoryColor,
                                radius = 4.5.dp.toPx(),
                                center = Offset(dotX, dotY)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.dp.toPx(),
                                center = Offset(dotX, dotY)
                            )
                        }
                    }
                }

                // Legend and labels
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(categoryColor, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Actual Spend", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(14.dp).height(2.dp).background(ExpenseRed))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limit: ₹${numberFormat.format(effectiveLimit)}", fontSize = 10.sp, fontFamily = SFMonoFontFamily, color = ExpenseRed, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.width(14.dp).height(2.dp).background(Color.Gray))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pace", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Burn Rate Metrics Row
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                val dailyBurnStr = "₹${numberFormat.format(dailyBurn)}/day"
                val targetBurnStr = "₹${numberFormat.format(targetDailyBurn)}/day"
                val projectedStr = "₹${numberFormat.format(projectedPeriodSpend)}"
                val burnStyle = androidx.compose.ui.text.TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontFamily = SFMonoFontFamily,
                    fontSize = 13.sp
                )

                BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    val fitsInThreeCols = com.example.ui.components.rememberAllFitColumnWidth(
                        textsAndStyles = listOf(
                            dailyBurnStr to burnStyle,
                            targetBurnStr to burnStyle,
                            projectedStr to burnStyle
                        ),
                        columnWidth = (maxWidth - 12.dp) / 3,
                        horizontalPaddingPerColumn = 2.dp
                    )
                    if (fitsInThreeCols) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Burn Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = dailyBurnStr,
                                    style = burnStyle,
                                    color = if (isPacingFast) WarningAmber else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Budgeted Pace", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                Text(
                                    text = targetBurnStr,
                                    style = burnStyle,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Projected Spend", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                                Text(
                                    text = projectedStr,
                                    style = burnStyle,
                                    color = if (projectedPeriodSpend > effectiveLimit) ExpenseRed else IncomeGreen,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column {
                                Text("Burn Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = dailyBurnStr,
                                    style = burnStyle,
                                    color = if (isPacingFast) WarningAmber else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Column {
                                Text("Budgeted Pace", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = targetBurnStr,
                                    style = burnStyle,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Column {
                                Text("Projected Spend", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = projectedStr,
                                    style = burnStyle,
                                    color = if (projectedPeriodSpend > effectiveLimit) ExpenseRed else IncomeGreen,
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

@Composable
fun AddEditBudgetDialog(
    budgetToEdit: BudgetEntity? = null,
    categoriesList: List<CategoryEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (id: Long, category: String, limit: Double, period: String) -> Unit
) {
    var selectedCategory by remember(budgetToEdit) { mutableStateOf(budgetToEdit?.category ?: "Petrol") }
    var limitText by remember(budgetToEdit) {
        mutableStateOf(budgetToEdit?.amountLimit?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "6000")
    }
    var selectedPeriod by remember(budgetToEdit) { mutableStateOf(budgetToEdit?.period ?: "Monthly") }

    var categoryDropdownOpen by remember { mutableStateOf(false) }
    var categorySearchQuery by remember { mutableStateOf("") }

    val categories = remember(categoriesList) {
        val fromDb = categoriesList.filter { it.type.equals("Expense", ignoreCase = true) }.map { it.name }
        (fromDb + CategoryHelper.commonExpenseCategories).distinct().sorted()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = ExpressiveDialogShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("add_budget_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (budgetToEdit != null) "Edit Category Budget" else "Set Category Budget",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Select Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            IconButton(onClick = { categoryDropdownOpen = !categoryDropdownOpen }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose Category")
                            }
                        },
                        shape = ExpressiveButtonShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(ExpressiveButtonShape)
                            .clickable { categoryDropdownOpen = true }
                    )
                    DropdownMenu(
                        expanded = categoryDropdownOpen,
                        onDismissRequest = {
                            categoryDropdownOpen = false
                            categorySearchQuery = ""
                        },
                        shape = ExpressiveDialogShape,
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .heightIn(max = 340.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        // Search bar inside Dropdown (BasicTextField so typed text & placeholder never clip)
                        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 42.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                        if (categorySearchQuery.isEmpty()) {
                                            Text(
                                                text = "Search category...",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                maxLines = 1
                                            )
                                        }
                                        BasicTextField(
                                            value = categorySearchQuery,
                                            onValueChange = { categorySearchQuery = it },
                                            singleLine = true,
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            ),
                                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("budget_category_search_input")
                                        )
                                    }
                                    if (categorySearchQuery.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { categorySearchQuery = "" },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "Clear search",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )

                        val filteredCategories = categories.filter {
                            it.contains(categorySearchQuery, ignoreCase = true)
                        }

                        if (filteredCategories.isEmpty()) {
                            Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Text("No categories found", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            filteredCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = CategoryHelper.getCategoryIcon(cat),
                                                contentDescription = null,
                                                tint = CategoryHelper.getCategoryColor(cat),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(cat, fontWeight = if (cat == selectedCategory) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = cat
                                        categoryDropdownOpen = false
                                        categorySearchQuery = ""
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = cat == selectedCategory,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            shape = ExpressiveChipShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Budget Limit (₹)") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    placeholder = { Text("e.g. 6000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_amount_input"),
                    shape = ExpressiveButtonShape
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Period", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Monthly", "Quarterly", "Yearly").forEach { p ->
                        FilterChip(
                            selected = p == selectedPeriod,
                            onClick = { selectedPeriod = p },
                            label = { Text(p) },
                            shape = ExpressiveChipShape
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val limit = limitText.toDoubleOrNull() ?: 0.0
                        if (limit > 0) {
                            onSave(budgetToEdit?.id ?: 0L, selectedCategory, limit, selectedPeriod)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("save_budget_button"),
                    shape = ExpressiveButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (budgetToEdit != null) "Update Budget" else "Save Budget",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
