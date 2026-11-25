package com.example.widget

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.example.R
import com.example.ui.theme.ExpenseRed
import kotlin.math.abs

/**
 * Resolves live wallpaper-adaptive Material 3 color palettes for both the App UI and Home Screen Widgets.
 *
 * Uses zero storage permissions:
 * - Primary source (API 27–36, including Vivo OriginOS / OEM skins where `system_accent1` may not
 *   sync with the wallpaper): extracts live dominant colors directly from
 *   `WallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)`.
 * - Secondary fallback (API 31+ if `WallpaperColors` is unavailable, e.g. custom live wallpaper):
 *   reads system Material You `android.R.color.system_accent1_500`.
 * - Synthesizes accurate Material 3 tonal palettes (Light & Dark) with WCAG contrast guarantees
 *   for both the App UI and Home Screen Widgets (keeping solid widgets solid and transparent widgets transparent).
 */
object WidgetWallpaperTintManager {

    const val PREFS_NAME = "money_manager_prefs"
    const val PREF_WIDGET_WALLPAPER_TINT = "widget_wallpaper_tint_enabled"
    const val PREF_WALLPAPER_TINT_APP = "wallpaper_tint_apply_app"
    const val PREF_WALLPAPER_TINT_WIDGETS = "wallpaper_tint_apply_widgets"
    private const val PREF_LAST_WALLPAPER_SIG = "widget_last_wallpaper_sig"

    data class WallpaperSeed(
        val primaryArgb: Int,
        val secondaryArgb: Int,
        val tertiaryArgb: Int,
        val swatchColors: List<Int>,
        val isFromWallpaper: Boolean
    )

    data class WidgetDynamicPalette(
        val cardBg: Int,
        val cardBorder: Int,
        val cardBorderBold: Int,
        val glowInner: Int,
        val glowHalo: Int,
        val transparentCardBg: Int,
        val transparentBorder: Int,
        val primaryPillBg: Int,
        val primaryPillText: Int,
        val secondaryPillBg: Int,
        val secondaryPillText: Int,
        val transparentPillBg: Int,
        val accentText: Int,
        val primaryTextOnTransparent: Int,
        val trackRing: Int,
        val swatchColors: List<Int>,
        val isFromWallpaper: Boolean
    )

