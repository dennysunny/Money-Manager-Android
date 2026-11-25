package com.example

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import com.example.widget.WallpaperChangeReceiver
import com.example.widget.WidgetWallpaperTintManager

class MoneyManagerApplication : Application() {

    private var unregisterWallpaperListener: (() -> Unit)? = null
    private val wallpaperChangeReceiver = WallpaperChangeReceiver()

    override fun onCreate() {
        super.onCreate()

        // Live listener for WallpaperColors updates (API 27+)
        unregisterWallpaperListener = WidgetWallpaperTintManager.registerWallpaperListener(this) {
            if (WidgetWallpaperTintManager.shouldTintWidgets(this)) {
                WidgetWallpaperTintManager.refreshWidgetsIfWallpaperChanged(this, force = true)
            }
        }

        // Dynamic registration for wallpaper change broadcasts while process is alive
        try {
            @Suppress("DEPRECATION")
            val filter = IntentFilter(Intent.ACTION_WALLPAPER_CHANGED)
            ContextCompat.registerReceiver(
                this,
                wallpaperChangeReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
        } catch (_: Exception) {
            // Safe fallback on restricted test environments
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Android 12+ Material You wallpaper updates also trigger a configuration/overlay update
        WidgetWallpaperTintManager.refreshWidgetsIfWallpaperChanged(this)
    }
}
