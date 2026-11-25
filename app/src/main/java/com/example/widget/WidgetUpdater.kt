package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import kotlin.math.roundToInt

object WidgetUpdater {

    const val PREF_SHOW_WIDGET_NAMES = "show_widget_names"
    const val PREF_WIDGET_INNER_PADDING_DP = "widget_inner_padding_dp"
    const val PREF_WIDGET_BORDER_STYLE = "widget_border_style"
    const val PREF_WIDGET_BORDER_ENABLED = "widget_border_enabled"
    const val PREF_WIDGET_TRANSPARENT_BG = "widget_transparent_bg"
    const val PREF_WIDGET_WALLPAPER_TINT = WidgetWallpaperTintManager.PREF_WIDGET_WALLPAPER_TINT
    const val DEFAULT_WIDGET_INNER_PADDING_DP = 14
    const val DEFAULT_WIDGET_CORNER_RADIUS_DP = 26f

    fun isWidgetTransparentBg(context: Context): Boolean {
        return context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE)
            .getBoolean(PREF_WIDGET_TRANSPARENT_BG, false)
    }

    fun setWidgetTransparentBg(context: Context, transparent: Boolean) {
        context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_WIDGET_TRANSPARENT_BG, transparent)
            .apply()
    }

    /**
     * Caps bitmap backdrop corner radius at min(width, height) * 0.45 so rounded backdrops
     * never deform at compact aspect ratios.
     */
    fun computeBackdropCornerRadiusPx(
        widthPx: Float,
        heightPx: Float,
        density: Float,
        desiredRadiusDp: Float = DEFAULT_WIDGET_CORNER_RADIUS_DP
    ): Float {
        val maxAllowed = minOf(widthPx, heightPx) * 0.45f
        return (desiredRadiusDp * density).coerceIn(0f, maxAllowed.coerceAtLeast(0f))
    }

    private fun applyAtomicCardStyle(
        context: Context,
        views: RemoteViews,
        cardViewId: Int,
        isTransparent: Boolean,
        isSingleRow: Boolean = false
    ): Boolean {
        val prefs = context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE)
        val paddingDp = prefs.getInt(PREF_WIDGET_INNER_PADDING_DP, DEFAULT_WIDGET_INNER_PADDING_DP)
            .coerceIn(8, 22)
        val borderEnabled = prefs.getBoolean(PREF_WIDGET_BORDER_ENABLED, true)
        val borderStyle = if (borderEnabled) {
            prefs.getString(PREF_WIDGET_BORDER_STYLE, "hairline") ?: "hairline"
        } else {
            "none"
        }
        val wallpaperTintEnabled = WidgetWallpaperTintManager.shouldTintWidgets(context)

        val bgRes = if (wallpaperTintEnabled) {
            when (borderStyle.lowercase(Locale.US)) {
                "none" -> if (isTransparent) R.drawable.widget_dynamic_transparent_card_bg_none else R.drawable.widget_dynamic_atomic_card_bg_none
                "glow" -> if (isTransparent) R.drawable.widget_dynamic_transparent_card_bg_glow else R.drawable.widget_dynamic_atomic_card_bg_glow
                "bold" -> if (isTransparent) R.drawable.widget_dynamic_transparent_card_bg_bold else R.drawable.widget_dynamic_atomic_card_bg_bold
                else -> if (isTransparent) R.drawable.widget_dynamic_transparent_card_bg else R.drawable.widget_dynamic_atomic_card_bg
            }
        } else {
            when (borderStyle.lowercase(Locale.US)) {
                "none" -> if (isTransparent) R.drawable.widget_transparent_card_bg_none else R.drawable.widget_atomic_card_bg_none
                "glow" -> if (isTransparent) R.drawable.widget_transparent_card_bg_glow else R.drawable.widget_atomic_card_bg_glow
                "bold" -> if (isTransparent) R.drawable.widget_transparent_card_bg_bold else R.drawable.widget_atomic_card_bg_bold
                else -> if (isTransparent) R.drawable.widget_transparent_card_bg else R.drawable.widget_atomic_card_bg
            }
        }
        views.setInt(cardViewId, "setBackgroundResource", bgRes)

        val density = context.resources.displayMetrics.density
        // Glow layer-list includes a 2dp inset around the inner card for the 4dp soft outer halo
        val extraHaloInsetDp = if (borderStyle.equals("glow", ignoreCase = true)) 2 else 0
        val padHorizontalPx = ((paddingDp + extraHaloInsetDp) * density).roundToInt()
        val effectiveVerticalDp = if (isSingleRow) minOf(paddingDp, 10) else paddingDp
        val padVerticalPx = ((effectiveVerticalDp + extraHaloInsetDp) * density).roundToInt()
        views.setViewPadding(cardViewId, padHorizontalPx, padVerticalPx, padHorizontalPx, padVerticalPx)
        return wallpaperTintEnabled
    }

    private fun primaryTintTextColor(
        isTransparent: Boolean,
        palette: WidgetWallpaperTintManager.WidgetDynamicPalette
    ): Int = if (isTransparent) palette.primaryTextOnTransparent else palette.accentText

    private fun applyTintedPillBackground(
        views: RemoteViews,
        viewId: Int,
        tintColor: Int,
        fallbackDrawableRes: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            views.setInt(viewId, "setBackgroundResource", R.drawable.widget_pill_tintable)
            views.setColorStateList(viewId, "setBackgroundTintList", ColorStateList.valueOf(tintColor))
        } else {
            views.setInt(viewId, "setBackgroundResource", fallbackDrawableRes)
        }
    }

    private fun applyStaticPillBackground(
        views: RemoteViews,
        viewId: Int,
        staticDrawableRes: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            views.setColorStateList(viewId, "setBackgroundTintList", null)
        }
        views.setInt(viewId, "setBackgroundResource", staticDrawableRes)
    }

    private fun applyPrimaryPillStyle(
        context: Context,
        views: RemoteViews,
        viewId: Int,
        wallpaperTintEnabled: Boolean,
        palette: WidgetWallpaperTintManager.WidgetDynamicPalette
    ) {
        if (wallpaperTintEnabled) {
            applyTintedPillBackground(
                views = views,
                viewId = viewId,
                tintColor = palette.primaryPillBg,
                fallbackDrawableRes = R.drawable.widget_dynamic_pill_primary
            )
            views.setTextColor(viewId, palette.primaryPillText)
        } else {
            applyStaticPillBackground(views, viewId, R.drawable.widget_pixel_pill_primary)
            views.setTextColor(viewId, ContextCompat.getColor(context, R.color.widget_pill_primary_text))
        }
    }

    private fun applySecondaryPillStyle(
        context: Context,
        views: RemoteViews,
        viewId: Int,
        isTransparent: Boolean,
        wallpaperTintEnabled: Boolean,
        palette: WidgetWallpaperTintManager.WidgetDynamicPalette,
        updateTextColor: Boolean = true
    ) {
        if (wallpaperTintEnabled) {
            val pillColor = if (isTransparent) palette.transparentPillBg else palette.secondaryPillBg
            val fallbackRes = if (isTransparent) R.drawable.widget_dynamic_transparent_pill else R.drawable.widget_dynamic_pill_secondary
            applyTintedPillBackground(views, viewId, pillColor, fallbackRes)
        } else {
            val staticRes = if (isTransparent) R.drawable.widget_transparent_pill else R.drawable.widget_pixel_pill_secondary
            applyStaticPillBackground(views, viewId, staticRes)
        }
        if (updateTextColor) {
            if (isTransparent) {
                views.setTextColor(
                    viewId,
                    if (wallpaperTintEnabled) palette.primaryTextOnTransparent else ContextCompat.getColor(context, R.color.widget_transparent_text_primary)
                )
            } else {
                views.setTextColor(
                    viewId,
                    if (wallpaperTintEnabled) palette.secondaryPillText else ContextCompat.getColor(context, R.color.widget_pill_secondary_text)
                )
            }
        }
    }

    private fun setScaledNumberText(
        views: RemoteViews,
        viewId: Int,
        text: String,
        baseSp: Float,
        minSp: Float,
        thresholdLen: Int = 9
    ) {
        val excess = (text.length - thresholdLen).coerceAtLeast(0)
        val scaledSp = (baseSp - excess * 0.7f).coerceIn(minSp, baseSp)
        views.setTextViewText(viewId, text)
        views.setTextViewTextSize(viewId, TypedValue.COMPLEX_UNIT_SP, scaledSp)
    }

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
        val coroutineScope = CoroutineScope(Dispatchers.IO)

        coroutineScope.launch {
            try {
                val isTransparent = isWidgetTransparentBg(context)
                // Unified OriginOS widgets (dynamically switch between Solid and Transparent via Settings)
                updateQuickAddWidget(context, appWidgetManager, isTransparent = isTransparent)
                updateSummaryWidget(context, appWidgetManager, isTransparent = isTransparent)
                updateMinimalWidget(context, appWidgetManager, isTransparent = isTransparent)
                updateBudgetWidget(context, appWidgetManager, isTransparent = isTransparent)
                updateHabitWidget(context, appWidgetManager, isTransparent = isTransparent)
                updateVelocityWidget(context, appWidgetManager, isTransparent = isTransparent)
                updateSpendingHabitsWidget(context, appWidgetManager, isTransparent = isTransparent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateQuickAddWidget(context: Context, appWidgetManager: AppWidgetManager, targetIds: IntArray? = null, isTransparent: Boolean = isWidgetTransparentBg(context)) {
        val componentName = ComponentName(context, QuickAddWidgetProvider::class.java)
        val appWidgetIds = if (targetIds != null && targetIds.isNotEmpty()) targetIds else appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds == null || appWidgetIds.isEmpty()) return

        val layoutId = if (isTransparent) R.layout.widget_transparent_quick_add_1x4 else R.layout.widget_quick_add_1x4
        val showWidgetNames = context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE)
            .getBoolean("show_widget_names", true)
        val labelVisibility = if (showWidgetNames) View.VISIBLE else View.INVISIBLE

        val palette = WidgetWallpaperTintManager.getDynamicPalette(context)
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutId)
            val wallpaperTintEnabled = applyAtomicCardStyle(context, views, R.id.widget_1x4_card, isTransparent, isSingleRow = true)
            if (isTransparent) {
                if (wallpaperTintEnabled) {
                    applyTintedPillBackground(views, R.id.btn_add_expense, palette.transparentPillBg, R.drawable.widget_dynamic_transparent_pill)
                    views.setTextColor(R.id.btn_add_expense, palette.primaryTextOnTransparent)
                    applyTintedPillBackground(views, R.id.btn_add_income, palette.primaryPillBg, R.drawable.widget_dynamic_pill_primary)
                    views.setTextColor(R.id.btn_add_income, palette.primaryPillText)
                } else {
                    applyStaticPillBackground(views, R.id.btn_add_expense, R.drawable.widget_transparent_pill)
                    views.setTextColor(R.id.btn_add_expense, Color.parseColor("#F87171"))
                    applyStaticPillBackground(views, R.id.btn_add_income, R.drawable.widget_transparent_pill)
                    views.setTextColor(R.id.btn_add_income, Color.parseColor("#4ADE80"))
                }
            } else {
                if (wallpaperTintEnabled) {
                    applyTintedPillBackground(views, R.id.btn_add_expense, palette.secondaryPillBg, R.drawable.widget_dynamic_pill_secondary)
                    views.setTextColor(R.id.btn_add_expense, palette.accentText)
                    applyTintedPillBackground(views, R.id.btn_add_income, palette.primaryPillBg, R.drawable.widget_dynamic_pill_primary)
                    views.setTextColor(R.id.btn_add_income, palette.primaryPillText)
                } else {
                    applyStaticPillBackground(views, R.id.btn_add_expense, R.drawable.widget_btn_expense_bg)
                    views.setTextColor(R.id.btn_add_expense, ContextCompat.getColor(context, R.color.widget_btn_expense_text))
                    applyStaticPillBackground(views, R.id.btn_add_income, R.drawable.widget_btn_income_bg)
                    views.setTextColor(R.id.btn_add_income, ContextCompat.getColor(context, R.color.widget_btn_income_text))
                }
            }
            views.setViewVisibility(R.id.widget_1x4_atomic_label, labelVisibility)

            // Open app normally when tapping background
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val mainPending = PendingIntent.getActivity(
                context, if (isTransparent) 110 else 100, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_1x4_root, mainPending)

            // Open app with Add Expense
            val expenseIntent = Intent(context, MainActivity::class.java).apply {
                action = "com.example.ACTION_ADD_EXPENSE"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val expensePending = PendingIntent.getActivity(
                context, if (isTransparent) 111 else 101, expenseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_add_expense, expensePending)

            // Open app with Add Income
            val incomeIntent = Intent(context, MainActivity::class.java).apply {
                action = "com.example.ACTION_ADD_INCOME"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val incomePending = PendingIntent.getActivity(
                context, if (isTransparent) 112 else 102, incomeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_add_income, incomePending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    suspend fun updateSummaryWidget(context: Context, appWidgetManager: AppWidgetManager, targetIds: IntArray? = null, isTransparent: Boolean = isWidgetTransparentBg(context)) = withContext(Dispatchers.IO) {
        val componentName = ComponentName(context, SummaryWidgetProvider::class.java)
        val appWidgetIds = if (targetIds != null && targetIds.isNotEmpty()) targetIds else appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds == null || appWidgetIds.isEmpty()) return@withContext

        val layoutId = if (isTransparent) R.layout.widget_transparent_summary_2x2 else R.layout.widget_summary_2x2

        val db = AppDatabase.getDatabase(context)
        val cal = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val currentMonth = monthFormat.format(cal.time)
        val numberFormat = DecimalFormat("#,##,##0")

        val startCal = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val txs = try {
            db.transactionDao().getTransactionsInRangeSync(startCal.timeInMillis, endCal.timeInMillis)
        } catch (e: Exception) {
            emptyList()
        }

        val expenseTxs = txs.filter { it.type.equals("Expense", ignoreCase = true) }
        val monthExpense = expenseTxs.sumOf { it.amount }
        val monthIncome = txs.filter { it.type.equals("Income", ignoreCase = true) }.sumOf { it.amount }
        val netBalance = monthIncome - monthExpense

        val catExpenses = expenseTxs.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }

        val isDark = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val wallpaperTintEnabled = WidgetWallpaperTintManager.shouldTintWidgets(context)
        val palette = WidgetWallpaperTintManager.getDynamicPalette(context, isDark)
        val primaryTint = primaryTintTextColor(isTransparent, palette)
        val pieChartBitmap = createPieChartBitmap(
            catExpenses,
            monthExpense,
            isDark,
            dynamicTrackColor = if (wallpaperTintEnabled) palette.trackRing else null,
            dynamicCenterTextColor = if (wallpaperTintEnabled) primaryTint else null
        )

        val todayStartCal = (cal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayExpense = expenseTxs.filter { it.date >= todayStartCal.timeInMillis }.sumOf { it.amount }
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        val dailyVelocity = monthExpense / dayOfMonth

        val showWidgetNames = context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE)
            .getBoolean("show_widget_names", true)
        val labelVisibility = if (showWidgetNames) View.VISIBLE else View.INVISIBLE

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutId)
            applyAtomicCardStyle(context, views, R.id.widget_2x2_card, isTransparent)
            applyPrimaryPillStyle(context, views, R.id.widget_2x2_add_btn, wallpaperTintEnabled, palette)
            applySecondaryPillStyle(context, views, R.id.widget_2x2_month, isTransparent, wallpaperTintEnabled, palette)
            applySecondaryPillStyle(context, views, R.id.widget_2x2_today_chip, isTransparent, wallpaperTintEnabled, palette)
            applySecondaryPillStyle(context, views, R.id.widget_2x2_velocity_chip, isTransparent, wallpaperTintEnabled, palette)
            views.setTextColor(
                R.id.widget_2x2_spent,
                if (wallpaperTintEnabled) primaryTint
                else if (isTransparent) Color.parseColor("#F87171")
                else ContextCompat.getColor(context, R.color.widget_amount_expense)
            )
            views.setTextColor(
                R.id.widget_2x2_balance,
                if (wallpaperTintEnabled) primaryTint
                else if (isTransparent) ContextCompat.getColor(context, R.color.widget_transparent_text_primary)
                else ContextCompat.getColor(context, R.color.widget_text_primary)
            )
            views.setViewVisibility(R.id.widget_2x2_atomic_label, labelVisibility)
            views.setTextViewText(R.id.widget_2x2_month, currentMonth.uppercase(Locale.US))
            setScaledNumberText(views, R.id.widget_2x2_spent, "₹" + numberFormat.format(monthExpense), baseSp = 16f, minSp = 10.5f, thresholdLen = 8)
            setScaledNumberText(views, R.id.widget_2x2_balance, "₹" + numberFormat.format(netBalance), baseSp = 14f, minSp = 10f, thresholdLen = 8)
            setScaledNumberText(views, R.id.widget_2x2_today_chip, "Today: ₹" + numberFormat.format(todayExpense), baseSp = 10.5f, minSp = 8.5f, thresholdLen = 10)
            setScaledNumberText(views, R.id.widget_2x2_velocity_chip, "₹" + numberFormat.format(dailyVelocity) + "/day", baseSp = 10.5f, minSp = 8.5f, thresholdLen = 9)

            if (pieChartBitmap != null) {
                views.setImageViewBitmap(R.id.widget_2x2_pie_chart, pieChartBitmap)
            }

            // Open app
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val mainPending = PendingIntent.getActivity(
                context, 200, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_2x2_root, mainPending)

            // Quick Add
            val addIntent = Intent(context, MainActivity::class.java).apply {
                action = "com.example.ACTION_ADD_EXPENSE"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val addPending = PendingIntent.getActivity(
                context, 201, addIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_2x2_add_btn, addPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun createPieChartBitmap(
        categoryExpenses: Map<String, Double>,
        totalExpense: Double,
        isDark: Boolean = false,
        dynamicTrackColor: Int? = null,
        dynamicCenterTextColor: Int? = null
    ): Bitmap? {
        val size = 220
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val palette = listOf(
            Color.parseColor("#10B981"), // Emerald Green
            Color.parseColor("#3B82F6"), // Sapphire Blue
            Color.parseColor("#F97316"), // Sunset Orange
            Color.parseColor("#8B5CF6"), // Amethyst Purple
            Color.parseColor("#EC4899"), // Rose Pink
            Color.parseColor("#EAB308")  // Amber Yellow
        )

        val stroke = 28f
        val arcRect = RectF(stroke / 2f + 4f, stroke / 2f + 4f, size - stroke / 2f - 4f, size - stroke / 2f - 4f)

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
        }

        val resolvedTrackColor = dynamicTrackColor ?: if (isDark) Color.parseColor("#36373C") else Color.parseColor("#E2E8F0")

        if (categoryExpenses.isEmpty() || totalExpense <= 0) {
            paint.color = resolvedTrackColor
            canvas.drawArc(arcRect, 0f, 360f, false, paint)

            val textPaint = Paint().apply {
                color = dynamicCenterTextColor ?: if (isDark) Color.parseColor("#94A3B8") else Color.parseColor("#64748B")
                textSize = 22f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("₹0", size / 2f, size / 2f + 8f, textPaint)
            return bitmap
        }

        // Draw subtle background track ring
        val trackPaint = Paint(paint).apply {
            color = dynamicTrackColor ?: if (isDark) Color.parseColor("#36373C") else Color.parseColor("#EEF2F6")
        }
        canvas.drawArc(arcRect, 0f, 360f, false, trackPaint)

        var startAngle = -90f
        var colorIndex = 0

        val sorted = categoryExpenses.toList().sortedByDescending { it.second }.take(6)
        for ((_, amt) in sorted) {
            val sweep = ((amt / totalExpense) * 360f).toFloat().coerceAtLeast(6f)
            paint.color = palette[colorIndex % palette.size]
            canvas.drawArc(arcRect, startAngle, sweep - 2f, false, paint)
            startAngle += sweep
            colorIndex++
        }

        // Center total text
        val centerPaint = Paint().apply {
            color = dynamicCenterTextColor ?: if (isDark) Color.parseColor("#F8FAFC") else Color.parseColor("#0F172A")
            textSize = 24f
            isAntiAlias = true
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val label = if (totalExpense >= 1000) "${(totalExpense / 1000).toInt()}k" else "${totalExpense.toInt()}"
        canvas.drawText("₹$label", size / 2f, size / 2f + 8f, centerPaint)

        return bitmap
    }


    suspend fun updateMinimalWidget(context: Context, appWidgetManager: AppWidgetManager, targetIds: IntArray? = null, isTransparent: Boolean = isWidgetTransparentBg(context)) = withContext(Dispatchers.IO) {
        val componentName = ComponentName(context, MinimalWidgetProvider::class.java)
        val appWidgetIds = if (targetIds != null && targetIds.isNotEmpty()) targetIds else appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds == null || appWidgetIds.isEmpty()) return@withContext

        val layoutId = if (isTransparent) R.layout.widget_transparent_minimal_2x1 else R.layout.widget_minimal_2x1

        val db = AppDatabase.getDatabase(context)
        val allTx = db.transactionDao().getAllTransactionsSync()
        var totalIncome = 0.0
        var totalExpense = 0.0
        for (tx in allTx) {
            if (tx.type.equals("Income", ignoreCase = true)) {
                totalIncome += tx.amount
            } else {
                totalExpense += tx.amount
            }
        }
        val netBalance = totalIncome - totalExpense

        // Today's spend
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = todayCal.timeInMillis
        val endOfToday = System.currentTimeMillis()
        val todayTx = db.transactionDao().getTransactionsInRangeSync(startOfToday, endOfToday)
        val todayExpense = todayTx.filter { !it.type.equals("Income", ignoreCase = true) }.sumOf { it.amount }

        val numberFormat = DecimalFormat("#,##,##0")
        val showWidgetNames = context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE)
            .getBoolean("show_widget_names", true)
        val labelVisibility = if (showWidgetNames) View.VISIBLE else View.INVISIBLE

        val palette = WidgetWallpaperTintManager.getDynamicPalette(context)
        val primaryTint = primaryTintTextColor(isTransparent, palette)
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutId)
            val wallpaperTintEnabled = applyAtomicCardStyle(context, views, R.id.widget_minimal_card, isTransparent, isSingleRow = true)
            applyPrimaryPillStyle(context, views, R.id.widget_minimal_add_btn, wallpaperTintEnabled, palette)
            views.setViewVisibility(R.id.widget_minimal_atomic_label, labelVisibility)

            setScaledNumberText(views, R.id.widget_minimal_balance, "₹${numberFormat.format(netBalance)}", baseSp = 15f, minSp = 10.5f, thresholdLen = 8)
            setScaledNumberText(views, R.id.widget_minimal_today, "₹${numberFormat.format(todayExpense)}", baseSp = 15f, minSp = 10.5f, thresholdLen = 8)

            if (wallpaperTintEnabled) {
                views.setTextColor(R.id.widget_minimal_balance, primaryTint)
                views.setTextColor(R.id.widget_minimal_today, primaryTint)
            } else {
                if (netBalance >= 0) {
                    views.setTextColor(R.id.widget_minimal_balance, Color.parseColor("#10B981"))
                } else {
                    views.setTextColor(R.id.widget_minimal_balance, Color.parseColor("#EF4444"))
                }
                views.setTextColor(
                    R.id.widget_minimal_today,
                    if (isTransparent) Color.parseColor("#F87171")
                    else ContextCompat.getColor(context, R.color.widget_amount_expense)
                )
            }

            // Tap root to open app
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val mainPending = PendingIntent.getActivity(
                context, if (isTransparent) 511 else 501, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_minimal_root, mainPending)

            // Tap Add button to open Add Expense
            val addIntent = Intent(context, MainActivity::class.java).apply {
                action = "com.example.ACTION_ADD_EXPENSE"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val addPending = PendingIntent.getActivity(
                context, if (isTransparent) 512 else 502, addIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_minimal_add_btn, addPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    suspend fun updateBudgetWidget(context: Context, appWidgetManager: AppWidgetManager, targetIds: IntArray? = null, isTransparent: Boolean = isWidgetTransparentBg(context)) = withContext(Dispatchers.IO) {
        val componentName = ComponentName(context, BudgetWidgetProvider::class.java)
        val appWidgetIds = if (targetIds != null && targetIds.isNotEmpty()) targetIds else appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds == null || appWidgetIds.isEmpty()) return@withContext

        val layoutId = if (isTransparent) R.layout.widget_transparent_budget_3x2 else R.layout.widget_budget_3x2

        val db = AppDatabase.getDatabase(context)
        val cal = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val monthTitle = monthFormat.format(cal.time)

        val startCal = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val monthTransactions = db.transactionDao().getTransactionsInRangeSync(startCal.timeInMillis, endCal.timeInMillis)
        val monthlySpent = monthTransactions.filter { !it.type.equals("Income", ignoreCase = true) }.sumOf { it.amount }

        val budgets = db.budgetDao().getAllBudgetsSync()
        val totalBudget = if (budgets.isNotEmpty()) budgets.sumOf { it.amountLimit } else 30000.0
        val remaining = (totalBudget - monthlySpent).coerceAtLeast(0.0)
        val pctUsed = if (totalBudget > 0) ((monthlySpent / totalBudget) * 100).toInt() else 0

        val isDark = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val wallpaperTintEnabled = WidgetWallpaperTintManager.shouldTintWidgets(context)
        val palette = WidgetWallpaperTintManager.getDynamicPalette(context, isDark)
        val primaryTint = primaryTintTextColor(isTransparent, palette)
        val numberFormat = DecimalFormat("#,##,##0")
        val ringBitmap = drawBudgetRingBitmap(
            monthlySpent,
            totalBudget,
            isDark,
            dynamicAccentColor = if (wallpaperTintEnabled) primaryTint else null,
            dynamicTrackColor = if (wallpaperTintEnabled) palette.trackRing else null,
            dynamicCenterTextColor = if (wallpaperTintEnabled) primaryTint else null
        )
        val showWidgetNames = context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE)
            .getBoolean("show_widget_names", true)
        val labelVisibility = if (showWidgetNames) View.VISIBLE else View.INVISIBLE

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutId)
            applyAtomicCardStyle(context, views, R.id.widget_budget_card, isTransparent)
            applyPrimaryPillStyle(context, views, R.id.widget_budget_add_btn, wallpaperTintEnabled, palette)
            applySecondaryPillStyle(context, views, R.id.widget_budget_pct, isTransparent, wallpaperTintEnabled, palette, updateTextColor = false)
            views.setTextColor(
                R.id.widget_budget_title,
                if (wallpaperTintEnabled) primaryTint
                else ContextCompat.getColor(context, if (isTransparent) R.color.widget_transparent_text_primary else R.color.widget_text_secondary)
            )
            views.setTextColor(
                R.id.widget_budget_limit,
                if (wallpaperTintEnabled) primaryTint
                else ContextCompat.getColor(context, if (isTransparent) R.color.widget_transparent_text_primary else R.color.widget_text_primary)
            )
            views.setViewVisibility(R.id.widget_budget_atomic_label, labelVisibility)

            views.setTextViewText(R.id.widget_budget_title, monthTitle)
            setScaledNumberText(views, R.id.widget_budget_spent, "₹${numberFormat.format(monthlySpent)}", baseSp = 13f, minSp = 10f, thresholdLen = 8)
            setScaledNumberText(views, R.id.widget_budget_limit, "₹${numberFormat.format(totalBudget)}", baseSp = 13f, minSp = 10f, thresholdLen = 8)
            setScaledNumberText(views, R.id.widget_budget_remaining, "₹${numberFormat.format(remaining)}", baseSp = 13f, minSp = 10f, thresholdLen = 8)
            views.setTextViewText(R.id.widget_budget_pct, "$pctUsed% Used")

            val pctColor = when {
                pctUsed > 90 -> if (isDark) Color.parseColor("#F87171") else Color.parseColor("#DC2626")
                pctUsed > 75 -> if (isDark) Color.parseColor("#FBBF24") else Color.parseColor("#D97706")
                wallpaperTintEnabled -> primaryTint
                else -> if (isDark) Color.parseColor("#818CF8") else Color.parseColor("#4F46E5")
            }
            views.setTextColor(R.id.widget_budget_pct, pctColor)

            views.setImageViewBitmap(R.id.widget_budget_chart, ringBitmap)

            // Tap root to open app
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val mainPending = PendingIntent.getActivity(
                context, 601, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_budget_root, mainPending)

            // Tap Add button
            val addIntent = Intent(context, MainActivity::class.java).apply {
                action = "com.example.ACTION_ADD_EXPENSE"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val addPending = PendingIntent.getActivity(
                context, 602, addIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_budget_add_btn, addPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun drawBudgetRingBitmap(
        spent: Double,
        budget: Double,
        isDark: Boolean = false,
        dynamicAccentColor: Int? = null,
        dynamicTrackColor: Int? = null,
        dynamicCenterTextColor: Int? = null
    ): Bitmap {
        val size = 200
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val strokeWidth = 22f
        val padding = strokeWidth / 2f + 4f
        val rect = RectF(padding, padding, size - padding, size - padding)

        val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            color = dynamicTrackColor ?: if (isDark) Color.parseColor("#36373C") else Color.parseColor("#E2E8F0")
        }
        canvas.drawOval(rect, trackPaint)

        val pct = if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
        val sweepAngle = pct * 360f

        val progressColor = when {
            pct >= 0.90f -> if (isDark) Color.parseColor("#F87171") else Color.parseColor("#DC2626")
            pct >= 0.75f -> if (isDark) Color.parseColor("#FBBF24") else Color.parseColor("#D97706")
            dynamicAccentColor != null -> dynamicAccentColor
            else -> if (isDark) Color.parseColor("#818CF8") else Color.parseColor("#4F46E5")
        }

        val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            color = progressColor
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = dynamicCenterTextColor ?: if (isDark) Color.parseColor("#F8FAFC") else Color.parseColor("#0F172A")
            textSize = 34f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val pctText = "${(pct * 100).toInt()}%"
        val fontMetrics = textPaint.fontMetrics
        val y = size / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(pctText, size / 2f, y, textPaint)

        return bitmap
    }

    suspend fun updateHabitWidget(context: Context, appWidgetManager: AppWidgetManager, targetIds: IntArray? = null, isTransparent: Boolean = isWidgetTransparentBg(context)) = withContext(Dispatchers.IO) {
        val componentName = ComponentName(context, HabitWidgetProvider::class.java)
        val appWidgetIds = if (targetIds != null && targetIds.isNotEmpty()) targetIds else appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds == null || appWidgetIds.isEmpty()) return@withContext

        val layoutId = if (isTransparent) R.layout.widget_transparent_habit_2x2 else R.layout.widget_habit_2x2

        val db = AppDatabase.getDatabase(context)
        val habits = try {
            db.habitDao().getAllHabitsSync()
        } catch (e: Exception) {
            emptyList()
        }

        val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = todayFormat.format(Date())
        val logs = try {
            db.habitDao().getLogsForDateSync(todayStr)
        } catch (e: Exception) {
            emptyList()
        }

        val completedHabitIds = logs.filter { it.isCompleted }.map { it.habitId }.toSet()
        val totalHabits = habits.size
        val completedCount = habits.count { completedHabitIds.contains(it.id) }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.ACTION_OPEN_HABITS"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPending = PendingIntent.getActivity(
            context, if (isTransparent) 801 else 800, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showWidgetNames = context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE)
            .getBoolean("show_widget_names", true)
        val labelVisibility = if (showWidgetNames) View.VISIBLE else View.INVISIBLE

        val palette = WidgetWallpaperTintManager.getDynamicPalette(context)
        val primaryTint = primaryTintTextColor(isTransparent, palette)
        val staticPrimaryText = ContextCompat.getColor(
            context,
            if (isTransparent) R.color.widget_transparent_text_primary else R.color.widget_text_primary
        )
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutId)
            val wallpaperTintEnabled = applyAtomicCardStyle(context, views, R.id.widget_habit_card, isTransparent)
            applyPrimaryPillStyle(context, views, R.id.widget_habit_open_btn, wallpaperTintEnabled, palette)
            applySecondaryPillStyle(context, views, R.id.widget_habit_streak, isTransparent, wallpaperTintEnabled, palette)
            if (isTransparent) {
                applySecondaryPillStyle(context, views, R.id.widget_habit_status_pill, isTransparent = true, wallpaperTintEnabled = wallpaperTintEnabled, palette = palette, updateTextColor = false)
            }
            views.setViewVisibility(R.id.widget_habit_atomic_label, labelVisibility)
            views.setTextViewText(R.id.widget_habit_status_pill, "$completedCount/$totalHabits Done")

            val itemIds = listOf(R.id.widget_habit_item_1, R.id.widget_habit_item_2, R.id.widget_habit_item_3)
            for (i in 0 until 3) {
                views.setTextColor(itemIds[i], if (wallpaperTintEnabled) primaryTint else staticPrimaryText)
                if (i < habits.size) {
                    val habit = habits[i]
                    val isDone = completedHabitIds.contains(habit.id)
                    val prefix = if (isDone) "✓ " else "○ "
                    views.setTextViewText(itemIds[i], prefix + habit.title)
                    views.setViewVisibility(itemIds[i], View.VISIBLE)
                } else {
                    if (i == 0 && habits.isEmpty()) {
                        views.setTextViewText(itemIds[i], "Tap to set habits in app")
                        views.setViewVisibility(itemIds[i], View.VISIBLE)
                    } else {
                        views.setViewVisibility(itemIds[i], View.GONE)
                    }
                }
            }

            views.setTextViewText(R.id.widget_habit_streak, if (completedCount > 0) "🔥 $completedCount Completed" else "⏳ Start Today")
            views.setOnClickPendingIntent(R.id.widget_habit_root, mainPending)
            views.setOnClickPendingIntent(R.id.widget_habit_open_btn, mainPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    suspend fun updateVelocityWidget(context: Context, appWidgetManager: AppWidgetManager, targetIds: IntArray? = null, isTransparent: Boolean = isWidgetTransparentBg(context)) = withContext(Dispatchers.IO) {
        val componentName = ComponentName(context, VelocityBurnRateWidgetProvider::class.java)
        val appWidgetIds = if (targetIds != null && targetIds.isNotEmpty()) targetIds else appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds == null || appWidgetIds.isEmpty()) return@withContext

        val layoutId = if (isTransparent) R.layout.widget_transparent_velocity_2x2 else R.layout.widget_velocity_2x2

        val db = AppDatabase.getDatabase(context)
        val cal = Calendar.getInstance()
        val numberFormat = DecimalFormat("#,##,##0")

        val startCal = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val txs = try {
            db.transactionDao().getTransactionsInRangeSync(startCal.timeInMillis, endCal.timeInMillis)
        } catch (e: Exception) {
            emptyList()
        }

        val expenseTxs = txs.filter { it.type.equals("Expense", ignoreCase = true) }
        val monthExpense = expenseTxs.sumOf { it.amount }
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysRemaining = (daysInMonth - dayOfMonth).coerceAtLeast(1)

        val dailyVelocity = monthExpense / dayOfMonth
        val projectedMonthEnd = dailyVelocity * daysInMonth

        val budgets = try { db.budgetDao().getAllBudgetsSync() } catch (e: Exception) { emptyList() }
        val totalBudget = budgets.sumOf { it.amountLimit }
        val safeDailyAllowance = if (totalBudget > 0) {
            (totalBudget - monthExpense).coerceAtLeast(0.0) / daysRemaining
        } else {
            (monthExpense * 1.15) / daysInMonth
        }

        val burnStatusText = if (totalBudget > 0) {
            if (projectedMonthEnd <= totalBudget * 0.9) "Safe Pace"
            else if (projectedMonthEnd <= totalBudget * 1.05) "Normal Pace"
            else "High Burn ⚠"
        } else {
            if (dailyVelocity < 1500) "Normal Pace" else "High Burn ⚠"
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.ACTION_OPEN_INSIGHTS"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPending = PendingIntent.getActivity(
            context, if (isTransparent) 811 else 810, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showWidgetNames = context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE)
            .getBoolean("show_widget_names", true)
        val labelVisibility = if (showWidgetNames) View.VISIBLE else View.INVISIBLE

        val palette = WidgetWallpaperTintManager.getDynamicPalette(context)
        val primaryTint = primaryTintTextColor(isTransparent, palette)
        val staticPrimaryText = ContextCompat.getColor(
            context,
            if (isTransparent) R.color.widget_transparent_text_primary else R.color.widget_text_primary
        )
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutId)
            val wallpaperTintEnabled = applyAtomicCardStyle(context, views, R.id.widget_velocity_card, isTransparent)
            applyPrimaryPillStyle(context, views, R.id.widget_velocity_action_btn, wallpaperTintEnabled, palette)
            applySecondaryPillStyle(context, views, R.id.widget_velocity_days_left, isTransparent, wallpaperTintEnabled, palette)
            if (isTransparent) {
                applySecondaryPillStyle(context, views, R.id.widget_velocity_burn_status, isTransparent = true, wallpaperTintEnabled = wallpaperTintEnabled, palette = palette, updateTextColor = false)
            }
            views.setTextColor(
                R.id.widget_velocity_rate,
                if (wallpaperTintEnabled) primaryTint else staticPrimaryText
            )
            views.setTextColor(
                R.id.widget_velocity_projected,
                if (wallpaperTintEnabled) primaryTint else ContextCompat.getColor(context, R.color.widget_amount_neutral)
            )
            views.setViewVisibility(R.id.widget_velocity_atomic_label, labelVisibility)
            views.setTextViewText(R.id.widget_velocity_burn_status, burnStatusText)
            setScaledNumberText(views, R.id.widget_velocity_rate, "₹" + numberFormat.format(dailyVelocity) + " / day", baseSp = 20f, minSp = 13f, thresholdLen = 11)
            setScaledNumberText(views, R.id.widget_velocity_projected, "₹" + numberFormat.format(projectedMonthEnd), baseSp = 12f, minSp = 9f, thresholdLen = 9)
            setScaledNumberText(views, R.id.widget_velocity_safe_allowance, "₹" + numberFormat.format(safeDailyAllowance) + "/day", baseSp = 12f, minSp = 9f, thresholdLen = 10)
            views.setTextViewText(R.id.widget_velocity_days_left, "$daysRemaining Days Left")

            views.setOnClickPendingIntent(R.id.widget_velocity_root, mainPending)
            views.setOnClickPendingIntent(R.id.widget_velocity_action_btn, mainPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    suspend fun updateSpendingHabitsWidget(context: Context, appWidgetManager: AppWidgetManager, targetIds: IntArray? = null, isTransparent: Boolean = isWidgetTransparentBg(context)) = withContext(Dispatchers.IO) {
        val componentName = ComponentName(context, SpendingHabitsWidgetProvider::class.java)
        val appWidgetIds = if (targetIds != null && targetIds.isNotEmpty()) targetIds else appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds == null || appWidgetIds.isEmpty()) return@withContext

        val layoutId = if (isTransparent) R.layout.widget_transparent_spending_habits_2x2 else R.layout.widget_spending_habits_2x2

        val db = AppDatabase.getDatabase(context)
        val cal = Calendar.getInstance()

        val startCal = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val endCal = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val txs = try {
            db.transactionDao().getTransactionsInRangeSync(startCal.timeInMillis, endCal.timeInMillis)
        } catch (e: Exception) {
            emptyList()
        }

        val expenseTxs = txs.filter { it.type.equals("Expense", ignoreCase = true) }
        val monthExpense = expenseTxs.sumOf { it.amount }
        val monthIncome = txs.filter { it.type.equals("Income", ignoreCase = true) }.sumOf { it.amount }

        val needsCategories = setOf(
            "Grocery Home", "Grocery", "Petrol", "Electricity Bill", "Home Expenses",
            "Broadband & TV", "Maintenance", "Flat Expenses", "Medical", "Bills",
            "Car / Bike Expenses", "Maternity", "Baby", "EMI", "Car EMI", "Loan"
        )

        var needsSum = 0.0
        var wantsSum = 0.0
        for (tx in expenseTxs) {
            if (needsCategories.any { tx.category.contains(it, ignoreCase = true) }) {
                needsSum += tx.amount
            } else {
                wantsSum += tx.amount
            }
        }

        val netSavings = (monthIncome - monthExpense).coerceAtLeast(0.0)
        val totalBasis = if (monthIncome > monthExpense) monthIncome else (monthExpense * 1.15).coerceAtLeast(1.0)

        val needsPct = ((needsSum / totalBasis) * 100).toInt().coerceIn(0, 100)
        val wantsPct = ((wantsSum / totalBasis) * 100).toInt().coerceIn(0, 100)
        val savingsPct = ((netSavings / totalBasis) * 100).toInt().coerceIn(0, 100)

        val topCategory = expenseTxs.groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .maxByOrNull { it.value }?.key ?: "None"

        val statusText = if (needsPct <= 55 && wantsPct <= 35) {
            "Healthy Balance ✓"
        } else if (wantsPct > 35) {
            "High Discretionary"
        } else {
            "Essentials Heavy"
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            action = "com.example.ACTION_OPEN_INSIGHTS"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPending = PendingIntent.getActivity(
            context, if (isTransparent) 821 else 820, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showWidgetNames = context.getSharedPreferences("money_manager_prefs", Context.MODE_PRIVATE)
            .getBoolean("show_widget_names", true)
        val labelVisibility = if (showWidgetNames) View.VISIBLE else View.INVISIBLE

        val palette = WidgetWallpaperTintManager.getDynamicPalette(context)
        val primaryTint = primaryTintTextColor(isTransparent, palette)
        val staticPrimaryText = ContextCompat.getColor(
            context,
            if (isTransparent) R.color.widget_transparent_text_primary else R.color.widget_text_primary
        )
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, layoutId)
            val wallpaperTintEnabled = applyAtomicCardStyle(context, views, R.id.widget_habits_card, isTransparent)
            applyPrimaryPillStyle(context, views, R.id.widget_habits_open_btn, wallpaperTintEnabled, palette)
            applySecondaryPillStyle(context, views, R.id.widget_habits_rule_badge, isTransparent, wallpaperTintEnabled, palette)
            if (isTransparent) {
                applySecondaryPillStyle(context, views, R.id.widget_habits_status_pill, isTransparent = true, wallpaperTintEnabled = wallpaperTintEnabled, palette = palette, updateTextColor = false)
            }
            views.setTextColor(
                R.id.widget_habits_top_category,
                if (wallpaperTintEnabled) primaryTint else staticPrimaryText
            )
            views.setTextColor(
                R.id.widget_habits_needs_val,
                if (wallpaperTintEnabled) primaryTint else ContextCompat.getColor(context, R.color.widget_amount_neutral)
            )
            views.setViewVisibility(R.id.widget_habits_atomic_label, labelVisibility)
            views.setTextViewText(R.id.widget_habits_needs_val, "$needsPct%")
            views.setTextViewText(R.id.widget_habits_wants_val, "$wantsPct%")
            views.setTextViewText(R.id.widget_habits_savings_val, "$savingsPct%")
            views.setTextViewText(R.id.widget_habits_top_category, "Top: $topCategory")
            views.setTextViewText(R.id.widget_habits_status_pill, statusText)

            views.setOnClickPendingIntent(R.id.widget_habits_root, mainPending)
            views.setOnClickPendingIntent(R.id.widget_habits_open_btn, mainPending)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    fun pinWidget(context: Context, widgetClass: Class<*>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val myProvider = ComponentName(context, widgetClass)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(myProvider, null, null)
                Toast.makeText(context, "Adding widget to home screen...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    context,
                    "Pinning unsupported on launcher. Long-press home screen to add widget manually.",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                "Long-press your home screen to add widget.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
