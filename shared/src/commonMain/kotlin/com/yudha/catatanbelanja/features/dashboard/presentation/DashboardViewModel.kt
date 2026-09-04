package com.yudha.catatanbelanja.features.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.repository.BackupRepository
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.features.dashboard.domain.model.DashboardScope
import com.yudha.catatanbelanja.features.dashboard.domain.usecase.BuildDashboardData
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
    private val buildDashboardData: BuildDashboardData,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _effects = Channel<DashboardEffect>(Channel.BUFFERED)
    val effects: Flow<DashboardEffect> = _effects.receiveAsFlow()

    /** Kept so the scope and trend pickers re-derive the whole dashboard without a second query. */
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
                            data = buildDashboardData(loaded, current.scope, current.data.trendName),
                        )
                    }
                },
                onError = { failure ->
                    _state.update { it.copy(loadState = UiState.Error(failure)) }
                },
            )
        }
    }

    fun selectScope(scope: DashboardScope) {
        if (_state.value.scope == scope) return

        _state.update { current ->
            current.copy(
                scope = scope,
                data = buildDashboardData(sessions, scope, current.data.trendName),
            )
        }
    }

    fun selectTrendItem(name: String) {
        if (_state.value.data.trendName == name) return

        _state.update { current ->
            current.copy(data = buildDashboardData(sessions, current.scope, name))
        }
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
