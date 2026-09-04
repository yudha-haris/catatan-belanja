package com.yudha.catatanbelanja.android.screen.history.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppBadgeTone
import com.yudha.catatanbelanja.android.designsystem.component.display.AppCompareRow
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.features.history.domain.model.CompareRow

private const val ARROW_UP = "▲"
private const val ARROW_DOWN = "▼"

/**
 * One line of the A/B grid. A side the item is missing from prints the dash, and the delta
 * column only appears when the item exists on both sides.
 */
@Composable
internal fun CompareItemRow(
    row: CompareRow,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    val fallbackUnit = CatalogData.units.first()
    val dash = stringResource(R.string.common_empty_value)
    val amount = row.deltaAmount?.toRupiah().orEmpty()
    val deltaLabel = when (row.delta) {
        CompareRow.Delta.NONE -> null
        CompareRow.Delta.SAME -> stringResource(R.string.compare_same)
        CompareRow.Delta.UP -> "$ARROW_UP $amount"
        CompareRow.Delta.DOWN -> "$ARROW_DOWN $amount"
    }
    val deltaTone = when (row.delta) {
        CompareRow.Delta.NONE, CompareRow.Delta.SAME -> AppBadgeTone.Neutral
        CompareRow.Delta.UP -> AppBadgeTone.Up
        CompareRow.Delta.DOWN -> AppBadgeTone.Down
    }

    AppCompareRow(
        title = row.name,
        emoji = row.emoji,
        leftLabel = row.priceA?.toRupiah() ?: dash,
        modifier = modifier,
        leftSub = row.qtyA?.let {
            stringResource(R.string.common_item_qty_unit, it.toQtyLabel(), row.unitA ?: fallbackUnit)
        },
        rightLabel = row.priceB?.toRupiah() ?: dash,
        rightSub = row.qtyB?.let {
            stringResource(R.string.common_item_qty_unit, it.toQtyLabel(), row.unitB ?: fallbackUnit)
        },
        deltaLabel = deltaLabel,
        deltaTone = deltaTone,
        showDivider = showDivider,
    )
}
