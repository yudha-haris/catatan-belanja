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
import com.yudha.catatanbelanja.features.dashboard.domain.model.ReportRange

/** The window picker both report pages carry. Which windows are offered comes from the state. */
@Composable
internal fun ReportRangeChips(
    options: List<ReportRange>,
    selected: ReportRange,
    onSelect: (ReportRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.x8),
    ) {
        options.forEach { range ->
            val label = when (range) {
                ReportRange.MONTH -> stringResource(R.string.report_range_month)
                ReportRange.THREE_MONTHS -> stringResource(R.string.report_range_three_months)
                ReportRange.SIX_MONTHS -> stringResource(R.string.report_range_six_months)
                ReportRange.ALL -> stringResource(R.string.report_range_all)
            }
            AppChip(
                text = label,
                onClick = { onSelect(range) },
                selected = range == selected,
                variant = AppChipVariant.Plain,
            )
        }
    }
}
