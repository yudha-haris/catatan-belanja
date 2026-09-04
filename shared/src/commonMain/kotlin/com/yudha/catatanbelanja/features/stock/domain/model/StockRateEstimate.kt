package com.yudha.catatanbelanja.features.stock.domain.model

/**
 * A drain rate inferred from an item's own readings, quoted in that item's current unit per day.
 * [windowCount] and [observedDays] are the workings shown to the user, so "±30 gram/hari" can be
 * backed by "dari 4 pembaruan" instead of arriving as an unexplained number.
 */
data class StockRateEstimate(
    val perDayQty: Double,
    val unit: String,
    val confidence: RateConfidence,
    val windowCount: Int,
    val observedDays: Int,
)
