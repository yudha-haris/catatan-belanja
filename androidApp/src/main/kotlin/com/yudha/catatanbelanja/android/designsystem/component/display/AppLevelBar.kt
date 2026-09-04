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

/** The `.lvl` bar: 6dp track, primary fill, coral when the item is running low. */
@Composable
fun AppLevelBar(
    progress: Float,
    modifier: Modifier = Modifier,
    isLow: Boolean = false,
) {
    val colors = AppTheme.colors
    val fill = if (isLow) colors.coral else colors.primary
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 300),
        label = "levelBar",
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
            color = fill,
            size = Size(size.width * animated, size.height),
            cornerRadius = corner,
        )
    }
}
