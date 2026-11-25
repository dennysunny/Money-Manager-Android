package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DebtEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.RecurringExpenseEntity
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE date >= :startTime AND date <= :endTime ORDER BY date DESC, id DESC")
    fun getTransactionsInRange(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date >= :startTime AND date <= :endTime ORDER BY date DESC")
    suspend fun getTransactionsInRangeSync(startTime: Long, endTime: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC, id DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT DISTINCT category FROM transactions ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT DISTINCT wallet FROM transactions ORDER BY wallet ASC")
    fun getAllWallets(): Flow<List<String>>
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentCategory IS NULL ORDER BY name ASC")
    fun getMainCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentCategory = :parentName ORDER BY name ASC")
    fun getSubcategories(parentName: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentCategory = :parentName")
    suspend fun getSubcategoriesSync(parentName: String): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategoriesSync(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE parentCategory = :parentName")
    suspend fun deleteSubcategoriesOf(parentName: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets ORDER BY category ASC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgetsSync(): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE category = :category LIMIT 1")
    suspend fun getBudgetByCategory(category: String): BudgetEntity?

    @Query("SELECT COUNT(*) FROM budgets")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()
}

@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expenses ORDER BY dayOfMonth ASC")
    fun getAllRecurring(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE autoAdd = 1")
    suspend fun getActiveAutoAddRecurring(): List<RecurringExpenseEntity>

    @Query("SELECT * FROM recurring_expenses ORDER BY dayOfMonth ASC")
    suspend fun getAllRecurringSync(): List<RecurringExpenseEntity>

    @Query("SELECT * FROM recurring_expenses WHERE id = :id LIMIT 1")
    suspend fun getRecurringById(id: Long): RecurringExpenseEntity?

    @Query("SELECT COUNT(*) FROM recurring_expenses")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recurring: RecurringExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recurringList: List<RecurringExpenseEntity>)

    @Update
    suspend fun update(recurring: RecurringExpenseEntity)

    @Delete
    suspend fun delete(recurring: RecurringExpenseEntity)

    @Query("DELETE FROM recurring_expenses")
    suspend fun deleteAll()
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY date DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts ORDER BY date DESC")
    suspend fun getAllDebtsSync(): List<DebtEntity>

    @Query("SELECT COUNT(*) FROM debts")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(debt: DebtEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(debts: List<DebtEntity>)

    @Update
    suspend fun update(debt: DebtEntity)

    @Delete
    suspend fun delete(debt: DebtEntity)

    @Query("DELETE FROM debts")
    suspend fun deleteAll()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY id ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY id ASC")
    suspend fun getAllHabitsSync(): List<HabitEntity>

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(habits: List<HabitEntity>)

    @Update
    suspend fun update(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("SELECT * FROM habit_logs ORDER BY dateString DESC")
    fun getAllLogs(): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE dateString = :dateString")
    fun getLogsForDate(dateString: String): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM habit_logs WHERE dateString = :dateString")
    suspend fun getLogsForDateSync(dateString: String): List<HabitLogEntity>

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString LIMIT 1")
    suspend fun getLog(habitId: Long, dateString: String): HabitLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLogEntity): Long

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString")
    suspend fun deleteLog(habitId: Long, dateString: String)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId")
    suspend fun deleteLogsForHabit(habitId: Long)
}
