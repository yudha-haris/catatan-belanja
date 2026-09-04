package com.yudha.catatanbelanja.android.designsystem.component.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

enum class AppChipVariant { Tint, Plain, Dark, Danger, OnHero }

/** The `.chip` pill. [selected] is `.chip.on` — it wins over [variant]'s resting colours. */
@Composable
fun AppChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    selected: Boolean = false,
    variant: AppChipVariant = AppChipVariant.Tint,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val restingContainer = when (variant) {
        AppChipVariant.Tint -> colors.tint
        AppChipVariant.Plain -> colors.background
        AppChipVariant.Dark -> colors.ink
        AppChipVariant.Danger -> colors.coralBg
        AppChipVariant.OnHero -> colors.paper.copy(alpha = 0.2f)
    }
    val restingContent = when (variant) {
        AppChipVariant.Tint -> colors.primaryDark
        AppChipVariant.Plain -> colors.inkSecondary
        AppChipVariant.Dark -> colors.paper
        AppChipVariant.Danger -> colors.coral
        AppChipVariant.OnHero -> colors.paper
    }
    val selectedContainer = when (variant) {
        AppChipVariant.OnHero -> colors.paper
        AppChipVariant.Tint, AppChipVariant.Plain, AppChipVariant.Dark, AppChipVariant.Danger -> colors.primary
    }
    val selectedContent = when (variant) {
        AppChipVariant.OnHero -> colors.primaryDark
        AppChipVariant.Tint, AppChipVariant.Plain, AppChipVariant.Dark, AppChipVariant.Danger -> colors.paper
    }

    val container = if (selected) selectedContainer else restingContainer
    val content = if (selected) selectedContent else restingContent
    val contentColor = if (enabled) content else colors.inkTertiary

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.95f else 1f, label = "chipPress")

    val shape = RoundedCornerShape(AppTheme.shapes.pill)

    Row(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(if (enabled) container else colors.line)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x6),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (emoji != null) {
            Text(text = emoji, style = AppTheme.typography.label, color = contentColor)
        }
        Text(
            text = text,
            style = AppTheme.typography.label,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
