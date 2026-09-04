package com.yudha.catatanbelanja.features.dashboard.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.dashboard.domain.model.DashboardData
import com.yudha.catatanbelanja.features.dashboard.domain.model.DashboardScope

data class DashboardState(
    val loadState: UiState<Unit> = UiState.Initial,
    val actionState: UiState<Unit> = UiState.Initial,
    val data: DashboardData = DashboardData(),
    val scope: DashboardScope = DashboardScope.MONTH,
    val hasAnySession: Boolean = false,
)
