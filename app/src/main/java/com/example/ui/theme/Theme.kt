package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.widget.WidgetWallpaperTintManager

fun getThemeColorScheme(themeName: String, darkTheme: Boolean): ColorScheme {
    val normTheme = themeName.lowercase()
    val primaryColor = when (normTheme) {
        "ocean" -> OceanThemePrimary
        "amethyst" -> AmethystThemePrimary
        "sunset" -> SunsetThemePrimary
        "rose" -> RoseThemePrimary
        else -> EmeraldThemePrimary
    }
    val primaryDark = when (normTheme) {
        "ocean" -> OceanThemeDark
        "amethyst" -> AmethystThemeDark
        "sunset" -> SunsetThemeDark
        "rose" -> RoseThemeDark
        else -> EmeraldThemeDark
    }
    val accentColor = when (normTheme) {
        "ocean" -> OceanThemeAccent
        "amethyst" -> AmethystThemeAccent
        "sunset" -> SunsetThemeAccent
        "rose" -> RoseThemeAccent
        else -> EmeraldThemeAccent
    }

    return if (darkTheme) {
        val darkSurfaceBase = Color(0xFF111827)
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.28f).compositeOver(darkSurfaceBase),
            onPrimaryContainer = primaryColor.copy(alpha = 0.35f).compositeOver(Color.White),
            secondary = accentColor,
            onSecondary = Color.Black,
            secondaryContainer = primaryColor.copy(alpha = 0.18f).compositeOver(darkSurfaceBase),
            onSecondaryContainer = accentColor,
            tertiary = IncomeGreen,
            tertiaryContainer = Color(0xFF064E3B),
            onTertiaryContainer = Color(0xFFA7F3D0),
            error = ExpenseRed,
            errorContainer = Color(0xFF451A20),
            onErrorContainer = Color(0xFFFCA5A5),
            background = Color(0xFF0B0F17),
            onBackground = Color(0xFFF8FAFC),
            surface = darkSurfaceBase,
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF1E293B),
            onSurfaceVariant = Color(0xFF94A3B8),
            surfaceContainerLowest = Color(0xFF090D14),
            surfaceContainerLow = Color(0xFF151D2C),
            surfaceContainer = Color(0xFF1E293B),
            surfaceContainerHigh = Color(0xFF28354A),
            surfaceContainerHighest = Color(0xFF334155),
            surfaceBright = Color(0xFF3B4B64),
            surfaceDim = Color(0xFF0E131E),
            outline = Color(0xFF334155),
            outlineVariant = Color(0xFF1E293B)
        )
    } else {
        val lightSurfaceBase = Color(0xFFFFFFFF)
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.14f).compositeOver(lightSurfaceBase),
            onPrimaryContainer = primaryDark,
            secondary = accentColor,
            onSecondary = Color.White,
            secondaryContainer = primaryColor.copy(alpha = 0.09f).compositeOver(lightSurfaceBase),
            onSecondaryContainer = primaryDark,
            tertiary = IncomeGreen,
            tertiaryContainer = Color(0xFFDCFCE7),
            onTertiaryContainer = Color(0xFF166534),
            error = ExpenseRed,
            errorContainer = Color(0xFFFEE2E2),
            onErrorContainer = Color(0xFF991B1B),
            background = Color(0xFFF8FAFC),
            onBackground = Color(0xFF0F172A),
            surface = Color(0xFFF8FAFC),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = Color(0xFF475569),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFF8FAFC),
            surfaceContainer = Color(0xFFF1F5F9),
            surfaceContainerHigh = Color(0xFFE2E8F0),
            surfaceContainerHighest = Color(0xFFCBD5E1),
            surfaceBright = Color(0xFFFFFFFF),
            surfaceDim = Color(0xFFE2E8F0),
            outline = Color(0xFFCBD5E1),
            outlineVariant = Color(0xFFE2E8F0)
        )
    }
}

@Composable
fun MoneyManagerTheme(
    appTheme: String = "Emerald",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    wallpaperPaletteTick: Int = 0,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = remember(appTheme, darkTheme, dynamicColor, wallpaperPaletteTick) {
        if (dynamicColor) {
            WidgetWallpaperTintManager.buildMaterial3AppColorScheme(
                context = context,
                darkTheme = darkTheme
            ) ?: getThemeColorScheme(appTheme, darkTheme)
        } else {
            getThemeColorScheme(appTheme, darkTheme)
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    window.isStatusBarContrastEnforced = false
                    window.isNavigationBarContrastEnforced = false
                }
                WindowCompat.setDecorFitsSystemWindows(window, false)
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = ExpressiveShapes,
        content = content
    )
}
