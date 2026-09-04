package com.yudha.catatanbelanja.android.screen.stock.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBadgeTone
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.format.toDayLabel
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.features.stock.domain.model.StockRowView

/**
 * One `.item` of the Stok list: level bar under the name, quantity and warning on the right.
 *
 * The trailing figure is always the stored quantity, never the estimate — the row states what is
 * on record and lets the bar's faded tail and the subtitle carry the guess.
 */
@Composable
internal fun StockRowItem(
    row: StockRowView,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = row.item
    val subtitle = when (row.subtitle) {
        StockRowView.Subtitle.REMINDER ->
            stringResource(R.string.stock_row_remind, item.minQty.toQtyLabel(), item.unit)
        StockRowView.Subtitle.UPDATED ->
            stringResource(R.string.stock_row_updated, item.updatedAt.toDayLabel())
        StockRowView.Subtitle.ESTIMATE ->
            stringResource(
                R.string.stock_row_estimate,
                row.shadow?.estimatedQty.toQtyLabel(),
                item.unit,
            )
    }
    val alert = when (row.alert) {
        StockRowView.Alert.NONE -> null
        StockRowView.Alert.RUNNING_LOW -> stringResource(R.string.stock_running_low)
        StockRowView.Alert.NEED_BUY -> stringResource(R.string.stock_need_buy)
        StockRowView.Alert.MAYBE_LOW -> stringResource(R.string.stock_maybe_low)
    }
    // A guess never gets to shout in the same colour a counted shortage does.
    val alertTone = when (row.alert) {
        StockRowView.Alert.RUNNING_LOW, StockRowView.Alert.NEED_BUY -> AppBadgeTone.Up
        StockRowView.Alert.NONE, StockRowView.Alert.MAYBE_LOW -> AppBadgeTone.Neutral
    }
    val quantity = when {
        row.isOut -> stringResource(R.string.stock_out)
        else -> stringResource(R.string.common_item_qty_unit, item.qty.toQtyLabel(), item.unit)
    }

    AppListRow(
        title = item.name,
        modifier = modifier,
        subtitle = subtitle,
        trailing = quantity,
        trailingSub = alert,
        trailingSubTone = alertTone,
        emoji = row.emoji,
        progress = row.ratio,
        progressIsLow = row.isLow,
        progressEstimate = row.shadow?.ratio,
        onClick = onClick,
    )
}
