package com.yudha.catatanbelanja.features.shopping.domain.usecase

import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import com.yudha.catatanbelanja.features.shopping.domain.model.ShoppingItemView
import kotlin.math.roundToInt

private const val SINGLE_QTY = 1.0

/** The prototype's `itemRow()` subtitle parts and its ▲/▼ price comparison. */
class BuildSessionItemViews(private val findItemCategory: FindItemCategory) {
    /**
     * [previousItems] are the same-session-before items the detail screen compares against;
     * the live cart passes none.
     */
    operator fun invoke(
        items: List<ShoppingItem>,
        previousItems: List<ShoppingItem> = emptyList(),
    ): List<ShoppingItemView> {
        val previousPrices = previousItems.associate { it.name.normalized() to it.price }

        return items.map { item ->
            val qty = item.qty?.takeIf { it > 0.0 }
            ShoppingItemView(
                item = item,
                emoji = findItemCategory.emojiFor(item.name),
                qty = qty,
                unit = item.unit.takeIf { qty != null },
                unitPrice = qty?.takeIf { it != SINGLE_QTY }?.let { (item.price / it).roundToInt() },
                note = item.note,
                deltaFromPrevious = previousPrices[item.name.normalized()]
                    ?.let { item.price - it }
                    ?.takeIf { it != 0 },
            )
        }
    }
}
