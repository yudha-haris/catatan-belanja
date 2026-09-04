package com.yudha.catatanbelanja.features.stock.domain.usecase

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.toMonthKey
import com.yudha.catatanbelanja.features.stock.domain.model.StockCheckStamp

/** Stamps a fresh month-end check — a use case, so no ViewModel has to hold a [Clock]. */
class CurrentStockCheckStamp(private val clock: Clock) {
    operator fun invoke(): StockCheckStamp {
        val now = clock.nowMillis()
        return StockCheckStamp(checkedAtMillis = now, month = now.toMonthKey())
    }
}
