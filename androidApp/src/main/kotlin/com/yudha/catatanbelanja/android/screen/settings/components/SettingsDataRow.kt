package com.yudha.catatanbelanja.android.screen.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/**
 * The `.srow`: emoji tile, title over a muted subtitle, and a hairline under every row but the
 * last. [isDanger] is the prototype's coral treatment on "Hapus semua data".
 */
@Composable
internal fun SettingsDataRow(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDanger: Boolean = false,
    showDivider: Boolean = true,
) {
    val colors = AppTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val tileColor = if (isDanger) colors.coralBg else colors.tint
    val titleColor = when {
        !enabled -> colors.inkTertiary
        isDanger -> colors.coral
        else -> colors.ink
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick,
                )
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(tileColor),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, style = AppTheme.typography.emoji)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTheme.typography.rowTitle,
                    color = titleColor,
                )
                Text(
                    text = subtitle,
                    style = AppTheme.typography.subtitle,
                    color = colors.inkSecondary,
                )
            }
        }

        if (!showDivider) return@Column

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(colors.line),
        )
    }
}
