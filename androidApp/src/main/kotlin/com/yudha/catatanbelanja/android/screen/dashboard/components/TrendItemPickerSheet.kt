package com.yudha.catatanbelanja.android.screen.dashboard.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.input.AppSearchField
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.toDayLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.features.dashboard.domain.model.TrendCandidate

/**
 * Picks which item the page is about. A dropdown was enough for the summary card's handful; a
 * household with two years of receipts has hundreds of names, which is a search field's job.
 */
@Composable
internal fun TrendItemPickerSheet(
    candidates: List<TrendCandidate>,
    query: String,
    onQueryChanged: (String) -> Unit,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(R.string.trend_picker_title),
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x14))
        AppSearchField(
            value = query,
            onValueChange = onQueryChanged,
            onClear = { onQueryChanged("") },
            placeholder = stringResource(R.string.trend_picker_search),
        )
        Spacer(Modifier.height(Spacing.x12))

        if (candidates.isEmpty()) {
            Text(
                text = stringResource(R.string.trend_picker_empty),
                style = AppTheme.typography.muted,
                color = AppTheme.colors.inkSecondary,
            )
            return@AppBottomSheet
        }

        candidates.forEach { candidate ->
            AppListRow(
                title = candidate.name,
                subtitle = stringResource(
                    R.string.trend_candidate_hint,
                    candidate.purchaseCount,
                    candidate.lastBoughtAt.toDayLabel(),
                ),
                trailing = candidate.lastPrice.toRupiah(),
                emoji = candidate.emoji,
                dense = true,
                onClick = { onSelect(candidate.name) },
            )
            Spacer(Modifier.height(Spacing.x6))
        }
    }
}
