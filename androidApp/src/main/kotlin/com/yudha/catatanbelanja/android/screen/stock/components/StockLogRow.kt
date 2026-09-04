package com.yudha.catatanbelanja.android.screen.stock.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.format.monthKeyToLabel
import com.yudha.catatanbelanja.android.format.toDayLabel
import com.yudha.catatanbelanja.features.stock.domain.model.StockCheckLogView

/** A "Riwayat cek stok" entry: the month, when it was taken, and how much of it was empty. */
@Composable
internal fun StockLogRow(
    view: StockCheckLogView,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppListRow(
        title = view.log.month.monthKeyToLabel(),
        modifier = modifier,
        subtitle = pluralStringResource(
            R.plurals.stock_log_subtitle,
            view.itemCount,
            view.log.checkedAt.toDayLabel(),
            view.itemCount,
            view.outCount,
        ),
        trailing = "›",
        emoji = "🗓️",
        onClick = onClick,
    )
}
