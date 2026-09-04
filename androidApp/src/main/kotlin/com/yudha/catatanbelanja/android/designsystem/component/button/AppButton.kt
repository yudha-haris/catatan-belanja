package com.yudha.catatanbelanja.android.designsystem.component.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

enum class AppButtonVariant { Primary, Ghost, Danger, Soft, OnHero }

/** The `.btn` family. [big] is the tall CTA (`.btn.big`); [fillWidth] = false is `.btn.auto`. */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    enabled: Boolean = true,
    emoji: String? = null,
    icon: ImageVector? = null,
    big: Boolean = false,
    fillWidth: Boolean = true,
) {
    val colors = AppTheme.colors
    val container = when (variant) {
        AppButtonVariant.Primary -> colors.primary
        AppButtonVariant.Ghost -> colors.tint
        AppButtonVariant.Danger -> colors.coralBg
        AppButtonVariant.Soft -> colors.background
        AppButtonVariant.OnHero -> colors.paper
    }
    val onContainer = when (variant) {
        AppButtonVariant.Primary -> colors.paper
        AppButtonVariant.Ghost -> colors.primaryDark
        AppButtonVariant.Danger -> colors.coral
        AppButtonVariant.Soft -> colors.inkSecondary
        AppButtonVariant.OnHero -> colors.primaryDark
    }

    val containerColor = if (enabled) container else colors.line
    val contentColor = if (enabled) onContainer else colors.inkTertiary
    val lifted = enabled && variant == AppButtonVariant.Primary

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, label = "buttonPress")

    val shape = RoundedCornerShape(if (big) AppTheme.shapes.radiusItem else 16.dp)
    val padding = if (big) PaddingValues(17.dp) else PaddingValues(horizontal = 18.dp, vertical = 14.dp)

    Row(
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .scale(scale)
            .then(
                if (lifted) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = shape,
                        clip = false,
                        ambientColor = colors.primary,
                        spotColor = colors.primary,
                    )
                } else {
                    Modifier
                },
            )
            .clip(shape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(padding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x8, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (emoji != null) {
            Text(text = emoji, style = AppTheme.typography.rowTitle, color = contentColor)
        }
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = text,
            style = AppTheme.typography.rowTitle,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
