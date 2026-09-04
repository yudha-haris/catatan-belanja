package com.yudha.catatanbelanja.features.dashboard.domain.model

import com.yudha.catatanbelanja.core.domain.model.PriceBasis

/**
 * Everything the "Tren harga" page renders for one item.
 *
 * [points] runs oldest first (the chart reads left to right) and holds only the usable purchases;
 * [purchases] runs newest first and holds every one of them, because the unusable ones are exactly
 * what the page exists to let the user fix.
 */
data class PriceTrendData(
    val name: String = "",
    val emoji: String = "",
    val basis: PriceBasis = PriceBasis.RAW,
    val baseUnit: String = "",
    val baseUnitOptions: List<String> = emptyList(),
    val canUsePerUnit: Boolean = false,
    val points: List<TrendPoint> = emptyList(),
    val purchases: List<TrendPurchase> = emptyList(),
    val hasTrend: Boolean = false,
    val usableCount: Int = 0,
    val skippedCount: Int = 0,
    val firstValue: Int = 0,
    val lastValue: Int = 0,
    val deltaPercent: Int = 0,
    val isUp: Boolean = false,
    val isDown: Boolean = false,
    val cheapest: Int = 0,
    val dearest: Int = 0,
    val average: Int = 0,
)
