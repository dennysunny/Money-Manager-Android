package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.ui.TimeFilter
import com.example.ui.components.CategoryHelper
import com.example.ui.components.PeriodOverviewCard
import com.example.ui.components.PeriodTimeFilterBar
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpressiveButtonShape
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.ExpressiveChipShape
import com.example.ui.theme.IncomeGreen
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LedgerScreen(
    transactions: List<TransactionEntity>,
    totalExpense: Double,
    totalIncome: Double,
    selectedTimeFilter: TimeFilter,
    periodLabel: String,
    searchQuery: String,
    typeFilter: String,
    onTimeFilterSelected: (TimeFilter) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onResetToday: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onTypeFilterChange: (String) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onOpenSummaryDashboard: () -> Unit = {},
    allTransactions: List<TransactionEntity> = emptyList(),
    currentReferenceDate: java.util.Calendar = java.util.Calendar.getInstance(),
    onSelectDayDate: ((java.util.Calendar) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dayHeaderFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.US) }
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    var selectedWalletFilter by remember { mutableStateOf("All") }
    var selectedTxForSheet by remember { mutableStateOf<TransactionEntity?>(null) }
    var isSearchExpanded by remember { mutableStateOf(searchQuery.isNotEmpty()) }
    var showCalendarPicker by remember { mutableStateOf(false) }

    if (showCalendarPicker) {
        CalendarPickerDialog(
            selectedDate = currentReferenceDate,
            allTransactions = allTransactions,
            onDateSelected = { newDate ->
                onSelectDayDate?.invoke(newDate)
            },
            onDismiss = { showCalendarPicker = false }
        )
    }

    // Filter by wallet (Cash / UPI vs Credit Card vs All)
    val displayTransactions = remember(transactions, selectedWalletFilter) {
        if (selectedWalletFilter == "All") transactions
        else if (selectedWalletFilter == "Cash / UPI") {
            transactions.filter { !it.wallet.contains("card", ignoreCase = true) && !it.wallet.contains("credit", ignoreCase = true) }
        } else if (selectedWalletFilter == "Credit Card") {
            transactions.filter { it.wallet.contains("card", ignoreCase = true) || it.wallet.contains("credit", ignoreCase = true) }
        } else {
            transactions
        }
    }

    // Group transactions by calendar day string
    val groupedByDay = remember(displayTransactions) {
        displayTransactions.groupBy { tx ->
            dayHeaderFormat.format(Date(tx.date))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ledger_screen")
    ) {
        // Pinned Time Filter Pills
        PeriodTimeFilterBar(
            selectedTimeFilter = selectedTimeFilter,
            onTimeFilterSelected = onTimeFilterSelected
        )

        // Overview Card + Transaction Filters + List (scrolls smoothly together)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 190.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "period_overview_card") {
                PeriodOverviewCard(
                    selectedTimeFilter = selectedTimeFilter,
                    periodLabel = periodLabel,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    onPrevious = onPreviousPeriod,
                    onNext = onNextPeriod,
                    onResetToday = onResetToday,
                    onOpenCalendar = { showCalendarPicker = true }
                )
            }

            item(key = "ledger_search_and_filters") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (isSearchExpanded) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                androidx.compose.foundation.text.BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchQueryChange,
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("search_input"),
                                    decorationBox = { innerTextField ->
                                        if (searchQuery.isEmpty()) {
                                            Text(
                                                "Search memo, category...",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                maxLines = 1
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                                IconButton(
                                    onClick = {
                                        onSearchQueryChange("")
                                        isSearchExpanded = false
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "Close Search",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isSearchExpanded) {
                            Surface(
                                shape = ExpressiveChipShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .height(34.dp)
                                    .clickable { isSearchExpanded = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    if (searchQuery.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            searchQuery,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Type filter chips
                        listOf("All", "Expense", "Income").forEach { t ->
                            val isSelected = typeFilter == t
                            FilterChip(
                                selected = isSelected,
                                onClick = { onTypeFilterChange(t) },
                                label = { Text(t, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                shape = ExpressiveChipShape,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (t == "Expense") ExpenseRed else if (t == "Income") IncomeGreen else MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("type_filter_$t")
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        )

                        // Payment mode filter chips
                        listOf("All", "Cash / UPI", "Credit Card").forEach { w ->
                            val isSelected = selectedWalletFilter == w
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedWalletFilter = w },
                                shape = ExpressiveChipShape,
                                leadingIcon = {
                                    if (w == "Cash / UPI") {
                                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                                    } else if (w == "Credit Card") {
                                        Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                },
                                label = { Text(if (w == "All") "All Modes" else w, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (w == "Credit Card") Color(0xFF8B5CF6) else MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            if (displayTransactions.isEmpty()) {
                item(key = "empty_transactions_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No transactions found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to add an expense or income entry.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                // Summary Dashboard Entry Banner
                item(key = "banner_summary_dashboard") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSummaryDashboard() }
                            .testTag("banner_open_summary_dashboard"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Dashboard,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Summary Dashboard",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = ExpressiveChipShape,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Deep Insights",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "50/30/20 Rule • Weekend Surge • Habit Score • Burn Rate",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open Dashboard",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                groupedByDay.forEach { (dayString, dayTransactions) ->
                    val dayExpense = dayTransactions.filter { it.type.equals("Expense", ignoreCase = true) }.sumOf { it.amount }
                    val dayIncome = dayTransactions.filter { it.type.equals("Income", ignoreCase = true) }.sumOf { it.amount }

                    // Floating Date Header (labelLarge in secondary)
                    item(key = "header_$dayString") {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = dayString,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                if (dayIncome > 0) {
                                    Text(
                                        text = "+₹${numberFormat.format(dayIncome)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = com.example.ui.theme.SFMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = IncomeGreen,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                                if (dayExpense > 0) {
                                    Text(
                                        text = "-₹${numberFormat.format(dayExpense)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = com.example.ui.theme.SFMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRed,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    // Transactions for this day
                    items(dayTransactions, key = { it.id }) { tx ->
                        TransactionCard(
                            transaction = tx,
                            onClick = { onEditTransaction(tx) },
                            onEdit = { onEditTransaction(tx) },
                            onDelete = { onDeleteTransaction(tx) }
                        )
                    }
                }
            }
        }
    }

    // Material 3 Action Bottom Sheet on Transaction Tap
    selectedTxForSheet?.let { tx ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedTxForSheet = null },
            sheetState = sheetState,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            TransactionDetailSheetContent(
                transaction = tx,
                onEdit = {
                    val toEdit = tx
                    selectedTxForSheet = null
                    onEditTransaction(toEdit)
                },
                onDelete = {
                    val toDelete = tx
                    selectedTxForSheet = null
                    onDeleteTransaction(toDelete)
                },
                onDismiss = { selectedTxForSheet = null }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    onClick: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    val isExpense = transaction.type.equals("Expense", ignoreCase = true)
    val categoryColor = CategoryHelper.getCategoryColor(transaction.category)
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.US) }
    val isCreditCard = transaction.wallet.contains("card", ignoreCase = true) || transaction.wallet.contains("credit", ignoreCase = true)
    val clickAction = onClick ?: onEdit

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (clickAction != null) Modifier.clickable { clickAction() } else Modifier
            )
            .testTag("transaction_item_${transaction.id}"),
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(categoryColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CategoryHelper.getCategoryIcon(transaction.category),
                    contentDescription = transaction.category,
                    tint = categoryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            val amountStr = "${if (isExpense) "-" else "+"}₹${numberFormat.format(transaction.amount)}"
            val actionsCount = (if (onEdit != null) 1 else 0) + (if (onDelete != null) 1 else 0)
            val actionsReserveWidth = if (actionsCount > 0) (actionsCount * 28 + 8).dp else 0.dp
            val amountStyle = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontFamily = com.example.ui.theme.SFMonoFontFamily
            )

            androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val amountFitsSideBySide = com.example.ui.components.rememberAllFitColumnWidth(
                    textsAndStyles = listOf(amountStr to amountStyle),
                    columnWidth = (maxWidth - 116.dp - actionsReserveWidth).coerceAtLeast(0.dp),
                    horizontalPaddingPerColumn = 0.dp
                )

                val detailsContent: @Composable (Modifier) -> Unit = { mod ->
                    Column(modifier = mod) {
                        Text(
                            text = if (transaction.subcategory.isNotBlank()) "${transaction.category} • ${transaction.subcategory}" else transaction.category,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (transaction.memo.isNotBlank()) {
                            Text(
                                text = transaction.memo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(percent = 50),
                                color = if (isCreditCard) Color(0xFF8B5CF6).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Text(
                                    text = transaction.wallet,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isCreditCard) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = timeFormat.format(Date(transaction.date)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                            if (transaction.isRecurring) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "• Auto",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                val amountAndActionsContent: @Composable (Boolean) -> Unit = { inlineActions ->
                    if (inlineActions) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = amountStr,
                                style = amountStyle,
                                color = if (isExpense) ExpenseRed else IncomeGreen,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                softWrap = false
                            )
                            if (onEdit != null || onDelete != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (onEdit != null) {
                                        IconButton(
                                            onClick = onEdit,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .testTag("edit_tx_${transaction.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Transaction",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    if (onDelete != null) {
                                        IconButton(
                                            onClick = onDelete,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .testTag("delete_tx_${transaction.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Delete Transaction",
                                                tint = ExpenseRed.copy(alpha = 0.7f),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = amountStr,
                                style = amountStyle,
                                color = if (isExpense) ExpenseRed else IncomeGreen,
                                maxLines = 1,
                                softWrap = false
                            )
                            if (onEdit != null || onDelete != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (onEdit != null) {
                                        IconButton(
                                            onClick = onEdit,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .testTag("edit_tx_${transaction.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit Transaction",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                    if (onDelete != null) {
                                        IconButton(
                                            onClick = onDelete,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .testTag("delete_tx_${transaction.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Delete Transaction",
                                                tint = ExpenseRed.copy(alpha = 0.7f),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (amountFitsSideBySide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        detailsContent(Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        amountAndActionsContent(true)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        detailsContent(Modifier.fillMaxWidth())
                        amountAndActionsContent(false)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    onClick: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    TransactionCard(
        transaction = transaction,
        onClick = onClick,
        onEdit = onEdit,
        onDelete = onDelete,
        modifier = modifier
    )
}

@Composable
fun TransactionDetailSheetContent(
    transaction: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    val isExpense = transaction.type.equals("Expense", ignoreCase = true)
    val categoryColor = CategoryHelper.getCategoryColor(transaction.category)
    val fullDateFormat = remember { SimpleDateFormat("EEEE, dd MMM yyyy 'at' h:mm a", Locale.US) }
    val isCreditCard = transaction.wallet.contains("card", ignoreCase = true) || transaction.wallet.contains("credit", ignoreCase = true)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Category Squircle Icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(categoryColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = CategoryHelper.getCategoryIcon(transaction.category),
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (transaction.subcategory.isNotBlank()) "${transaction.category} • ${transaction.subcategory}" else transaction.category,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Hero Financial Figure in DisplayMedium with tight tracking
        Text(
            text = "${if (isExpense) "-" else "+"}₹${numberFormat.format(transaction.amount)}",
            style = MaterialTheme.typography.displayMedium,
            fontFamily = com.example.ui.theme.SFMonoFontFamily,
            fontWeight = FontWeight.ExtraBold,
            color = if (isExpense) ExpenseRed else IncomeGreen,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Detail Pill Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = ExpressiveCardShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Payment Mode", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        transaction.wallet,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCreditCard) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Date & Time", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        fullDateFormat.format(Date(transaction.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (transaction.memo.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Note / Memo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            transaction.memo,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (transaction.isRecurring) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Type", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Recurring Expense", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Actions: Edit and Delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("delete_tx_${transaction.id}"),
                shape = ExpressiveButtonShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed)
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1.3f)
                    .height(48.dp)
                    .testTag("edit_tx_action"),
                shape = ExpressiveButtonShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Edit Entry", fontWeight = FontWeight.Bold)
            }
        }
    }
}
