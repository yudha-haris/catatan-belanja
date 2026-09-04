package com.yudha.catatanbelanja.android.screen.stock.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButton
import com.yudha.catatanbelanja.android.designsystem.component.button.AppButtonVariant
import com.yudha.catatanbelanja.android.designsystem.component.feedback.AppBottomSheet
import com.yudha.catatanbelanja.android.designsystem.component.layout.AppCard
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme
import com.yudha.catatanbelanja.android.designsystem.theme.Spacing
import com.yudha.catatanbelanja.android.format.monthKeyToLabel
import com.yudha.catatanbelanja.android.format.toLongDateLabel
import com.yudha.catatanbelanja.features.stock.domain.model.StockCheckLogView
import com.yudha.catatanbelanja.features.stock.domain.model.StockUsageRow

/** Same cap as the entry sheet, so "Hapus catatan ini" stays in reach on a long check. */
private const val LIST_HEIGHT_FRACTION = 0.52f

/** The detail of one month-end check: beli / sisa / pakai per item, and the delete action. */
@Composable
internal fun StockLogSheet(
    view: StockCheckLogView,
    usageRows: List<StockUsageRow>,
    previousMonth: String?,
    enabled: Boolean,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val checkedAt = view.log.checkedAt.toLongDateLabel()
    val listMaxHeight = LocalConfiguration.current.screenHeightDp.dp * LIST_HEIGHT_FRACTION

    AppBottomSheet(onDismiss = onDismiss, modifier = modifier) {
        Text(
            text = stringResource(
                R.string.stock_log_sheet_title,
                view.log.month.monthKeyToLabel(),
            ),
            style = AppTheme.typography.sheetTitle,
            color = AppTheme.colors.ink,
        )
        Spacer(Modifier.height(Spacing.x8))
        Text(
            text = when (previousMonth) {
                null -> stringResource(R.string.stock_log_sheet_checked, checkedAt)
                else -> stringResource(
                    R.string.stock_log_sheet_checked_with_previous,
                    checkedAt,
                    previousMonth.monthKeyToLabel(),
                )
            },
            style = AppTheme.typography.muted,
            color = AppTheme.colors.inkSecondary,
        )
        Spacer(Modifier.height(Spacing.x12))

        AppCard(flat = true, contentPadding = PaddingValues(horizontal = 14.dp)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = listMaxHeight),
            ) {
                itemsIndexed(usageRows, key = { _, row -> "${row.name}|${row.unit}" }) { index, row ->
                    StockUsageLine(row = row, showDivider = index < usageRows.lastIndex)
                }
            }
        }

        Spacer(Modifier.height(Spacing.x12))
        AppButton(
            text = stringResource(R.string.stock_log_delete),
            onClick = onDelete,
            variant = AppButtonVariant.Danger,
            enabled = enabled,
        )
    }
}
