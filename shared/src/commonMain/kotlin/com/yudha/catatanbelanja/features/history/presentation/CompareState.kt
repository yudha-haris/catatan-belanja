package com.yudha.catatanbelanja.features.history.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.SessionSummary
import com.yudha.catatanbelanja.features.history.domain.model.CompareResult

data class CompareState(
    val loadState: UiState<Unit> = UiState.Initial,
    val sessionA: SessionSummary? = null,
    val sessionB: SessionSummary? = null,
    val result: CompareResult = CompareResult(),
)
