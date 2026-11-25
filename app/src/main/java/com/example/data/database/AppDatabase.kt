package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BudgetDao
import com.example.data.dao.CategoryDao
import com.example.data.dao.DebtDao
import com.example.data.dao.HabitDao
import com.example.data.dao.RecurringExpenseDao
import com.example.data.dao.TransactionDao
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DebtEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.RecurringExpenseEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        RecurringExpenseEntity::class,
        DebtEntity::class,
        HabitEntity::class,
        HabitLogEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun debtDao(): DebtDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "money_manager_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

suspend fun seedDatabaseIfEmpty(context: Context, database: AppDatabase) = withContext(Dispatchers.IO) {
    try {
        val transactionDao = database.transactionDao()
        val categoryDao = database.categoryDao()
        val budgetDao = database.budgetDao()
        val recurringDao = database.recurringExpenseDao()
        val debtDao = database.debtDao()

        // 1. Seed Categories & Subcategories if empty
        if (categoryDao.getCount() == 0) {
            val categories = listOf(
                // Main categories
                CategoryEntity(name = "Petrol", type = "Expense", iconName = "LocalGasStation", colorHex = "#F97316"),
                CategoryEntity(name = "Car", parentCategory = "Petrol", type = "Expense"),
                CategoryEntity(name = "Bike", parentCategory = "Petrol", type = "Expense"),

                CategoryEntity(name = "Grocery Home", type = "Expense", iconName = "ShoppingBag", colorHex = "#10B981"),
                CategoryEntity(name = "Meat & Fish", parentCategory = "Grocery Home", type = "Expense"),
                CategoryEntity(name = "Milk & Eggs", parentCategory = "Grocery Home", type = "Expense"),
                CategoryEntity(name = "Fruits & Veggies", parentCategory = "Grocery Home", type = "Expense"),
                CategoryEntity(name = "Provisions", parentCategory = "Grocery Home", type = "Expense"),

                CategoryEntity(name = "Food", type = "Expense", iconName = "Fastfood", colorHex = "#EAB308"),
                CategoryEntity(name = "Office Lunch", parentCategory = "Food", type = "Expense"),
                CategoryEntity(name = "Dining Out", parentCategory = "Food", type = "Expense"),
                CategoryEntity(name = "Thattukada", parentCategory = "Food", type = "Expense"),

                CategoryEntity(name = "Snacks", type = "Expense", iconName = "Fastfood", colorHex = "#F59E0B"),
                CategoryEntity(name = "Tea & Coffee", parentCategory = "Snacks", type = "Expense"),
                CategoryEntity(name = "Bakery & Sweets", parentCategory = "Snacks", type = "Expense"),

                CategoryEntity(name = "Maternity", type = "Expense", iconName = "ChildFriendly", colorHex = "#EC4899"),
                CategoryEntity(name = "Hospital & Scans", parentCategory = "Maternity", type = "Expense"),
                CategoryEntity(name = "Medicines", parentCategory = "Maternity", type = "Expense"),
                CategoryEntity(name = "Baby Essentials", parentCategory = "Maternity", type = "Expense"),

                CategoryEntity(name = "EMI", type = "Expense", iconName = "CreditCard", colorHex = "#8B5CF6"),
                CategoryEntity(name = "Car EMI", parentCategory = "EMI", type = "Expense"),
                CategoryEntity(name = "Loan", parentCategory = "EMI", type = "Expense"),

                CategoryEntity(name = "Home Expenses", type = "Expense", iconName = "Home", colorHex = "#3B82F6"),
                CategoryEntity(name = "Electricity Bill", parentCategory = "Home Expenses", type = "Expense"),
                CategoryEntity(name = "Broadband & TV", parentCategory = "Home Expenses", type = "Expense"),
                CategoryEntity(name = "Maintenance", parentCategory = "Home Expenses", type = "Expense"),

                CategoryEntity(name = "Flat Expenses", type = "Expense", iconName = "Home", colorHex = "#3B82F6"),
                CategoryEntity(name = "Medical", type = "Expense", iconName = "LocalHospital", colorHex = "#EF4444"),
                CategoryEntity(name = "Clothing", type = "Expense", iconName = "Checkroom", colorHex = "#06B6D4"),
                CategoryEntity(name = "Gym", type = "Expense", iconName = "FitnessCenter", colorHex = "#6366F1"),
                CategoryEntity(name = "Health", type = "Expense", iconName = "LocalHospital", colorHex = "#EF4444"),
                CategoryEntity(name = "Bills", type = "Expense", iconName = "Receipt", colorHex = "#64748B"),
                CategoryEntity(name = "Car / Bike Expenses", type = "Expense", iconName = "DirectionsCar", colorHex = "#0284C7"),
                CategoryEntity(name = "Mobile Recharge", type = "Expense", iconName = "Smartphone", colorHex = "#8B5CF6"),
                CategoryEntity(name = "Tech", type = "Expense", iconName = "Smartphone", colorHex = "#8B5CF6"),
                CategoryEntity(name = "Baby", type = "Expense", iconName = "ChildFriendly", colorHex = "#EC4899"),
                CategoryEntity(name = "Gifts", type = "Expense", iconName = "CardGiftcard", colorHex = "#F43F5E"),
                CategoryEntity(name = "Transportation", type = "Expense", iconName = "DirectionsCar", colorHex = "#0284C7"),
                CategoryEntity(name = "Entertainment", type = "Expense", iconName = "Movie", colorHex = "#D946EF"),
                CategoryEntity(name = "Shopping", type = "Expense", iconName = "ShoppingBag", colorHex = "#10B981"),
                CategoryEntity(name = "Travel", type = "Expense", iconName = "Flight", colorHex = "#0EA5E9"),
                CategoryEntity(name = "Wedding Expenses", type = "Expense", iconName = "Celebration", colorHex = "#EC4899"),
                CategoryEntity(name = "Others", type = "Expense", iconName = "Payments", colorHex = "#6B7280"),

                // Incomes
                CategoryEntity(name = "Salary", type = "Income", iconName = "Payments", colorHex = "#10B981"),
                CategoryEntity(name = "Debt", type = "Income", iconName = "AccountBalance", colorHex = "#F43F5E"),
                CategoryEntity(name = "Allowance", type = "Income", iconName = "Payments", colorHex = "#10B981"),
                CategoryEntity(name = "Bill Reimbursement", type = "Income", iconName = "Receipt", colorHex = "#3B82F6"),
                CategoryEntity(name = "Purchase", type = "Income", iconName = "ShoppingBag", colorHex = "#F59E0B"),
                CategoryEntity(name = "Loan", type = "Income", iconName = "AccountBalance", colorHex = "#8B5CF6"),
                CategoryEntity(name = "Bonus", type = "Income", iconName = "Payments", colorHex = "#10B981"),
                CategoryEntity(name = "Investment", type = "Income", iconName = "TrendingUp", colorHex = "#059669")
            )
            categoryDao.insertAll(categories)
        }

        // 2. Seed Budgets if empty
        if (budgetDao.getCount() == 0) {
            budgetDao.insertAll(
                listOf(
                    BudgetEntity(category = "Petrol", amountLimit = 6000.0, period = "Monthly"),
                    BudgetEntity(category = "Grocery Home", amountLimit = 10000.0, period = "Monthly"),
                    BudgetEntity(category = "Food", amountLimit = 5000.0, period = "Monthly"),
                    BudgetEntity(category = "Home Expenses", amountLimit = 8000.0, period = "Monthly"),
                    BudgetEntity(category = "Snacks", amountLimit = 2000.0, period = "Monthly")
                )
            )
        }

        // 3. Keep transactions, recurring and debts clean on fresh install.
        // User starts with a clean slate. Sample data can be loaded from Cloud & Backup settings if desired.
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

suspend fun seedSampleTransactions(context: Context, database: AppDatabase) = withContext(Dispatchers.IO) {
    try {
        val transactionDao = database.transactionDao()
        val transactions = mutableListOf<TransactionEntity>()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy, h:mm a", Locale.US)
        val altDateFormat = SimpleDateFormat("MM/dd/yyyy, hh:mm a", Locale.US)

        context.assets.open("initial_transactions.csv").use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line = reader.readLine() // skip header line
                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line?.trim() ?: continue
                    if (currentLine.isBlank()) continue

                    val tokens = parseCsvLine(currentLine)
                    if (tokens.size >= 7) {
                        val dateStr = tokens[0].trim('"', ' ')
                        val type = tokens[1].trim('"', ' ')
                        val category = tokens[2].trim('"', ' ')
                        val rawAmount = tokens[3].trim('"', ' ').toDoubleOrNull() ?: 0.0
                        val amount = abs(rawAmount)
                        val currency = tokens[4].trim('"', ' ')
                        val memo = tokens[5].trim('"', ' ')
                        val wallet = tokens[6].trim('"', ' ')

                        val timestamp = try {
                            dateFormat.parse(dateStr)?.time ?: altDateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }

                        var subcat = ""
                        val memoLower = memo.lowercase()
                        if (category.equals("Petrol", ignoreCase = true)) {
                            if (memoLower.contains("car")) subcat = "Car"
                            else if (memoLower.contains("bike")) subcat = "Bike"
                        } else if (category.equals("Grocery Home", ignoreCase = true)) {
                            if (memoLower.contains("chicken") || memoLower.contains("fish") || memoLower.contains("beef") || memoLower.contains("pork") || memoLower.contains("duck")) {
                                subcat = "Meat & Fish"
                            } else if (memoLower.contains("milk") || memoLower.contains("egg")) {
                                subcat = "Milk & Eggs"
                            }
                        }

                        transactions.add(
                            TransactionEntity(
                                date = timestamp,
                                dateString = dateStr,
                                type = type,
                                category = category,
                                subcategory = subcat,
                                amount = amount,
                                currency = currency,
                                memo = memo,
                                wallet = wallet,
                                isRecurring = category.equals("EMI", ignoreCase = true)
                            )
                        )
                    }
                }
            }
        }

        if (transactions.isNotEmpty()) {
            transactionDao.insertAll(transactions)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun parseCsvLine(line: String): List<String> {
    val result = mutableListOf<String>()
    var inQuotes = false
    val sb = StringBuilder()
    for (ch in line) {
        when {
            ch == '"' -> inQuotes = !inQuotes
            ch == ',' && !inQuotes -> {
                result.add(sb.toString())
                sb.clear()
            }
            else -> sb.append(ch)
        }
    }
    result.add(sb.toString())
    return result
}
