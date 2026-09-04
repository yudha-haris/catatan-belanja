package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChip
import com.yudha.catatanbelanja.android.designsystem.component.button.AppChipVariant
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.features.dashboard.domain.model.RankingMode

/** Item or category. Five kinds of vegetable are five small rows one way and one big row the other. */
@Composable
internal fun RankingModeChips(
    selected: RankingMode,
    onSelect: (RankingMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
    ) {
        RankingMode.entries.forEach { mode ->
            val label = when (mode) {
                RankingMode.ITEM -> stringResource(R.string.ranking_mode_item)
                RankingMode.CATEGORY -> stringResource(R.string.ranking_mode_category)
            }
            AppChip(
                text = label,
                onClick = { onSelect(mode) },
                selected = mode == selected,
                variant = AppChipVariant.Tint,
            )
        }
    }
}
