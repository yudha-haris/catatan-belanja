package com.yudha.catatanbelanja.features.dashboard.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.dashboard.domain.model.ReportRange
import com.yudha.catatanbelanja.features.dashboard.domain.model.SpendingReportData

data class SpendingReportState(
    val loadState: UiState<Unit> = UiState.Initial,
    val range: ReportRange = ReportRange.SIX_MONTHS,
    /** The chips this page offers — the shorter windows say nothing a single month chart cannot. */
    val rangeOptions: List<ReportRange> = listOf(
        ReportRange.THREE_MONTHS,
        ReportRange.SIX_MONTHS,
        ReportRange.ALL,
    ),
    val data: SpendingReportData = SpendingReportData(),
    val hasAnySession: Boolean = false,
)
