package com.yudha.catatanbelanja.features.dashboard.domain.model

import com.yudha.catatanbelanja.core.domain.model.PriceBasis

/**
 * Everything the Ringkasan tab renders, already derived. Money stays in rupiah as [Int] and dates
 * as epoch millis — the screen owns the formatting.
 *
 * The price trend is deliberately absent: it depends on the item's saved [PriceBasis] and its
 * manual quantity corrections, which are read from the database rather than derived from the
 * sessions. `DashboardState` carries it as a [PriceTrendData] built by the same use case the
 * trend page uses, so the card and the page can never disagree about what an item's price did.
 */
data class DashboardData(
    val monthKey: String = "",
    val monthTotal: Int = 0,
    val previousMonthTotal: Int = 0,
    /** Absolute percent gap against the previous calendar month; the arrow comes from the flags. */
    val monthDeltaPercent: Int = 0,
    val hasMonthComparison: Boolean = false,
    val isMonthSpendingUp: Boolean = false,
    val isMonthSpendingDown: Boolean = false,
    val monthSessionCount: Int = 0,
    val monthAverage: Int = 0,
    val hasMonthAverage: Boolean = false,
    val recentBars: List<SpendingBar> = emptyList(),
    val topItems: List<TopItem> = emptyList(),
)
