package com.yudha.catatanbelanja.features.dashboard.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.dashboard.domain.model.PriceTrendData
import com.yudha.catatanbelanja.features.dashboard.domain.model.TrendCandidate
import com.yudha.catatanbelanja.features.dashboard.domain.model.TrendPurchase

data class PriceTrendState(
    val loadState: UiState<Unit> = UiState.Initial,
    val actionState: UiState<Unit> = UiState.Initial,
    val candidates: List<TrendCandidate> = emptyList(),
    val query: String = "",
    val visibleCandidates: List<TrendCandidate> = emptyList(),
    val isPickerOpen: Boolean = false,
    val data: PriceTrendData = PriceTrendData(),
    val hasAnyCandidate: Boolean = false,
    /** The purchase whose quantity sheet is open. Null closes it — the sheet is not UI-local state. */
    val editing: TrendPurchase? = null,
    val editingUnitOptions: List<String> = emptyList(),
)
