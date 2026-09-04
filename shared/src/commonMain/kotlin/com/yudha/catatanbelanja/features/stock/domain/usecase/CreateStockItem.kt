package com.yudha.catatanbelanja.features.stock.domain.usecase

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.IdGenerator
import com.yudha.catatanbelanja.core.common.capitalizeWords
import com.yudha.catatanbelanja.core.domain.model.StockItem

/**
 * The prototype's `saveStock()` item shape. [target] is the row being edited — it keeps its id
 * and its high-water mark, so the level bar never shrinks its own scale.
 */
class CreateStockItem(
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) {
    operator fun invoke(
        name: String,
        qtyText: String,
        unit: String,
        minText: String,
        target: StockItem?,
    ): StockItem {
        val qty = qtyText.toQty() ?: 0.0
        return StockItem(
            id = target?.id ?: idGenerator.next(),
            name = name.trim().capitalizeWords(),
            qty = qty,
            unit = unit,
            minQty = minText.toQty(),
            fullQty = maxOf(target?.fullQty ?: 0.0, qty),
            updatedAt = clock.nowMillis(),
        )
    }

    /** "Habis": the shelf is empty as of now, everything else about the row stays. */
    fun markedEmpty(item: StockItem): StockItem =
        item.copy(qty = 0.0, updatedAt = clock.nowMillis())

    /** The prototype accepts "1,5" as well as "1.5"; anything unreadable counts as none. */
    private fun String.toQty(): Double? = trim().replace(',', '.').toDoubleOrNull()
}
