package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppSectionHeader
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.features.history.domain.model.CompareRow

/** "Ada di keduanya", already sorted by the biggest swing. */
@Composable
internal fun CompareBothSection(
    rows: List<CompareRow>,
    modifier: Modifier = Modifier,
) {
    if (rows.isEmpty()) return

    Column(modifier = modifier) {
        AppSectionHeader(
            title = stringResource(R.string.compare_in_both),
            trailing = {
                Text(
                    text = stringResource(R.string.compare_in_both_hint),
                    style = AppTheme.typography.tiny,
                    color = AppTheme.colors.inkTertiary,
                )
            },
        )

        AppCard {
            rows.forEachIndexed { index, row ->
                CompareItemRow(row = row, showDivider = index < rows.lastIndex)
            }
        }
    }
}
