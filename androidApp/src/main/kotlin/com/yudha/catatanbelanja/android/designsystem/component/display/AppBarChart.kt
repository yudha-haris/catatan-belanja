package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import kotlinx.coroutines.delay

/** One bar. [ratio] is pre-computed by the ViewModel: 0..1 against the tallest bar. */
data class AppBarChartBar(
    val label: String,
    val valueLabel: String,
    val ratio: Float,
    val highlighted: Boolean = false,
)

/** The "8 belanja terakhir" chart. [onBarClick] receives the index of the tapped bar. */
@Composable
fun AppBarChart(
    bars: List<AppBarChartBar>,
    modifier: Modifier = Modifier,
    onBarClick: (Int) -> Unit = {},
) {
    if (bars.isEmpty()) return
    val colors = AppTheme.colors
    val barShape = RoundedCornerShape(
        topStart = 10.dp,
        topEnd = 10.dp,
        bottomStart = 6.dp,
        bottomEnd = 6.dp,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEachIndexed { index, bar ->
            val grow = remember(bar.label, index) { Animatable(0f) }
            LaunchedEffect(bar.label, index) {
                delay(index * 50L)
                grow.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 500,
                        easing = CubicBezierEasing(0.2f, 0.9f, 0.3f, 1f),
                    ),
                )
            }
            val brush = when (bar.highlighted) {
                true -> Brush.verticalGradient(listOf(colors.heroEnd, colors.primary))
                false -> Brush.verticalGradient(listOf(colors.primaryLight, colors.primary))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onBarClick(index) },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = bar.valueLabel,
                    style = AppTheme.typography.barLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
                Spacer(Modifier.height(Spacing.x6))
                Box(
                    modifier = Modifier
                        .widthIn(max = 38.dp)
                        .fillMaxWidth()
                        .height((72 * bar.ratio.coerceIn(0f, 1f)).dp.coerceAtLeast(6.dp))
                        .graphicsLayer {
                            scaleY = grow.value
                            transformOrigin = TransformOrigin(0.5f, 1f)
                        }
                        .background(brush, barShape),
                )
                Spacer(Modifier.height(Spacing.x6))
                Text(
                    text = bar.label,
                    style = AppTheme.typography.barLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}
