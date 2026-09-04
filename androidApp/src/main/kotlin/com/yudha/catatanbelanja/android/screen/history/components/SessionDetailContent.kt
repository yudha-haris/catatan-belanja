package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppSectionHeader
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.core.domain.model.SessionSummary
import com.yudha.catatanbelanja.features.history.domain.model.SessionItemRow
import com.yudha.catatanbelanja.features.history.presentation.SessionDetailState

/** Receipt, the two shortcuts, the item list and the destructive footer — `detailView()` under
 * its pinned header. */
@Composable
internal fun SessionDetailContent(
    summary: SessionSummary,
    state: SessionDetailState,
    onOpenComparePicker: () -> Unit,
    onRepeatSession: () -> Unit,
    onItemClicked: (SessionItemRow) -> Unit,
    onDeleteSession: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = AppTheme.shapes.listPadding,
    ) {
        item(key = "receipt") {
            SessionDetailReceipt(summary = summary, state = state)
        }

        item(key = "actions") {
            SessionDetailActions(
                canCompare = state.canCompare,
                onOpenComparePicker = onOpenComparePicker,
                onRepeatSession = onRepeatSession,
            )
        }

        item(key = "items-header") {
            AppSectionHeader(
                title = stringResource(R.string.detail_items_title),
                trailing = {
                    Text(
                        text = when (state.hasPrevious) {
                            true -> stringResource(R.string.detail_items_hint_diff)
                            false -> stringResource(R.string.detail_items_hint_edit)
                        },
                        style = AppTheme.typography.tiny,
                        color = AppTheme.colors.inkTertiary,
                    )
                },
            )
        }

        items(items = state.itemRows, key = { it.item.id }) { row ->
            SessionDetailItemRow(
                row = row,
                onClick = { onItemClicked(row) },
                modifier = Modifier.padding(bottom = Spacing.x8),
            )
        }

        item(key = "delete") {
            Spacer(Modifier.height(Spacing.x24))
            AppButton(
                text = stringResource(R.string.detail_delete_button),
                onClick = onDeleteSession,
                variant = AppButtonVariant.Danger,
            )
        }
    }
}
