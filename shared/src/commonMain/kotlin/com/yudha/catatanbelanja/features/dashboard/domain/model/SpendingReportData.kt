package com.yudha.catatanbelanja.features.dashboard.domain.model

/**
 * Everything the "Laporan belanja" page renders.
 *
 * [months] and [trips] both run newest first, the way the history tab is read. [monthBars] is the
 * chart's own slice of [months] — oldest first, and short, because a bar is about 30dp wide and a
 * year of them is a smear rather than a chart.
 */
data class SpendingReportData(
    val range: ReportRange = ReportRange.SIX_MONTHS,
    val total: Int = 0,
    val tripCount: Int = 0,
    val tripAverage: Int = 0,
    val monthCount: Int = 0,
    val monthAverage: Int = 0,
    val highestTotal: Int = 0,
    val highestSessionId: String = "",
    val hasHighest: Boolean = false,
    val months: List<MonthSpending> = emptyList(),
    val monthBars: List<MonthSpending> = emptyList(),
    val trips: List<TripSpending> = emptyList(),
    val hasAnyTrip: Boolean = false,
)
