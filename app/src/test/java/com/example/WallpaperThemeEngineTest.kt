package com.example

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.example.widget.WidgetWallpaperTintManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WallpaperThemeEngineTest {

    @Test
    fun lightModeWallpaperSchemeStaysLightEvenWithDarkWallpaperSeed() {
        val darkBlueSeed = WidgetWallpaperTintManager.WallpaperSeed(
            primaryArgb = AndroidColor.parseColor("#0D1B2A"),
            secondaryArgb = AndroidColor.parseColor("#1B263B"),
            tertiaryArgb = AndroidColor.parseColor("#415A77"),
            swatchColors = listOf(AndroidColor.parseColor("#0D1B2A")),
            isFromWallpaper = true
        )

        val lightScheme = WidgetWallpaperTintManager.buildMaterial3ColorSchemeFromSeed(
            seed = darkBlueSeed,
            darkTheme = false
        )

        assertTrue("Light mode background must remain bright", lightScheme.background.luminance() > 0.85f)
        assertTrue("Light mode surface must remain bright", lightScheme.surface.luminance() > 0.85f)
        assertEquals(Color.White, lightScheme.surfaceContainerLowest)
        assertTrue("Light mode text must remain dark", lightScheme.onSurface.luminance() < 0.15f)
        assertTrue(
            "Primary button must have >= 4.5 contrast against white",
            ColorUtils.calculateContrast(AndroidColor.WHITE, lightScheme.primary.toArgb()) >= 4.5
        )
    }

    @Test
    fun distinctWallpapersProduceDistinctMaterial3AndWidgetColors() {
        val sunsetSeed = WidgetWallpaperTintManager.WallpaperSeed(
            primaryArgb = AndroidColor.parseColor("#EA580C"),
            secondaryArgb = AndroidColor.parseColor("#F59E0B"),
            tertiaryArgb = AndroidColor.parseColor("#DC2626"),
            swatchColors = listOf(AndroidColor.parseColor("#EA580C")),
            isFromWallpaper = true
        )
        val emeraldSeed = WidgetWallpaperTintManager.WallpaperSeed(
            primaryArgb = AndroidColor.parseColor("#059669"),
            secondaryArgb = AndroidColor.parseColor("#10B981"),
            tertiaryArgb = AndroidColor.parseColor("#0D9488"),
            swatchColors = listOf(AndroidColor.parseColor("#059669")),
            isFromWallpaper = true
        )

        val sunsetScheme = WidgetWallpaperTintManager.buildMaterial3ColorSchemeFromSeed(sunsetSeed, darkTheme = false)
        val emeraldScheme = WidgetWallpaperTintManager.buildMaterial3ColorSchemeFromSeed(emeraldSeed, darkTheme = false)

        assertNotEquals(sunsetScheme.primary, emeraldScheme.primary)
        assertNotEquals(sunsetScheme.primaryContainer, emeraldScheme.primaryContainer)

        val sunsetWidget = WidgetWallpaperTintManager.buildWidgetPaletteFromSeed(
            seed = sunsetSeed,
            solidCardBg = AndroidColor.WHITE,
            glassCardBg = AndroidColor.parseColor("#22000000"),
            isDark = false
        )
        val emeraldWidget = WidgetWallpaperTintManager.buildWidgetPaletteFromSeed(
            seed = emeraldSeed,
            solidCardBg = AndroidColor.WHITE,
            glassCardBg = AndroidColor.parseColor("#22000000"),
            isDark = false
        )

        // Solid & transparent backgrounds stay preserved
        assertEquals(AndroidColor.WHITE, sunsetWidget.cardBg)
        assertEquals(AndroidColor.parseColor("#22000000"), sunsetWidget.transparentCardBg)

        // Buttons & primary text visibly reflect distinct wallpaper colors
        assertNotEquals(sunsetWidget.primaryPillBg, emeraldWidget.primaryPillBg)
        assertNotEquals(sunsetWidget.accentText, emeraldWidget.accentText)
        assertNotEquals(sunsetWidget.primaryTextOnTransparent, emeraldWidget.primaryTextOnTransparent)
        assertTrue(
            "Widget accent text must be legible on solid white background",
            ColorUtils.calculateContrast(sunsetWidget.accentText, AndroidColor.WHITE) >= 4.5
        )
    }

    @Test
    fun darkModeWallpaperSchemeStaysDarkWithHighContrastPrimary() {
        val roseSeed = WidgetWallpaperTintManager.WallpaperSeed(
            primaryArgb = AndroidColor.parseColor("#E11D48"),
            secondaryArgb = AndroidColor.parseColor("#FB7185"),
            tertiaryArgb = AndroidColor.parseColor("#F43F5E"),
            swatchColors = listOf(AndroidColor.parseColor("#E11D48")),
            isFromWallpaper = true
        )

        val darkScheme = WidgetWallpaperTintManager.buildMaterial3ColorSchemeFromSeed(roseSeed, darkTheme = true)

        assertTrue("Dark mode background must be dark", darkScheme.background.luminance() < 0.08f)
        assertTrue("Dark mode text must be bright", darkScheme.onSurface.luminance() > 0.75f)
        assertTrue(
            "Dark primary must have strong contrast against dark surface",
            ColorUtils.calculateContrast(darkScheme.primary.toArgb(), darkScheme.surfaceContainerLow.toArgb()) >= 4.5
        )
    }
}
