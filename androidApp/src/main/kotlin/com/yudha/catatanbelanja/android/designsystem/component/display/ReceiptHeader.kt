package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/**
 * The receipt hero: 135° hero gradient, tabular total that bumps when [amount] changes, and the
 * torn zigzag bottom edge (16dp triangle pitch) from the prototype's `.receipt:after`.
 */
@Composable
fun ReceiptHeader(
    label: String,
    amount: String,
    footerLeft: String,
    footerRight: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val colors = AppTheme.colors
    val onHero = colors.paper
    val bump = remember { Animatable(1f) }
    val isFirstAmount = remember { mutableStateOf(true) }
    LaunchedEffect(amount) {
        if (isFirstAmount.value) {
            isFirstAmount.value = false
            return@LaunchedEffect
        }
        bump.animateTo(targetValue = 1.06f, animationSpec = tween(durationMillis = 105))
        bump.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 245))
    }
    val topShape = RoundedCornerShape(
        topStart = AppTheme.shapes.radius,
        topEnd = AppTheme.shapes.radius,
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(topShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(colors.heroStart, colors.heroEnd),
                        start = Offset.Zero,
                        end = Offset.Infinite,
                    ),
                )
                .padding(
                    start = if (compact) 20.dp else 22.dp,
                    end = if (compact) 20.dp else 22.dp,
                    top = if (compact) 16.dp else 20.dp,
                    bottom = if (compact) 22.dp else 26.dp,
                ),
        ) {
            Text(
                text = label,
                style = AppTheme.typography.muted,
                color = onHero.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(Spacing.x4))
            Text(
                text = amount,
                modifier = Modifier.graphicsLayer {
                    scaleX = bump.value
                    scaleY = bump.value
                    transformOrigin = TransformOrigin(0f, 0.5f)
                },
                style = if (compact) {
                    AppTheme.typography.receiptTotalSmall
                } else {
                    AppTheme.typography.receiptTotal
                },
                color = onHero,
                maxLines = 1,
            )
            Spacer(Modifier.height(Spacing.x6))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = footerLeft,
                    style = AppTheme.typography.muted,
                    color = onHero.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = footerRight,
                    style = AppTheme.typography.muted,
                    color = onHero.copy(alpha = 0.9f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
        ) {
            val pitch = 16.dp.toPx()
            val path = Path()
            path.moveTo(0f, 0f)
            var x = 0f
            while (x < size.width) {
                path.lineTo(x + pitch / 2f, size.height)
                path.lineTo(x + pitch, 0f)
                x += pitch
            }
            path.lineTo(size.width, 0f)
            path.close()
            drawPath(path = path, color = colors.heroEnd)
        }
    }
}
