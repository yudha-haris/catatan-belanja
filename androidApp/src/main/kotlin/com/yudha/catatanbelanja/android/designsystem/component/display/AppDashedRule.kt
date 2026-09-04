package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme

/** The dashed rule a till roll prints between its sections. */
@Composable
fun AppDashedRule(
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.line,
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(DASH_THICKNESS),
    ) {
        val dash = DASH_LENGTH.toPx()
        val y = size.height / 2f
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = size.height,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, dash)),
        )
    }
}

private val DASH_THICKNESS = 2.dp
private val DASH_LENGTH = 5.dp
