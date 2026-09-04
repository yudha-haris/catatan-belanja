package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.component.display.AppLevelBar
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.core.domain.model.NameChipView

/**
 * The plan, inside the trip. Every chip is the same `pickName()` tap the suggestion chips already
 * were, so following a list costs the user nothing extra — the difference is that these chips
 * disappear as they are bought, and the bar fills behind them.
 *
 * It sits *below* the add form, and shows [preview] rather than [remaining] until asked: a
 * twenty-item plan is a wall of chips, and a wall of chips must never be what stands between the
 * user and the field they came here to type in.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LiveListStrip(
    remaining: List<NameChipView>,
    preview: List<NameChipView>,
    hiddenCount: Int,
    checkedCount: Int,
    totalCount: Int,
    progress: Float,
    isComplete: Boolean,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onPickName: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The card gives a little kick on the tap that empties the list.
    val pop by animateFloatAsState(
        targetValue = if (isComplete) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.35f, stiffness = 500f),
        label = "liveListPop",
    )
    val shown = when (isExpanded) {
        true -> remaining
        false -> preview
    }

    AppCard(modifier = modifier.scale(pop)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "📝 " + stringResource(R.string.live_list_title),
                style = AppTheme.typography.sectionTitle,
                color = AppTheme.colors.ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.live_list_progress, checkedCount, totalCount),
                style = AppTheme.typography.price,
                color = AppTheme.colors.primaryDark,
            )
        }

        Spacer(Modifier.height(Spacing.x10))
        AppLevelBar(progress = progress)
        Spacer(Modifier.height(Spacing.x12))

        if (isComplete) {
            Text(
                text = stringResource(R.string.live_list_done),
                style = AppTheme.typography.muted,
            )
            return@AppCard
        }

        Text(text = stringResource(R.string.live_list_hint), style = AppTheme.typography.fieldLabel)
        Spacer(Modifier.height(Spacing.x8))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
            verticalArrangement = Arrangement.spacedBy(Spacing.x8),
        ) {
            shown.forEach { chip ->
                AppChip(text = chip.name, onClick = { onPickName(chip.name) }, emoji = chip.emoji)
            }

            if (hiddenCount == 0) return@FlowRow

            AppChip(
                text = when (isExpanded) {
                    true -> stringResource(R.string.live_list_less)
                    false -> stringResource(R.string.live_list_more, hiddenCount)
                },
                onClick = onToggleExpanded,
                variant = AppChipVariant.Plain,
            )
        }
    }
}
