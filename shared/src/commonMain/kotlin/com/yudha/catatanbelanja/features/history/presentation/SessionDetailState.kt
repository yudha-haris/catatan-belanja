package com.yudha.catatanbelanja.features.history.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.SessionSummary
import com.yudha.catatanbelanja.features.history.domain.model.HistorySessionRowView
import com.yudha.catatanbelanja.features.history.domain.model.SessionItemRow

data class SessionDetailState(
    val loadState: UiState<Unit> = UiState.Initial,
    val actionState: UiState<Unit> = UiState.Initial,
    val summary: SessionSummary? = null,
    val itemRows: List<SessionItemRow> = emptyList(),
    val hasPrevious: Boolean = false,
    /** How far this session's total sits from the previous one's, unsigned. Meaningless when !hasPrevious. */
    val totalDeltaAmount: Int = 0,
    /** Which arrow [totalDeltaAmount] earns: spending more is up. */
    val isTotalUp: Boolean = false,
    val otherSessions: List<HistorySessionRowView> = emptyList(),
    val canCompare: Boolean = false,
)
