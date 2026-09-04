package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme

private const val SPENT_ALPHA = 0.3f

/**
 * The `.lvl` bar: 6dp track, primary fill, coral when the item is running low.
 *
 * [estimate] draws the same bar as two tones instead of one: solid up to what the app reckons is
 * still there, faded from there to what was last written down. The faded stretch is the part it
 * believes has already been used — which is a thing to show, not a number to argue with, so it
 * reads as a shadow of the fill rather than as a second bar competing with it.
 */
@Composable
fun AppLevelBar(
    progress: Float,
    modifier: Modifier = Modifier,
    isLow: Boolean = false,
    estimate: Float? = null,
) {
    val colors = AppTheme.colors
    val fill = if (isLow) colors.coral else colors.primary
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 300),
        label = "levelBar",
    )
    // Never above the stored fill: consumption only ever takes the bar down.
    val animatedEstimate by animateFloatAsState(
        targetValue = (estimate ?: target).coerceIn(0f, target),
        animationSpec = tween(durationMillis = 300),
        label = "levelBarEstimate",
    )
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp),
    ) {
        val corner = CornerRadius(9.dp.toPx(), 9.dp.toPx())
        drawRoundRect(color = colors.background, cornerRadius = corner)
        if (animated <= 0f) return@Canvas
        drawRoundRect(
            color = if (estimate == null) fill else fill.copy(alpha = SPENT_ALPHA),
            size = Size(size.width * animated, size.height),
            cornerRadius = corner,
        )
        if (estimate == null || animatedEstimate <= 0f) return@Canvas
        drawRoundRect(
            color = fill,
            size = Size(size.width * animatedEstimate, size.height),
            cornerRadius = corner,
        )
    }
}
