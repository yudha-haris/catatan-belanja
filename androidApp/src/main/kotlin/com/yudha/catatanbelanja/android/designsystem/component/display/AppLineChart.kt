package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme

/** One point of the price trend. [ratio] is 0 at the cheapest point and 1 at the dearest. */
data class AppLineChartPoint(
    val valueLabel: String,
    val dateLabel: String,
    val ratio: Float,
)

/** The price-trend sparkline: gradient area, 3dp line, white dots, value and date labels. */
@Composable
fun AppLineChart(
    points: List<AppLineChartPoint>,
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) return
    val colors = AppTheme.colors
    val measurer: TextMeasurer = rememberTextMeasurer()
    val valueStyle = AppTheme.typography.barLabel.copy(color = colors.ink)
    val dateStyle = AppTheme.typography.barLabel

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp),
    ) {
        val left = 8.dp.toPx()
        val right = 8.dp.toPx()
        val top = 22.dp.toPx()
        val bottom = 26.dp.toPx()
        val baseline = size.height - bottom
        val stepX = (size.width - left - right) / (points.size - 1)
        val xOf = { index: Int -> left + index * stepX }
        val yOf = { ratio: Float -> top + (1f - ratio.coerceIn(0f, 1f)) * (baseline - top) }

        val line = Path()
        points.forEachIndexed { index, point ->
            val x = xOf(index)
            val y = yOf(point.ratio)
            if (index == 0) {
                line.moveTo(x, y)
                return@forEachIndexed
            }
            line.lineTo(x, y)
        }
        val area = Path()
        area.addPath(line)
        area.lineTo(xOf(points.lastIndex), baseline)
        area.lineTo(xOf(0), baseline)
        area.close()

        drawPath(
            path = area,
            brush = Brush.verticalGradient(
                colors = listOf(colors.primary.copy(alpha = 0.25f), colors.primary.copy(alpha = 0f)),
                startY = top,
                endY = baseline,
            ),
        )
        drawPath(
            path = line,
            color = colors.primary,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        points.forEachIndexed { index, point ->
            val x = xOf(index)
            val y = yOf(point.ratio)
            drawCircle(color = colors.paper, radius = 4.5.dp.toPx(), center = Offset(x, y))
            drawCircle(
                color = colors.primary,
                radius = 4.5.dp.toPx(),
                center = Offset(x, y),
                style = Stroke(width = 2.5.dp.toPx()),
            )

            val value = measurer.measure(point.valueLabel, valueStyle)
            val date = measurer.measure(point.dateLabel, dateStyle)
            val valueX = when (index) {
                0 -> x
                points.lastIndex -> x - value.size.width
                else -> x - value.size.width / 2f
            }
            val dateX = when (index) {
                0 -> x
                points.lastIndex -> x - date.size.width
                else -> x - date.size.width / 2f
            }
            drawText(
                textLayoutResult = value,
                topLeft = Offset(
                    x = valueX.coerceAtMost(size.width - value.size.width).coerceAtLeast(0f),
                    y = y - 9.dp.toPx() - value.size.height,
                ),
            )
            drawText(
                textLayoutResult = date,
                topLeft = Offset(
                    x = dateX.coerceAtMost(size.width - date.size.width).coerceAtLeast(0f),
                    y = size.height - date.size.height - 2.dp.toPx(),
                ),
            )
        }
    }
}
