package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBadge
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppSectionHeader
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.monthKeyToLabel
import com.yudha.catatanbelanja.android.format.toRupiahShort
import com.yudha.catatanbelanja.features.history.presentation.HistoryState

/** The optional pick hint, then one section per month — `riwayatView()` under its pinned header. */
@Composable
internal fun HistorySessionList(
    state: HistoryState,
    onSessionClicked: (String) -> Unit,
    onQuickCompare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = AppTheme.shapes.listPadding,
    ) {
        if (state.compareMode) {
            item(key = "compare-hint") {
                HistoryCompareHint(
                    pickedCount = state.pickedCount,
                    canQuickCompare = state.canQuickCompare,
                    onQuickCompare = onQuickCompare,
                )
            }
        }

        state.groups.forEach { group ->
            item(key = "month-${group.monthKey}") {
                AppSectionHeader(
                    title = group.monthKey.monthKeyToLabel(),
                    trailing = { AppBadge(text = group.total.toRupiahShort()) },
                )
            }

            items(items = group.summaries, key = { it.summary.session.id }) { view ->
                HistorySessionRow(
                    view = view,
                    compareMode = state.compareMode,
                    picked = state.pickedIds.contains(view.summary.session.id),
                    onClick = { onSessionClicked(view.summary.session.id) },
                    modifier = Modifier.padding(bottom = Spacing.x8),
                )
            }
        }
    }
}
