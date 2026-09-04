package com.yudha.catatanbelanja.features.dashboard.domain.model

/** Everything the "Pengeluaran terbesar" page renders, for the chosen range and mode. */
data class SpendingRankingData(
    val range: ReportRange = ReportRange.MONTH,
    val mode: RankingMode = RankingMode.ITEM,
    val total: Int = 0,
    val entryCount: Int = 0,
    val tripCount: Int = 0,
    val entries: List<RankedEntry> = emptyList(),
    val slices: List<ShareSlice> = emptyList(),
    val leaderLabel: String = "",
    val leaderPercent: Int = 0,
    val hasEntries: Boolean = false,
)
