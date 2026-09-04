package com.yudha.catatanbelanja.features.stock.domain.usecase

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.domain.model.RateMode
import com.yudha.catatanbelanja.core.domain.model.RatePeriod
import com.yudha.catatanbelanja.core.domain.model.StockRate

/**
 * The rate sheet's fields as a [StockRate]. AUTO and OFF clear the manual figure rather than
 * keeping it around invisibly — a number that is not in force should not be able to come back
 * without the user retyping it.
 */
class CreateStockRate(private val clock: Clock) {

    operator fun invoke(
        itemId: String,
        mode: RateMode,
        qtyText: String,
        unit: String,
        period: RatePeriod,
    ): StockRate {
        val now = clock.nowMillis()
        if (mode != RateMode.MANUAL) {
            return StockRate(itemId = itemId, mode = mode, updatedAt = now)
        }
        return StockRate(
            itemId = itemId,
            mode = RateMode.MANUAL,
            manualQty = qtyText.toQty(),
            manualUnit = unit,
            manualPeriod = period,
            updatedAt = now,
        )
    }

    /** The prototype accepts "1,5" as well as "1.5"; anything unreadable counts as none. */
    private fun String.toQty(): Double? = trim().replace(',', '.').toDoubleOrNull()
}
