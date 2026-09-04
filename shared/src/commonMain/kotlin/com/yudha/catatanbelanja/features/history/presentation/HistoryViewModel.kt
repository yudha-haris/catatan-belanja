package com.yudha.catatanbelanja.features.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.Failure
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.repository.BackupRepository
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.features.history.domain.usecase.GroupSessionsByMonth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MAX_PICKED = 2

class HistoryViewModel(
    private val sessionRepository: SessionRepository,
    private val backupRepository: BackupRepository,
    private val groupSessionsByMonth: GroupSessionsByMonth,
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state.asStateFlow()

    private val _effects = Channel<HistoryEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /** Session ids newest first — orders the A/B pair and feeds the "2 terakhir" shortcut. */
    private var orderedIds: List<String> = emptyList()

    fun load() {
        if (_state.value.loadState is UiState.Loading) return

        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.getFinishedSessions().returnWhen(
                onSuccess = ::onSessionsLoaded,
                onError = ::onLoadFailed,
            )
        }
    }

    fun onSessionClicked(sessionId: String) {
        if (_state.value.compareMode) {
            pickSession(sessionId)
            return
        }
        viewModelScope.launch { _effects.send(HistoryEffect.OpenDetail(sessionId)) }
    }

    fun toggleCompareMode() {
        val enabled = !_state.value.compareMode
        _state.update {
            it.copy(
                compareMode = enabled,
                pickedIds = emptyList(),
                pickedCount = 0,
                canQuickCompare = quickCompareOffered(emptyList()),
                canRunCompare = false,
            )
        }
    }

    /** Compares the two most recent sessions without picking them by hand. */
    fun quickCompare() {
        if (orderedIds.size < MAX_PICKED) return

        viewModelScope.launch {
            _effects.send(HistoryEffect.OpenCompare(aId = orderedIds[1], bId = orderedIds[0]))
        }
    }

    fun runCompare() {
        val picked = _state.value.pickedIds
        if (picked.size != MAX_PICKED) return

        // Oldest is always A: the higher index in the newest-first list.
        val ordered = picked.sortedByDescending { orderedIds.indexOf(it) }
        viewModelScope.launch {
            _effects.send(HistoryEffect.OpenCompare(aId = ordered[0], bId = ordered[1]))
        }
    }

    fun seedDemo() {
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            backupRepository.seedDemoData().returnWhen(
                onSuccess = { _ ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    _effects.send(HistoryEffect.DemoSeeded)
                    load()
                },
                onError = { failure -> _state.update { it.copy(actionState = UiState.Error(failure)) } },
            )
        }
    }

    private fun pickSession(sessionId: String) {
        val picked = _state.value.pickedIds
        val next = when (picked.contains(sessionId)) {
            true -> picked - sessionId
            false -> (picked + sessionId).takeLast(MAX_PICKED) // a third pick drops the oldest
        }
        _state.update {
            it.copy(
                pickedIds = next,
                pickedCount = next.size,
                canQuickCompare = quickCompareOffered(next),
                canRunCompare = next.size == MAX_PICKED,
            )
        }
    }

    /**
     * "2 terakhir" is a shortcut for the user who has not picked anything yet. The moment there is
     * a manual selection it competes with the "Bandingkan" bar for the same job, so it steps aside.
     */
    private fun quickCompareOffered(picked: List<String>): Boolean =
        orderedIds.size >= MAX_PICKED && picked.isEmpty()

    private fun onSessionsLoaded(sessions: List<ShoppingSession>) {
        val groups = groupSessionsByMonth(sessions)
        orderedIds = groups.flatMap { group -> group.summaries.map { it.summary.session.id } }
        val picked = _state.value.pickedIds.filter { orderedIds.contains(it) }

        _state.update {
            it.copy(
                loadState = UiState.Success(Unit),
                groups = groups,
                sessionCount = orderedIds.size,
                hasAny = orderedIds.isNotEmpty(),
                pickedIds = picked,
                pickedCount = picked.size,
                canQuickCompare = quickCompareOffered(picked),
                canRunCompare = picked.size == MAX_PICKED,
            )
        }
    }

    private fun onLoadFailed(failure: Failure) {
        _state.update { it.copy(loadState = UiState.Error(failure)) }
    }
}
