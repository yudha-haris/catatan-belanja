package com.yudha.catatanbelanja.features.dashboard.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.dashboard.domain.model.DashboardData
import com.yudha.catatanbelanja.features.dashboard.domain.model.DashboardScope
import com.yudha.catatanbelanja.features.dashboard.domain.model.PriceTrendData
import com.yudha.catatanbelanja.features.dashboard.domain.model.TrendCandidate

data class DashboardState(
    val loadState: UiState<Unit> = UiState.Initial,
    val actionState: UiState<Unit> = UiState.Initial,
    val data: DashboardData = DashboardData(),
    val scope: DashboardScope = DashboardScope.MONTH,
    val hasAnySession: Boolean = false,
    /** The trend card's picker: everything bought twice or more, most bought first. */
    val trendCandidates: List<TrendCandidate> = emptyList(),
    val trendNames: List<String> = emptyList(),
    /**
     * Built by the same use case the trend page uses, so the card honours a basis or a manual
     * quantity the user set over there instead of quietly plotting something else.
     */
    val trend: PriceTrendData = PriceTrendData(),
)
