package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

/**
 * Maintained data class for style parameters.
 */
data class LiquidGlassStyle(
    val tint: Color = Color.Unspecified,
    val blurRadius: Dp = 0.dp,
    val lensCurvature: Float = 0f,
    val chromaticDispersion: Float = 0f,
    val specularHighlight: Float = 0f,
    val borderAlpha: Float = 0f,
    val interactiveSheen: Boolean = false
)

/**
 * Clean Material 3 modifier (liquid glass effects removed per user request).
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    tint: Color? = null,
    blurRadius: Dp = 0.dp,
    lensCurvature: Float = 0f,
    specularHighlight: Float = 0f,
    chromaticDispersion: Float = 0f,
    borderAlpha: Float = 0f,
    animatedSheen: Boolean = false
): Modifier = this

private fun Color.hasNoticeableChroma(): Boolean {
    val maxC = max(red, max(green, blue))
    val minC = min(red, min(green, blue))
    return (maxC - minC) > 0.12f
}

/**
 * Material 3 Expressive Card container.
 * Drop-in replacement for cards across all screens.
 *
 * Guarantees a 100% opaque container surface so hardware elevation shadows never
 * bleed inward through semi-transparent fills in light mode (fixing thick grey inner borders).
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    tint: Color? = null,
    contentColor: Color? = null,
    blurRadius: Dp = 0.dp,
    lensCurvature: Float = 0f,
    specularHighlight: Float = 0f,
    chromaticDispersion: Float = 0f,
    borderAlpha: Float = 0f,
    animatedSheen: Boolean = false,
    elevation: Dp = 2.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isLightMode = colorScheme.surface.luminance() > 0.5f

    val resolvedContainerColor: Color = when {
        tint == null || tint == Color.Unspecified -> {
            if (isLightMode) colorScheme.surfaceContainerLowest else colorScheme.surfaceContainerLow
        }
        tint.alpha < 0.99f -> {
            if (isLightMode) {
                // Legacy callers pass semi-transparent surfaceVariant.copy(alpha = 0.55f);
                // in light mode render a crisp, 100% opaque elevated surface instead of muddy grey.
                if (tint.hasNoticeableChroma()) {
                    tint.compositeOver(colorScheme.surfaceContainerLowest)
                } else {
                    colorScheme.surfaceContainerLowest
                }
            } else {
                tint.compositeOver(colorScheme.surfaceContainerLow)
            }
        }
        else -> tint
    }

    val resolvedElevation = if (isLightMode) 0.dp else elevation.coerceAtMost(2.dp)
    val resolvedBorderColor = if (isLightMode) {
        colorScheme.outlineVariant.copy(alpha = 0.65f)
    } else {
        colorScheme.outlineVariant.copy(alpha = 0.40f)
    }

    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = resolvedContainerColor,
            contentColor = contentColor ?: colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = resolvedElevation),
        border = BorderStroke(1.dp, resolvedBorderColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

/**
 * Material 3 Expressive Pill Surface.
 * Ideal for floating navigation bars, chips, and quick action bars.
 */
@Composable
fun LiquidGlassPill(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(32.dp),
    tint: Color? = null,
    contentColor: Color? = null,
    blurRadius: Dp = 0.dp,
    animatedSheen: Boolean = false,
    elevation: Dp = 3.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isLightMode = colorScheme.surface.luminance() > 0.5f

    val resolvedColor: Color = when {
        tint == null || tint == Color.Unspecified -> colorScheme.surfaceContainer
        tint.alpha < 0.99f -> tint.compositeOver(colorScheme.surfaceContainer)
        else -> tint
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = resolvedColor,
        contentColor = contentColor ?: colorScheme.onSurface,
        tonalElevation = if (isLightMode) 0.dp else elevation.coerceAtMost(3.dp),
        shadowElevation = if (isLightMode) elevation.coerceAtMost(2.dp) else elevation.coerceAtMost(3.dp),
        border = BorderStroke(
            1.dp,
            colorScheme.outlineVariant.copy(alpha = if (isLightMode) 0.60f else 0.35f)
        )
    ) {
        Box {
            content()
        }
    }
}

/**
 * Material 3 Expressive Floating Action Button (FAB).
 */
@Composable
fun LiquidGlassFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    contentColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        containerColor = tint ?: MaterialTheme.colorScheme.primary,
        contentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}
