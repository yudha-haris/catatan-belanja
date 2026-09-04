package com.yudha.catatanbelanja.features.stock.domain.model

/** When a month-end check is being taken, and the "YYYY-MM" it is filed under. */
data class StockCheckStamp(
    val checkedAtMillis: Long,
    val month: String,
)
