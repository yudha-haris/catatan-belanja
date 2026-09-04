package com.yudha.catatanbelanja.features.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.dataOrNull
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.PriceBasis
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.repository.BackupRepository
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.core.domain.repository.TrendRepository
import com.yudha.catatanbelanja.features.dashboard.domain.model.DashboardScope
import com.yudha.catatanbelanja.features.dashboard.domain.usecase.BuildDashboardData
import com.yudha.catatanbelanja.features.dashboard.domain.usecase.BuildPriceTrend
import com.yudha.catatanbelanja.features.dashboard.domain.usecase.BuildTrendCandidates
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val sessionRepository: SessionRepository,
    private val backupRepository: BackupRepository,
    private val trendRepository: TrendRepository,
    private val buildDashboardData: BuildDashboardData,
    private val buildTrendCandidates: BuildTrendCandidates,
    private val buildPriceTrend: BuildPriceTrend,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _effects = Channel<DashboardEffect>(Channel.BUFFERED)
    val effects: Flow<DashboardEffect> = _effects.receiveAsFlow()

    /** Kept so the scope toggle re-derives the whole dashboard without a second query. */
    private var sessions: List<ShoppingSession> = emptyList()

    fun load() {
        if (_state.value.loadState is UiState.Loading) return

        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.getFinishedSessions().returnWhen(
                onSuccess = { loaded ->
                    sessions = loaded
                    val candidates = buildTrendCandidates(loaded)
                    _state.update { current ->
                        current.copy(
                            loadState = UiState.Success(Unit),
                            hasAnySession = loaded.isNotEmpty(),
                            data = buildDashboardData(loaded, current.scope),
                            trendCandidates = candidates,
                            trendNames = candidates.map { it.name },
                        )
                    }
                    // Keep whatever the user was already looking at across a reload.
                    val tracked = _state.value.trend.name.ifBlank {
                        candidates.firstOrNull()?.name.orEmpty()
                    }
                    if (tracked.isBlank()) return@returnWhen

                    drawTrend(tracked)
                },
                onError = { failure ->
                    _state.update { it.copy(loadState = UiState.Error(failure)) }
                },
            )
        }
    }

    fun selectScope(scope: DashboardScope) {
        if (_state.value.scope == scope) return

        _state.update { it.copy(scope = scope, data = buildDashboardData(sessions, scope)) }
    }

    fun selectTrendItem(name: String) {
        if (_state.value.trend.name == name) return

        viewModelScope.launch { drawTrend(name) }
    }

    /**
     * Reads the item's saved basis and its manual corrections before plotting it. A failure is
     * swallowed on purpose: the summary tab is not the place to raise a dialog about a chart, and
     * an unadjusted line is a better answer than an error where a card used to be.
     */
    private suspend fun drawTrend(name: String) {
        val setting = trendRepository.getSetting(name.normalized()).dataOrNull()
        val overrides = trendRepository.getOverrides(name.normalized()).dataOrNull().orEmpty()
        val trend = buildPriceTrend(
            sessions = sessions,
            name = name,
            basis = setting?.basis ?: PriceBasis.RAW,
            requestedBaseUnit = setting?.baseUnit,
            overrides = overrides,
        )
        _state.update { it.copy(trend = trend) }
    }

    fun seedDemo() {
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            backupRepository.seedDemoData().returnWhen(
                onSuccess = { _ ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    _effects.send(DashboardEffect.DemoSeeded)
                    load()
                },
                onError = { failure ->
                    _state.update { it.copy(actionState = UiState.Error(failure)) }
                },
            )
        }
    }
}
