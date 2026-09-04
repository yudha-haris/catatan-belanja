package com.yudha.catatanbelanja.android.designsystem.component.feedback

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val BURST_MILLIS = 1_200f
private const val CONFETTI_COUNT = 26
private const val RING_MILLIS = 700f
private const val CHECK_DELAY_MILLIS = 300f
private const val CHECK_MILLIS = 400f
private const val CONFETTI_MILLIS = 1_000f

/**
 * The prototype's `#boom`: a primary circle scaling in with a drawn check mark plus 26 confetti
 * squares flying out on random angles. Everything is one Canvas, so it stays cheap.
 */
@Composable
fun SuccessBurst(
    visible: Boolean,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {},
) {
    if (!visible) return
    val colors = AppTheme.colors
    val confetti = colors.confetti
    val progress = remember { Animatable(0f) }
    // angle, distance, start delay — fixed per burst so the pieces do not jitter on recomposition.
    val seeds = remember {
        List(CONFETTI_COUNT) {
            floatArrayOf(
                Random.nextFloat() * 2f * PI.toFloat(),
                90f + Random.nextFloat() * 120f,
                Random.nextFloat() * 150f,
            )
        }
    }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = BURST_MILLIS.toInt(), easing = LinearEasing),
        )
        onFinished()
    }

    val ringEasing = remember { CubicBezierEasing(0.2f, 0.9f, 0.3f, 1.4f) }
    Canvas(modifier = modifier.fillMaxSize()) {
        val elapsed = progress.value * BURST_MILLIS
        val center = Offset(size.width / 2f, size.height / 2f)

        val confettiSize = 10.dp.toPx()
        val confettiRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        seeds.forEachIndexed { index, seed ->
            val local = ((elapsed - seed[2]) / CONFETTI_MILLIS).coerceIn(0f, 1f)
            if (local <= 0f) return@forEachIndexed
            val eased = 1f - (1f - local) * (1f - local) * (1f - local)
            val dx = cos(seed[0]) * seed[1] * eased
            val dy = sin(seed[0]) * seed[1] * eased
            val topLeft = Offset(
                x = center.x + dx.dp.toPx() - confettiSize / 2f,
                y = center.y + dy.dp.toPx() - confettiSize / 2f,
            )
            val pivot = topLeft + Offset(confettiSize / 2f, confettiSize / 2f)
            rotate(degrees = 540f * local, pivot = pivot) {
                drawRoundRect(
                    color = confetti[index % confetti.size],
                    topLeft = topLeft,
                    size = Size(confettiSize, confettiSize),
                    cornerRadius = confettiRadius,
                    alpha = 1f - local,
                )
            }
        }

        val ringScale = ringEasing.transform((elapsed / RING_MILLIS).coerceIn(0f, 1f))
        drawCircle(color = colors.primary, radius = 55.dp.toPx() * ringScale, center = center)

        val checkProgress =
            ((elapsed - CHECK_DELAY_MILLIS) / CHECK_MILLIS).coerceIn(0f, 1f)
        if (checkProgress <= 0f) return@Canvas
        val unit = 54.dp.toPx() / 24f
        val origin = Offset(center.x - 27.dp.toPx(), center.y - 27.dp.toPx())
        val check = Path()
        check.moveTo(origin.x + 5f * unit, origin.y + 12f * unit)
        check.lineTo(origin.x + 10f * unit, origin.y + 17f * unit)
        check.lineTo(origin.x + 19f * unit, origin.y + 7f * unit)
        val measure = PathMeasure()
        measure.setPath(check, false)
        val drawn = Path()
        measure.getSegment(0f, measure.length * checkProgress, drawn, true)
        drawPath(
            path = drawn,
            color = colors.paper,
            style = Stroke(
                width = 4f * unit,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )
    }
}
