package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.appShadow

/**
 * One figure of a stats row. [onClick] is optional: a stat that stands for a single thing — the
 * biggest trip of the window, say — can open it, and a stat that is only a number stays inert
 * rather than pretending to be a button.
 */
@Composable
fun AppStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    hintTone: AppBadgeTone = AppBadgeTone.Neutral,
    onClick: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        label = "statCardPress",
    )
    val hintColor = when (hintTone) {
        AppBadgeTone.Tint -> colors.primaryDark
        AppBadgeTone.Up -> colors.coral
        AppBadgeTone.Down -> colors.mint
        AppBadgeTone.Neutral -> colors.inkTertiary
    }
    val shape = RoundedCornerShape(AppTheme.shapes.radius)
    val press = when (onClick) {
        null -> Modifier
        else -> Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
    }
    Column(
        modifier = modifier
            .appShadow(shape)
            .clip(shape)
            .background(colors.paper)
            .then(press)
            .padding(14.dp),
    ) {
        Text(
            text = label,
            style = AppTheme.typography.subtitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = AppTheme.typography.statValue,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (hint == null) return@Column
        Spacer(Modifier.height(2.dp))
        Text(
            text = hint,
            style = AppTheme.typography.tiny,
            color = hintColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
