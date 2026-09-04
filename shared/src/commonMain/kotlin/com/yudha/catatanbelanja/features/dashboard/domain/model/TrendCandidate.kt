package com.yudha.catatanbelanja.features.dashboard.domain.model

/**
 * One item the price trend can be drawn for — bought at least twice, since a single purchase is a
 * dot, not a trend. [lastPrice] is what the most recent trip paid, so the picker is readable
 * without opening anything.
 */
data class TrendCandidate(
    val name: String,
    val emoji: String,
    val purchaseCount: Int,
    val lastPrice: Int,
    val lastBoughtAt: Long,
)
