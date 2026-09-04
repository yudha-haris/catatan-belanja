package com.yudha.catatanbelanja.features.history.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.history.domain.model.HistoryMonthGroup

data class HistoryState(
    val loadState: UiState<Unit> = UiState.Initial,
    val actionState: UiState<Unit> = UiState.Initial,
    val groups: List<HistoryMonthGroup> = emptyList(),
    val sessionCount: Int = 0,
    val hasAny: Boolean = false,
    val compareMode: Boolean = false,
    val pickedIds: List<String> = emptyList(),
    val pickedCount: Int = 0,
    val canQuickCompare: Boolean = false,
    val canRunCompare: Boolean = false,
)
