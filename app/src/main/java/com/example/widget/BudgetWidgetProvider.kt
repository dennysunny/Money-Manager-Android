package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BudgetWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val initialLayout = if (WidgetUpdater.isWidgetTransparentBg(context)) {
            com.example.R.layout.widget_transparent_budget_3x2
        } else {
            com.example.R.layout.widget_budget_3x2
        }
        for (id in appWidgetIds) {
            try {
                val views = android.widget.RemoteViews(context.packageName, initialLayout)
                appWidgetManager.updateAppWidget(id, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                WidgetUpdater.updateBudgetWidget(context, appWidgetManager, appWidgetIds)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                WidgetUpdater.updateBudgetWidget(context, appWidgetManager, intArrayOf(appWidgetId))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdater.updateAllWidgets(context)
    }
}
