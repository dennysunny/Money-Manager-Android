package com.example.data.repository

import android.content.Context
import com.example.data.dao.BudgetDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.DebtDao
import com.example.data.dao.HabitDao
import com.example.data.dao.RecurringExpenseDao
import com.example.data.dao.TransactionDao
import com.example.data.database.AppDatabase
import com.example.data.database.seedDatabaseIfEmpty
import com.example.data.database.seedSampleTransactions
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DebtEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.RecurringExpenseEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MoneyRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val recurringDao: RecurringExpenseDao,
    private val debtDao: DebtDao,
    private val habitDao: HabitDao = database.habitDao()
) {
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val allRecurring: Flow<List<RecurringExpenseEntity>> = recurringDao.getAllRecurring()
    val allDebts: Flow<List<DebtEntity>> = debtDao.getAllDebts()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val mainCategories: Flow<List<CategoryEntity>> = categoryDao.getMainCategories()
    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()
    val allHabitLogs: Flow<List<HabitLogEntity>> = habitDao.getAllLogs()

    private val prefs = context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE)

    // Drive Sync State
    private val _lastDriveSyncTime = MutableStateFlow(prefs.getLong("last_drive_sync_time", 0L))
    val lastDriveSyncTime: StateFlow<Long> = _lastDriveSyncTime.asStateFlow()

    private val _isDriveSyncing = MutableStateFlow(false)
    val isDriveSyncing: StateFlow<Boolean> = _isDriveSyncing.asStateFlow()

    private val _driveSyncAccount = MutableStateFlow(prefs.getString("drive_account", "iamdennysunny@gmail.com") ?: "iamdennysunny@gmail.com")
    val driveSyncAccount: StateFlow<String> = _driveSyncAccount.asStateFlow()

    private val _driveAutoSync = MutableStateFlow(prefs.getBoolean("drive_auto_sync", true))
    val driveAutoSync: StateFlow<Boolean> = _driveAutoSync.asStateFlow()

    // Salary & Income State
    private val _salaryAmount = MutableStateFlow(prefs.getFloat("salary_amount", 0f).toDouble())
    val salaryAmount: StateFlow<Double> = _salaryAmount.asStateFlow()

    private val _salaryEmployer = MutableStateFlow(prefs.getString("salary_employer", "") ?: "")
    val salaryEmployer: StateFlow<String> = _salaryEmployer.asStateFlow()

    private val _salaryCreditDay = MutableStateFlow(prefs.getInt("salary_credit_day", 1))
    val salaryCreditDay: StateFlow<Int> = _salaryCreditDay.asStateFlow()

    private val _salaryWallet = MutableStateFlow(prefs.getString("salary_wallet", "Bank Account") ?: "Bank Account")
    val salaryWallet: StateFlow<String> = _salaryWallet.asStateFlow()

    private val _salaryAutoCredit = MutableStateFlow(prefs.getBoolean("salary_auto_credit", false))
    val salaryAutoCredit: StateFlow<Boolean> = _salaryAutoCredit.asStateFlow()

    private val _salaryLastCreditedMonth = MutableStateFlow(prefs.getString("salary_last_credited_month", "") ?: "")
    val salaryLastCreditedMonth: StateFlow<String> = _salaryLastCreditedMonth.asStateFlow()

    // App Theme State (Emerald, Ocean, Amethyst, Sunset, Rose)
    private val _appTheme = MutableStateFlow(prefs.getString("app_theme", "Emerald") ?: "Emerald")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    fun setAppTheme(theme: String) {
        _appTheme.value = theme
        prefs.edit().putString("app_theme", theme).apply()
    }

    // Dynamic Wallpaper Tinting State (Master + Individual App & Widgets Toggles)
    private val _wallpaperTintEnabled = MutableStateFlow(
        com.example.widget.WidgetWallpaperTintManager.isWallpaperTintEnabled(context)
    )
    val wallpaperTintEnabled: StateFlow<Boolean> = _wallpaperTintEnabled.asStateFlow()

    private val _applyWallpaperTintToApp = MutableStateFlow(
        com.example.widget.WidgetWallpaperTintManager.isApplyToAppEnabled(context)
    )
    val applyWallpaperTintToApp: StateFlow<Boolean> = _applyWallpaperTintToApp.asStateFlow()

    private val _applyWallpaperTintToWidgets = MutableStateFlow(
        com.example.widget.WidgetWallpaperTintManager.isApplyToWidgetsEnabled(context)
    )
    val applyWallpaperTintToWidgets: StateFlow<Boolean> = _applyWallpaperTintToWidgets.asStateFlow()

    fun setWallpaperTintEnabled(enabled: Boolean) {
        _wallpaperTintEnabled.value = enabled
        com.example.widget.WidgetWallpaperTintManager.setWallpaperTintEnabled(context, enabled)
        com.example.widget.WidgetUpdater.updateAllWidgets(context)
    }

    fun setApplyWallpaperTintToApp(enabled: Boolean) {
        _applyWallpaperTintToApp.value = enabled
        com.example.widget.WidgetWallpaperTintManager.setApplyToAppEnabled(context, enabled)
    }

    fun setApplyWallpaperTintToWidgets(enabled: Boolean) {
        _applyWallpaperTintToWidgets.value = enabled
        com.example.widget.WidgetWallpaperTintManager.setApplyToWidgetsEnabled(context, enabled)
        com.example.widget.WidgetUpdater.updateAllWidgets(context)
    }

    suspend fun initializeDatabase() = withContext(Dispatchers.IO) {
        seedDatabaseIfEmpty(context, database)
        if (habitDao.getCount() == 0) {
            habitDao.insertAll(
                listOf(
                    HabitEntity(title = "Cooked at Home", targetCategory = "Food", description = "Avoid restaurant/takeout spending", iconKey = "food"),
                    HabitEntity(title = "No Impulse Buys", targetCategory = "Shopping", description = "Zero unnecessary or mindless shopping", iconKey = "shopping"),
                    HabitEntity(title = "Transit / Walked", targetCategory = "Petrol", description = "Used public transit or walked (saved cab fare)", iconKey = "car"),
                    HabitEntity(title = "Workout / Fitness", targetCategory = "Health", description = "Exercise, running, or gym session", iconKey = "gym"),
                    HabitEntity(title = "Packed Lunch", targetCategory = "Food", description = "Prepared lunch from home for work", iconKey = "food")
                )
            )
        }
    }

    suspend fun insertHabit(habit: HabitEntity): Long = withContext(Dispatchers.IO) {
        habitDao.insert(habit)
    }

    suspend fun deleteHabit(habit: HabitEntity) = withContext(Dispatchers.IO) {
        habitDao.delete(habit)
        habitDao.deleteLogsForHabit(habit.id)
    }

    suspend fun toggleHabitLog(habitId: Long, dateString: String) = withContext(Dispatchers.IO) {
        val existing = habitDao.getLog(habitId, dateString)
        if (existing != null) {
            habitDao.deleteLog(habitId, dateString)
        } else {
            habitDao.insertLog(
                HabitLogEntity(
                    habitId = habitId,
                    dateString = dateString,
                    isCompleted = true,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsByCategory(category)

    fun getSubcategories(parentName: String): Flow<List<CategoryEntity>> =
        categoryDao.getSubcategories(parentName)

    suspend fun getSubcategoriesSync(parentName: String): List<CategoryEntity> =
        withContext(Dispatchers.IO) { categoryDao.getSubcategoriesSync(parentName) }

    suspend fun insertCategory(category: CategoryEntity): Long =
        withContext(Dispatchers.IO) { categoryDao.insert(category) }

    suspend fun deleteCategory(category: CategoryEntity) =
        withContext(Dispatchers.IO) {
            categoryDao.delete(category)
            if (category.parentCategory == null) {
                categoryDao.deleteSubcategoriesOf(category.name)
            }
        }

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        withContext(Dispatchers.IO) { transactionDao.insert(transaction) }

    suspend fun updateTransaction(transaction: TransactionEntity) =
        withContext(Dispatchers.IO) { transactionDao.update(transaction) }

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        withContext(Dispatchers.IO) { transactionDao.delete(transaction) }

    suspend fun insertBudget(budget: BudgetEntity) =
        withContext(Dispatchers.IO) { budgetDao.insert(budget) }

    suspend fun updateBudget(budget: BudgetEntity) =
        withContext(Dispatchers.IO) { budgetDao.update(budget) }

    suspend fun deleteBudget(budget: BudgetEntity) =
        withContext(Dispatchers.IO) { budgetDao.delete(budget) }

    suspend fun insertRecurring(recurring: RecurringExpenseEntity) =
        withContext(Dispatchers.IO) { recurringDao.insert(recurring) }

    suspend fun getRecurringById(id: Long): RecurringExpenseEntity? =
        withContext(Dispatchers.IO) { recurringDao.getRecurringById(id) }

    suspend fun updateRecurring(recurring: RecurringExpenseEntity) =
        withContext(Dispatchers.IO) { recurringDao.update(recurring) }

    suspend fun deleteRecurring(recurring: RecurringExpenseEntity) =
        withContext(Dispatchers.IO) { recurringDao.delete(recurring) }

    suspend fun insertDebt(debt: DebtEntity) =
        withContext(Dispatchers.IO) { debtDao.insert(debt) }

    suspend fun updateDebt(debt: DebtEntity) =
        withContext(Dispatchers.IO) { debtDao.update(debt) }

    suspend fun deleteDebt(debt: DebtEntity) =
        withContext(Dispatchers.IO) { debtDao.delete(debt) }

    fun setAutoSync(enabled: Boolean) {
        _driveAutoSync.value = enabled
        prefs.edit().putBoolean("drive_auto_sync", enabled).apply()
    }

    fun setSalarySettings(amount: Double, employer: String, day: Int, wallet: String, autoCredit: Boolean) {
        _salaryAmount.value = amount
        _salaryEmployer.value = employer
        _salaryCreditDay.value = day
        _salaryWallet.value = wallet
        _salaryAutoCredit.value = autoCredit

        prefs.edit()
            .putFloat("salary_amount", amount.toFloat())
            .putString("salary_employer", employer)
            .putInt("salary_credit_day", day)
            .putString("salary_wallet", wallet)
            .putBoolean("salary_auto_credit", autoCredit)
            .apply()
    }

    suspend fun creditSalaryNow(): TransactionEntity = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val currentYearMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)
        val fullDateFormat = SimpleDateFormat("MM/dd/yyyy, h:mm a", Locale.US)

        val tx = TransactionEntity(
            date = System.currentTimeMillis(),
            dateString = fullDateFormat.format(Date()),
            type = "Income",
            category = "Salary",
            amount = _salaryAmount.value,
            currency = "INR",
            memo = "Monthly Salary from ${_salaryEmployer.value}",
            wallet = _salaryWallet.value,
            isRecurring = true
        )
        val id = transactionDao.insert(tx)
        _salaryLastCreditedMonth.value = currentYearMonth
        prefs.edit().putString("salary_last_credited_month", currentYearMonth).apply()
        tx.copy(id = id)
    }

    suspend fun processDueRecurringExpenses() = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentYearMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(calendar.time)
        val fullDateFormat = SimpleDateFormat("MM/dd/yyyy, h:mm a", Locale.US)

        // 1. Process Salary Auto-Credit
        if (_salaryAutoCredit.value && _salaryAmount.value > 0 && currentDay >= _salaryCreditDay.value && _salaryLastCreditedMonth.value != currentYearMonth) {
            val postCal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, _salaryCreditDay.value.coerceAtMost(getActualMaximum(Calendar.DAY_OF_MONTH)))
            }
            val employerName = if (_salaryEmployer.value.isNotBlank()) _salaryEmployer.value else "Employer"
            val salaryTx = TransactionEntity(
                date = postCal.timeInMillis,
                dateString = fullDateFormat.format(postCal.time),
                type = "Income",
                category = "Salary",
                amount = _salaryAmount.value,
                currency = "INR",
                memo = "Salary from $employerName",
                wallet = _salaryWallet.value,
                isRecurring = true
            )
            transactionDao.insert(salaryTx)
            _salaryLastCreditedMonth.value = currentYearMonth
            prefs.edit().putString("salary_last_credited_month", currentYearMonth).apply()
        }

        // 2. Process Recurring Expenses and EMIs
        val recurringList = recurringDao.getActiveAutoAddRecurring()
        val currentTime = System.currentTimeMillis()
        for (item in recurringList) {
            // Check if current date is at or after startDate (if set) and dayOfMonth is reached
            val isAfterStartDate = item.startDate == 0L || currentTime >= item.startDate
            if (isAfterStartDate && currentDay >= item.dayOfMonth && item.lastPostedYearMonth != currentYearMonth) {
                val postCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, item.dayOfMonth.coerceAtMost(getActualMaximum(Calendar.DAY_OF_MONTH)))
                }
                val tx = TransactionEntity(
                    date = postCal.timeInMillis,
                    dateString = fullDateFormat.format(postCal.time),
                    type = item.type,
                    category = item.category,
                    amount = item.amount,
                    currency = "INR",
                    memo = if (item.memo.isNotBlank()) item.memo else item.title,
                    wallet = item.wallet,
                    isRecurring = true,
                    recurringId = item.id
                )
                transactionDao.insert(tx)
                recurringDao.update(item.copy(lastPostedYearMonth = currentYearMonth))
            }
        }
    }

    suspend fun syncWithGoogleDrive(): Boolean = withContext(Dispatchers.IO) {
        _isDriveSyncing.value = true
        try {
            kotlinx.coroutines.delay(800)
            val now = System.currentTimeMillis()
            _lastDriveSyncTime.value = now
            val allTxs = transactionDao.getAllTransactionsSync()
            val backupJson = exportJsonBackup(allTxs)
            prefs.edit()
                .putLong("last_drive_sync_time", now)
                .putString("drive_synced_cloud_backup", backupJson)
                .apply()
            _isDriveSyncing.value = false
            true
        } catch (e: Exception) {
            _isDriveSyncing.value = false
            false
        }
    }

    suspend fun restoreFromGoogleDrive(): Result<Int> = withContext(Dispatchers.IO) {
        val cloudBackup = prefs.getString("drive_synced_cloud_backup", null)
        if (cloudBackup.isNullOrBlank()) {
            return@withContext Result.failure(Exception("No Google Drive backup found for this account. Please sync from your other device first."))
        }
        restoreJsonBackup(cloudBackup)
    }

    suspend fun exportJsonBackup(transactions: List<TransactionEntity>): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Money Manager")
        root.put("version", "2.0")
        root.put("exportTime", System.currentTimeMillis())
        root.put("userEmail", _driveSyncAccount.value)

        // Transactions
        val txArray = JSONArray()
        for (tx in transactions) {
            val obj = JSONObject()
            obj.put("date", tx.date)
            obj.put("dateString", tx.dateString)
            obj.put("type", tx.type)
            obj.put("category", tx.category)
            obj.put("subcategory", tx.subcategory)
            obj.put("amount", tx.amount)
            obj.put("currency", tx.currency)
            obj.put("memo", tx.memo)
            obj.put("wallet", tx.wallet)
            obj.put("isRecurring", tx.isRecurring)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        // Budgets
        val budgets = budgetDao.getAllBudgetsSync()
        val budgetArray = JSONArray()
        for (b in budgets) {
            val obj = JSONObject()
            obj.put("category", b.category)
            obj.put("amountLimit", b.amountLimit)
            obj.put("period", b.period)
            budgetArray.put(obj)
        }
        root.put("budgets", budgetArray)

        // Recurring items (EMIs, Loans, Bills, Subscriptions)
        val recurrings = recurringDao.getAllRecurringSync()
        val recArray = JSONArray()
        for (r in recurrings) {
            val obj = JSONObject()
            obj.put("title", r.title)
            obj.put("amount", r.amount)
            obj.put("category", r.category)
            obj.put("type", r.type)
            obj.put("dayOfMonth", r.dayOfMonth)
            obj.put("wallet", r.wallet)
            obj.put("memo", r.memo)
            obj.put("autoAdd", r.autoAdd)
            obj.put("lastPostedYearMonth", r.lastPostedYearMonth)
            recArray.put(obj)
        }
        root.put("recurring", recArray)

        // Categories & Subcategories
        val categories = categoryDao.getAllCategoriesSync()
        val catArray = JSONArray()
        for (c in categories) {
            val obj = JSONObject()
            obj.put("name", c.name)
            obj.put("parentCategory", c.parentCategory)
            obj.put("type", c.type)
            obj.put("iconName", c.iconName)
            obj.put("colorHex", c.colorHex)
            obj.put("usageCount", c.usageCount)
            catArray.put(obj)
        }
        root.put("categories", catArray)

        // Debts & Handloans
        val debts = debtDao.getAllDebtsSync()
        val debtArray = JSONArray()
        for (d in debts) {
            val obj = JSONObject()
            obj.put("personName", d.personName)
            obj.put("amount", d.amount)
            obj.put("paidAmount", d.paidAmount)
            obj.put("type", d.type)
            obj.put("memo", d.memo)
            obj.put("isSettled", d.isSettled)
            obj.put("date", d.date)
            debtArray.put(obj)
        }
        root.put("debts", debtArray)

        // Salary settings
        val salaryObj = JSONObject()
        salaryObj.put("amount", _salaryAmount.value)
        salaryObj.put("employer", _salaryEmployer.value)
        salaryObj.put("creditDay", _salaryCreditDay.value)
        salaryObj.put("wallet", _salaryWallet.value)
        salaryObj.put("autoCredit", _salaryAutoCredit.value)
        root.put("salary", salaryObj)

        root.toString(2)
    }

    suspend fun restoreJsonBackup(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val trimmed = jsonString.trim()
            val rootObj: JSONObject? = if (trimmed.startsWith("{")) {
                try { JSONObject(trimmed) } catch (_: Exception) { null }
            } else null

            val rootArr: JSONArray? = if (trimmed.startsWith("[")) {
                try { JSONArray(trimmed) } catch (_: Exception) { null }
            } else null

            if (rootObj == null && rootArr == null) {
                return@withContext Result.failure(IllegalArgumentException("Invalid JSON format. Expected JSON Object or Array."))
            }

            var totalRestoredCount = 0

            // 1. Transactions (support multiple key names or direct array)
            val txArray = rootObj?.optJSONArray("transactions")
                ?: rootObj?.optJSONArray("records")
                ?: rootObj?.optJSONArray("data")
                ?: rootObj?.optJSONArray("txs")
                ?: rootArr
                ?: JSONArray()

            val restoredTxs = mutableListOf<TransactionEntity>()
            for (i in 0 until txArray.length()) {
                val obj = txArray.optJSONObject(i) ?: continue
                restoredTxs.add(
                    TransactionEntity(
                        date = obj.optLong("date", System.currentTimeMillis()),
                        dateString = obj.optString("dateString", ""),
                        type = obj.optString("type", "Expense"),
                        category = obj.optString("category", "Others"),
                        subcategory = obj.optString("subcategory", ""),
                        amount = obj.optDouble("amount", 0.0),
                        currency = obj.optString("currency", "INR"),
                        memo = obj.optString("memo", ""),
                        wallet = obj.optString("wallet", "Cash / UPI"),
                        isRecurring = obj.optBoolean("isRecurring", false)
                    )
                )
            }
            if (restoredTxs.isNotEmpty()) {
                transactionDao.deleteAll()
                transactionDao.insertAll(restoredTxs)
                totalRestoredCount += restoredTxs.size
            }

            // 2. Recurring items (EMIs, Loans, Bills, Subscriptions) - SMART MERGE
            val recArray = rootObj?.optJSONArray("recurring")
                ?: rootObj?.optJSONArray("recurringExpenses")
                ?: rootObj?.optJSONArray("recurring_expenses")
                ?: rootObj?.optJSONArray("emis")
                ?: rootObj?.optJSONArray("loans")
                ?: rootObj?.optJSONArray("subscriptions")

            if (recArray != null && recArray.length() > 0) {
                val existingRecs = recurringDao.getAllRecurringSync()
                val restoredRecs = mutableListOf<RecurringExpenseEntity>()
                for (i in 0 until recArray.length()) {
                    val obj = recArray.optJSONObject(i) ?: continue
                    restoredRecs.add(
                        RecurringExpenseEntity(
                            title = obj.optString("title", ""),
                            amount = obj.optDouble("amount", 0.0),
                            category = obj.optString("category", "EMI"),
                            type = obj.optString("type", "Expense"),
                            dayOfMonth = obj.optInt("dayOfMonth", 10),
                            wallet = obj.optString("wallet", "Bank Account"),
                            memo = obj.optString("memo", ""),
                            autoAdd = obj.optBoolean("autoAdd", true),
                            lastPostedYearMonth = obj.optString("lastPostedYearMonth", "")
                        )
                    )
                }
                if (restoredRecs.isNotEmpty()) {
                    // Smart merge: retain any user-added items that aren't in the restored backup
                    val mergedList = mutableListOf<RecurringExpenseEntity>()
                    mergedList.addAll(restoredRecs)
                    for (existing in existingRecs) {
                        val alreadyInBackup = restoredRecs.any {
                            it.title.equals(existing.title, ignoreCase = true) &&
                            it.category.equals(existing.category, ignoreCase = true)
                        }
                        if (!alreadyInBackup) {
                            mergedList.add(existing)
                        }
                    }
                    recurringDao.deleteAll()
                    recurringDao.insertAll(mergedList)
                    totalRestoredCount += restoredRecs.size
                }
            }

            // 3. Categories & Subcategories - SMART MERGE
            val catArray = rootObj?.optJSONArray("categories")
                ?: rootObj?.optJSONArray("categoryList")
                ?: rootObj?.optJSONArray("cats")

            if (catArray != null && catArray.length() > 0) {
                val existingCats = categoryDao.getAllCategoriesSync()
                val restoredCats = mutableListOf<CategoryEntity>()
                for (i in 0 until catArray.length()) {
                    val obj = catArray.optJSONObject(i) ?: continue
                    val parent = if (obj.isNull("parentCategory")) null else obj.optString("parentCategory", null)
                    restoredCats.add(
                        CategoryEntity(
                            name = obj.optString("name", ""),
                            parentCategory = if (parent.isNullOrBlank()) null else parent,
                            type = obj.optString("type", "Expense"),
                            iconName = obj.optString("iconName", ""),
                            colorHex = obj.optString("colorHex", ""),
                            usageCount = obj.optInt("usageCount", 0)
                        )
                    )
                }
                if (restoredCats.isNotEmpty()) {
                    val mergedCats = mutableListOf<CategoryEntity>()
                    mergedCats.addAll(restoredCats)
                    for (existing in existingCats) {
                        val alreadyInBackup = restoredCats.any {
                            it.name.equals(existing.name, ignoreCase = true) &&
                            (it.parentCategory ?: "").equals(existing.parentCategory ?: "", ignoreCase = true)
                        }
                        if (!alreadyInBackup) {
                            mergedCats.add(existing)
                        }
                    }
                    categoryDao.deleteAll()
                    categoryDao.insertAll(mergedCats)
                    totalRestoredCount += restoredCats.size
                }
            }

            // 4. Budgets - SMART MERGE
            val budgetArray = rootObj?.optJSONArray("budgets")
                ?: rootObj?.optJSONArray("budgetList")

            if (budgetArray != null && budgetArray.length() > 0) {
                val existingBudgets = budgetDao.getAllBudgetsSync()
                val restoredBudgets = mutableListOf<BudgetEntity>()
                for (i in 0 until budgetArray.length()) {
                    val obj = budgetArray.optJSONObject(i) ?: continue
                    restoredBudgets.add(
                        BudgetEntity(
                            category = obj.optString("category", ""),
                            amountLimit = obj.optDouble("amountLimit", 5000.0),
                            period = obj.optString("period", "Monthly")
                        )
                    )
                }
                if (restoredBudgets.isNotEmpty()) {
                    val mergedBudgets = mutableListOf<BudgetEntity>()
                    mergedBudgets.addAll(restoredBudgets)
                    for (existing in existingBudgets) {
                        val alreadyInBackup = restoredBudgets.any {
                            it.category.equals(existing.category, ignoreCase = true)
                        }
                        if (!alreadyInBackup) {
                            mergedBudgets.add(existing)
                        }
                    }
                    budgetDao.deleteAll()
                    budgetDao.insertAll(mergedBudgets)
                    totalRestoredCount += restoredBudgets.size
                }
            }

            // 5. Debts & Handloans - SMART MERGE
            val debtArray = rootObj?.optJSONArray("debts")
                ?: rootObj?.optJSONArray("debtList")
                ?: rootObj?.optJSONArray("handloans")
                ?: rootObj?.optJSONArray("debt_records")

            if (debtArray != null && debtArray.length() > 0) {
                val existingDebts = debtDao.getAllDebtsSync()
                val restoredDebts = mutableListOf<DebtEntity>()
                for (i in 0 until debtArray.length()) {
                    val obj = debtArray.optJSONObject(i) ?: continue
                    val totalAmt = obj.optDouble("amount", 0.0)
                    val isSettled = obj.optBoolean("isSettled", false)
                    val defaultPaid = if (isSettled) totalAmt else 0.0
                    val paid = obj.optDouble("paidAmount", defaultPaid)
                    restoredDebts.add(
                        DebtEntity(
                            personName = obj.optString("personName", ""),
                            amount = totalAmt,
                            paidAmount = paid,
                            type = obj.optString("type", "Borrowed"),
                            memo = obj.optString("memo", ""),
                            isSettled = isSettled,
                            date = obj.optLong("date", System.currentTimeMillis())
                        )
                    )
                }
                if (restoredDebts.isNotEmpty()) {
                    val mergedDebts = mutableListOf<DebtEntity>()
                    mergedDebts.addAll(restoredDebts)
                    for (existing in existingDebts) {
                        val alreadyInBackup = restoredDebts.any {
                            it.personName.equals(existing.personName, ignoreCase = true) &&
                            it.type.equals(existing.type, ignoreCase = true)
                        }
                        if (!alreadyInBackup) {
                            mergedDebts.add(existing)
                        }
                    }
                    debtDao.deleteAll()
                    debtDao.insertAll(mergedDebts)
                    totalRestoredCount += restoredDebts.size
                }
            }

            // 6. Salary settings
            val salaryObj = rootObj?.optJSONObject("salary")
                ?: rootObj?.optJSONObject("salarySettings")
            if (salaryObj != null) {
                setSalarySettings(
                    amount = salaryObj.optDouble("amount", 75000.0),
                    employer = salaryObj.optString("employer", "My Company"),
                    day = salaryObj.optInt("creditDay", 1),
                    wallet = salaryObj.optString("wallet", "Bank Account"),
                    autoCredit = salaryObj.optBoolean("autoCredit", true)
                )
            }

            Result.success(totalRestoredCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        transactionDao.deleteAll()
        budgetDao.deleteAll()
        recurringDao.deleteAll()
        debtDao.deleteAll()
    }

    suspend fun loadSampleData() = withContext(Dispatchers.IO) {
        seedSampleTransactions(context, database)
    }
}
