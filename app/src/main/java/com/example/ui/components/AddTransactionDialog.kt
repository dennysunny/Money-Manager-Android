package com.example.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.ExpressiveButtonShape
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.ExpressiveChipShape
import com.example.ui.theme.ExpressiveDialogShape
import com.example.ui.theme.ExpressiveMenuShape
import com.example.ui.theme.ExpressivePillButtonShape
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    initialTransaction: TransactionEntity? = null,
    initialType: String = "Expense",
    initialAmount: String = "",
    initialCategory: String = "",
    initialMemo: String = "",
    initialWallet: String = "",
    defaultDate: Long = System.currentTimeMillis(),
    sortedExpenseCategories: List<String>,
    sortedIncomeCategories: List<String>,
    subcategoriesMap: Map<String, List<String>>,
    onDismiss: () -> Unit,
    onSave: (id: Long, amount: Double, category: String, subcategory: String, type: String, memo: String, wallet: String, date: Long) -> Unit,
    onAddSubcategory: (parentCategory: String, subcategoryName: String, iconName: String) -> Unit = { _, _, _ -> },
    onAddNewCategory: (categoryName: String, type: String, iconName: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    var type by remember(initialTransaction, initialType) { mutableStateOf(initialTransaction?.type ?: initialType) }
    var amountText by remember(initialTransaction, initialAmount) {
        mutableStateOf(initialTransaction?.amount?.let { if (it > 0) it.toString() else "" } ?: initialAmount)
    }

    val categories = remember(type, sortedExpenseCategories, sortedIncomeCategories) {
        val base = if (type == "Expense") {
            (sortedExpenseCategories + CategoryHelper.commonExpenseCategories)
        } else {
            (sortedIncomeCategories + CategoryHelper.commonIncomeCategories)
        }
        base.map { it.trim() }.filter { it.isNotBlank() }.distinct()
    }

    var selectedCategory by remember(initialTransaction, type, initialCategory) {
        mutableStateOf(
            initialTransaction?.category
                ?: (if (initialCategory.isNotBlank()) initialCategory else (categories.firstOrNull() ?: if (type == "Expense") "Petrol" else "Salary"))
        )
    }

    var selectedSubcategory by remember { mutableStateOf(initialTransaction?.subcategory ?: "") }
    var memo by remember(initialTransaction, initialMemo) {
        mutableStateOf(initialTransaction?.memo ?: initialMemo)
    }
    var wallet by remember(initialTransaction, initialWallet) {
        mutableStateOf(
            initialTransaction?.wallet?.let {
                if (it.contains("card", ignoreCase = true) || it.contains("credit", ignoreCase = true)) "Credit Card" else "Cash / UPI"
            } ?: (if (initialWallet.isNotBlank()) initialWallet else "Cash / UPI")
        )
    }
    var dateMillis by remember { mutableLongStateOf(initialTransaction?.date ?: defaultDate) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownSearchQuery by remember { mutableStateOf("") }
    var showAllCategoriesDialog by remember { mutableStateOf(false) }
    var showAddSubcatDialog by remember { mutableStateOf(false) }
    var showAddCatDialog by remember { mutableStateOf(false) }
    var newSubcatInput by remember { mutableStateOf("") }
    var newCatInput by remember { mutableStateOf("") }
    var newCatIconKey by remember { mutableStateOf("") }
    var newSubcatIconKey by remember { mutableStateOf("") }

    val availableSubcategories = subcategoriesMap[selectedCategory] ?: emptyList()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.US) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier.testTag("add_transaction_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .navigationBarsPadding()
        ) {
            // Top Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (initialTransaction == null) {
                            if (type == "Expense") "Add Expense" else "Add Income"
                        } else "Edit Transaction",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Record payment details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp).testTag("close_dialog_button")
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // M3 Segmented Button Switcher (Expense vs Income) spanning full width
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("type_segmented_button")
                ) {
                    SegmentedButton(
                        selected = type == "Expense",
                        onClick = {
                            type = "Expense"
                            val expCats = if (sortedExpenseCategories.isNotEmpty()) sortedExpenseCategories else CategoryHelper.commonExpenseCategories
                            selectedCategory = expCats.firstOrNull() ?: "Petrol"
                            selectedSubcategory = ""
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        modifier = Modifier.testTag("type_expense_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (type == "Expense") ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Expense",
                                fontWeight = if (type == "Expense") FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    SegmentedButton(
                        selected = type == "Income",
                        onClick = {
                            type = "Income"
                            val incCats = if (sortedIncomeCategories.isNotEmpty()) sortedIncomeCategories else CategoryHelper.commonIncomeCategories
                            selectedCategory = incCats.firstOrNull() ?: "Salary"
                            selectedSubcategory = ""
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        modifier = Modifier.testTag("type_income_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (type == "Income") IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Income",
                                fontWeight = if (type == "Income") FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Keypad-Style Amount Display Centered in displayMedium with Quick-Add Chips
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ExpressiveCardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(
                        1.5.dp,
                        if (type == "Expense") ExpenseRed.copy(alpha = 0.35f) else IncomeGreen.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Amount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "₹",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (type == "Expense") ExpenseRed else IncomeGreen
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            BasicTextField(
                                value = amountText,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() || it == '.' }) {
                                        amountText = input
                                    }
                                },
                                textStyle = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Start
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier
                                    .widthIn(min = 60.dp, max = 260.dp)
                                    .testTag("amount_input"),
                                decorationBox = { innerTextField ->
                                    if (amountText.isEmpty()) {
                                        Text(
                                            text = "0.00",
                                            style = MaterialTheme.typography.displayMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Increment Chips (+100, +500, +1000, +2000, Clear)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(100, 500, 1000, 2000).forEach { inc ->
                                Surface(
                                    shape = RoundedCornerShape(percent = 50),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .clickable {
                                            val current = amountText.toDoubleOrNull() ?: 0.0
                                            val updated = (current + inc).toInt()
                                            amountText = updated.toString()
                                        }
                                ) {
                                    Text(
                                        text = "+₹$inc",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            if (amountText.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(percent = 50),
                                    color = ExpenseRed.copy(alpha = 0.15f),
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .clickable { amountText = "" }
                                ) {
                                    Text(
                                        text = "Clear",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRed,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Visual Category Section (Interactive Grid Selection)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category (Frequently used)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Dropdown menu button and Search / View All button to view all categories
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = ExpressiveChipShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier
                                .clickable { showAllCategoriesDialog = true }
                                .testTag("btn_search_all_categories")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search Categories",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "All (${categories.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box {
                            Text(
                                text = "▼",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { categoryDropdownExpanded = true }
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                            )

                            DropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = {
                                    categoryDropdownExpanded = false
                                    categoryDropdownSearchQuery = ""
                                },
                                shape = ExpressiveMenuShape,
                                modifier = Modifier
                                    .widthIn(min = 250.dp, max = 320.dp)
                                    .heightIn(max = 380.dp)
                                    .background(MaterialTheme.colorScheme.surface, ExpressiveMenuShape)
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
                                                if (categoryDropdownSearchQuery.isEmpty()) {
                                                    Text(
                                                        text = "Search category...",
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                        maxLines = 1
                                                    )
                                                }
                                                BasicTextField(
                                                    value = categoryDropdownSearchQuery,
                                                    onValueChange = { categoryDropdownSearchQuery = it },
                                                    singleLine = true,
                                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                                        fontSize = 13.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    ),
                                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                            if (categoryDropdownSearchQuery.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                IconButton(
                                                    onClick = { categoryDropdownSearchQuery = "" },
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

                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Browse All Grid (${categories.size})", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    onClick = {
                                        categoryDropdownExpanded = false
                                        categoryDropdownSearchQuery = ""
                                        showAllCategoriesDialog = true
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("+ Add New Category", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    },
                                    onClick = {
                                        categoryDropdownExpanded = false
                                        categoryDropdownSearchQuery = ""
                                        showAddCatDialog = true
                                    }
                                )

                                val filteredCategories = categories.filter {
                                    it.contains(categoryDropdownSearchQuery, ignoreCase = true)
                                }

                                if (filteredCategories.isEmpty()) {
                                    Box(modifier = Modifier.padding(14.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("No matching category", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                                    Text(
                                                        cat,
                                                        fontWeight = if (cat == selectedCategory) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            },
                                            onClick = {
                                                selectedCategory = cat
                                                selectedSubcategory = ""
                                                categoryDropdownExpanded = false
                                                categoryDropdownSearchQuery = ""
                                            },
                                            trailingIcon = {
                                                if (cat == selectedCategory) {
                                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Grid Selection: Present categories as a rounded square grid (FilledTonalCard with squircle icons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    categories.take(12).forEach { cat ->
                        val isSelected = cat.equals(selectedCategory, ignoreCase = true)
                        val catColor = CategoryHelper.getCategoryColor(cat)
                        val icon = CategoryHelper.getCategoryIcon(cat)

                        Card(
                            onClick = {
                                selectedCategory = cat
                                selectedSubcategory = ""
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .width(82.dp)
                                .testTag("cat_pill_$cat")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary else catColor.copy(alpha = 0.18f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = cat,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else catColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // "+ Add New" Category Card
                    Card(
                        onClick = { showAddCatDialog = true },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        modifier = Modifier.width(82.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add New Category",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "+ New",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Subcategories Selector (e.g. Petrol -> Car / Bike)
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subcategory (under $selectedCategory)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "+ Add",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showAddSubcatDialog = true }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "None / General" chip
                    FilterChip(
                        selected = selectedSubcategory.isEmpty(),
                        onClick = { selectedSubcategory = "" },
                        label = { Text("None") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    availableSubcategories.forEach { subcat ->
                        val isSelected = subcat.equals(selectedSubcategory, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubcategory = if (isSelected) "" else subcat },
                            label = { Text(subcat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }

                    FilterChip(
                        selected = false,
                        onClick = { showAddSubcatDialog = true },
                        label = { Text("+ Subcategory") },
                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Payment Mode / Wallet Selector (Cash / UPI vs Credit Card)
                Text(
                    text = "Payment Mode / Wallet",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isCash = wallet == "Cash / UPI"
                    val isCard = wallet == "Credit Card"

                    // Cash / UPI Card
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isCash) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                            .border(
                                BorderStroke(
                                    width = if (isCash) 1.5.dp else 1.dp,
                                    color = if (isCash) MaterialTheme.colorScheme.primary else Color.Transparent
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { wallet = "Cash / UPI" }
                            .padding(horizontal = 12.dp)
                            .testTag("wallet_chip_Cash / UPI"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = if (isCash) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cash / UPI",
                            fontSize = 12.sp,
                            fontWeight = if (isCash) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCash) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Credit Card Card
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isCard) Color(0xFF8B5CF6).copy(alpha = 0.18f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                            .border(
                                BorderStroke(
                                    width = if (isCard) 1.5.dp else 1.dp,
                                    color = if (isCard) Color(0xFF8B5CF6) else Color.Transparent
                                ),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { wallet = "Credit Card" }
                            .padding(horizontal = 12.dp)
                            .testTag("wallet_chip_Credit Card"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = if (isCard) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Credit Card",
                            fontSize = 12.sp,
                            fontWeight = if (isCard) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCard) Color(0xFF8B5CF6) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date & Time Picker Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newCal = Calendar.getInstance().apply {
                                        timeInMillis = dateMillis
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    }
                                    // Also show TimePicker for exact precision
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            newCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            newCal.set(Calendar.MINUTE, minute)
                                            dateMillis = newCal.timeInMillis
                                        },
                                        newCal.get(Calendar.HOUR_OF_DAY),
                                        newCal.get(Calendar.MINUTE),
                                        false
                                    ).show()
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("date_picker_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Date & Time",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dateFormat.format(Date(dateMillis)),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = "Edit",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Memo / Note Field
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("Memo / Note (Optional)") },
                    placeholder = { Text("e.g. petrol car, smart home, groceries") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("memo_input"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Save / Update Button (Pinned at the bottom so it never overflows)
            Button(
                onClick = {
                    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                    onSave(
                        initialTransaction?.id ?: 0L,
                        parsedAmount,
                        selectedCategory,
                        selectedSubcategory,
                        type,
                        memo,
                        wallet,
                        dateMillis
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 50.dp)
                    .testTag("save_transaction_button"),
                shape = ExpressiveButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "Expense") MaterialTheme.colorScheme.primary else IncomeGreen
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialTransaction == null) {
                        if (type == "Expense") "Save Expense" else "Save Income"
                    } else "Update Transaction",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    // Dialog: Add Subcategory under current category
    if (showAddSubcatDialog) {
        Dialog(onDismissRequest = { showAddSubcatDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Add Subcategory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Under parent category: $selectedCategory",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newSubcatInput,
                        onValueChange = { newSubcatInput = it },
                        label = { Text("Subcategory (e.g. Car, Bike, Snacks)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Pick Icon (optional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryHelper.iconOptions.forEach { opt ->
                            val isChosen = newSubcatIconKey == opt.key
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        BorderStroke(if (isChosen) 2.dp else 1.dp, if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                        CircleShape
                                    )
                                    .clickable { newSubcatIconKey = if (isChosen) "" else opt.key },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = opt.icon,
                                    contentDescription = opt.label,
                                    tint = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                newSubcatIconKey = ""
                                showAddSubcatDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp),
                            shape = ExpressiveButtonShape
                        ) {
                            Text("Cancel", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Button(
                            onClick = {
                                if (newSubcatInput.isNotBlank()) {
                                    onAddSubcategory(selectedCategory, newSubcatInput.trim(), newSubcatIconKey)
                                    selectedSubcategory = newSubcatInput.trim()
                                    newSubcatInput = ""
                                    newSubcatIconKey = ""
                                    showAddSubcatDialog = false
                                }
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .defaultMinSize(minHeight = 48.dp),
                            shape = ExpressiveButtonShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Add Subcategory", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add New Main Category
    if (showAddCatDialog) {
        Dialog(onDismissRequest = { showAddCatDialog = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(0.92f)
            ) {
                Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Add New Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Create new $type category",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newCatInput,
                        onValueChange = { newCatInput = it },
                        label = { Text("Category Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Pick Icon (optional)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryHelper.iconOptions.forEach { opt ->
                            val isChosen = newCatIconKey == opt.key
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        BorderStroke(if (isChosen) 2.dp else 1.dp, if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                        CircleShape
                                    )
                                    .clickable { newCatIconKey = if (isChosen) "" else opt.key },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = opt.icon,
                                    contentDescription = opt.label,
                                    tint = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                newCatIconKey = ""
                                showAddCatDialog = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp),
                            shape = ExpressiveButtonShape
                        ) {
                            Text("Cancel", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Button(
                            onClick = {
                                if (newCatInput.isNotBlank()) {
                                    onAddNewCategory(newCatInput.trim(), type, newCatIconKey)
                                    selectedCategory = newCatInput.trim()
                                    selectedSubcategory = ""
                                    newCatInput = ""
                                    newCatIconKey = ""
                                    showAddCatDialog = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp),
                            shape = ExpressiveButtonShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Create", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }

    // Dialog: Full All-Categories Searchable Picker
    if (showAllCategoriesDialog) {
        var categorySearchQuery by remember { mutableStateOf("") }
        val displayedCategories = remember(categories, categorySearchQuery) {
            if (categorySearchQuery.isBlank()) categories
            else categories.filter { it.contains(categorySearchQuery.trim(), ignoreCase = true) }
        }

        Dialog(
            onDismissRequest = { showAllCategoriesDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = ExpressiveDialogShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .heightIn(max = 560.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "All Categories (${categories.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select or search $type categories",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showAllCategoriesDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = categorySearchQuery,
                        onValueChange = { categorySearchQuery = it },
                        label = { Text("Search category...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (categorySearchQuery.isNotEmpty()) {
                                IconButton(onClick = { categorySearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = ExpressiveButtonShape,
                        modifier = Modifier.fillMaxWidth().testTag("category_search_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(max = 340.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (displayedCategories.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No category matching \"$categorySearchQuery\"",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            displayedCategories.forEach { cat ->
                                val isSelected = cat == selectedCategory
                                val catColor = CategoryHelper.getCategoryColor(cat)
                                Surface(
                                    shape = ExpressiveChipShape,
                                    color = if (isSelected) catColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) catColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCategory = cat
                                            selectedSubcategory = ""
                                            showAllCategoriesDialog = false
                                        }
                                        .testTag("picker_cat_$cat")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(catColor.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = CategoryHelper.getCategoryIcon(cat),
                                                    contentDescription = null,
                                                    tint = catColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = cat,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = catColor,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            showAllCategoriesDialog = false
                            showAddCatDialog = true
                        },
                        shape = ExpressiveButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("btn_add_category_from_picker")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "+ Add New Custom Category",
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
