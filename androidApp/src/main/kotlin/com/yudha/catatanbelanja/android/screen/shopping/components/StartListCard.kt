package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing

/**
 * The plan, on the home screen. A fixed slot whether or not a list exists: with none it offers
 * to start one, so the screen does not reshuffle itself the first time the user makes a list.
 */
@Composable
internal fun StartListCard(
    hasList: Boolean,
    remainingCount: Int,
    previewNames: List<String>,
    extraCount: Int,
    onOpenList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val separator = stringResource(R.string.common_separator_dot)
    val preview = previewNames.joinToString(" $separator ")
    val previewLine = when (extraCount) {
        0 -> preview
        else -> stringResource(R.string.home_list_preview_more, preview, extraCount)
    }

    AppCard(modifier = modifier, onClick = if (hasList) onOpenList else null) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.x12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(AppTheme.shapes.radiusSmall))
                    .background(colors.tint),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "📝", style = AppTheme.typography.emoji)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (hasList) {
                        true -> stringResource(R.string.home_list_title)
                        false -> stringResource(R.string.home_list_empty_title)
                    },
                    style = AppTheme.typography.rowTitle,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = when {
                        !hasList -> stringResource(R.string.home_list_empty_message)
                        remainingCount == 0 -> stringResource(R.string.home_list_done)
                        else -> pluralStringResource(
                            R.plurals.home_list_remaining,
                            remainingCount,
                            remainingCount,
                        )
                    },
                    style = AppTheme.typography.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (!hasList) return@Row

            AppChip(
                text = stringResource(R.string.home_list_open),
                onClick = onOpenList,
                variant = AppChipVariant.Plain,
            )
        }

        if (!hasList) {
            Spacer(Modifier.height(Spacing.x12))
            AppButton(
                text = stringResource(R.string.home_list_empty_action),
                onClick = onOpenList,
                variant = AppButtonVariant.Ghost,
                emoji = "📝",
            )
            return@AppCard
        }

        if (previewLine.isEmpty()) return@AppCard

        Spacer(Modifier.height(Spacing.x10))
        Text(
            text = previewLine,
            style = AppTheme.typography.tiny,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
