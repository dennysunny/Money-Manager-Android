package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle

class QuickAddWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        WidgetUpdater.updateQuickAddWidget(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        WidgetUpdater.updateQuickAddWidget(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdater.updateAllWidgets(context)
    }
}
