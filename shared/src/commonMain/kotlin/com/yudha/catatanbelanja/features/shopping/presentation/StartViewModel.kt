package com.yudha.catatanbelanja.features.shopping.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.ShoppingList
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.repository.BackupRepository
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.core.domain.repository.ShoppingListRepository
import com.yudha.catatanbelanja.core.domain.repository.StockRepository
import com.yudha.catatanbelanja.features.shopping.domain.usecase.BuildStartOverview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Failure.code the session repository returns when a second session is started.
private const val ACTIVE_SESSION_EXISTS = "ACTIVE_SESSION_EXISTS"

class StartViewModel(
    private val sessionRepository: SessionRepository,
    private val stockRepository: StockRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val backupRepository: BackupRepository,
    private val buildStartOverview: BuildStartOverview,
) : ViewModel() {

    private val _state = MutableStateFlow(StartState())
    val state: StateFlow<StartState> = _state.asStateFlow()

    private val _effects = Channel<StartEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun load() {
        if (_state.value.loadState is UiState.Loading) return

        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            val finished = when (val result = sessionRepository.getFinishedSessions()) {
                is Resource.Error -> return@launch failLoad(result)
                is Resource.Success -> result.value
            }
            val active = when (val result = sessionRepository.getActiveSession()) {
                is Resource.Error -> return@launch failLoad(result)
                is Resource.Success -> result.value
            }
            val stock = when (val result = stockRepository.getStockItems()) {
                is Resource.Error -> return@launch failLoad(result)
                is Resource.Success -> result.value
            }
            val list = when (val result = shoppingListRepository.getActiveList()) {
                is Resource.Error -> return@launch failLoad(result)
                is Resource.Success -> result.value
            }

            applyOverview(
                finished = finished,
                active = active,
                stock = stock,
                list = list,
            )
        }
    }

    fun startSession(store: String) {
        if (_state.value.loadState is UiState.Loading) return

        viewModelScope.launch {
            sessionRepository.startSession(store.trim()).returnWhen(
                onSuccess = { _effects.send(StartEffect.SessionStarted(it.id)) },
                onError = { failure ->
                    if (failure.code == ACTIVE_SESSION_EXISTS) {
                        _effects.send(StartEffect.ActiveSessionExists)
                        load()
                        return@returnWhen
                    }
                    _effects.send(StartEffect.ShowError(failure))
                },
            )
        }
    }

    fun seedDemo() {
        if (_state.value.loadState is UiState.Loading) return

        viewModelScope.launch {
            backupRepository.seedDemoData().returnWhen(
                onSuccess = {
                    _effects.send(StartEffect.DemoSeeded)
                    load()
                },
                onError = { _effects.send(StartEffect.ShowError(it)) },
            )
        }
    }

    private fun applyOverview(
        finished: List<ShoppingSession>,
        active: ShoppingSession?,
        stock: List<StockItem>,
        list: ShoppingList?,
    ) {
        val overview = buildStartOverview(finished = finished, active = active, stock = stock)
        val remaining = list?.items.orEmpty().filterNot { it.isChecked }
        _state.update {
            it.copy(
                loadState = UiState.Success(Unit),
                greeting = overview.greeting,
                activeSession = overview.activeSession,
                monthTotal = overview.monthTotal,
                monthCount = overview.monthCount,
                monthAverage = overview.monthAverage,
                recent = overview.recent,
                storeSuggestions = overview.storeSuggestions,
                hasAnySession = overview.hasAnySession,
                hasList = list != null,
                listTotalCount = list?.items?.size ?: 0,
                listRemainingCount = remaining.size,
                listPreviewNames = remaining.take(LIST_PREVIEW_LIMIT).map { it.name },
                listExtraCount = (remaining.size - LIST_PREVIEW_LIMIT).coerceAtLeast(0),
            )
        }
    }

    private suspend fun failLoad(error: Resource.Error) {
        _state.update { it.copy(loadState = UiState.Error(error.failure)) }
        _effects.send(StartEffect.ShowError(error.failure))
    }

    private companion object {
        /** How many list names the home card names before it falls back to a "+n" tail. */
        const val LIST_PREVIEW_LIMIT = 3
    }
}
