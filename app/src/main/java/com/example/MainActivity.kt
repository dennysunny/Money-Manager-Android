package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import com.example.ui.theme.ExpressiveFabShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionEntity
import com.example.ui.AppTab
import com.example.ui.MoneyViewModel
import com.example.ui.TimeFilter
import com.example.ui.components.AddEditTransactionDialog
import com.example.ui.components.FloatingNavBar
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CombinedAnalyticsScreen
import com.example.ui.screens.AnalyticsSubTab
import com.example.ui.screens.DriveScreen
import com.example.ui.screens.EmiScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.SalaryScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.MoneyManagerTheme
import com.example.receiver.SmsNotificationHelper
import com.example.widget.WidgetWallpaperTintManager

class MainActivity : ComponentActivity() {
    private val viewModel: MoneyViewModel by viewModels()
    private val triggerWidgetAction = mutableStateOf<String?>(null)
    private val triggerSmsIntent = mutableStateOf<Intent?>(null)
    private val resumeWallpaperTick = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }

        SmsNotificationHelper.createNotificationChannel(this)
        handleIncomingIntent(intent)

        // Auto-sync on app open
        if (viewModel.driveAutoSync.value) {
            viewModel.syncGoogleDrive()
        }

        setContent {
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
            val isAppWallpaperTintActive by viewModel.isAppWallpaperTintActive.collectAsStateWithLifecycle()
            var wallpaperPaletteTick by remember { mutableIntStateOf(0) }
            DisposableEffect(this) {
                val unregister = WidgetWallpaperTintManager.registerWallpaperListener(this@MainActivity) {
                    wallpaperPaletteTick++
                }
                onDispose { unregister() }
            }
            MoneyManagerTheme(
                appTheme = appTheme,
                dynamicColor = isAppWallpaperTintActive,
                wallpaperPaletteTick = wallpaperPaletteTick + resumeWallpaperTick.intValue
            ) {
                var showSplash by remember { mutableStateOf(triggerWidgetAction.value == null && triggerSmsIntent.value == null) }

                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(180)) togetherWith
                        fadeOut(animationSpec = tween(180))
                    },
                    label = "splash_screen_transition"
                ) { isSplash ->
                    if (isSplash) {
                        AppSplashScreen(onFinish = { showSplash = false })
                    } else {
                        MoneyManagerApp(
                            viewModel = viewModel,
                            widgetIntentAction = triggerWidgetAction.value,
                            onWidgetActionConsumed = { triggerWidgetAction.value = null },
                            smsIntent = triggerSmsIntent.value,
                            onSmsActionConsumed = { triggerSmsIntent.value = null }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumeWallpaperTick.intValue++
        WidgetWallpaperTintManager.refreshWidgetsIfWallpaperChanged(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val action = intent?.action
        if (action == "com.example.ACTION_ADD_EXPENSE" ||
            action == "com.example.ACTION_ADD_INCOME" ||
            action == "com.example.ACTION_OPEN_HABITS" ||
            action == "com.example.ACTION_OPEN_INSIGHTS") {
            triggerWidgetAction.value = action
        } else if (action == SmsNotificationHelper.ACTION_RECORD_SMS_TRANSACTION) {
            triggerSmsIntent.value = intent
        }
    }

    override fun onStop() {
        super.onStop()
        // Auto-sync on app close / background
        if (viewModel.driveAutoSync.value) {
            viewModel.syncGoogleDrive()
        }
    }
}

@Composable
fun AppSplashScreen(onFinish: () -> Unit) {
    val scale = remember { Animatable(0.78f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
    }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
        )
        delay(350)
        alpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
        )
        onFinish()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("splash_screen")
            .clickable { onFinish() },
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
            ) {
                Surface(
                    modifier = Modifier.size(96.dp),
                    shape = ExpressiveFabShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Money Manager Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Money Manager",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Smart Personal Expense Tracker",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(30.dp))

                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MoneyManagerApp(
    viewModel: MoneyViewModel,
    widgetIntentAction: String?,
    onWidgetActionConsumed: () -> Unit,
    smsIntent: Intent? = null,
    onSmsActionConsumed: () -> Unit = {}
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val selectedTimeFilter by viewModel.selectedTimeFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsStateWithLifecycle()
    val budgetProgressList by viewModel.budgetProgressList.collectAsStateWithLifecycle()
    val recurringList by viewModel.allRecurring.collectAsStateWithLifecycle()
    val debtsList by viewModel.allDebts.collectAsStateWithLifecycle()
    val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
    val sortedExpenseCats by viewModel.sortedExpenseCategories.collectAsStateWithLifecycle()
    val sortedIncomeCats by viewModel.sortedIncomeCategories.collectAsStateWithLifecycle()
    val subcategoriesMap by viewModel.subcategoriesByParent.collectAsStateWithLifecycle()

    val selectedStatCategory by viewModel.selectedStatCategory.collectAsStateWithLifecycle()
    val categoryDrillDownTransactions by viewModel.categoryDrillDownTransactions.collectAsStateWithLifecycle()
    val categoryTrendBars by viewModel.categoryTrendBars.collectAsStateWithLifecycle()
    val overallTrendBars by viewModel.overallTrendBars.collectAsStateWithLifecycle()
    val insightsData by viewModel.insightsData.collectAsStateWithLifecycle()

    val lastDriveSyncTime by viewModel.lastDriveSyncTime.collectAsStateWithLifecycle()
    val isDriveSyncing by viewModel.isDriveSyncing.collectAsStateWithLifecycle()
    val driveAccount by viewModel.driveSyncAccount.collectAsStateWithLifecycle()
    val driveAutoSync by viewModel.driveAutoSync.collectAsStateWithLifecycle()
    val currentAppTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val wallpaperTintEnabled by viewModel.wallpaperTintEnabled.collectAsStateWithLifecycle()
    val applyWallpaperTintToApp by viewModel.applyWallpaperTintToApp.collectAsStateWithLifecycle()
    val applyWallpaperTintToWidgets by viewModel.applyWallpaperTintToWidgets.collectAsStateWithLifecycle()

    val paymentModeStats by viewModel.paymentModeStats.collectAsStateWithLifecycle()
    val salaryAmount by viewModel.salaryAmount.collectAsStateWithLifecycle()
    val salaryEmployer by viewModel.salaryEmployer.collectAsStateWithLifecycle()
    val salaryCreditDay by viewModel.salaryCreditDay.collectAsStateWithLifecycle()
    val salaryWallet by viewModel.salaryWallet.collectAsStateWithLifecycle()
    val salaryAutoCredit by viewModel.salaryAutoCredit.collectAsStateWithLifecycle()
    val salaryLastCreditedMonth by viewModel.salaryLastCreditedMonth.collectAsStateWithLifecycle()
    var showSalaryScreen by remember { mutableStateOf(false) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var addDialogDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var addDialogInitialType by remember { mutableStateOf("Expense") }
    var addDialogInitialAmount by remember { mutableStateOf("") }
    var addDialogInitialCategory by remember { mutableStateOf("") }
    var addDialogInitialMemo by remember { mutableStateOf("") }
    var addDialogInitialWallet by remember { mutableStateOf("") }
    var statsSelectedTab by remember { mutableStateOf("SummaryDashboard") }

    // Respond to Widget quick add or navigation intent
    LaunchedEffect(widgetIntentAction) {
        if (widgetIntentAction != null) {
            if (widgetIntentAction == "com.example.ACTION_OPEN_HABITS" || widgetIntentAction == "com.example.ACTION_OPEN_INSIGHTS") {
                viewModel.selectTab(AppTab.INSIGHTS)
                onWidgetActionConsumed()
            } else {
                editingTransaction = null
                addDialogDateMillis = System.currentTimeMillis()
                addDialogInitialType = if (widgetIntentAction == "com.example.ACTION_ADD_INCOME") "Income" else "Expense"
                addDialogInitialAmount = ""
                addDialogInitialCategory = ""
                addDialogInitialMemo = ""
                addDialogInitialWallet = ""
                showAddDialog = true
                onWidgetActionConsumed()
            }
        }
    }

    // Respond to SMS transaction push notification intent
    LaunchedEffect(smsIntent) {
        if (smsIntent != null) {
            val amt = smsIntent.getDoubleExtra(SmsNotificationHelper.EXTRA_SMS_AMOUNT, 0.0)
            val type = smsIntent.getStringExtra(SmsNotificationHelper.EXTRA_SMS_TYPE) ?: "Expense"
            val category = smsIntent.getStringExtra(SmsNotificationHelper.EXTRA_SMS_CATEGORY) ?: "Others"
            val memo = smsIntent.getStringExtra(SmsNotificationHelper.EXTRA_SMS_MEMO) ?: ""
            val wallet = smsIntent.getStringExtra(SmsNotificationHelper.EXTRA_SMS_WALLET) ?: "Cash / UPI"
            val date = smsIntent.getLongExtra(SmsNotificationHelper.EXTRA_SMS_DATE, System.currentTimeMillis())

            editingTransaction = null
            addDialogDateMillis = date
            addDialogInitialType = type
            addDialogInitialAmount = if (amt > 0) {
                if (amt % 1.0 == 0.0) amt.toInt().toString() else amt.toString()
            } else ""
            addDialogInitialCategory = category
            addDialogInitialMemo = memo
            addDialogInitialWallet = wallet
            showAddDialog = true
            onSmsActionConsumed()
        }
    }

    // BackHandler: Handle returning from category drilldown or returning to Ledger
    if (selectedStatCategory != null && currentTab == AppTab.STATISTICS) {
        BackHandler {
            viewModel.selectCategoryForDrillDown(null)
        }
    } else if (currentTab != AppTab.LEDGER) {
        BackHandler {
            viewModel.selectTab(AppTab.LEDGER)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (showSalaryScreen) {
                SalaryScreen(
                    currentAmount = salaryAmount,
                    currentEmployer = salaryEmployer,
                    currentCreditDay = salaryCreditDay,
                    currentWallet = salaryWallet,
                    currentAutoCredit = salaryAutoCredit,
                    lastCreditedMonth = salaryLastCreditedMonth,
                    onSaveSalarySettings = { amt, emp, day, wal, auto ->
                        viewModel.updateSalarySettings(amt, emp, day, wal, auto)
                    },
                    onCreditSalaryNow = { viewModel.creditSalaryNow() },
                    onBack = { showSalaryScreen = false }
                )
            } else {
                // Main Screen Content based on active tab
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_animation"
                ) { tab ->
                    when (tab) {
                        AppTab.LEDGER -> {
                            val currentReferenceDate by viewModel.currentReferenceDate.collectAsStateWithLifecycle()
                            LedgerScreen(
                                transactions = filteredTransactions,
                                totalExpense = totalExpense,
                                totalIncome = totalIncome,
                                selectedTimeFilter = selectedTimeFilter,
                                periodLabel = viewModel.getPeriodLabel(),
                                searchQuery = searchQuery,
                                typeFilter = typeFilter,
                                onTimeFilterSelected = { viewModel.setTimeFilter(it) },
                                onPreviousPeriod = { viewModel.previousPeriod() },
                                onNextPeriod = { viewModel.nextPeriod() },
                                onResetToday = { viewModel.resetToToday() },
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onTypeFilterChange = { viewModel.setTypeFilter(it) },
                                onEditTransaction = { tx ->
                                    editingTransaction = tx
                                    showAddDialog = true
                                },
                                onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) },
                                onOpenSummaryDashboard = {
                                    viewModel.selectTab(AppTab.INSIGHTS)
                                },
                                allTransactions = allTransactions,
                                currentReferenceDate = currentReferenceDate,
                                onSelectDayDate = { newCal ->
                                    viewModel.setTimeFilter(TimeFilter.DAY)
                                    viewModel.setReferenceDate(newCal)
                                }
                            )
                        }

                        AppTab.INSIGHTS, AppTab.STATISTICS -> {
                            val habitTrackerState by viewModel.habitTrackerState.collectAsStateWithLifecycle()
                            CombinedAnalyticsScreen(
                                insightsData = insightsData,
                                habitTrackerState = habitTrackerState,
                                onToggleHabit = { habitId -> viewModel.toggleHabit(habitId) },
                                onAddCustomHabit = { title, cat, desc -> viewModel.addCustomHabit(title, cat, desc) },
                                onDeleteHabit = { habit -> viewModel.deleteHabit(habit) },
                                categorySpends = categoryBreakdown,
                                paymentModeStats = paymentModeStats,
                                totalExpense = totalExpense,
                                totalIncome = totalIncome,
                                selectedTimeFilter = selectedTimeFilter,
                                periodLabel = viewModel.getPeriodLabel(),
                                selectedCategory = selectedStatCategory,
                                categoryTransactions = categoryDrillDownTransactions,
                                categoryTrendBars = categoryTrendBars,
                                overallTrendBars = overallTrendBars,
                                allTransactions = allTransactions,
                                onSelectCategory = { cat -> viewModel.selectCategoryForDrillDown(cat) },
                                onTimeFilterSelected = { viewModel.setTimeFilter(it) },
                                onPreviousPeriod = { viewModel.previousPeriod() },
                                onNextPeriod = { viewModel.nextPeriod() },
                                onResetToday = { viewModel.resetToToday() },
                                onEditTransaction = { tx ->
                                    editingTransaction = tx
                                    showAddDialog = true
                                },
                                onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) },
                                initialSubTab = if (currentTab == AppTab.STATISTICS) {
                                    when (statsSelectedTab) {
                                        "Categories" -> AnalyticsSubTab.CATEGORIES
                                        "MonthlyReport" -> AnalyticsSubTab.MONTHLY_REPORT
                                        "PaymentModes" -> AnalyticsSubTab.PAYMENT_MODES
                                        else -> AnalyticsSubTab.CATEGORIES
                                    }
                                } else {
                                    AnalyticsSubTab.INSIGHTS
                                }
                            )
                        }

                        AppTab.BUDGETS -> BudgetsScreen(
                            budgetProgressList = budgetProgressList,
                            totalExpense = totalExpense,
                            totalIncome = totalIncome,
                            selectedTimeFilter = selectedTimeFilter,
                            periodLabel = viewModel.getPeriodLabel(),
                            onTimeFilterSelected = { viewModel.setTimeFilter(it) },
                            onPreviousPeriod = { viewModel.previousPeriod() },
                            onNextPeriod = { viewModel.nextPeriod() },
                            onResetToday = { viewModel.resetToToday() },
                            onSaveBudget = { id, cat, limit, period ->
                                viewModel.saveBudget(id, cat, limit, period)
                            },
                            onDeleteBudget = { b -> viewModel.deleteBudget(b) },
                            onEditTransaction = { tx ->
                                editingTransaction = tx
                                showAddDialog = true
                            },
                            onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) },
                            allCategories = allCategories
                        )


                        AppTab.EMI -> EmiScreen(
                            recurringList = recurringList,
                            debtsList = debtsList,
                            categoriesList = allCategories,
                            salaryAmount = salaryAmount,
                            salaryEmployer = salaryEmployer,
                            salaryCreditDay = salaryCreditDay,
                            salaryWallet = salaryWallet,
                            salaryAutoCredit = salaryAutoCredit,
                            onOpenSalaryScreen = { showSalaryScreen = true },
                            onAddRecurring = { title, amt, cat, day, memo, autoAdd, startDate ->
                                viewModel.saveRecurring(title = title, amount = amt, category = cat, dayOfMonth = day, memo = memo, autoAdd = autoAdd, startDate = startDate)
                            },
                            onUpdateRecurring = { id, title, amt, cat, day, memo, autoAdd, startDate ->
                                viewModel.saveRecurring(id = id, title = title, amount = amt, category = cat, dayOfMonth = day, memo = memo, autoAdd = autoAdd, startDate = startDate)
                            },
                            onTriggerRecurringNow = { r -> viewModel.triggerRecurringNow(r) },
                            onDeleteRecurring = { r -> viewModel.deleteRecurring(r) },
                            onAddDebt = { name, amt, type, memo ->
                                viewModel.saveDebt(personName = name, amount = amt, type = type, memo = memo)
                            },
                            onToggleDebtSettled = { d -> viewModel.toggleDebtSettled(d) },
                            onPayDebt = { d, amt -> viewModel.payDebtAmount(d, amt) },
                            onDeleteDebt = { d -> viewModel.deleteDebt(d) },
                            onAddCategory = { name, parent, type, iconName ->
                                viewModel.addCategory(name = name, parentCategory = parent, type = type, iconName = iconName)
                            },
                            onDeleteCategory = { cat -> viewModel.deleteCategory(cat) }
                        )

                        AppTab.DRIVE -> DriveScreen(
                            driveAccount = driveAccount,
                            lastDriveSyncTime = lastDriveSyncTime,
                            isDriveSyncing = isDriveSyncing,
                            driveAutoSync = driveAutoSync,
                            onSyncNow = { viewModel.syncGoogleDrive() },
                            onToggleAutoSync = { viewModel.setAutoSync(it) },
                            onExportBackup = { callback ->
                                viewModel.exportBackupJson { json -> callback(json) }
                            },
                            onRestoreBackup = { json, callback ->
                                viewModel.restoreBackup(json) { success, count -> callback(success, count) }
                            },
                            onClearAllData = { callback ->
                                viewModel.clearAllData { callback() }
                            },
                            onLoadSampleData = { callback ->
                                viewModel.loadSampleData { callback() }
                            },
                            currentTheme = currentAppTheme,
                            onSelectTheme = { theme -> viewModel.setAppTheme(theme) },
                            wallpaperTintEnabled = wallpaperTintEnabled,
                            applyWallpaperTintToApp = applyWallpaperTintToApp,
                            applyWallpaperTintToWidgets = applyWallpaperTintToWidgets,
                            onToggleWallpaperTint = { enabled -> viewModel.setWallpaperTintEnabled(enabled) },
                            onToggleApplyWallpaperTintToApp = { enabled -> viewModel.setApplyWallpaperTintToApp(enabled) },
                            onToggleApplyWallpaperTintToWidgets = { enabled -> viewModel.setApplyWallpaperTintToWidgets(enabled) },
                            onRestoreFromGoogleDrive = { callback ->
                                viewModel.restoreFromGoogleDrive { success, count, msg ->
                                    callback(success, count, msg)
                                }
                            }
                        )
                    }
                }
            }

            // Quick Add Liquid Glass FAB positioned above the floating nav bar
            com.example.ui.components.LiquidGlassFab(
                onClick = {
                    editingTransaction = null
                    addDialogDateMillis = System.currentTimeMillis()
                    addDialogInitialType = "Expense"
                    showAddDialog = true
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 82.dp)
                    .size(56.dp)
                    .testTag("fab_quick_add")
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Quick Add Transaction",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Material 3 Expressive Floating Navigation Bar
            FloatingNavBar(
                selectedTab = currentTab,
                onTabSelected = { tab ->
                    if (tab != AppTab.STATISTICS) {
                        viewModel.selectCategoryForDrillDown(null)
                    }
                    viewModel.selectTab(tab)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Add or Edit Transaction Dialog
        if (showAddDialog) {
            AddEditTransactionDialog(
                initialTransaction = editingTransaction,
                initialType = addDialogInitialType,
                initialAmount = addDialogInitialAmount,
                initialCategory = addDialogInitialCategory,
                initialMemo = addDialogInitialMemo,
                initialWallet = addDialogInitialWallet,
                defaultDate = addDialogDateMillis,
                sortedExpenseCategories = sortedExpenseCats,
                sortedIncomeCategories = sortedIncomeCats,
                subcategoriesMap = subcategoriesMap,
                onDismiss = {
                    showAddDialog = false
                    editingTransaction = null
                    addDialogInitialAmount = ""
                    addDialogInitialCategory = ""
                    addDialogInitialMemo = ""
                    addDialogInitialWallet = ""
                },
                onSave = { id, amount, category, subcategory, type, memo, wallet, date ->
                    viewModel.saveTransaction(
                        id = id,
                        amount = amount,
                        category = category,
                        subcategory = subcategory,
                        type = type,
                        memo = memo,
                        wallet = wallet,
                        date = date
                    )
                    showAddDialog = false
                    editingTransaction = null
                    addDialogInitialAmount = ""
                    addDialogInitialCategory = ""
                    addDialogInitialMemo = ""
                    addDialogInitialWallet = ""
                },
                onAddSubcategory = { parent, subcatName, iconName ->
                    viewModel.addCategory(name = subcatName, parentCategory = parent, type = "Expense", iconName = iconName)
                },
                onAddNewCategory = { catName, type, iconName ->
                    viewModel.addCategory(name = catName, parentCategory = null, type = type, iconName = iconName)
                }
            )
        }
    }
}
