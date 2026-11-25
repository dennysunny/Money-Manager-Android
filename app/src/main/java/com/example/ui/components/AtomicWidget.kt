package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Standard OriginOS Atomic Widget card dimensions (excluding the 24dp Compose footer band).
 * Total Compose footprint: cardHeight (156dp) + 24dp footer = 180dp.
 */
enum class AtomicWidgetSize(val cardWidth: Dp, val cardHeight: Dp) {
    Square2x2(cardWidth = 156.dp, cardHeight = 156.dp),
    Wide4x2(cardWidth = 312.dp, cardHeight = 156.dp)
}

/**
 * Widget border thickness & halo styles matching home screen RemoteViews drawables.
 */
enum class AtomicBorderStyle(val key: String, val label: String) {
    None(key = "none", label = "None"),
    Hairline(key = "hairline", label = "Hairline"),
    Glow(key = "glow", label = "Glow"),
    Bold(key = "bold", label = "Bold");

    companion object {
        fun fromKey(key: String?): AtomicBorderStyle =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: Hairline

        val visibleBorderStyles: List<AtomicBorderStyle> = listOf(Hairline, Glow, Bold)
    }
}

/**
 * Caps bitmap backdrop corner radius to 45% of the shortest side so rounded cards
 * never deform on narrow or shallow resize states.
 */
fun computeCappedAtomicCornerRadiusPx(
    widthPx: Float,
    heightPx: Float,
    desiredRadiusPx: Float
): Float {
    val maxAllowed = minOf(widthPx, heightPx) * 0.45f
    return desiredRadiusPx.coerceIn(0f, maxAllowed.coerceAtLeast(0f))
}

/**
 * Reusable Compose OriginOS AtomicWidget container.
 *
 * - Default reusable container corner radius: 22dp (Home screen preview cards can pass 26dp)
 * - Default reusable inner content padding: 16dp (Home screen XML preview can pass user padding, default 8dp)
 * - 24dp OriginOS footer band with -0.5dp optical lift (11.5dp optical center below card bottom)
 * - Typography: sans-serif-medium, 13sp, lineHeight 15.34sp (1.18x), includeFontPadding = false,
 *   letterSpacing = 0.015em, drop shadow (#B3000000, dy = 1.5, radius = 3.0)
 * - Hidden labels use alpha(0f) instead of removing the Box so card height never stretches.
 */
@Composable
fun AtomicWidget(
    label: String,
    modifier: Modifier = Modifier,
    size: AtomicWidgetSize = AtomicWidgetSize.Square2x2,
    showLabel: Boolean = true,
    cornerRadius: Dp = 22.dp,
    contentPadding: Dp = 16.dp,
    borderStyle: AtomicBorderStyle = AtomicBorderStyle.Hairline,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
    glowInnerColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.60f),
    glowHaloColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
    labelColor: Color = Color.White,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val cardShape = RoundedCornerShape(cornerRadius)
    val shadowOffsetY = with(density) { 1.0.dp.toPx() }
    val shadowBlurRadius = with(density) { 2.0.dp.toPx() }

    val borderModifier = when (borderStyle) {
        AtomicBorderStyle.None -> Modifier
        AtomicBorderStyle.Hairline -> Modifier.border(1.dp, borderColor, cardShape)
        AtomicBorderStyle.Bold -> Modifier.border(2.2.dp, borderColor, cardShape)
        AtomicBorderStyle.Glow -> Modifier
            .drawBehind {
                val haloStrokePx = 4.dp.toPx()
                val radiusPx = computeCappedAtomicCornerRadiusPx(
                    widthPx = this.size.width,
                    heightPx = this.size.height,
                    desiredRadiusPx = cornerRadius.toPx()
                )
                drawRoundRect(
                    color = glowHaloColor,
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = Stroke(width = haloStrokePx)
                )
            }
            .border(2.dp, glowInnerColor, cardShape)
    }

    Column(
        modifier = modifier.width(size.cardWidth),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = size.cardWidth, height = size.cardHeight)
                .then(borderModifier)
                .clip(cardShape)
                .background(containerColor)
                .padding(contentPadding),
            content = content
        )

        // 24dp OriginOS footer standard (geometric center 12.0dp, -0.5dp optical lift = 11.5dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .alpha(if (showLabel) 1f else 0f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = labelColor,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 16.52.sp,
                    letterSpacing = 0.015.em,
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    shadow = Shadow(
                        color = Color(0x8C000000),
                        offset = Offset(0f, shadowOffsetY),
                        blurRadius = shadowBlurRadius
                    )
                ),
                modifier = Modifier.offset(y = (-0.5).dp)
            )
        }
    }
}
