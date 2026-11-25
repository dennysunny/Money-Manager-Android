package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DebtEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.RecurringExpenseEntity
import com.example.data.model.TransactionEntity
import com.example.data.repository.MoneyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.ui.components.CategoryHelper

enum class AppTab(val title: String) {
    LEDGER("Ledger"),
    INSIGHTS("Insights"),
    STATISTICS("Stats"),
    BUDGETS("Budgets"),
    EMI("Recurring"),
    DRIVE("Settings")
}

enum class TimeFilter(val label: String) {
    DAY("Day"),
    WEEK("Week"),
    MONTH("Month"),
    QUARTER("Quarter"),
    YEAR("Year"),
    ALL("All Time")
}

data class CategorySpend(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val count: Int
)

data class BudgetProgress(
    val budget: BudgetEntity,
    val effectiveLimit: Double = budget.amountLimit,
    val spent: Double,
    val remaining: Double,
    val progress: Float,
    val isOverBudget: Boolean,
    val transactions: List<TransactionEntity> = emptyList()
)

data class MonthlySpendComparison(
    val monthYearKey: String,
    val monthLabel: String,
    val yearLabel: String,
    val fullLabel: String,
    val totalExpense: Double,
    val totalIncome: Double,
    val netSavings: Double,
    val transactionCount: Int,
    val deltaAmountVsPrev: Double,
    val deltaPercentVsPrev: Float,
    val topCategory: String
)

data class CategoryMoMComparison(
    val category: String,
    val currentMonthAmount: Double,
    val previousMonthAmount: Double,
    val deltaAmount: Double,
    val deltaPercent: Float,
    val percentageOfTotal: Float,
    val transactionCount: Int,
    val avgPerTx: Double
)

data class SpendingVelocity(
    val currentMonthDailyAvg: Double,
    val prevMonthDailyAvg: Double,
    val daysElapsed: Int,
    val totalDaysInMonth: Int,
    val projectedMonthTotal: Double,
    val projectedDifference: Double
)

data class CategoryShift(
    val category: String,
    val currentAmount: Double,
    val prevAmount: Double,
    val deltaAmount: Double,
    val deltaPercent: Float,
    val isIncrease: Boolean
)

data class InsightsData(
    val currentMonthLabel: String = "",
    val prevMonthLabel: String = "",
    val currentMonthExpense: Double = 0.0,
    val prevMonthExpense: Double = 0.0,
    val momDeltaAmount: Double = 0.0,
    val momDeltaPercent: Float = 0.0f,
    val monthlyComparisons: List<MonthlySpendComparison> = emptyList(),
    val topCategories: List<CategoryMoMComparison> = emptyList(),
    val biggestSpikes: List<CategoryShift> = emptyList(),
    val biggestSavings: List<CategoryShift> = emptyList(),
    val spendingVelocity: SpendingVelocity = SpendingVelocity(0.0, 0.0, 1, 30, 0.0, 0.0),
    val totalTransactionsAnalyzed: Int = 0
)

data class TrendBarData(
    val label: String,
    val amount: Double,
    val heightRatio: Float
)

data class PaymentModeStat(
    val mode: String,
    val totalExpense: Double,
    val percentage: Float,
    val count: Int,
    val transactions: List<TransactionEntity>
)

data class HabitWithStatus(
    val habit: HabitEntity,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val totalCompletionsThisMonth: Int,
    val completionRate: Float,
    val recent7DaysStatus: List<Boolean>
)

data class HabitCorrelationData(
    val habitTitle: String,
    val targetCategory: String,
    val completedDaysCount: Int,
    val missedDaysCount: Int,
    val avgSpendOnCompletedDays: Double,
    val avgSpendOnMissedDays: Double,
    val savingsPerDay: Double,
    val totalEstimatedSavings: Double,
    val correlationDescription: String
)

data class HabitTrackerState(
    val todayDateString: String = "",
    val habits: List<HabitWithStatus> = emptyList(),
    val correlations: List<HabitCorrelationData> = emptyList(),
    val overallHabitAdherence: Float = 0f,
    val avgSpendHighAdherenceDays: Double = 0.0,
    val avgSpendLowAdherenceDays: Double = 0.0,
    val totalEstimatedHabitSavings: Double = 0.0
)

class MoneyViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = MoneyRepository(
        context = application,
        database = database,
        transactionDao = database.transactionDao(),
        categoryDao = database.categoryDao(),
        budgetDao = database.budgetDao(),
        recurringDao = database.recurringExpenseDao(),
        debtDao = database.debtDao(),
        habitDao = database.habitDao()
    )

    // Current Active Tab
    private val _currentTab = MutableStateFlow(AppTab.LEDGER)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Time & Filter states
    private val _selectedTimeFilter = MutableStateFlow(TimeFilter.MONTH)
    val selectedTimeFilter: StateFlow<TimeFilter> = _selectedTimeFilter.asStateFlow()

    // Reference calendar date for period
    private val _currentReferenceDate = MutableStateFlow(Calendar.getInstance())
    val currentReferenceDate: StateFlow<Calendar> = _currentReferenceDate.asStateFlow()

    // Search query in ledger
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Type filter in ledger ("All", "Expense", "Income")
    private val _typeFilter = MutableStateFlow("All")
    val typeFilter: StateFlow<String> = _typeFilter.asStateFlow()

    // Selected day in Calendar tab
    private val _selectedCalendarDate = MutableStateFlow(Date())
    val selectedCalendarDate: StateFlow<Date> = _selectedCalendarDate.asStateFlow()

    // Category Drill-Down in Stats
    private val _selectedStatCategory = MutableStateFlow<String?>(null)
    val selectedStatCategory: StateFlow<String?> = _selectedStatCategory.asStateFlow()

    private val _statCategoryTimeFilter = MutableStateFlow(TimeFilter.MONTH)
    val statCategoryTimeFilter: StateFlow<TimeFilter> = _statCategoryTimeFilter.asStateFlow()

    // All raw data streams
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mainCategories: StateFlow<List<CategoryEntity>> = repository.mainCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecurring: StateFlow<List<RecurringExpenseEntity>> = repository.allRecurring
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDebts: StateFlow<List<DebtEntity>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabitLogs: StateFlow<List<HabitLogEntity>> = repository.allHabitLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitTrackerState: StateFlow<HabitTrackerState> = combine(
        allTransactions,
        repository.allHabits,
        repository.allHabitLogs
    ) { transactions, habits, logs ->
        calculateHabitTracker(transactions, habits, logs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitTrackerState())

    val lastDriveSyncTime: StateFlow<Long> = repository.lastDriveSyncTime
    val isDriveSyncing: StateFlow<Boolean> = repository.isDriveSyncing
    val driveSyncAccount: StateFlow<String> = repository.driveSyncAccount
    val driveAutoSync: StateFlow<Boolean> = repository.driveAutoSync

    // Sorted categories with MOST USED on top, guaranteeing all categories are present
    val sortedExpenseCategories: StateFlow<List<String>> = combine(allTransactions, allCategories) { transactions, cats ->
        val frequency = transactions
            .filter { it.type.equals("Expense", ignoreCase = true) }
            .groupingBy { it.category }
            .eachCount()

        val dbExpenseNames = cats.filter { it.type.equals("Expense", ignoreCase = true) }.map { it.name }
        val transactionCats = transactions.filter { it.type.equals("Expense", ignoreCase = true) }.map { it.category }
        val allExpense = (CategoryHelper.commonExpenseCategories + dbExpenseNames + transactionCats)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        allExpense.sortedWith(
            compareByDescending<String> { frequency[it] ?: 0 }
                .thenBy { it.lowercase() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryHelper.commonExpenseCategories)

    val sortedIncomeCategories: StateFlow<List<String>> = combine(allTransactions, allCategories) { transactions, cats ->
        val frequency = transactions
            .filter { it.type.equals("Income", ignoreCase = true) }
            .groupingBy { it.category }
            .eachCount()

        val dbIncomeNames = cats.filter { it.type.equals("Income", ignoreCase = true) }.map { it.name }
        val transactionCats = transactions.filter { it.type.equals("Income", ignoreCase = true) }.map { it.category }
        val allIncome = (CategoryHelper.commonIncomeCategories + dbIncomeNames + transactionCats)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        allIncome.sortedWith(
            compareByDescending<String> { frequency[it] ?: 0 }
                .thenBy { it.lowercase() }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryHelper.commonIncomeCategories)

    // Subcategories map (Parent Category Name -> List<String>)
    val subcategoriesByParent: StateFlow<Map<String, List<String>>> = allCategories.combine(MutableStateFlow(0)) { cats, _ ->
        cats.filter { it.parentCategory != null }
            .groupBy { it.parentCategory!! }
            .mapValues { (_, list) -> list.map { it.name } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Filtered transactions based on timeframe, search, and type
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _selectedTimeFilter,
        _currentReferenceDate,
        _searchQuery,
        _typeFilter
    ) { transactions, timeFilter, refCal, query, type ->
        val (startTime, endTime) = calculateTimeRange(timeFilter, refCal)
        transactions.filter { tx ->
            val withinTime = if (timeFilter == TimeFilter.ALL) true else (tx.date in startTime..endTime)
            val matchesQuery = query.isBlank() ||
                    tx.category.contains(query, ignoreCase = true) ||
                    tx.subcategory.contains(query, ignoreCase = true) ||
                    tx.memo.contains(query, ignoreCase = true) ||
                    tx.wallet.contains(query, ignoreCase = true)
            val matchesType = when (type) {
                "Expense" -> tx.type.equals("Expense", ignoreCase = true)
                "Income" -> tx.type.equals("Income", ignoreCase = true)
                else -> true
            }
            withinTime && matchesQuery && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpense: StateFlow<Double> = filteredTransactions.combine(MutableStateFlow(0)) { list, _ ->
        list.filter { it.type.equals("Expense", ignoreCase = true) }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncome: StateFlow<Double> = filteredTransactions.combine(MutableStateFlow(0)) { list, _ ->
        list.filter { it.type.equals("Income", ignoreCase = true) }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val categoryBreakdown: StateFlow<List<CategorySpend>> = filteredTransactions.combine(totalExpense) { list, totalExp ->
        val expenseItems = list.filter { it.type.equals("Expense", ignoreCase = true) }
        val grouped = expenseItems.groupBy { it.category }
        grouped.map { (cat, items) ->
            val catTotal = items.sumOf { it.amount }
            val pct = if (totalExp > 0) (catTotal / totalExp).toFloat() else 0f
            CategorySpend(
                category = cat,
                amount = catTotal,
                percentage = pct,
                count = items.size
            )
        }.sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun calculateEffectiveBudgetLimit(budget: BudgetEntity, timeFilter: TimeFilter): Double {
        val base = budget.amountLimit
        val period = budget.period.lowercase()
        return when (timeFilter) {
            TimeFilter.YEAR -> {
                when {
                    period.contains("month") -> base * 12.0
                    period.contains("quarter") -> base * 4.0
                    period.contains("week") -> base * 52.0
                    else -> base
                }
            }
            TimeFilter.QUARTER -> {
                when {
                    period.contains("month") -> base * 3.0
                    period.contains("year") -> base / 4.0
                    period.contains("week") -> base * 13.0
                    else -> base
                }
            }
            TimeFilter.MONTH -> {
                when {
                    period.contains("year") -> base / 12.0
                    period.contains("quarter") -> base / 3.0
                    period.contains("week") -> base * 4.33
                    else -> base
                }
            }
            TimeFilter.WEEK -> {
                when {
                    period.contains("month") -> base / 4.33
                    period.contains("year") -> base / 52.0
                    period.contains("quarter") -> base / 13.0
                    else -> base
                }
            }
            TimeFilter.DAY -> {
                when {
                    period.contains("month") -> base / 30.0
                    period.contains("year") -> base / 365.0
                    period.contains("week") -> base / 7.0
                    else -> base
                }
            }
            TimeFilter.ALL -> {
                when {
                    period.contains("month") -> base * 12.0
                    else -> base
                }
            }
        }
    }

    val budgetProgressList: StateFlow<List<BudgetProgress>> = combine(
        allBudgets,
        filteredTransactions,
        _selectedTimeFilter
    ) { budgets, transactions, timeFilter ->
        val expensesByCategory = transactions
            .filter { it.type.equals("Expense", ignoreCase = true) }
            .groupBy { it.category }

        budgets.map { b ->
            val bTxs = expensesByCategory[b.category] ?: emptyList()
            val spent = bTxs.sumOf { it.amount }
            val effectiveLimit = calculateEffectiveBudgetLimit(b, timeFilter)
            val remaining = (effectiveLimit - spent).coerceAtLeast(0.0)
            val progress = if (effectiveLimit > 0) (spent / effectiveLimit).toFloat() else 0f
            val isOver = spent > effectiveLimit
            BudgetProgress(
                budget = b,
                effectiveLimit = effectiveLimit,
                spent = spent,
                remaining = remaining,
                progress = progress,
                isOverBudget = isOver,
                transactions = bTxs.sortedByDescending { it.date }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category Drill-Down Data
    val categoryDrillDownTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        _selectedStatCategory,
        _selectedTimeFilter,
        _currentReferenceDate
    ) { txs, selectedCat, filter, refCal ->
        if (selectedCat == null) emptyList()
        else {
            val (start, end) = calculateTimeRange(filter, refCal)
            txs.filter { it.category.equals(selectedCat, ignoreCase = true) && (filter == TimeFilter.ALL || it.date in start..end) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Trend bars data for the category drill-down
    val categoryTrendBars: StateFlow<List<TrendBarData>> = combine(
        categoryDrillDownTransactions,
        _selectedTimeFilter
    ) { txs, filter ->
        if (txs.isEmpty()) emptyList()
        else {
            val groupingFormat = when (filter) {
                TimeFilter.DAY -> SimpleDateFormat("h a", Locale.US)
                TimeFilter.WEEK -> SimpleDateFormat("EEE", Locale.US)
                TimeFilter.MONTH -> SimpleDateFormat("dd MMM", Locale.US)
                TimeFilter.QUARTER -> SimpleDateFormat("MMM", Locale.US)
                TimeFilter.YEAR -> SimpleDateFormat("MMM", Locale.US)
                TimeFilter.ALL -> SimpleDateFormat("yyyy", Locale.US)
            }
            val grouped = txs.groupBy { groupingFormat.format(Date(it.date)) }
            val sortedGrouped = grouped.toList().sortedBy { (_, list) -> list.minOf { it.date } }
            val maxSpend = sortedGrouped.maxOfOrNull { (_, list) -> list.sumOf { it.amount } } ?: 1.0

            sortedGrouped.map { (label, items) ->
                val sum = items.sumOf { it.amount }
                val ratio = (sum / maxSpend).toFloat().coerceIn(0.05f, 1f)
                TrendBarData(label = label, amount = sum, heightRatio = ratio)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overall Trend bars data for the entire current period
    val overallTrendBars: StateFlow<List<TrendBarData>> = combine(
        filteredTransactions,
        _selectedTimeFilter
    ) { txs, filter ->
        val expenseTxs = txs.filter { it.type.equals("Expense", ignoreCase = true) }
        if (expenseTxs.isEmpty()) emptyList()
        else {
            val groupingFormat = when (filter) {
                TimeFilter.DAY -> SimpleDateFormat("h a", Locale.US)
                TimeFilter.WEEK -> SimpleDateFormat("EEE", Locale.US)
                TimeFilter.MONTH -> SimpleDateFormat("dd MMM", Locale.US)
                TimeFilter.QUARTER -> SimpleDateFormat("MMM", Locale.US)
                TimeFilter.YEAR -> SimpleDateFormat("MMM", Locale.US)
                TimeFilter.ALL -> SimpleDateFormat("yyyy", Locale.US)
            }
            val grouped = expenseTxs.groupBy { groupingFormat.format(Date(it.date)) }
            val sortedGrouped = grouped.toList().sortedBy { (_, list) -> list.minOf { it.date } }
            val maxSpend = sortedGrouped.maxOfOrNull { (_, list) -> list.sumOf { it.amount } } ?: 1.0

            sortedGrouped.map { (label, items) ->
                val sum = items.sumOf { it.amount }
                val ratio = (sum / maxSpend).toFloat().coerceIn(0.05f, 1f)
                TrendBarData(label = label, amount = sum, heightRatio = ratio)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Payment Mode stats: Cash / UPI vs Credit Card
    val paymentModeStats: StateFlow<List<PaymentModeStat>> = filteredTransactions.map { txs ->
        val expenseTxs = txs.filter { it.type.equals("Expense", ignoreCase = true) }
        val totalExp = expenseTxs.sumOf { it.amount }

        val cardList = expenseTxs.filter {
            it.wallet.contains("card", ignoreCase = true) || it.wallet.contains("credit", ignoreCase = true)
        }
        val cashUpiList = expenseTxs.filter {
            !it.wallet.contains("card", ignoreCase = true) && !it.wallet.contains("credit", ignoreCase = true)
        }

        val cashUpiTotal = cashUpiList.sumOf { it.amount }
        val cardTotal = cardList.sumOf { it.amount }

        listOf(
            PaymentModeStat("Cash / UPI", cashUpiTotal, if (totalExp > 0) (cashUpiTotal / totalExp).toFloat() else 0f, cashUpiList.size, cashUpiList),
            PaymentModeStat("Credit Card", cardTotal, if (totalExp > 0) (cardTotal / totalExp).toFloat() else 0f, cardList.size, cardList)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dedicated Insights & Trend Analysis derived from Room transactions
    val insightsData: StateFlow<InsightsData> = allTransactions.map { transactions ->
        calculateInsights(transactions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InsightsData())

    private fun calculateInsights(transactions: List<TransactionEntity>): InsightsData {
        val expenseTxs = transactions.filter { it.type.equals("Expense", ignoreCase = true) }
        val incomeTxs = transactions.filter { it.type.equals("Income", ignoreCase = true) }

        val cal = Calendar.getInstance()
        val monthFmt = SimpleDateFormat("MMM", Locale.US)
        val fullMonthFmt = SimpleDateFormat("MMMM yyyy", Locale.US)
        val keyFmt = SimpleDateFormat("yyyy-MM", Locale.US)
        val yearFmt = SimpleDateFormat("yyyy", Locale.US)

        data class MonthRange(
            val key: String,
            val label: String,
            val fullLabel: String,
            val year: String,
            val startTime: Long,
            val endTime: Long,
            val monthIndex: Int
        )

        // Previous 6 calendar months (0 = current month, 1 = last month, ... 5 = 5 months ago)
        val monthRanges = (0..5).map { offset ->
            val mCal = (cal.clone() as Calendar).apply {
                add(Calendar.MONTH, -offset)
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startTime = mCal.timeInMillis
            val maxDay = mCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val endCal = (mCal.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, maxDay)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val endTime = endCal.timeInMillis

            MonthRange(
                key = keyFmt.format(mCal.time),
                label = monthFmt.format(mCal.time),
                fullLabel = fullMonthFmt.format(mCal.time),
                year = yearFmt.format(mCal.time),
                startTime = startTime,
                endTime = endTime,
                monthIndex = offset
            )
        }

        // Monthly totals
        val rawMonthlyData = monthRanges.map { range ->
            val monthExpenses = expenseTxs.filter { it.date in range.startTime..range.endTime }
            val monthIncomes = incomeTxs.filter { it.date in range.startTime..range.endTime }
            val totalExp = monthExpenses.sumOf { it.amount }
            val totalInc = monthIncomes.sumOf { it.amount }
            val topCat = monthExpenses.groupBy { it.category }
                .maxByOrNull { (_, list) -> list.sumOf { it.amount } }?.key ?: "None"
            Triple(range, totalExp, Triple(totalInc, monthExpenses, topCat))
        }

        // Reverse to chronological order (oldest to newest) for charts
        val comparisonsChronological = rawMonthlyData.reversed().mapIndexed { index, (range, exp, rest) ->
            val (inc, expList, topCat) = rest
            val prevMonthExp = if (index > 0) rawMonthlyData.reversed()[index - 1].second else 0.0
            val deltaAmt = if (index > 0) exp - prevMonthExp else 0.0
            val deltaPct = if (index > 0 && prevMonthExp > 0) ((deltaAmt / prevMonthExp) * 100).toFloat() else 0f

            MonthlySpendComparison(
                monthYearKey = range.key,
                monthLabel = range.label,
                yearLabel = range.year,
                fullLabel = range.fullLabel,
                totalExpense = exp,
                totalIncome = inc,
                netSavings = inc - exp,
                transactionCount = expList.size,
                deltaAmountVsPrev = deltaAmt,
                deltaPercentVsPrev = deltaPct,
                topCategory = topCat
            )
        }

        val currentMonthRange = monthRanges[0]
        val prevMonthRange = monthRanges[1]

        val currentMonthExpenses = expenseTxs.filter { it.date in currentMonthRange.startTime..currentMonthRange.endTime }
        val prevMonthExpenses = expenseTxs.filter { it.date in prevMonthRange.startTime..prevMonthRange.endTime }

        val currentMonthTotal = currentMonthExpenses.sumOf { it.amount }
        val prevMonthTotal = prevMonthExpenses.sumOf { it.amount }

        val momDeltaAmount = currentMonthTotal - prevMonthTotal
        val momDeltaPercent = if (prevMonthTotal > 0) ((momDeltaAmount / prevMonthTotal) * 100).toFloat() else 0f

        // Top categories in current month with MoM comparison
        val prevByCat = prevMonthExpenses.groupBy { it.category }.mapValues { (_, list) -> list.sumOf { it.amount } }
        val currByCat = currentMonthExpenses.groupBy { it.category }

        val topCats = currByCat.map { (cat, list) ->
            val spent = list.sumOf { it.amount }
            val prevSpent = prevByCat[cat] ?: 0.0
            val deltaAmt = spent - prevSpent
            val deltaPct = if (prevSpent > 0) ((deltaAmt / prevSpent) * 100).toFloat() else 0f
            val share = if (currentMonthTotal > 0) (spent / currentMonthTotal).toFloat() else 0f

            CategoryMoMComparison(
                category = cat,
                currentMonthAmount = spent,
                previousMonthAmount = prevSpent,
                deltaAmount = deltaAmt,
                deltaPercent = deltaPct,
                percentageOfTotal = share,
                transactionCount = list.size,
                avgPerTx = if (list.isNotEmpty()) spent / list.size else 0.0
            )
        }.sortedByDescending { it.currentMonthAmount }

        // Category shifts: Biggest Spikes & Biggest Savings
        val allCatsMerged = (currByCat.keys + prevByCat.keys).distinct()
        val shifts = allCatsMerged.map { cat ->
            val curr = currByCat[cat]?.sumOf { it.amount } ?: 0.0
            val prev = prevByCat[cat] ?: 0.0
            val delta = curr - prev
            val deltaPct = if (prev > 0) ((delta / prev) * 100).toFloat() else 0f
            CategoryShift(
                category = cat,
                currentAmount = curr,
                prevAmount = prev,
                deltaAmount = delta,
                deltaPercent = deltaPct,
                isIncrease = delta > 0
            )
        }

        val biggestSpikes = shifts.filter { it.deltaAmount > 0 }.sortedByDescending { it.deltaAmount }.take(3)
        val biggestSavings = shifts.filter { it.deltaAmount < 0 }.sortedBy { it.deltaAmount }.take(3)

        // Daily Velocity & Projections
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val prevMonthDays = (cal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }.getActualMaximum(Calendar.DAY_OF_MONTH)

        val currentDailyAvg = if (currentDay > 0) currentMonthTotal / currentDay else 0.0
        val prevDailyAvg = if (prevMonthDays > 0) prevMonthTotal / prevMonthDays else 0.0
        val projectedTotal = currentDailyAvg * totalDays
        val projectedDiff = projectedTotal - prevMonthTotal

        val velocity = SpendingVelocity(
            currentMonthDailyAvg = currentDailyAvg,
            prevMonthDailyAvg = prevDailyAvg,
            daysElapsed = currentDay,
            totalDaysInMonth = totalDays,
            projectedMonthTotal = projectedTotal,
            projectedDifference = projectedDiff
        )

        return InsightsData(
            currentMonthLabel = currentMonthRange.label,
            prevMonthLabel = prevMonthRange.label,
            currentMonthExpense = currentMonthTotal,
            prevMonthExpense = prevMonthTotal,
            momDeltaAmount = momDeltaAmount,
            momDeltaPercent = momDeltaPercent,
            monthlyComparisons = comparisonsChronological,
            topCategories = topCats,
            biggestSpikes = biggestSpikes,
            biggestSavings = biggestSavings,
            spendingVelocity = velocity,
            totalTransactionsAnalyzed = expenseTxs.size
        )
    }

    private fun calculateHabitTracker(
        transactions: List<TransactionEntity>,
        habits: List<HabitEntity>,
        logs: List<HabitLogEntity>
    ): HabitTrackerState {
        val todayCal = Calendar.getInstance()
        val ymdFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = ymdFmt.format(todayCal.time)
        val currentYearMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(todayCal.time)

        // Generate past 7 days strings [day-6, day-5, ... today]
        val past7Days = (6 downTo 0).map { offset ->
            val c = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -offset) }
            ymdFmt.format(c.time)
        }

        val logsByHabit = logs.groupBy { it.habitId }

        val habitWithStatusList = habits.map { habit ->
            val habitLogs = logsByHabit[habit.id] ?: emptyList()
            val completedDatesSet = habitLogs.filter { it.isCompleted }.map { it.dateString }.toSet()

            val isCompletedToday = completedDatesSet.contains(todayStr)

            var streak = 0
            val checkCal = todayCal.clone() as Calendar
            if (!isCompletedToday) {
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            }
            while (true) {
                val checkStr = ymdFmt.format(checkCal.time)
                if (completedDatesSet.contains(checkStr)) {
                    streak++
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }

            val completionsThisMonth = habitLogs.count { it.dateString.startsWith(currentYearMonth) && it.isCompleted }
            val dayOfMonth = todayCal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
            val rate = (completionsThisMonth.toFloat() / dayOfMonth).coerceIn(0f, 1f)

            val recent7 = past7Days.map { d -> completedDatesSet.contains(d) }

            HabitWithStatus(
                habit = habit,
                isCompletedToday = isCompletedToday,
                currentStreak = streak,
                totalCompletionsThisMonth = completionsThisMonth,
                completionRate = rate,
                recent7DaysStatus = recent7
            )
        }

        // Calculate Correlations with actual Room spending records
        val expenseTxs = transactions.filter { it.type.equals("Expense", ignoreCase = true) }
        val expensesByDate = expenseTxs.groupBy {
            ymdFmt.format(Date(it.date))
        }

        val past30Days = (0..29).map { offset ->
            val c = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -offset) }
            ymdFmt.format(c.time)
        }

        var totalEstimatedSavingsAll = 0.0

        val correlations = habits.map { habit ->
            val habitLogs = logsByHabit[habit.id] ?: emptyList()
            val completedDatesSet = habitLogs.filter { it.isCompleted }.map { it.dateString }.toSet()

            val completedDays = past30Days.filter { completedDatesSet.contains(it) }
            val missedDays = past30Days.filter { !completedDatesSet.contains(it) }

            val targetCat = habit.targetCategory.trim()

            fun getDaySpend(dateStr: String): Double {
                val dayTxs = expensesByDate[dateStr] ?: return 0.0
                return if (targetCat.isNotBlank()) {
                    dayTxs.filter { it.category.contains(targetCat, ignoreCase = true) }.sumOf { it.amount }
                } else {
                    dayTxs.sumOf { it.amount }
                }
            }

            val avgCompleted = if (completedDays.isNotEmpty()) completedDays.map { getDaySpend(it) }.average() else 0.0
            val avgMissed = if (missedDays.isNotEmpty()) missedDays.map { getDaySpend(it) }.average() else 0.0

            val savingsPerDay = (avgMissed - avgCompleted).coerceAtLeast(0.0)
            val totalSavings = savingsPerDay * completedDays.size
            totalEstimatedSavingsAll += totalSavings

            val desc = if (targetCat.isNotBlank()) {
                if (savingsPerDay > 0) {
                    "Avg ₹${avgCompleted.toInt()} on ${targetCat.lowercase()} when completed vs ₹${avgMissed.toInt()} on off days (₹${savingsPerDay.toInt()}/day saved!)"
                } else {
                    "Maintains disciplined ${targetCat.lowercase()} baseline of ₹${avgCompleted.toInt()}/day across ${completedDays.size} active days."
                }
            } else {
                if (savingsPerDay > 0) {
                    "Overall daily spend dropped from ₹${avgMissed.toInt()} to ₹${avgCompleted.toInt()} on completed days."
                } else {
                    "Consistent daily financial tracking across ${completedDays.size} days this month."
                }
            }

            HabitCorrelationData(
                habitTitle = habit.title,
                targetCategory = if (targetCat.isNotBlank()) targetCat else "Overall Spending",
                completedDaysCount = completedDays.size,
                missedDaysCount = missedDays.size,
                avgSpendOnCompletedDays = avgCompleted,
                avgSpendOnMissedDays = avgMissed,
                savingsPerDay = savingsPerDay,
                totalEstimatedSavings = totalSavings,
                correlationDescription = desc
            )
        }

        val completedTodayCount = habitWithStatusList.count { it.isCompletedToday }
        val overallAdherence = if (habits.isNotEmpty()) completedTodayCount.toFloat() / habits.size else 0f

        return HabitTrackerState(
            todayDateString = todayStr,
            habits = habitWithStatusList,
            correlations = correlations,
            overallHabitAdherence = overallAdherence,
            totalEstimatedHabitSavings = totalEstimatedSavingsAll
        )
    }

    fun toggleHabit(habitId: Long, dateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) {
        viewModelScope.launch {
            repository.toggleHabitLog(habitId, dateString)
        }
    }

    fun addCustomHabit(title: String, targetCategory: String = "", description: String = "", iconKey: String = "check") {
        viewModelScope.launch {
            repository.insertHabit(
                HabitEntity(
                    title = title.trim(),
                    targetCategory = targetCategory.trim(),
                    description = description.trim(),
                    iconKey = iconKey
                )
            )
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // Salary and Income settings
    val salaryAmount: StateFlow<Double> = repository.salaryAmount
    val salaryEmployer: StateFlow<String> = repository.salaryEmployer
    val salaryCreditDay: StateFlow<Int> = repository.salaryCreditDay
    val salaryWallet: StateFlow<String> = repository.salaryWallet
    val salaryAutoCredit: StateFlow<Boolean> = repository.salaryAutoCredit
    val salaryLastCreditedMonth: StateFlow<String> = repository.salaryLastCreditedMonth

    // App Theming & Dynamic Wallpaper Tinting
    val appTheme: StateFlow<String> = repository.appTheme
    val wallpaperTintEnabled: StateFlow<Boolean> = repository.wallpaperTintEnabled
    val applyWallpaperTintToApp: StateFlow<Boolean> = repository.applyWallpaperTintToApp
    val applyWallpaperTintToWidgets: StateFlow<Boolean> = repository.applyWallpaperTintToWidgets
    val isAppWallpaperTintActive: StateFlow<Boolean> = combine(
        repository.wallpaperTintEnabled,
        repository.applyWallpaperTintToApp
    ) { master, applyApp -> master && applyApp }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = repository.wallpaperTintEnabled.value && repository.applyWallpaperTintToApp.value
        )

    fun setAppTheme(theme: String) {
        repository.setAppTheme(theme)
    }

    fun setWallpaperTintEnabled(enabled: Boolean) {
        repository.setWallpaperTintEnabled(enabled)
    }

    fun setApplyWallpaperTintToApp(enabled: Boolean) {
        repository.setApplyWallpaperTintToApp(enabled)
    }

    fun setApplyWallpaperTintToWidgets(enabled: Boolean) {
        repository.setApplyWallpaperTintToWidgets(enabled)
    }

    init {
        viewModelScope.launch {
            repository.initializeDatabase()
            repository.processDueRecurringExpenses()
            com.example.widget.WidgetUpdater.updateAllWidgets(application)
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setTimeFilter(filter: TimeFilter) {
        _selectedTimeFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: String) {
        _typeFilter.value = type
    }

    fun setSelectedCalendarDate(date: Date) {
        _selectedCalendarDate.value = date
    }

    fun selectCategoryForDrillDown(category: String?) {
        _selectedStatCategory.value = category
    }

    fun setCategoryDrillDownTimeFilter(filter: TimeFilter) {
        _statCategoryTimeFilter.value = filter
    }

    fun previousPeriod() {
        val cal = (_currentReferenceDate.value.clone() as Calendar)
        when (_selectedTimeFilter.value) {
            TimeFilter.DAY -> cal.add(Calendar.DAY_OF_YEAR, -1)
            TimeFilter.WEEK -> cal.add(Calendar.WEEK_OF_YEAR, -1)
            TimeFilter.MONTH -> cal.add(Calendar.MONTH, -1)
            TimeFilter.QUARTER -> cal.add(Calendar.MONTH, -3)
            TimeFilter.YEAR -> cal.add(Calendar.YEAR, -1)
            TimeFilter.ALL -> Unit
        }
        _currentReferenceDate.value = cal
    }

    fun nextPeriod() {
        val cal = (_currentReferenceDate.value.clone() as Calendar)
        when (_selectedTimeFilter.value) {
            TimeFilter.DAY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            TimeFilter.WEEK -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            TimeFilter.MONTH -> cal.add(Calendar.MONTH, 1)
            TimeFilter.QUARTER -> cal.add(Calendar.MONTH, 3)
            TimeFilter.YEAR -> cal.add(Calendar.YEAR, 1)
            TimeFilter.ALL -> Unit
        }
        _currentReferenceDate.value = cal
    }

    fun resetToToday() {
        _currentReferenceDate.value = Calendar.getInstance()
    }

    fun setReferenceDate(calendar: Calendar) {
        _currentReferenceDate.value = (calendar.clone() as Calendar)
    }

    fun getPeriodLabel(): String {
        val cal = _currentReferenceDate.value
        return when (_selectedTimeFilter.value) {
            TimeFilter.DAY -> SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(cal.time)
            TimeFilter.WEEK -> {
                val start = (cal.clone() as Calendar).apply { set(Calendar.DAY_OF_WEEK, firstDayOfWeek) }
                val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_WEEK, 6) }
                val fmt = SimpleDateFormat("dd MMM", Locale.US)
                "${fmt.format(start.time)} - ${fmt.format(end.time)} ${cal.get(Calendar.YEAR)}"
            }
            TimeFilter.MONTH -> SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)
            TimeFilter.QUARTER -> {
                val q = (cal.get(Calendar.MONTH) / 3) + 1
                "Q$q ${cal.get(Calendar.YEAR)}"
            }
            TimeFilter.YEAR -> "${cal.get(Calendar.YEAR)}"
            TimeFilter.ALL -> "All History"
        }
    }

    // CRUD Transactions
    fun saveTransaction(
        id: Long = 0,
        amount: Double,
        category: String,
        subcategory: String = "",
        type: String,
        memo: String,
        wallet: String,
        date: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("MM/dd/yyyy, h:mm a", Locale.US).format(Date(date))
            val tx = TransactionEntity(
                id = id,
                date = date,
                dateString = dateStr,
                type = type,
                category = category,
                subcategory = subcategory,
                amount = amount,
                currency = "INR",
                memo = memo,
                wallet = wallet
            )
            if (id == 0L) {
                repository.insertTransaction(tx)
            } else {
                repository.updateTransaction(tx)
            }
            com.example.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            com.example.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    // CRUD Category & Subcategory
    fun addCategory(name: String, parentCategory: String? = null, type: String = "Expense", iconName: String = "") {
        viewModelScope.launch {
            val entity = CategoryEntity(
                name = name.trim(),
                parentCategory = parentCategory?.trim(),
                type = type,
                iconName = iconName
            )
            repository.insertCategory(entity)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // CRUD Budgets
    fun saveBudget(id: Long = 0, category: String, amountLimit: Double, period: String = "Monthly") {
        viewModelScope.launch {
            val budget = BudgetEntity(
                id = id,
                category = category,
                amountLimit = amountLimit,
                period = period
            )
            if (id == 0L) {
                repository.insertBudget(budget)
            } else {
                repository.updateBudget(budget)
            }
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    // CRUD Recurring Expenses (EMI)
    fun saveRecurring(
        id: Long = 0,
        title: String,
        amount: Double,
        category: String,
        type: String = "Expense",
        dayOfMonth: Int,
        wallet: String = "Cash",
        memo: String = "",
        autoAdd: Boolean = true,
        startDate: Long = 0L
    ) {
        viewModelScope.launch {
            val existing = if (id != 0L) repository.getRecurringById(id) else null
            val item = RecurringExpenseEntity(
                id = id,
                title = title,
                amount = amount,
                category = category,
                type = type,
                dayOfMonth = dayOfMonth,
                wallet = wallet,
                memo = memo,
                autoAdd = autoAdd,
                lastPostedYearMonth = existing?.lastPostedYearMonth ?: "",
                startDate = if (startDate != 0L) startDate else (existing?.startDate ?: 0L)
            )
            if (id == 0L) {
                repository.insertRecurring(item)
            } else {
                repository.updateRecurring(item)
            }
            // Only triggers upcoming records whose due date has arrived; never modifies past ledger history
            repository.processDueRecurringExpenses()
        }
    }

    fun deleteRecurring(recurring: RecurringExpenseEntity) {
        viewModelScope.launch {
            repository.deleteRecurring(recurring)
        }
    }

    fun triggerRecurringNow(recurring: RecurringExpenseEntity) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            val dateStr = SimpleDateFormat("MM/dd/yyyy, h:mm a", Locale.US).format(cal.time)
            val tx = TransactionEntity(
                date = cal.timeInMillis,
                dateString = dateStr,
                type = recurring.type,
                category = recurring.category,
                amount = recurring.amount,
                currency = "INR",
                memo = if (recurring.memo.isNotBlank()) recurring.memo else recurring.title,
                wallet = recurring.wallet,
                isRecurring = true,
                recurringId = recurring.id
            )
            repository.insertTransaction(tx)
        }
    }

    // CRUD Debts
    fun saveDebt(
        id: Long = 0,
        personName: String,
        amount: Double,
        type: String,
        memo: String = "",
        isSettled: Boolean = false
    ) {
        viewModelScope.launch {
            val debt = DebtEntity(
                id = id,
                personName = personName,
                amount = amount,
                type = type,
                memo = memo,
                isSettled = isSettled
            )
            if (id == 0L) {
                repository.insertDebt(debt)
            } else {
                repository.updateDebt(debt)
            }
        }
    }

    fun toggleDebtSettled(debt: DebtEntity) {
        viewModelScope.launch {
            val newSettled = !debt.isSettled
            val newPaid = if (newSettled) debt.amount else 0.0
            val unpaidDiff = (debt.amount - debt.paidAmount).coerceAtLeast(0.0)
            repository.updateDebt(debt.copy(isSettled = newSettled, paidAmount = newPaid))

            // When marking debt settled in full, record the payoff amount in the ledger
            if (newSettled && unpaidDiff > 0) {
                val isBorrowed = debt.type.equals("Borrowed", ignoreCase = true)
                val now = System.currentTimeMillis()
                val dateStr = SimpleDateFormat("MM/dd/yyyy, h:mm a", Locale.US).format(Date(now))
                val tx = TransactionEntity(
                    amount = unpaidDiff,
                    type = if (isBorrowed) "Expense" else "Income",
                    category = if (isBorrowed) "Debt Repayment" else "Debt Collected",
                    date = now,
                    dateString = dateStr,
                    memo = if (isBorrowed) {
                        "Full debt payoff to ${debt.personName} (Settled)${if (debt.memo.isNotBlank()) " • ${debt.memo}" else ""}"
                    } else {
                        "Received full debt payoff from ${debt.personName} (Settled)${if (debt.memo.isNotBlank()) " • ${debt.memo}" else ""}"
                    },
                    wallet = "Bank / UPI"
                )
                repository.insertTransaction(tx)
            }
        }
    }

    fun payDebtAmount(debt: DebtEntity, paymentAmount: Double) {
        viewModelScope.launch {
            if (paymentAmount <= 0) return@launch
            val newPaid = (debt.paidAmount + paymentAmount).coerceIn(0.0, debt.amount)
            val newSettled = newPaid >= debt.amount
            repository.updateDebt(debt.copy(paidAmount = newPaid, isSettled = newSettled))

            // Add payoff or partial pay amount directly to ledger
            val isBorrowed = debt.type.equals("Borrowed", ignoreCase = true)
            val now = System.currentTimeMillis()
            val dateStr = SimpleDateFormat("MM/dd/yyyy, h:mm a", Locale.US).format(Date(now))
            val tx = TransactionEntity(
                amount = paymentAmount,
                type = if (isBorrowed) "Expense" else "Income",
                category = if (isBorrowed) "Debt Repayment" else "Debt Collected",
                date = now,
                dateString = dateStr,
                memo = if (isBorrowed) {
                    "Debt payoff to ${debt.personName}${if (newSettled) " (Settled in Full)" else " (Partial Pay)"}${if (debt.memo.isNotBlank()) " • ${debt.memo}" else ""}"
                } else {
                    "Received debt repayment from ${debt.personName}${if (newSettled) " (Settled in Full)" else " (Partial Pay)"}${if (debt.memo.isNotBlank()) " • ${debt.memo}" else ""}"
                },
                wallet = "Bank / UPI"
            )
            repository.insertTransaction(tx)
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }

    // Google Drive Sync
    fun syncGoogleDrive(onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val success = repository.syncWithGoogleDrive()
            onComplete?.invoke(success)
        }
    }

    fun setAutoSync(enabled: Boolean) {
        repository.setAutoSync(enabled)
    }

    fun updateSalarySettings(amount: Double, employer: String, day: Int, wallet: String, autoCredit: Boolean) {
        repository.setSalarySettings(amount, employer, day, wallet, autoCredit)
    }

    fun creditSalaryNow(onComplete: (TransactionEntity) -> Unit = {}) {
        viewModelScope.launch {
            val tx = repository.creditSalaryNow()
            com.example.widget.WidgetUpdater.updateAllWidgets(getApplication())
            onComplete(tx)
        }
    }

    fun exportBackupJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportJsonBackup(allTransactions.value)
            onResult(json)
        }
    }

    fun restoreBackup(jsonString: String, onResult: (Boolean, Int) -> Unit) {
        viewModelScope.launch {
            val result = repository.restoreJsonBackup(jsonString)
            if (result.isSuccess) {
                com.example.widget.WidgetUpdater.updateAllWidgets(getApplication())
                onResult(true, result.getOrDefault(0))
            } else {
                onResult(false, 0)
            }
        }
    }

    fun restoreFromGoogleDrive(onResult: (Boolean, Int, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.restoreFromGoogleDrive()
            if (result.isSuccess) {
                com.example.widget.WidgetUpdater.updateAllWidgets(getApplication())
                onResult(true, result.getOrDefault(0), "")
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Restore failed"
                onResult(false, 0, errorMsg)
            }
        }
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.clearAllData()
            com.example.widget.WidgetUpdater.updateAllWidgets(getApplication())
            onComplete()
        }
    }

    fun loadSampleData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.loadSampleData()
            com.example.widget.WidgetUpdater.updateAllWidgets(getApplication())
            onComplete()
        }
    }

    private fun calculateTimeRange(timeFilter: TimeFilter, refCal: Calendar): Pair<Long, Long> {
        val cal = (refCal.clone() as Calendar)
        return when (timeFilter) {
            TimeFilter.DAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            TimeFilter.WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_WEEK, 6)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            TimeFilter.MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, maxDay)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            TimeFilter.QUARTER -> {
                val currentMonth = cal.get(Calendar.MONTH)
                val quarterStartMonth = (currentMonth / 3) * 3
                cal.set(Calendar.MONTH, quarterStartMonth)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis

                cal.set(Calendar.MONTH, quarterStartMonth + 2)
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, maxDay)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            TimeFilter.YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_YEAR)
                cal.set(Calendar.DAY_OF_YEAR, maxDay)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                Pair(start, end)
            }
            TimeFilter.ALL -> Pair(0L, Long.MAX_VALUE)
        }
    }
}
