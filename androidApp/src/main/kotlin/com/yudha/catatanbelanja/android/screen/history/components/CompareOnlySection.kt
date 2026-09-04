package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBadge
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppSectionHeader
import com.yudha.catatanbelanja.android.format.toRupiahShort
import com.yudha.catatanbelanja.features.history.domain.model.CompareRow

/** "Hanya di A" / "Hanya di B" — what only one of the two trips carried, and what it cost. */
@Composable
internal fun CompareOnlySection(
    title: String,
    total: Int,
    rows: List<CompareRow>,
    modifier: Modifier = Modifier,
) {
    if (rows.isEmpty()) return

    Column(modifier = modifier) {
        AppSectionHeader(
            title = title,
            trailing = { AppBadge(text = total.toRupiahShort()) },
        )

        AppCard {
            rows.forEachIndexed { index, row ->
                CompareItemRow(row = row, showDivider = index < rows.lastIndex)
            }
        }
    }
}
