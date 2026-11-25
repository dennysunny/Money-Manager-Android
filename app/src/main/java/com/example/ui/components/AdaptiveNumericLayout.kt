package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Checks whether every formatted numeric string fits inside its equal column width
 * without wrapping digits across lines or breaking layout padding.
 */
@Composable
fun rememberAllFitColumnWidth(
    textsAndStyles: List<Pair<String, TextStyle>>,
    columnWidth: Dp,
    horizontalPaddingPerColumn: Dp = 8.dp
): Boolean {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val availablePx = with(density) { (columnWidth - horizontalPaddingPerColumn).coerceAtLeast(0.dp).toPx() }
    return remember(textsAndStyles, availablePx) {
        textsAndStyles.all { (text, style) ->
            val measured = textMeasurer.measure(
                text = text,
                style = style,
                maxLines = 1,
                softWrap = false
            )
            measured.size.width <= availablePx
        }
    }
}
