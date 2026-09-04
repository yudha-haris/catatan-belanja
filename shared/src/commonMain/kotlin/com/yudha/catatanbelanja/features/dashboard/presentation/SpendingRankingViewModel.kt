package com.yudha.catatanbelanja.features.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.features.dashboard.domain.model.RankingMode
import com.yudha.catatanbelanja.features.dashboard.domain.model.ReportRange
import com.yudha.catatanbelanja.features.dashboard.domain.usecase.BuildSpendingRanking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** "Pengeluaran terbesar" — the full ranking, and how concentrated the spending is. */
class SpendingRankingViewModel(
    private val sessionRepository: SessionRepository,
    private val buildSpendingRanking: BuildSpendingRanking,
) : ViewModel() {

    private val _state = MutableStateFlow(SpendingRankingState())
    val state: StateFlow<SpendingRankingState> = _state.asStateFlow()

    /** Kept so the range and mode chips re-derive the page without a second query. */
    private var sessions: List<ShoppingSession> = emptyList()

    fun load() {
        if (_state.value.loadState is UiState.Loading) return

        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.getFinishedSessions().returnWhen(
                onSuccess = { loaded ->
                    sessions = loaded
                    _state.update { current ->
                        current.copy(
                            loadState = UiState.Success(Unit),
                            hasAnySession = loaded.isNotEmpty(),
                            data = buildSpendingRanking(loaded, current.range, current.mode),
                        )
                    }
                },
                onError = { failure ->
                    _state.update { it.copy(loadState = UiState.Error(failure)) }
                },
            )
        }
    }

    fun selectRange(range: ReportRange) {
        if (_state.value.range == range) return

        _state.update { current ->
            current.copy(range = range, data = buildSpendingRanking(sessions, range, current.mode))
        }
    }

    fun selectMode(mode: RankingMode) {
        if (_state.value.mode == mode) return

        _state.update { current ->
            current.copy(mode = mode, data = buildSpendingRanking(sessions, current.range, mode))
        }
    }
}
