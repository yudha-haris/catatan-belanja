package com.yudha.catatanbelanja.features.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.Failure
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.SessionSummary
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.features.history.domain.usecase.BuildCompareResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CompareViewModel(
    private val sessionRepository: SessionRepository,
    private val buildCompareResult: BuildCompareResult,
) : ViewModel() {

    private val _state = MutableStateFlow(CompareState())
    val state: StateFlow<CompareState> = _state.asStateFlow()

    /** [aId] is the older session, [bId] the newer one — the caller guarantees that order. */
    fun load(aId: String, bId: String) {
        if (_state.value.loadState is UiState.Loading) return

        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.getSession(aId).returnWhen(
                onSuccess = { sessionA -> loadSecondSide(aId, sessionA, bId) },
                onError = ::onFailed,
            )
        }
    }

    private suspend fun loadSecondSide(aId: String, sessionA: ShoppingSession?, bId: String) {
        if (sessionA == null) {
            onFailed(Failure("session $aId not found"))
            return
        }
        sessionRepository.getSession(bId).returnWhen(
            onSuccess = { sessionB -> onBothLoaded(sessionA, sessionB) },
            onError = ::onFailed,
        )
    }

    private fun onBothLoaded(sessionA: ShoppingSession, sessionB: ShoppingSession?) {
        if (sessionB == null) {
            onFailed(Failure("counterpart session not found for ${sessionA.id}"))
            return
        }

        val result = buildCompareResult(sessionA, sessionB)
        _state.update {
            it.copy(
                loadState = UiState.Success(Unit),
                sessionA = SessionSummary(sessionA, result.totalA, sessionA.items.size),
                sessionB = SessionSummary(sessionB, result.totalB, sessionB.items.size),
                result = result,
            )
        }
    }

    private fun onFailed(failure: Failure) {
        _state.update { it.copy(loadState = UiState.Error(failure)) }
    }
}
