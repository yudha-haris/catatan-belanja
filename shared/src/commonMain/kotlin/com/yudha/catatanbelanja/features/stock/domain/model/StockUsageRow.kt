package com.yudha.catatanbelanja.features.stock.domain.model

/** One line of the log-detail sheet: what was bought, what was left, what that implies was used. */
data class StockUsageRow(
    val name: String,
    val emoji: String,
    val unit: String,
    val boughtQty: Double,
    val hasBought: Boolean,
    val remainingQty: Double,
    val isOut: Boolean,
    /** null when there is neither an earlier log nor a purchase to infer usage from. */
    val usedQty: Double?,
)
