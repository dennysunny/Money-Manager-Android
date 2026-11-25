package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long, // timestamp epoch millis
    val dateString: String,
    val type: String, // "Expense" or "Income"
    val category: String,
    val subcategory: String = "",
    val amount: Double, // positive amount
    val currency: String = "INR",
    val memo: String = "",
    val wallet: String = "Cash",
    val isRecurring: Boolean = false,
    val recurringId: Long? = null
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val parentCategory: String? = null, // null if main category, non-null if sub-category
    val type: String = "Expense", // "Expense" or "Income"
    val iconName: String = "",
    val colorHex: String = "",
    val usageCount: Int = 0
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val amountLimit: Double,
    val period: String = "Monthly" // "Monthly", "Quarterly", "Yearly"
)

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String = "Expense",
    val dayOfMonth: Int, // 1..31
    val wallet: String = "Cash",
    val memo: String = "",
    val autoAdd: Boolean = true,
    val lastPostedYearMonth: String = "", // "yyyy-MM"
    val startDate: Long = 0L // Epoch timestamp from which this recurring entry is active
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personName: String,
    val amount: Double,
    val paidAmount: Double = 0.0,
    val type: String, // "Borrowed" or "Lent"
    val date: Long = System.currentTimeMillis(),
    val memo: String = "",
    val isSettled: Boolean = false
) {
    val remainingAmount: Double
        get() = (amount - paidAmount).coerceAtLeast(0.0)
}

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetCategory: String = "",
    val description: String = "",
    val iconKey: String = "check",
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_logs")
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateString: String, // "yyyy-MM-dd"
    val isCompleted: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