    /** Master toggle for Dynamic Wallpaper Tinting. */
    fun isWallpaperTintEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(PREF_WIDGET_WALLPAPER_TINT, true)
    }

    fun setWallpaperTintEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_WIDGET_WALLPAPER_TINT, enabled)
            .apply()
    }

    /** Individual toggle: Apply dynamic wallpaper tinting to the app UI. */
    fun isApplyToAppEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(PREF_WALLPAPER_TINT_APP, true)
    }

    fun setApplyToAppEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_WALLPAPER_TINT_APP, enabled)
            .apply()
    }

    /** Individual toggle: Apply dynamic wallpaper tinting to home screen widgets. */
    fun isApplyToWidgetsEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(PREF_WALLPAPER_TINT_WIDGETS, true)
    }

    fun setApplyToWidgetsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREF_WALLPAPER_TINT_WIDGETS, enabled)
            .apply()
    }

    /** Effective state: whether the App UI should currently be wallpaper-tinted. */
    fun shouldTintApp(context: Context): Boolean =
        isWallpaperTintEnabled(context) && isApplyToAppEnabled(context)

    /** Effective state: whether Home Screen Widgets should currently be wallpaper-tinted. */
    fun shouldTintWidgets(context: Context): Boolean =
        isWallpaperTintEnabled(context) && isApplyToWidgetsEnabled(context)

    fun isNightMode(context: Context): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Extracts up to 3 dominant wallpaper colors using the zero-permission `WallpaperColors` API.
     * Checks home screen wallpaper (`FLAG_SYSTEM`) first and falls back to lock screen (`FLAG_LOCK`)
     * if system wallpaper colors are unavailable.
     */
    fun extractWallpaperSwatches(context: Context): List<Int> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return emptyList()
        return try {
            val wm = WallpaperManager.getInstance(context)
            val colors: WallpaperColors? = wm.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                ?: wm.getWallpaperColors(WallpaperManager.FLAG_LOCK)
            if (colors != null) {
                buildList {
                    add(colors.primaryColor.toArgb() or 0xFF000000.toInt())
                    colors.secondaryColor?.toArgb()?.let { add(it or 0xFF000000.toInt()) }
                    colors.tertiaryColor?.toArgb()?.let { add(it or 0xFF000000.toInt()) }
                }.distinct()
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** Computes perceptual chroma [0..1] for selecting the most expressive wallpaper accent swatch. */
    private fun colorChroma(argb: Int): Float {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(argb, hsl)
        val saturation = hsl[1]
        val lightness = hsl[2]
        return saturation * (1f - abs(2f * lightness - 1f))
    }

    /**
     * Resolves the best primary/secondary/tertiary seed colors from the live wallpaper.
     * Prefers `WallpaperManager.getWallpaperColors` directly so OEMs like Vivo OriginOS
     * (where `system_accent1` may not follow wallpaper changes) still reflect true wallpaper colors.
     */
    fun resolveWallpaperSeed(context: Context): WallpaperSeed? {
        val liveSwatches = extractWallpaperSwatches(context)
        if (liveSwatches.isNotEmpty()) {
            // Rank swatches by chroma so dark/white wallpaper backgrounds don't hide vivid accents
            val ranked = liveSwatches.sortedByDescending { colorChroma(it) }
            val primary = ranked[0]
            val secondary = ranked.getOrElse(1) { shiftHue(primary, 22f) }
            val tertiary = ranked.getOrElse(2) { shiftHue(primary, 60f) }
            return WallpaperSeed(
                primaryArgb = primary,
                secondaryArgb = secondary,
                tertiaryArgb = tertiary,
                swatchColors = ranked,
                isFromWallpaper = true
            )
        }

        // Secondary fallback on API 31+: read system Monet accent tokens if live WallpaperColors is null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return try {
                val sysAccent1 = ContextCompat.getColor(context, android.R.color.system_accent1_500)
                val sysAccent2 = ContextCompat.getColor(context, android.R.color.system_accent2_500)
                val sysAccent3 = ContextCompat.getColor(context, android.R.color.system_accent3_500)
                WallpaperSeed(
                    primaryArgb = sysAccent1,
                    secondaryArgb = sysAccent2,
                    tertiaryArgb = sysAccent3,
                    swatchColors = listOf(sysAccent1, sysAccent2, sysAccent3).distinct(),
                    isFromWallpaper = true
                )
            } catch (_: Exception) {
                null
            }
        }
        return null
    }

    private fun shiftHue(argb: Int, deltaDegrees: Float): Int {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(argb, hsl)
        hsl[0] = (hsl[0] + deltaDegrees + 360f) % 360f
        return ColorUtils.HSLToColor(hsl)
    }

    /**
     * Generates a Material 3 tonal color for a given hue [0..360], saturation [0..1], and tone [0..100].
     * Applies smooth chroma tapering at extreme highlights (tone > 90) and deep shadows (tone < 15)
     * matching Material 3 tonal palette behavior.
     */
    internal fun tonalArgb(hue: Float, saturation: Float, tone: Int): Int {
        val clampedTone = tone.coerceIn(0, 100)
        if (clampedTone == 0) return Color.BLACK
        if (clampedTone == 100) return Color.WHITE
        val lightness = clampedTone / 100f
        val chromaScale = when {
            clampedTone >= 95 -> ((100 - clampedTone) / 5f).coerceIn(0.20f, 1f)
            clampedTone <= 10 -> (clampedTone / 10f).coerceIn(0.25f, 1f)
            else -> 1f
        }
        val effectiveSat = (saturation * chromaScale).coerceIn(0f, 1f)
        return ColorUtils.HSLToColor(floatArrayOf((hue + 360f) % 360f, effectiveSat, lightness))
    }

    /**
     * Adjusts a tonal color's lightness slightly until it satisfies the target WCAG contrast ratio
     * against [backgroundArgb].
     */
    internal fun ensureContrast(
        hue: Float,
        saturation: Float,
        initialTone: Int,
        backgroundArgb: Int,
        minContrast: Double,
        preferLighter: Boolean
    ): Int {
        var tone = initialTone.coerceIn(0, 100)
        var candidate = tonalArgb(hue, saturation, tone)
        var steps = 0
        while (ColorUtils.calculateContrast(candidate, backgroundArgb) < minContrast && steps < 45) {
            tone = if (preferLighter) (tone + 2).coerceAtMost(98) else (tone - 2).coerceAtLeast(8)
            candidate = tonalArgb(hue, saturation, tone)
            steps++
        }
        return candidate
    }

    private data class SeedHslPalettes(
        val primaryHue: Float,
        val primarySat: Float,
        val secondaryHue: Float,
        val secondarySat: Float,
        val tertiaryHue: Float,
        val tertiarySat: Float,
        val neutralHue: Float,
        val neutralSat: Float,
        val neutralVariantSat: Float
    )

    private fun extractPalettesFromSeed(seed: WallpaperSeed): SeedHslPalettes {
        val pHsl = FloatArray(3)
        ColorUtils.colorToHSL(seed.primaryArgb, pHsl)
        val sHsl = FloatArray(3)
        ColorUtils.colorToHSL(seed.secondaryArgb, sHsl)
        val tHsl = FloatArray(3)
        ColorUtils.colorToHSL(seed.tertiaryArgb, tHsl)

        val isAchromatic = pHsl[1] < 0.08f
        val pHue = if (isAchromatic) 222f else pHsl[0]
        val pSat = if (isAchromatic) 0.22f else pHsl[1].coerceIn(0.45f, 0.85f)

        val sIsChromatic = sHsl[1] >= 0.14f
        val sHue = if (sIsChromatic) sHsl[0] else (pHue + 18f) % 360f
        val sSat = if (isAchromatic) 0.14f else (sHsl[1] * 0.55f).coerceIn(0.22f, 0.45f)

        val tIsChromatic = tHsl[1] >= 0.16f
        val tHue = if (tIsChromatic) tHsl[0] else (pHue + 55f) % 360f
        val tSat = if (isAchromatic) 0.20f else tHsl[1].coerceIn(0.35f, 0.70f)

        val nSat = if (isAchromatic) 0.04f else 0.14f
        val nvSat = if (isAchromatic) 0.06f else 0.24f

        return SeedHslPalettes(
            primaryHue = pHue,
            primarySat = pSat,
            secondaryHue = sHue,
            secondarySat = sSat,
            tertiaryHue = tHue,
            tertiarySat = tSat,
            neutralHue = pHue,
            neutralSat = nSat,
            neutralVariantSat = nvSat
        )
    }

    /**
     * Builds an accurate Material Design 3 [ColorScheme] directly from the live wallpaper seed.
     * Guarantees bright, crisp, subtly wallpaper-tinted surfaces in Light Mode (never dark!)
     * and rich obsidian wallpaper-tinted surfaces in Dark Mode, with WCAG-verified primary contrast.
     *
     * Returns `null` only if no wallpaper colors are available (caller falls back to static theme).
     */
    fun buildMaterial3AppColorScheme(context: Context, darkTheme: Boolean): ColorScheme? {
        val seed = resolveWallpaperSeed(context) ?: return null
        return buildMaterial3ColorSchemeFromSeed(seed, darkTheme)
    }

    fun buildMaterial3ColorSchemeFromSeed(seed: WallpaperSeed, darkTheme: Boolean): ColorScheme {
        val pal = extractPalettesFromSeed(seed)

        return if (darkTheme) {
            val bgArgb = tonalArgb(pal.neutralHue, pal.neutralSat, 6)
            val surfaceLowArgb = tonalArgb(pal.neutralHue, pal.neutralSat, 10)
            val surfaceArgb = tonalArgb(pal.neutralHue, pal.neutralSat, 12)
            val surfaceHighArgb = tonalArgb(pal.neutralHue, pal.neutralSat, 17)
            val surfaceHighestArgb = tonalArgb(pal.neutralHue, pal.neutralSat, 22)

            val primaryArgb = ensureContrast(
                hue = pal.primaryHue,
                saturation = pal.primarySat,
                initialTone = 78,
                backgroundArgb = surfaceLowArgb,
                minContrast = 5.0,
                preferLighter = true
            )
            val onPrimaryArgb = ensureContrast(
                hue = pal.primaryHue,
                saturation = pal.primarySat * 0.7f,
                initialTone = 16,
                backgroundArgb = primaryArgb,
                minContrast = 5.0,
                preferLighter = false
            )
            val primaryContainerArgb = tonalArgb(pal.primaryHue, pal.primarySat * 0.75f, 26)
            val onPrimaryContainerArgb = tonalArgb(pal.primaryHue, pal.primarySat * 0.85f, 92)

            val secondaryArgb = ensureContrast(
                hue = pal.secondaryHue,
                saturation = pal.secondarySat,
                initialTone = 78,
                backgroundArgb = surfaceLowArgb,
                minContrast = 4.5,
                preferLighter = true
            )
            val onSecondaryArgb = tonalArgb(pal.secondaryHue, pal.secondarySat, 16)
            val secondaryContainerArgb = tonalArgb(pal.secondaryHue, pal.secondarySat * 0.8f, 26)
            val onSecondaryContainerArgb = tonalArgb(pal.secondaryHue, pal.secondarySat, 90)

            val tertiaryArgb = tonalArgb(pal.tertiaryHue, pal.tertiarySat, 80)
            val onTertiaryArgb = tonalArgb(pal.tertiaryHue, pal.tertiarySat, 18)
            val tertiaryContainerArgb = tonalArgb(pal.tertiaryHue, pal.tertiarySat * 0.75f, 28)
            val onTertiaryContainerArgb = tonalArgb(pal.tertiaryHue, pal.tertiarySat, 92)

            darkColorScheme(
                primary = androidx.compose.ui.graphics.Color(primaryArgb),
                onPrimary = androidx.compose.ui.graphics.Color(onPrimaryArgb),
                primaryContainer = androidx.compose.ui.graphics.Color(primaryContainerArgb),
                onPrimaryContainer = androidx.compose.ui.graphics.Color(onPrimaryContainerArgb),
                inversePrimary = androidx.compose.ui.graphics.Color(tonalArgb(pal.primaryHue, pal.primarySat, 40)),
                secondary = androidx.compose.ui.graphics.Color(secondaryArgb),
                onSecondary = androidx.compose.ui.graphics.Color(onSecondaryArgb),
                secondaryContainer = androidx.compose.ui.graphics.Color(secondaryContainerArgb),
                onSecondaryContainer = androidx.compose.ui.graphics.Color(onSecondaryContainerArgb),
                tertiary = androidx.compose.ui.graphics.Color(tertiaryArgb),
                onTertiary = androidx.compose.ui.graphics.Color(onTertiaryArgb),
                tertiaryContainer = androidx.compose.ui.graphics.Color(tertiaryContainerArgb),
                onTertiaryContainer = androidx.compose.ui.graphics.Color(onTertiaryContainerArgb),
                error = ExpenseRed,
                onError = androidx.compose.ui.graphics.Color.White,
                errorContainer = androidx.compose.ui.graphics.Color(0xFF5C151D),
                onErrorContainer = androidx.compose.ui.graphics.Color(0xFFFFDAD6),
                background = androidx.compose.ui.graphics.Color(bgArgb),
                onBackground = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, 0.06f, 94)),
                surface = androidx.compose.ui.graphics.Color(bgArgb),
                onSurface = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, 0.06f, 94)),
                surfaceVariant = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralVariantSat, 22)),
                onSurfaceVariant = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralVariantSat * 0.7f, 78)),
                surfaceContainerLowest = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralSat, 4)),
                surfaceContainerLow = androidx.compose.ui.graphics.Color(surfaceLowArgb),
                surfaceContainer = androidx.compose.ui.graphics.Color(surfaceArgb),
                surfaceContainerHigh = androidx.compose.ui.graphics.Color(surfaceHighArgb),
                surfaceContainerHighest = androidx.compose.ui.graphics.Color(surfaceHighestArgb),
                surfaceBright = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralSat, 26)),
                surfaceDim = androidx.compose.ui.graphics.Color(bgArgb),
                surfaceTint = androidx.compose.ui.graphics.Color(primaryArgb),
                outline = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralVariantSat, 52)),
                outlineVariant = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralVariantSat, 28)),
                inverseSurface = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralSat, 90)),
                inverseOnSurface = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralSat, 18))
            )
        } else {
            val bgArgb = tonalArgb(pal.neutralHue, pal.neutralSat, 97)
            val surfaceLowestArgb = Color.WHITE
            val surfaceLowArgb = tonalArgb(pal.neutralHue, pal.neutralSat, 96)
            val surfaceArgb = tonalArgb(pal.neutralHue, pal.neutralSat, 94)
            val surfaceHighArgb = tonalArgb(pal.neutralHue, pal.neutralSat, 91)
            val surfaceHighestArgb = tonalArgb(pal.neutralHue, pal.neutralSat, 88)

            val primaryArgb = ensureContrast(
                hue = pal.primaryHue,
                saturation = pal.primarySat,
                initialTone = 42,
                backgroundArgb = Color.WHITE,
                minContrast = 4.6,
                preferLighter = false
            )
            val primaryContainerArgb = tonalArgb(pal.primaryHue, pal.primarySat * 0.72f, 90)
            val onPrimaryContainerArgb = ensureContrast(
                hue = pal.primaryHue,
                saturation = pal.primarySat,
                initialTone = 16,
                backgroundArgb = primaryContainerArgb,
                minContrast = 7.0,
                preferLighter = false
            )

            val secondaryArgb = ensureContrast(
                hue = pal.secondaryHue,
                saturation = pal.secondarySat,
                initialTone = 42,
                backgroundArgb = Color.WHITE,
                minContrast = 4.5,
                preferLighter = false
            )
            val secondaryContainerArgb = tonalArgb(pal.secondaryHue, pal.secondarySat * 0.85f, 90)
            val onSecondaryContainerArgb = tonalArgb(pal.secondaryHue, pal.secondarySat, 16)

            val tertiaryArgb = ensureContrast(
                hue = pal.tertiaryHue,
                saturation = pal.tertiarySat,
                initialTone = 42,
                backgroundArgb = Color.WHITE,
                minContrast = 4.5,
                preferLighter = false
            )
            val tertiaryContainerArgb = tonalArgb(pal.tertiaryHue, pal.tertiarySat * 0.75f, 90)
            val onTertiaryContainerArgb = tonalArgb(pal.tertiaryHue, pal.tertiarySat, 16)

            lightColorScheme(
                primary = androidx.compose.ui.graphics.Color(primaryArgb),
                onPrimary = androidx.compose.ui.graphics.Color.White,
                primaryContainer = androidx.compose.ui.graphics.Color(primaryContainerArgb),
                onPrimaryContainer = androidx.compose.ui.graphics.Color(onPrimaryContainerArgb),
                inversePrimary = androidx.compose.ui.graphics.Color(tonalArgb(pal.primaryHue, pal.primarySat, 80)),
                secondary = androidx.compose.ui.graphics.Color(secondaryArgb),
                onSecondary = androidx.compose.ui.graphics.Color.White,
                secondaryContainer = androidx.compose.ui.graphics.Color(secondaryContainerArgb),
                onSecondaryContainer = androidx.compose.ui.graphics.Color(onSecondaryContainerArgb),
                tertiary = androidx.compose.ui.graphics.Color(tertiaryArgb),
                onTertiary = androidx.compose.ui.graphics.Color.White,
                tertiaryContainer = androidx.compose.ui.graphics.Color(tertiaryContainerArgb),
                onTertiaryContainer = androidx.compose.ui.graphics.Color(onTertiaryContainerArgb),
                error = ExpenseRed,
                onError = androidx.compose.ui.graphics.Color.White,
                errorContainer = androidx.compose.ui.graphics.Color(0xFFFEE2E2),
                onErrorContainer = androidx.compose.ui.graphics.Color(0xFF991B1B),
                background = androidx.compose.ui.graphics.Color(bgArgb),
                onBackground = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralSat, 12)),
                surface = androidx.compose.ui.graphics.Color(bgArgb),
                onSurface = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralSat, 12)),
                surfaceVariant = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralVariantSat, 92)),
                onSurfaceVariant = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralVariantSat * 0.85f, 34)),
                surfaceContainerLowest = androidx.compose.ui.graphics.Color(surfaceLowestArgb),
                surfaceContainerLow = androidx.compose.ui.graphics.Color(surfaceLowArgb),
                surfaceContainer = androidx.compose.ui.graphics.Color(surfaceArgb),
                surfaceContainerHigh = androidx.compose.ui.graphics.Color(surfaceHighArgb),
                surfaceContainerHighest = androidx.compose.ui.graphics.Color(surfaceHighestArgb),
                surfaceBright = androidx.compose.ui.graphics.Color(Color.WHITE),
                surfaceDim = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralSat, 86)),
                surfaceTint = androidx.compose.ui.graphics.Color(primaryArgb),
                outline = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralVariantSat, 52)),
                outlineVariant = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralVariantSat, 82)),
                inverseSurface = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralSat, 20)),
                inverseOnSurface = androidx.compose.ui.graphics.Color(tonalArgb(pal.neutralHue, pal.neutralSat, 95))
            )
        }
    }

    /**
     * Computes the active dynamic widget palette for either dark or light mode.
     *
     * Always prefers real live wallpaper colors from `WallpaperManager.getWallpaperColors(FLAG_SYSTEM)`
     * so widgets visibly reflect the wallpaper on all devices (including Vivo OriginOS),
     * while preserving pure solid (`widget_atomic_bg`) and pure transparent (`widget_transparent_card_bg`)
     * card backgrounds and tinting primary text, buttons, chips, and borders.
     */
    fun getDynamicPalette(context: Context, isDark: Boolean = isNightMode(context)): WidgetDynamicPalette {
        val solidCardBg = if (isDark) Color.parseColor("#242426") else Color.parseColor("#FFFFFF")
        val glassCardBg = if (isDark) Color.parseColor("#2D000000") else Color.parseColor("#22000000")

        val seed = resolveWallpaperSeed(context)
        if (seed != null) {
            return buildWidgetPaletteFromSeed(seed, solidCardBg, glassCardBg, isDark)
        }

        // Static fallback (API 24–26 or headless test environment without wallpaper colors)
        return getStaticFallbackPalette(context, isDark)
    }

    internal fun buildWidgetPaletteFromSeed(
        seed: WallpaperSeed,
        solidCardBg: Int,
        glassCardBg: Int,
        isDark: Boolean
    ): WidgetDynamicPalette {
        val pal = extractPalettesFromSeed(seed)

        val primaryPillBg = if (isDark) {
            ensureContrast(
                hue = pal.primaryHue,
                saturation = pal.primarySat,
                initialTone = 72,
                backgroundArgb = solidCardBg,
                minContrast = 4.2,
                preferLighter = true
            )
        } else {
            ensureContrast(
                hue = pal.primaryHue,
                saturation = pal.primarySat,
                initialTone = 42,
                backgroundArgb = Color.WHITE,
                minContrast = 4.6,
                preferLighter = false
            )
        }

        val primaryPillText = if (ColorUtils.calculateContrast(Color.WHITE, primaryPillBg) >=
            ColorUtils.calculateContrast(Color.parseColor("#0F172A"), primaryPillBg)
        ) {
            Color.WHITE
        } else {
            tonalArgb(pal.primaryHue, pal.primarySat * 0.6f, 12)
        }

        val secondaryPillBg = if (isDark) {
            tonalArgb(pal.primaryHue, pal.primarySat * 0.65f, 24)
        } else {
            tonalArgb(pal.primaryHue, pal.primarySat * 0.65f, 90)
        }

        val secondaryPillText = if (isDark) {
            ensureContrast(
                hue = pal.primaryHue,
                saturation = pal.primarySat * 0.75f,
                initialTone = 90,
                backgroundArgb = secondaryPillBg,
                minContrast = 5.0,
                preferLighter = true
            )
        } else {
            ensureContrast(
                hue = pal.primaryHue,
                saturation = pal.primarySat * 0.85f,
                initialTone = 22,
                backgroundArgb = secondaryPillBg,
                minContrast = 5.0,
                preferLighter = false
            )
        }

        // Translucent wallpaper-tinted glass pill for transparent widgets
        val transparentPillBase = tonalArgb(pal.primaryHue, pal.primarySat, if (isDark) 36 else 44)
        val transparentPillBg = ColorUtils.setAlphaComponent(transparentPillBase, 0x6E)

        val accentText = if (isDark) {
            ensureContrast(
                hue = pal.primaryHue,
                saturation = pal.primarySat.coerceAtLeast(0.55f),
                initialTone = 80,
                backgroundArgb = solidCardBg,
                minContrast = 5.0,
                preferLighter = true
            )
        } else {
            ensureContrast(
                hue = pal.primaryHue,
                saturation = pal.primarySat.coerceAtLeast(0.55f),
                initialTone = 38,
                backgroundArgb = solidCardBg,
                minContrast = 4.8,
                preferLighter = false
            )
        }

        // Luminous vibrant wallpaper tint for primary text over transparent dark glass
        val primaryTextOnTransparent = tonalArgb(
            hue = pal.primaryHue,
            saturation = pal.primarySat.coerceAtLeast(0.62f),
            tone = 86
        )

        val cardBorder = if (isDark) {
            tonalArgb(pal.primaryHue, pal.primarySat * 0.55f, 38)
        } else {
            tonalArgb(pal.primaryHue, pal.primarySat * 0.45f, 78)
        }
        val cardBorderBold = if (isDark) {
            tonalArgb(pal.primaryHue, pal.primarySat * 0.70f, 54)
        } else {
            tonalArgb(pal.primaryHue, pal.primarySat * 0.65f, 58)
        }
        val glowInner = ColorUtils.setAlphaComponent(
            tonalArgb(pal.primaryHue, pal.primarySat, if (isDark) 72 else 52),
            0xA6
        )
        val glowHalo = ColorUtils.setAlphaComponent(
            tonalArgb(pal.primaryHue, pal.primarySat, if (isDark) 65 else 55),
            0x3D
        )
        val transparentBorder = ColorUtils.setAlphaComponent(
            tonalArgb(pal.primaryHue, pal.primarySat * 0.60f, 84),
            0x66
        )
        val trackRing = if (isDark) {
            ColorUtils.blendARGB(solidCardBg, primaryPillBg, 0.22f)
        } else {
            ColorUtils.blendARGB(solidCardBg, primaryPillBg, 0.16f)
        }

        return WidgetDynamicPalette(
            cardBg = solidCardBg,
            cardBorder = cardBorder,
            cardBorderBold = cardBorderBold,
            glowInner = glowInner,
            glowHalo = glowHalo,
            transparentCardBg = glassCardBg,
            transparentBorder = transparentBorder,
            primaryPillBg = primaryPillBg,
            primaryPillText = primaryPillText,
            secondaryPillBg = secondaryPillBg,
            secondaryPillText = secondaryPillText,
            transparentPillBg = transparentPillBg,
            accentText = accentText,
            primaryTextOnTransparent = primaryTextOnTransparent,
            trackRing = trackRing,
            swatchColors = seed.swatchColors,
            isFromWallpaper = seed.isFromWallpaper
        )
    }

    fun getStaticFallbackPalette(context: Context, isDark: Boolean = isNightMode(context)): WidgetDynamicPalette {
        val cardBg = if (isDark) Color.parseColor("#242426") else Color.parseColor("#FFFFFF")
        val cardBorder = if (isDark) Color.parseColor("#323336") else Color.parseColor("#E2E8F0")
        val cardBorderBold = if (isDark) Color.parseColor("#48494D") else Color.parseColor("#CBD5E1")
        val glowInner = if (isDark) Color.parseColor("#99818CF8") else Color.parseColor("#806366F1")
        val glowHalo = if (isDark) Color.parseColor("#33818CF8") else Color.parseColor("#2E6366F1")
        val transparentCardBg = if (isDark) Color.parseColor("#2D000000") else Color.parseColor("#22000000")
        val transparentBorder = Color.parseColor("#3DFFFFFF")
        val primaryPillBg = if (isDark) Color.parseColor("#6366F1") else Color.parseColor("#4F46E5")
        val primaryPillText = Color.WHITE
        val secondaryPillBg = if (isDark) Color.parseColor("#303136") else Color.parseColor("#EEF2F6")
        val secondaryPillText = if (isDark) Color.parseColor("#CBD5E1") else Color.parseColor("#334155")
        val transparentPillBg = Color.parseColor("#4D0F172A")
        val accentText = if (isDark) Color.parseColor("#818CF8") else Color.parseColor("#4F46E5")
        val primaryTextOnTransparent = Color.parseColor("#C7D2FE")
        val trackRing = if (isDark) Color.parseColor("#36373C") else Color.parseColor("#E2E8F0")

        return WidgetDynamicPalette(
            cardBg = cardBg,
            cardBorder = cardBorder,
            cardBorderBold = cardBorderBold,
            glowInner = glowInner,
            glowHalo = glowHalo,
            transparentCardBg = transparentCardBg,
            transparentBorder = transparentBorder,
            primaryPillBg = primaryPillBg,
            primaryPillText = primaryPillText,
            secondaryPillBg = secondaryPillBg,
            secondaryPillText = secondaryPillText,
            transparentPillBg = transparentPillBg,
            accentText = accentText,
            primaryTextOnTransparent = primaryTextOnTransparent,
            trackRing = trackRing,
            swatchColors = listOf(primaryPillBg, accentText, primaryTextOnTransparent),
            isFromWallpaper = false
        )
    }

    /**
     * Computes a compact signature representing the current wallpaper colors and UI night mode.
     * Used to detect wallpaper changes reliably across process restarts and resume events.
     */
    fun computeWallpaperSignature(context: Context): String {
        val isDark = isNightMode(context)
        val swatches = extractWallpaperSwatches(context)
        val dynamicAccent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                ContextCompat.getColor(context, android.R.color.system_accent1_500)
            } catch (_: Exception) {
                0
            }
        } else {
            0
        }
        return "$isDark|${swatches.joinToString(",")}|$dynamicAccent"
    }

    /**
     * Checks if the wallpaper palette has changed since the last refresh, and updates all widgets
     * immediately when dynamic wallpaper tinting for widgets is active.
     */
    fun refreshWidgetsIfWallpaperChanged(context: Context, force: Boolean = false): Boolean {
        if (!shouldTintWidgets(context)) return false
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentSig = computeWallpaperSignature(context)
        val lastSig = prefs.getString(PREF_LAST_WALLPAPER_SIG, null)
        if (force || currentSig != lastSig) {
            prefs.edit().putString(PREF_LAST_WALLPAPER_SIG, currentSig).apply()
            WidgetUpdater.updateAllWidgets(context.applicationContext)
            return true
        }
        return false
    }

    /**
     * Registers a live `WallpaperManager.OnColorsChangedListener` (API 27+) so both the app theme,
     * placed widgets, and Settings previews react the moment the user changes their wallpaper.
     */
    fun registerWallpaperListener(
        context: Context,
        onColorsChanged: () -> Unit
    ): () -> Unit {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return {}
        return try {
            val wm = WallpaperManager.getInstance(context.applicationContext)
            val listener = WallpaperManager.OnColorsChangedListener { _, which ->
                if ((which and WallpaperManager.FLAG_SYSTEM) != 0) {
                    onColorsChanged()
                }
            }
            wm.addOnColorsChangedListener(listener, Handler(Looper.getMainLooper()))
            val unregister: () -> Unit = {
                try {
                    wm.removeOnColorsChangedListener(listener)
                } catch (_: Exception) {
                    // Ignore cleanup errors on restricted devices
                }
            }
            unregister
        } catch (_: Exception) {
            {}
        }
    }
}
