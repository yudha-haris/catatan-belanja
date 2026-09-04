package com.yudha.catatanbelanja.features.dashboard.domain.model

/**
 * Everything the Ringkasan tab renders, already derived. Money stays in rupiah as [Int] and dates
 * as epoch millis — the screen owns the formatting.
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
    /** Names bought at least twice — the trend picker's options, most bought first. */
    val trendableNames: List<String> = emptyList(),
    val trendName: String? = null,
    val trendPoints: List<TrendPoint> = emptyList(),
    val hasTrend: Boolean = false,
    val trendFirstPrice: Int = 0,
    val trendLastPrice: Int = 0,
    /** Signed: the first→last change, so the screen prints "+12%" or "-8%" straight from it. */
    val trendDeltaPercent: Int = 0,
    val isTrendUp: Boolean = false,
    val isTrendDown: Boolean = false,
)
