package com.example.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Listens for system wallpaper changes (`ACTION_WALLPAPER_CHANGED`) and refreshes all active
 * home screen widgets whenever Dynamic Wallpaper Tinting is enabled.
 */
class WallpaperChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        @Suppress("DEPRECATION")
        if (action == Intent.ACTION_WALLPAPER_CHANGED ||
            action == Intent.ACTION_CONFIGURATION_CHANGED
        ) {
            if (WidgetWallpaperTintManager.shouldTintWidgets(context)) {
                WidgetWallpaperTintManager.refreshWidgetsIfWallpaperChanged(
                    context = context.applicationContext,
                    force = action == Intent.ACTION_WALLPAPER_CHANGED
                )
            }
        }
    }
}
