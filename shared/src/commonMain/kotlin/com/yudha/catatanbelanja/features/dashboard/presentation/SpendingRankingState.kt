package com.yudha.catatanbelanja.features.dashboard.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.dashboard.domain.model.RankingMode
import com.yudha.catatanbelanja.features.dashboard.domain.model.ReportRange
import com.yudha.catatanbelanja.features.dashboard.domain.model.SpendingRankingData

data class SpendingRankingState(
    val loadState: UiState<Unit> = UiState.Initial,
    val range: ReportRange = ReportRange.MONTH,
    /** This page ranks, so it starts where the summary card left off: the current month. */
    val rangeOptions: List<ReportRange> = listOf(
        ReportRange.MONTH,
        ReportRange.THREE_MONTHS,
        ReportRange.ALL,
    ),
    val mode: RankingMode = RankingMode.ITEM,
    val data: SpendingRankingData = SpendingRankingData(),
    val hasAnySession: Boolean = false,
)
