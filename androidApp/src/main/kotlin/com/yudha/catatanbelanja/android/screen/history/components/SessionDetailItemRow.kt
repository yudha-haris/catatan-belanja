package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBadgeTone
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.features.history.domain.model.SessionItemRow

private const val ARROW_UP = "▲"
private const val ARROW_DOWN = "▼"
private const val NOTE_EMOJI = "🏷"

/** One bought item; the trailing arrow is the price move against the previous session. */
@Composable
internal fun SessionDetailItemRow(
    row: SessionItemRow,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = row.item
    val unit = item.unit ?: CatalogData.units.first()
    val separator = " ${stringResource(R.string.common_separator_dot)} "

    val qtyPart = item.qty?.let { stringResource(R.string.common_item_qty_unit, it.toQtyLabel(), unit) }
    val unitPricePart = row.unitPrice
        ?.let { stringResource(R.string.common_item_unit_price, it.toRupiah(), unit) }
    val notePart = item.note.takeIf { it.isNotBlank() }?.let { "$NOTE_EMOJI $it" }
    val subtitle = listOfNotNull(qtyPart, unitPricePart, notePart).joinToString(separator)

    val deltaArrow = when (row.isPriceUp) {
        true -> ARROW_UP
        false -> ARROW_DOWN
    }

    AppListRow(
        title = item.name,
        modifier = modifier,
        subtitle = subtitle.ifBlank { null },
        trailing = item.price.toRupiah(),
        trailingSub = row.priceDeltaAmount?.let { "$deltaArrow ${it.toRupiah()}" },
        trailingSubTone = when (row.isPriceUp) {
            true -> AppBadgeTone.Up
            false -> AppBadgeTone.Down
        },
        emoji = row.emoji,
        onClick = onClick,
    )
}
