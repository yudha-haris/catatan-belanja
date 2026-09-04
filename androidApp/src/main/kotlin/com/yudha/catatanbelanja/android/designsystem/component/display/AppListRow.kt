package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.designsystem.theme.appShadow

/**
 * The `.item` row: emoji badge, title + subtitle, trailing price with an optional delta line,
 * and an optional level bar under the title.
 */
@Composable
fun AppListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: String? = null,
    trailingSub: String? = null,
    trailingSubTone: AppBadgeTone = AppBadgeTone.Neutral,
    emoji: String? = null,
    leading: (@Composable () -> Unit)? = null,
    selected: Boolean = false,
    dense: Boolean = false,
    progress: Float? = null,
    progressIsLow: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(AppTheme.shapes.radiusItem)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        label = "listRowPress",
    )
    val trailingSubColor = when (trailingSubTone) {
        AppBadgeTone.Tint -> colors.primaryDark
        AppBadgeTone.Up -> colors.coral
        AppBadgeTone.Down -> colors.mint
        AppBadgeTone.Neutral -> colors.inkTertiary
    }
    val badgeSize = if (dense) 34.dp else 40.dp
    val badgeShape = RoundedCornerShape(if (dense) 11.dp else 14.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (dense) {
                    Modifier
                } else {
                    Modifier.appShadow(shape)
                },
            )
            .clip(shape)
            .background(colors.paper)
            .then(if (dense) Modifier.border(1.5.dp, colors.line, shape) else Modifier)
            .then(if (selected) Modifier.border(3.dp, colors.primary, shape) else Modifier)
            .then(
                if (onClick == null) {
                    Modifier
                } else {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                    )
                },
            )
            .padding(
                horizontal = if (dense) 12.dp else 14.dp,
                vertical = if (dense) 10.dp else 12.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
        }
        if (leading == null && emoji != null) {
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .clip(badgeShape)
                    .background(colors.tint),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, style = AppTheme.typography.emoji)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = AppTheme.typography.rowTitle,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (progress != null) {
                Spacer(Modifier.height(Spacing.x6))
                AppLevelBar(progress = progress, isLow = progressIsLow)
            }
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = AppTheme.typography.subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing == null) return@Row
        Spacer(Modifier.width(2.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = trailing,
                style = AppTheme.typography.price,
                color = colors.ink,
                maxLines = 1,
            )
            if (trailingSub == null) return@Column
            Text(
                text = trailingSub,
                style = AppTheme.typography.priceDelta,
                color = trailingSubColor,
                textAlign = TextAlign.End,
                maxLines = 1,
            )
        }
    }
}
