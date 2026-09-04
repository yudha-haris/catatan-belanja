package com.yudha.catatanbelanja.android.screen.receipt.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.features.receipt.domain.model.ScannedItemRow

private const val NOTE_EMOJI = "🏷"

/** One line read off the receipt, tappable so a misread name or price can be corrected. */
@Composable
internal fun ScanReceiptItemRow(
    row: ScannedItemRow,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = row.item
    val unit = item.unit ?: CatalogData.units.first()
    val separator = " ${stringResource(R.string.common_separator_dot)} "

    val qtyPart = item.qty
        ?.let { stringResource(R.string.common_item_qty_unit, it.toQtyLabel(), unit) }
    val notePart = item.note.takeIf { it.isNotBlank() }?.let { "$NOTE_EMOJI $it" }
    val subtitle = listOfNotNull(qtyPart, notePart).joinToString(separator)

    AppListRow(
        title = item.name,
        modifier = modifier,
        subtitle = subtitle.ifBlank { null },
        trailing = item.price.toRupiah(),
        emoji = row.emoji,
        onClick = onClick,
    )
}
