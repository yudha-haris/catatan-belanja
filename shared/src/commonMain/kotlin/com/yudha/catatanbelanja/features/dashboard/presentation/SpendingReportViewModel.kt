package com.yudha.catatanbelanja.features.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.features.dashboard.domain.model.ReportRange
import com.yudha.catatanbelanja.features.dashboard.domain.usecase.BuildSpendingReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** "Laporan belanja" — the whole spending history behind the summary tab's eight bars. */
class SpendingReportViewModel(
    private val sessionRepository: SessionRepository,
    private val buildSpendingReport: BuildSpendingReport,
) : ViewModel() {

    private val _state = MutableStateFlow(SpendingReportState())
    val state: StateFlow<SpendingReportState> = _state.asStateFlow()

    /** Kept so the range chips re-derive the page without a second query. */
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
                            data = buildSpendingReport(loaded, current.range),
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

        _state.update { it.copy(range = range, data = buildSpendingReport(sessions, range)) }
    }
}
