package com.yudha.catatanbelanja.features.dashboard.domain.model

/**
 * One row of the ranking. [trendName] is the item name to open the price trend with; a category
 * row has none, which is what [canOpenTrend] says.
 *
 * [isOther] marks the catch-all category row. Its [label] is deliberately blank: "lain-lain" is
 * copy, and copy is resolved in the composable.
 */
data class RankedEntry(
    val key: String,
    val label: String,
    val emoji: String,
    val total: Int,
    val purchaseCount: Int,
    val averagePrice: Int,
    val ratio: Float,
    val sharePercent: Int,
    val trendName: String,
    val canOpenTrend: Boolean,
    val isOther: Boolean = false,
)
