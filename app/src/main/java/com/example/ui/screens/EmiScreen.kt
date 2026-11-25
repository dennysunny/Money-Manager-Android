package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.heightIn
import com.example.ui.theme.ExpressiveButtonShape
import com.example.ui.theme.ExpressiveCardShape
import com.example.ui.theme.ExpressiveChipShape
import com.example.ui.theme.ExpressiveDialogShape
import com.example.ui.theme.ExpressiveMenuShape
import com.example.ui.theme.ExpressivePillButtonShape
import com.example.ui.theme.SFMonoFontFamily
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import com.example.data.model.CategoryEntity
import com.example.data.model.DebtEntity
import com.example.data.model.RecurringExpenseEntity
import com.example.ui.components.CategoryHelper
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmiScreen(
    recurringList: List<RecurringExpenseEntity>,
    debtsList: List<DebtEntity>,
    categoriesList: List<CategoryEntity>,
    salaryAmount: Double,
    salaryEmployer: String,
    salaryCreditDay: Int,
    salaryWallet: String,
    salaryAutoCredit: Boolean,
    onOpenSalaryScreen: () -> Unit,
    onAddRecurring: (title: String, amount: Double, category: String, day: Int, memo: String, autoAdd: Boolean, startDate: Long) -> Unit,
    onUpdateRecurring: (id: Long, title: String, amount: Double, category: String, day: Int, memo: String, autoAdd: Boolean, startDate: Long) -> Unit = { _, _, _, _, _, _, _, _ -> },
    onTriggerRecurringNow: (RecurringExpenseEntity) -> Unit,
    onDeleteRecurring: (RecurringExpenseEntity) -> Unit,
    onAddDebt: (name: String, amount: Double, type: String, memo: String) -> Unit = { _, _, _, _ -> },
    onToggleDebtSettled: (DebtEntity) -> Unit,
    onPayDebt: (DebtEntity, Double) -> Unit = { _, _ -> },
    onDeleteDebt: (DebtEntity) -> Unit,
    onAddCategory: (name: String, parentCategory: String?, type: String, iconName: String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddEmiDialog by remember { mutableStateOf(false) }
    var showAddRecurringBillDialog by remember { mutableStateOf(false) }
    var editingRecurringItem by remember { mutableStateOf<RecurringExpenseEntity?>(null) }
    var showAddDebtDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var payingDebt by remember { mutableStateOf<DebtEntity?>(null) }
    var selectedParentForSubcat by remember { mutableStateOf<String?>(null) }
    var selectedSectionFilter by remember { mutableStateOf("All") }

    // Grouping expansion states for the recurring tab: collapsed by default
    var isEmiExpanded by remember { mutableStateOf(false) }
    var isBillsExpanded by remember { mutableStateOf(false) }
    var isDebtsExpanded by remember { mutableStateOf(false) }
    var isCategoriesExpanded by remember { mutableStateOf(false) }

    val numberFormat = remember { DecimalFormat("#,##,##0.00") }

    // 2 Separate Sections: Loans & EMIs vs Recurring Bills & Subscriptions
    val emiItems = remember(recurringList) {
        recurringList.filter {
            it.category.equals("EMI", ignoreCase = true) ||
            it.title.contains("emi", ignoreCase = true) ||
            it.title.contains("loan", ignoreCase = true)
        }
    }
    val billItems = remember(recurringList) {
        recurringList.filter {
            !it.category.equals("EMI", ignoreCase = true) &&
            !it.title.contains("emi", ignoreCase = true) &&
            !it.title.contains("loan", ignoreCase = true)
        }
    }

    val totalEmiMonthly = remember(emiItems) { emiItems.sumOf { it.amount } }
    val totalBillsMonthly = remember(billItems) { billItems.sumOf { it.amount } }

    val mainCategories = remember(categoriesList) { categoriesList.filter { it.parentCategory == null } }
    val subcategoriesByParent = remember(categoriesList) {
        categoriesList.filter { it.parentCategory != null }.groupBy { it.parentCategory!! }
    }

    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val safeDrawingTop = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val effectiveTopPadding = maxOf(statusBarTop, safeDrawingTop, 36.dp)

    Column(modifier = modifier.fillMaxSize().testTag("emi_screen")) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = effectiveTopPadding, bottom = 8.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Recurring & EMIs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage Loans, Fixed Bills, Debts & Categories",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Section Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All", "Loans & EMI", "Recurring Bills", "Salary", "Debts", "Categories").forEach { section ->
                val isSelected = selectedSectionFilter == section
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSectionFilter = section },
                    label = { Text(section, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (section == "Loans & EMI") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 190.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Salary Settings Banner / Card
            if (selectedSectionFilter == "All" || selectedSectionFilter == "Salary") {
                item(key = "section_salary_card") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSalaryScreen() }
                            .testTag("salary_nav_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(IncomeGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "Monthly Salary Settings",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (salaryAmount > 0) {
                                            Text(
                                                text = "₹${numberFormat.format(salaryAmount)}",
                                                fontSize = 11.sp,
                                                fontFamily = SFMonoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = IncomeGreen,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (salaryAmount > 0) "Credit Date: ${salaryCreditDay}th of month • Tap to change" else "Tap to set salary amount & credit date",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = "Open Salary Settings", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // ==========================================
            // GROUP 1: Loans & EMIs Group
            // ==========================================
            if (selectedSectionFilter == "All" || selectedSectionFilter == "Loans & EMI") {
                val shouldShowEmiContent = isEmiExpanded || selectedSectionFilter == "Loans & EMI"
                item(key = "section_emi_header") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isEmiExpanded = !isEmiExpanded },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF8B5CF6).copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.22f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            // Line 1: Header Icon, Title, and Expand Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF8B5CF6).copy(alpha = 0.22f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Loans & EMIs",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (selectedSectionFilter == "All") {
                                    IconButton(
                                        onClick = { isEmiExpanded = !isEmiExpanded },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (shouldShowEmiContent) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (shouldShowEmiContent) "Collapse" else "Expand",
                                            tint = Color(0xFF8B5CF6)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Line 2: Subtitle Metric Pill & Action Button
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF8B5CF6).copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "Total: ₹${numberFormat.format(totalEmiMonthly)} / mo (${emiItems.size} loans)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = SFMonoFontFamily,
                                        color = Color(0xFF8B5CF6),
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }

                                Button(
                                    onClick = { showAddEmiDialog = true },
                                    shape = ExpressiveButtonShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_add_emi")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add EMI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (shouldShowEmiContent) {
                    if (emiItems.isEmpty()) {
                        item(key = "empty_emi") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("No loan or EMI scheduled", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        Text("Tap '+ Add EMI' above to track car loan, home loan, personal loan.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    } else {
                        items(emiItems, key = { "emi_${it.id}_${it.title}" }) { emi ->
                            RecurringExpenseCard(
                                recurring = emi,
                                isEmi = true,
                                onTriggerNow = {
                                    onTriggerRecurringNow(emi)
                                    Toast.makeText(context, "${emi.title} posted to ledger", Toast.LENGTH_SHORT).show()
                                },
                                onEdit = { editingRecurringItem = emi },
                                onDelete = { onDeleteRecurring(emi) }
                            )
                        }
                    }
                }
            }

            // ==========================================
            // GROUP 2: Recurring Bills & Utilities Group
            // ==========================================
            if (selectedSectionFilter == "All" || selectedSectionFilter == "Recurring Bills") {
                val shouldShowBillsContent = isBillsExpanded || selectedSectionFilter == "Recurring Bills"
                item(key = "section_bills_header") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isBillsExpanded = !isBillsExpanded },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            // Line 1: Header Icon, Title, and Expand Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Recurring Bills",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (selectedSectionFilter == "All") {
                                    IconButton(
                                        onClick = { isBillsExpanded = !isBillsExpanded },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (shouldShowBillsContent) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (shouldShowBillsContent) "Collapse" else "Expand",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Line 2: Subtitle Metric Pill & Action Button
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Total: ₹${numberFormat.format(totalBillsMonthly)} / mo (${billItems.size} bills)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = SFMonoFontFamily,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }

                                Button(
                                    onClick = { showAddRecurringBillDialog = true },
                                    shape = ExpressiveButtonShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp).testTag("btn_add_recurring_bill")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Bill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (shouldShowBillsContent) {
                    if (billItems.isEmpty()) {
                        item(key = "empty_bills") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Repeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("No recurring utility bills scheduled", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        Text("Tap '+ Add Bill' to track house rent, WiFi, electricity, subscriptions.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    } else {
                        items(billItems, key = { "bill_${it.id}_${it.title}" }) { bill ->
                            RecurringExpenseCard(
                                recurring = bill,
                                isEmi = false,
                                onTriggerNow = {
                                    onTriggerRecurringNow(bill)
                                    Toast.makeText(context, "${bill.title} posted to ledger", Toast.LENGTH_SHORT).show()
                                },
                                onEdit = { editingRecurringItem = bill },
                                onDelete = { onDeleteRecurring(bill) }
                            )
                        }
                    }
                }
            }

            // ==========================================
            // GROUP 3: Debts & Personal Loans Group
            // ==========================================
            if (selectedSectionFilter == "All" || selectedSectionFilter == "Debts") {
                val shouldShowDebtsContent = isDebtsExpanded || selectedSectionFilter == "Debts"
                val activeDebtsCount = debtsList.count { !it.isSettled }
                val remainingDebtsTotal = debtsList.filter { !it.isSettled }.sumOf { it.remainingAmount }

                item(key = "section_debts_header") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDebtsExpanded = !isDebtsExpanded },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            // Line 1: Header Icon, Title, and Expand Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(ExpenseRed.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Debts & Personal Loans",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (selectedSectionFilter == "All") {
                                    IconButton(
                                        onClick = { isDebtsExpanded = !isDebtsExpanded },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (shouldShowDebtsContent) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (shouldShowDebtsContent) "Collapse" else "Expand",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Line 2: Subtitle Metric Pill & Action Button
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Text(
                                        text = "$activeDebtsCount active • ₹${numberFormat.format(remainingDebtsTotal)} balance",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = SFMonoFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }

                                Button(
                                    onClick = { showAddDebtDialog = true },
                                    shape = ExpressiveButtonShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp).testTag("add_debt_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Debt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (shouldShowDebtsContent) {
                    if (debtsList.isEmpty()) {
                        item(key = "empty_debts") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(14.dp), contentAlignment = Alignment.Center) {
                                    Text("No debt records. Tap '+ Add Debt' to track borrowed/lent money with small payoff increments.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        items(debtsList, key = { "debt_${it.id}_${it.personName}" }) { debt ->
                            DebtCard(
                                debt = debt,
                                onToggleSettled = { onToggleDebtSettled(debt) },
                                onPay = { payingDebt = debt },
                                onDelete = { onDeleteDebt(debt) }
                            )
                        }
                    }
                }
            }

            // ==========================================
            // GROUP 4: Categories & Subcategories Group
            // ==========================================
            if (selectedSectionFilter == "All" || selectedSectionFilter == "Categories") {
                val shouldShowCategoriesContent = isCategoriesExpanded || selectedSectionFilter == "Categories"

                item(key = "section_category_header") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCategoriesExpanded = !isCategoriesExpanded },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            // Line 1: Header Icon, Title, and Expand Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Categories & Subcategories",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (selectedSectionFilter == "All") {
                                    IconButton(
                                        onClick = { isCategoriesExpanded = !isCategoriesExpanded },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (shouldShowCategoriesContent) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = if (shouldShowCategoriesContent) "Collapse" else "Expand",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Line 2: Subtitle Metric Pill & Action Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(percent = 50),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Text(
                                        text = "${mainCategories.size} main categories with icons",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }

                                Button(
                                    onClick = {
                                        selectedParentForSubcat = null
                                        showAddCategoryDialog = true
                                    },
                                    shape = ExpressiveButtonShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp).testTag("add_category_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Cat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (shouldShowCategoriesContent) {
                    item(key = "section_categories_list") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                mainCategories.forEachIndexed { index, mainCat ->
                                    val subcats = subcategoriesByParent[mainCat.name] ?: emptyList()
                                    val color = CategoryHelper.getCategoryColor(mainCat.name)

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(30.dp)
                                                        .clip(CircleShape)
                                                        .background(color.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = CategoryHelper.getCategoryIcon(mainCat.name, mainCat.iconName),
                                                        contentDescription = null,
                                                        tint = color,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = mainCat.name,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Text(
                                                text = "+ Subcategory",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        selectedParentForSubcat = mainCat.name
                                                        showAddCategoryDialog = true
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (subcats.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                subcats.forEach { sub ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(MaterialTheme.colorScheme.surface)
                                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(
                                                                imageVector = CategoryHelper.getCategoryIcon(sub.name, sub.iconName),
                                                                contentDescription = null,
                                                                tint = color,
                                                                modifier = Modifier.size(14.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(sub.name, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Icon(
                                                                Icons.Default.Close,
                                                                contentDescription = "Delete",
                                                                modifier = Modifier
                                                                    .size(12.dp)
                                                                    .clickable { onDeleteCategory(sub) },
                                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (index < mainCategories.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
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

    // Add Loan & EMI Dialog
    if (showAddEmiDialog) {
        AddLoanEmiDialog(
            onDismiss = { showAddEmiDialog = false },
            onSave = { _, title, amt, day, memo, autoAdd, startDate ->
                onAddRecurring(title, amt, "EMI", day, memo, autoAdd, startDate)
                showAddEmiDialog = false
            }
        )
    }

    // Add Recurring Bill Dialog
    if (showAddRecurringBillDialog) {
        val allBillCategories = (mainCategories.map { it.name } + CategoryHelper.commonExpenseCategories)
            .filter { !it.equals("EMI", ignoreCase = true) }
            .distinct()
        AddRecurringBillDialog(
            categories = allBillCategories,
            onDismiss = { showAddRecurringBillDialog = false },
            onSave = { _, title, amt, cat, day, memo, autoAdd, startDate ->
                onAddRecurring(title, amt, cat, day, memo, autoAdd, startDate)
                showAddRecurringBillDialog = false
            }
        )
    }

    // Edit Loan EMI or Recurring Bill Dialog
    editingRecurringItem?.let { item ->
        val isEmiType = item.category.equals("EMI", ignoreCase = true) ||
                item.title.contains("emi", ignoreCase = true) ||
                item.title.contains("loan", ignoreCase = true)

        if (isEmiType) {
            AddLoanEmiDialog(
                initialRecurring = item,
                onDismiss = { editingRecurringItem = null },
                onSave = { id, title, amt, day, memo, autoAdd, startDate ->
                    onUpdateRecurring(id, title, amt, "EMI", day, memo, autoAdd, startDate)
                    editingRecurringItem = null
                    Toast.makeText(context, "Updated EMI: $title", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            val allBillCategories = (mainCategories.map { it.name } + CategoryHelper.commonExpenseCategories)
                .filter { !it.equals("EMI", ignoreCase = true) }
                .distinct()
            AddRecurringBillDialog(
                initialRecurring = item,
                categories = allBillCategories,
                onDismiss = { editingRecurringItem = null },
                onSave = { id, title, amt, cat, day, memo, autoAdd, startDate ->
                    onUpdateRecurring(id, title, amt, cat, day, memo, autoAdd, startDate)
                    editingRecurringItem = null
                    Toast.makeText(context, "Updated Bill: $title", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    // Add Category or Subcategory Dialog
    if (showAddCategoryDialog) {
        AddCategoryOrSubcatDialog(
            parentCategory = selectedParentForSubcat,
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, parent, type, iconName ->
                onAddCategory(name, parent, type, iconName)
                showAddCategoryDialog = false
            }
        )
    }

    // Add Debt Dialog
    if (showAddDebtDialog) {
        AddDebtDialog(
            onDismiss = { showAddDebtDialog = false },
            onSave = { name, amt, type, memo ->
                onAddDebt(name, amt, type, memo)
                showAddDebtDialog = false
            }
        )
    }

    // Pay Debt Dialog for small partial repayments
    payingDebt?.let { debt ->
        PayDebtDialog(
            debt = debt,
            onDismiss = { payingDebt = null },
            onPay = { amt ->
                onPayDebt(debt, amt)
                payingDebt = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecurringExpenseCard(
    recurring: RecurringExpenseEntity,
    isEmi: Boolean,
    onTriggerNow: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    val accentColor = if (isEmi) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
    val currentYm = remember { java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US).format(java.util.Date()) }
    val isPaidForThisMonth = recurring.lastPostedYearMonth == currentYm
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ExpressiveCardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Line 1: Icon, Title, and Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isEmi) Icons.Default.CreditCard else Icons.Default.Repeat,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
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
                            text = recurring.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = recurring.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = "₹${numberFormat.format(recurring.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = com.example.ui.theme.SFMonoFontFamily,
                        color = ExpenseRed,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Line 2: Badges (Due Date, Auto-Add, Paid Status, Start Date)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        text = "Due: ${recurring.dayOfMonth}th of month",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (recurring.autoAdd) {
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "Auto-Add",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(percent = 50),
                    color = if (isPaidForThisMonth) IncomeGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPaidForThisMonth) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isPaidForThisMonth) IncomeGreen else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPaidForThisMonth) "Recorded This Month" else "Pending Payment",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaidForThisMonth) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (recurring.memo.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Note: ${recurring.memo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Line 3: Actions Bar (Record, Edit, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onTriggerNow,
                    shape = ExpressiveButtonShape,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("trigger_recurring_${recurring.id}")
                ) {
                    Icon(
                        imageVector = if (isPaidForThisMonth) Icons.Default.Check else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isPaidForThisMonth) "Record Again" else "Record to Ledger",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_recurring_${recurring.id}")
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_recurring_${recurring.id}")
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = ExpenseRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DebtCard(
    debt: DebtEntity,
    onToggleSettled: () -> Unit,
    onPay: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    val isBorrowed = debt.type.equals("Borrowed", ignoreCase = true)
    val color = if (debt.isSettled) MaterialTheme.colorScheme.outline else if (isBorrowed) ExpenseRed else IncomeGreen
    val progress = if (debt.amount > 0) (debt.paidAmount / debt.amount).toFloat().coerceIn(0f, 1f) else 1f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = onToggleSettled, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (debt.isSettled) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Toggle Settled",
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = debt.personName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (debt.isSettled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isBorrowed) ExpenseRed.copy(alpha = 0.15f) else IncomeGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isBorrowed) "I Owe" else "Owed to Me",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBorrowed) ExpenseRed else IncomeGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (debt.memo.isNotBlank()) {
                            Text(
                                text = debt.memo,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Debt Balance Progress & Amounts
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(modifier = Modifier.padding(end = 12.dp)) {
                    Text(
                        text = if (debt.isSettled) "Settled in Full" else "Remaining Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (debt.isSettled) "₹0.00" else "₹${numberFormat.format(debt.remainingAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SFMonoFontFamily,
                        color = color,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Column {
                    Text(
                        text = "Total: ₹${numberFormat.format(debt.amount)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = SFMonoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "Paid: ₹${numberFormat.format(debt.paidAmount)}",
                        fontSize = 11.sp,
                        fontFamily = SFMonoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = IncomeGreen,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (debt.isSettled) MaterialTheme.colorScheme.outline else if (isBorrowed) ExpenseRed else IncomeGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (!debt.isSettled) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onPay,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBorrowed) "Pay Off / Partial Pay" else "Record Received Amount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PayDebtDialog(
    debt: DebtEntity,
    onDismiss: () -> Unit,
    onPay: (Double) -> Unit
) {
    var payAmountText by remember { mutableStateOf("") }
    val numberFormat = remember { DecimalFormat("#,##,##0.00") }
    val remaining = debt.remainingAmount

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.92f),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (debt.type == "Borrowed") "Pay Off Debt" else "Record Received Payment",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${debt.personName} (${debt.type})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    val totalDebtStr = "₹${numberFormat.format(debt.amount)}"
                    val paidDebtStr = "₹${numberFormat.format(debt.paidAmount)}"
                    val remDebtStr = "₹${numberFormat.format(remaining)}"
                    val smallDebtStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, fontFamily = SFMonoFontFamily)
                    val dueDebtStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFamily = SFMonoFontFamily)

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        val fitsInThreeCols = com.example.ui.components.rememberAllFitColumnWidth(
                            textsAndStyles = listOf(
                                totalDebtStr to smallDebtStyle,
                                paidDebtStr to smallDebtStyle,
                                remDebtStr to dueDebtStyle
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
                                    Text("Total Debt", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(totalDebtStr, style = smallDebtStyle, maxLines = 1, softWrap = false)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Already Paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                                    Text(paidDebtStr, style = smallDebtStyle, color = IncomeGreen, maxLines = 1, softWrap = false, textAlign = TextAlign.Center)
                                }
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("Balance Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.End)
                                    Text(remDebtStr, style = dueDebtStyle, color = ExpenseRed, maxLines = 1, softWrap = false, textAlign = TextAlign.End)
                                }
                            }
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column {
                                    Text("Total Debt", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(totalDebtStr, style = smallDebtStyle, maxLines = 1, softWrap = false)
                                }
                                Column {
                                    Text("Already Paid", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(paidDebtStr, style = smallDebtStyle, color = IncomeGreen, maxLines = 1, softWrap = false)
                                }
                                Column {
                                    Text("Balance Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(remDebtStr, style = dueDebtStyle, color = ExpenseRed, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = payAmountText,
                    onValueChange = { payAmountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Payment Amount (₹)") },
                    placeholder = { Text("e.g. 500") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Quick Amounts:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(100.0, 500.0, 1000.0, 2000.0, 5000.0).forEach { quickVal ->
                        if (quickVal <= remaining) {
                            FilterChip(
                                selected = payAmountText == quickVal.toInt().toString(),
                                onClick = { payAmountText = quickVal.toInt().toString() },
                                label = { Text("+₹${quickVal.toInt()}") }
                            )
                        }
                    }
                    FilterChip(
                        selected = payAmountText == remaining.toString(),
                        onClick = { payAmountText = if (remaining % 1.0 == 0.0) remaining.toLong().toString() else remaining.toString() },
                        label = { Text("Full (₹${numberFormat.format(remaining)})", fontFamily = SFMonoFontFamily) }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp),
                        shape = ExpressiveButtonShape
                    ) {
                        Text("Cancel", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Button(
                        onClick = {
                            val parsed = payAmountText.toDoubleOrNull() ?: 0.0
                            if (parsed > 0) {
                                onPay(parsed)
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .defaultMinSize(minHeight = 48.dp),
                        shape = ExpressiveButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            "Record Payoff",
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

@Composable
fun AddLoanEmiDialog(
    initialRecurring: RecurringExpenseEntity? = null,
    onDismiss: () -> Unit,
    onSave: (id: Long, title: String, amount: Double, day: Int, memo: String, autoAdd: Boolean, startDate: Long) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialRecurring?.title ?: "") }
    var amount by remember { mutableStateOf(initialRecurring?.amount?.let { if (it > 0) it.toString() else "" } ?: "") }
    var dayOfMonth by remember { mutableStateOf(initialRecurring?.dayOfMonth?.toString() ?: "10") }
    var memo by remember { mutableStateOf(initialRecurring?.memo ?: "") }
    var autoAdd by remember { mutableStateOf(initialRecurring?.autoAdd ?: true) }
    var startDateMillis by remember {
        mutableLongStateOf(
            if (initialRecurring != null && initialRecurring.startDate > 0L) initialRecurring.startDate else System.currentTimeMillis()
        )
    }
    val startDateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = ExpressiveDialogShape,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialRecurring == null) "Add Loan / EMI" else "Edit Loan / EMI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (initialRecurring != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = ExpressiveChipShape,
                        color = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Past recorded installments in your ledger remain unchanged. The updated amount only applies to upcoming payments.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Loan / EMI Title") },
                    placeholder = { Text("e.g. Car EMI, Home Loan") },
                    singleLine = true,
                    shape = ExpressiveButtonShape,
                    modifier = Modifier.fillMaxWidth().testTag("emi_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monthly Installment Amount") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = ExpressiveButtonShape,
                    modifier = Modifier.fillMaxWidth().testTag("emi_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dayOfMonth,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }.take(2)
                        val num = clean.toIntOrNull()
                        if (clean.isEmpty() || (num != null && num in 1..31)) {
                            dayOfMonth = clean
                        }
                    },
                    label = { Text("Due Day of Month (1 - 31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = ExpressiveButtonShape,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Start From Date selector
                Text(
                    text = "Start From Date",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ExpressiveButtonShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)), ExpressiveButtonShape)
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = startDateMillis }
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, day)
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    startDateMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            startDateFormat.format(Date(startDateMillis)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Auto-added only on/after this date",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Start Date",
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("Memo / Note (optional)") },
                    singleLine = true,
                    shape = ExpressiveButtonShape,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ExpressiveChipShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Add to Ledger", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Automatically records when due date arrives", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoAdd,
                        onCheckedChange = { autoAdd = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF8B5CF6))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val parsedAmt = amount.toDoubleOrNull() ?: 0.0
                        val parsedDay = dayOfMonth.toIntOrNull()?.coerceIn(1, 31) ?: 10
                        if (title.isNotBlank() && parsedAmt > 0) {
                            onSave(initialRecurring?.id ?: 0L, title.trim(), parsedAmt, parsedDay, memo.trim(), autoAdd, startDateMillis)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("save_emi_button"),
                    shape = ExpressiveButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    Text(
                        if (initialRecurring == null) "Save Loan EMI" else "Update Upcoming EMIs",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun AddRecurringBillDialog(
    initialRecurring: RecurringExpenseEntity? = null,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (id: Long, title: String, amount: Double, category: String, day: Int, memo: String, autoAdd: Boolean, startDate: Long) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialRecurring?.title ?: "") }
    var amount by remember { mutableStateOf(initialRecurring?.amount?.let { if (it > 0) it.toString() else "" } ?: "") }
    var selectedCategory by remember { mutableStateOf(initialRecurring?.category ?: (categories.firstOrNull() ?: "Home Expenses")) }
    var categoryDropdownOpen by remember { mutableStateOf(false) }
    var categorySearchQuery by remember { mutableStateOf("") }
    var dayOfMonth by remember { mutableStateOf(initialRecurring?.dayOfMonth?.toString() ?: "5") }
    var memo by remember { mutableStateOf(initialRecurring?.memo ?: "") }
    var autoAdd by remember { mutableStateOf(initialRecurring?.autoAdd ?: true) }
    var startDateMillis by remember {
        mutableLongStateOf(
            if (initialRecurring != null && initialRecurring.startDate > 0L) initialRecurring.startDate else System.currentTimeMillis()
        )
    }
    val startDateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.US) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = ExpressiveDialogShape,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialRecurring == null) "Add Recurring Bill" else "Edit Recurring Bill",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (initialRecurring != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = ExpressiveChipShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Past recorded bills in your ledger remain unchanged. The updated amount only applies to upcoming records.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Title (e.g. WiFi, Rent, Maid)") },
                    singleLine = true,
                    shape = ExpressiveButtonShape,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monthly Amount") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = ExpressiveButtonShape,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            IconButton(onClick = { categoryDropdownOpen = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Category")
                            }
                        },
                        shape = ExpressiveButtonShape,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Full clickable overlay to reliably open category dropdown from anywhere on the field
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
                        shape = ExpressiveMenuShape,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .heightIn(max = 320.dp)
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
                                            modifier = Modifier.fillMaxWidth()
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

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = dayOfMonth,
                    onValueChange = { input ->
                        val clean = input.filter { it.isDigit() }.take(2)
                        val num = clean.toIntOrNull()
                        if (clean.isEmpty() || (num != null && num in 1..31)) {
                            dayOfMonth = clean
                        }
                    },
                    label = { Text("Due Day of Month (1 - 31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = ExpressiveButtonShape,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Start From Date selector
                Text(
                    text = "Start From Date",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ExpressiveButtonShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)), ExpressiveButtonShape)
                        .clickable {
                            val cal = Calendar.getInstance().apply { timeInMillis = startDateMillis }
                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val newCal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, day)
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    startDateMillis = newCal.timeInMillis
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            startDateFormat.format(Date(startDateMillis)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Auto-added only on/after this date",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Select Start Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("Memo / Notes (optional)") },
                    singleLine = true,
                    shape = ExpressiveButtonShape,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ExpressiveChipShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Add to Ledger", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text("Automatically records bill on due date", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoAdd,
                        onCheckedChange = { autoAdd = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val parsedAmt = amount.toDoubleOrNull() ?: 0.0
                        val parsedDay = dayOfMonth.toIntOrNull()?.coerceIn(1, 31) ?: 5
                        if (title.isNotBlank() && parsedAmt > 0) {
                            onSave(initialRecurring?.id ?: 0L, title.trim(), parsedAmt, selectedCategory, parsedDay, memo.trim(), autoAdd, startDateMillis)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = ExpressiveButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        if (initialRecurring == null) "Save Recurring Bill" else "Update Upcoming Bills",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun AddDebtDialog(
    onDismiss: () -> Unit,
    onSave: (personName: String, amount: Double, type: String, memo: String) -> Unit
) {
    var personName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Borrowed") }
    var memo by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add Debt / Loan Entry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "Borrowed",
                        onClick = { type = "Borrowed" },
                        label = { Text("I Borrowed (I Owe)") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExpenseRed,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = type == "Lent",
                        onClick = { type = "Lent" },
                        label = { Text("I Lent (To Collect)") },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IncomeGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("Person Name") },
                    placeholder = { Text("e.g. John, Appa, Friend") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("Memo / Note (optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val parsedAmt = amount.toDoubleOrNull() ?: 0.0
                        if (personName.isNotBlank() && parsedAmt > 0) {
                            onSave(personName.trim(), parsedAmt, type, memo.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp),
                    shape = ExpressiveButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        "Save Debt Entry",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun AddCategoryOrSubcatDialog(
    parentCategory: String?,
    onDismiss: () -> Unit,
    onSave: (name: String, parentCategory: String?, type: String, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Expense") }
    var selectedIconKey by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState())) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (parentCategory != null) "Add Subcategory" else "Add New Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (parentCategory != null) {
                    Text(
                        text = "Parent: $parentCategory",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (parentCategory != null) "Subcategory Name (e.g. Bike, Car)" else "Category Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (parentCategory == null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = type == "Expense",
                            onClick = { type = "Expense" },
                            label = { Text("Expense") }
                        )
                        FilterChip(
                            selected = type == "Income",
                            onClick = { type = "Income" },
                            label = { Text("Income") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Pick Icon (Optional)",
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
                        val isChosen = selectedIconKey == opt.key
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .border(
                                    BorderStroke(if (isChosen) 2.dp else 1.dp, if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                                    CircleShape
                                )
                                .clickable { selectedIconKey = if (isChosen) "" else opt.key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = opt.icon,
                                contentDescription = opt.label,
                                tint = if (isChosen) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(name.trim(), parentCategory, type, selectedIconKey)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = ExpressiveButtonShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
