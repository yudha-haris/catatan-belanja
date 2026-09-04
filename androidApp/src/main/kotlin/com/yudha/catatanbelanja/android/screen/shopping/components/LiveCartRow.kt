package com.yudha.catatanbelanja.android.screen.shopping.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.yudha.catatanbelanja.R
import com.yudha.catatanbelanja.android.designsystem.component.display.AppListRow
import com.yudha.catatanbelanja.android.format.toQtyLabel
import com.yudha.catatanbelanja.android.format.toRupiah
import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.features.shopping.domain.model.ShoppingItemView

/**
 * One cart row. The prototype only draws the ▲/▼ comparison in the detail view, so the live
 * cart shows quantity, unit price and brand and nothing else.
 */
@Composable
internal fun LiveCartRow(
    view: ShoppingItemView,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val separator = stringResource(R.string.common_separator_dot)
    val unitPrice = view.unitPrice
    val qtyPart = when (view.qty) {
        null -> ""
        else -> stringResource(
            R.string.common_item_qty_unit,
            view.qty.toQtyLabel(),
            view.unit.orEmpty(),
        )
    }
    val unitPricePart = when (unitPrice) {
        null -> ""
        else -> stringResource(
            R.string.common_item_unit_price,
            unitPrice.toRupiah(),
            view.unit ?: CatalogData.units.first(),
        )
    }
    val notePart = when (view.note.isBlank()) {
        true -> ""
        false -> "🏷 ${view.note}"
    }
    val subtitle = listOf(qtyPart, unitPricePart, notePart)
        .filter { it.isNotBlank() }
        .joinToString(" $separator ")

    AppListRow(
        title = view.item.name,
        modifier = modifier,
        subtitle = subtitle.ifBlank { null },
        trailing = view.item.price.toRupiah(),
        emoji = view.emoji,
        onClick = onClick,
    )
}
