package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme

/** One arc. [fraction] is the slice's cut of the whole ring, 0..1, pre-computed by the ViewModel. */
data class AppDonutSlice(
    val fraction: Float,
    val color: Color,
)

/**
 * The share ring: how concentrated the spending is, at a glance. The centre carries the headline
 * number, because a ring on its own is a shape and the number is the point.
 *
 * The arcs are separated by a hairline gap rather than by a stroke, so two adjacent slices stay
 * distinguishable when their colours are two steps of the same hue.
 */
@Composable
fun AppDonutChart(
    slices: List<AppDonutSlice>,
    centerValue: String,
    centerLabel: String,
    modifier: Modifier = Modifier,
) {
    if (slices.isEmpty()) return
    val colors = AppTheme.colors
    val sweep = remember(slices) { Animatable(0f) }

    LaunchedEffect(slices) {
        sweep.snapTo(0f)
        sweep.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 700,
                easing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1f),
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(RING_SIZE_DP.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(RING_SIZE_DP.dp)) {
            val thickness = STROKE_DP.dp.toPx()
            val inset = thickness / 2f
            val diameter = size.minDimension - thickness
            val topLeft = Offset(inset, inset)
            val arcSize = Size(diameter, diameter)
            val stroke = Stroke(width = thickness, cap = StrokeCap.Butt)

            drawArc(
                color = colors.background,
                startAngle = 0f,
                sweepAngle = FULL_TURN,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )

            var angle = START_ANGLE
            slices.forEach { slice ->
                val span = slice.fraction.coerceIn(0f, 1f) * FULL_TURN * sweep.value
                // A slice thinner than the gap would render as nothing but a notch in its
                // neighbour, so the gap shrinks with it rather than eating it.
                val gap = GAP_DEGREES.coerceAtMost(span / 2f)
                drawArc(
                    color = slice.color,
                    startAngle = angle,
                    sweepAngle = span - gap,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke,
                )
                angle += span
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = centerValue,
                style = AppTheme.typography.receiptTotalSmall,
                color = colors.ink,
                maxLines = 1,
            )
            Text(
                text = centerLabel,
                style = AppTheme.typography.tiny,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private const val RING_SIZE_DP = 172
private const val STROKE_DP = 22
private const val FULL_TURN = 360f
private const val START_ANGLE = -90f
private const val GAP_DEGREES = 2.5f
