package com.yudha.catatanbelanja.core.domain.model

/**
 * A quantity typed in after the trip, for one purchased item. It never touches the receipt — the
 * session keeps exactly what was logged in the shop; only the price trend reads this.
 */
data class QtyOverride(
    val itemId: String,
    val nameKey: String,
    val qty: Double,
    val unit: String,
)
