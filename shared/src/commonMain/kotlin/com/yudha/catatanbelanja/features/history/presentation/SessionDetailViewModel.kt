package com.yudha.catatanbelanja.features.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.Failure
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.capitalizeWords
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.SessionSummary
import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.features.history.domain.usecase.BuildSessionDetail
import com.yudha.catatanbelanja.features.history.domain.usecase.BuildSessionRowView
import kotlin.math.abs
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionDetailViewModel(
    private val sessionRepository: SessionRepository,
    private val buildSessionDetail: BuildSessionDetail,
    private val buildSessionRowView: BuildSessionRowView,
) : ViewModel() {

    private val _state = MutableStateFlow(SessionDetailState())
    val state: StateFlow<SessionDetailState> = _state.asStateFlow()

    private val _effects = Channel<SessionDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var sessionId: String = ""

    fun load(sessionId: String) {
        if (_state.value.loadState is UiState.Loading) return

        this.sessionId = sessionId
        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.getFinishedSessions().returnWhen(
                onSuccess = { sessions -> onSessionsLoaded(sessions) },
                onError = { failure -> _state.update { it.copy(loadState = UiState.Error(failure)) } },
            )
        }
    }

    /** [otherId] is the session the user picked in the "bandingkan dengan…" sheet. */
    fun compareWith(otherId: String) {
        val summary = _state.value.summary ?: return
        val other = _state.value.otherSessions
            .firstOrNull { it.summary.session.id == otherId }
            ?.summary
            ?: return

        val olderFirst = listOf(summary, other).sortedBy { it.session.endedAt ?: it.session.startedAt }
        viewModelScope.launch {
            _effects.send(
                SessionDetailEffect.OpenCompare(
                    aId = olderFirst[0].session.id,
                    bId = olderFirst[1].session.id,
                ),
            )
        }
    }

    /** "Belanja lagi": a fresh session on the same store, seeded with this session's item names. */
    fun repeatSession() {
        val session = _state.value.summary?.session ?: return
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.getActiveSession().returnWhen(
                onSuccess = { active -> onActiveChecked(active, session) },
                onError = ::onActionFailed,
            )
        }
    }

    fun deleteSession() {
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId).returnWhen(
                onSuccess = { _ ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    _effects.send(SessionDetailEffect.Deleted)
                },
                onError = ::onActionFailed,
            )
        }
    }

    fun updateItem(
        itemId: String,
        name: String,
        qtyText: String,
        unit: String,
        note: String,
        priceText: String,
    ) {
        if (_state.value.actionState is UiState.Loading) return
        val trimmedName = name.trim()
        // The sheet already refuses a blank name; this only stops a stale submit.
        if (trimmedName.isEmpty()) return

        val qty = qtyText.toQty()?.takeIf { it > 0.0 }
        val item = ShoppingItem(
            id = itemId,
            name = trimmedName.capitalizeWords(),
            price = priceText.toRupiahAmount(),
            qty = qty,
            unit = unit.takeIf { qty != null },
            note = note.trim(),
        )
        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.updateItem(sessionId, item).returnWhen(
                onSuccess = { onItemChanged(SessionDetailEffect.ItemSaved) },
                onError = ::onActionFailed,
            )
        }
    }

    fun deleteItem(itemId: String) {
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.deleteItem(sessionId, itemId).returnWhen(
                onSuccess = { onItemChanged(SessionDetailEffect.ItemDeleted) },
                onError = ::onActionFailed,
            )
        }
    }

    /** See `LiveSessionViewModel.attachReceiptPhoto`: the bytes are already scaled and encoded. */
    fun attachReceiptPhoto(bytes: ByteArray) {
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.attachReceiptPhoto(sessionId, bytes).returnWhen(
                onSuccess = { onItemChanged(SessionDetailEffect.PhotoAttached) },
                onError = ::onActionFailed,
            )
        }
    }

    fun removeReceiptPhoto() {
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.removeReceiptPhoto(sessionId).returnWhen(
                onSuccess = { onItemChanged(SessionDetailEffect.PhotoRemoved) },
                onError = ::onActionFailed,
            )
        }
    }

    /**
     * [image] is the receipt card the screen just drew, as PNG bytes. The ViewModel never renders
     * it — what the receipt looks like is the UI's business — it only carries the result to the
     * repository that owns the share sheet.
     */
    fun shareReceiptImage(image: ByteArray) {
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.shareReceiptImage(sessionId, image).returnWhen(
                onSuccess = { _ ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    _effects.send(SessionDetailEffect.ReceiptShared)
                },
                onError = ::onActionFailed,
            )
        }
    }

    private suspend fun onItemChanged(effect: SessionDetailEffect) {
        _state.update { it.copy(actionState = UiState.Success(Unit)) }
        _effects.send(effect)
        reload()
    }

    private suspend fun onActiveChecked(active: ShoppingSession?, session: ShoppingSession) {
        if (active != null) {
            _state.update { it.copy(actionState = UiState.Success(Unit)) }
            _effects.send(SessionDetailEffect.ActiveSessionRunning)
            return
        }
        sessionRepository.startSession(session.store).returnWhen(
            onSuccess = { _ ->
                _state.update { it.copy(actionState = UiState.Success(Unit)) }
                _effects.send(SessionDetailEffect.OpenLiveSession(session.items.map { item -> item.name }))
            },
            onError = ::onActionFailed,
        )
    }

    private suspend fun onSessionsLoaded(sessions: List<ShoppingSession>) {
        val ordered = sessions
            .filter { it.endedAt != null }
            .sortedByDescending { it.endedAt }
        val session = ordered.firstOrNull { it.id == sessionId }
        if (session == null) {
            _state.update { it.copy(loadState = UiState.Error(Failure("session $sessionId not found"))) }
            _effects.send(SessionDetailEffect.NotFound)
            return
        }

        val endedAt = session.endedAt ?: session.startedAt
        val previous = ordered.firstOrNull { (it.endedAt ?: it.startedAt) < endedAt }
        val total = session.items.sumOf { it.price }
        val others = ordered.filter { it.id != session.id }.map(buildSessionRowView::invoke)
        val totalDelta = total - (previous?.items?.sumOf { item -> item.price } ?: 0)

        _state.update {
            it.copy(
                loadState = UiState.Success(Unit),
                summary = SessionSummary(session = session, total = total, itemCount = session.items.size),
                itemRows = buildSessionDetail(session, previous),
                hasPrevious = previous != null,
                totalDeltaAmount = abs(totalDelta),
                isTotalUp = totalDelta > 0,
                otherSessions = others,
                canCompare = others.isNotEmpty(),
            )
        }
    }

    private suspend fun reload() {
        sessionRepository.getFinishedSessions().returnWhen(
            onSuccess = { sessions -> onSessionsLoaded(sessions) },
            onError = { failure -> _state.update { it.copy(loadState = UiState.Error(failure)) } },
        )
    }

    private fun onActionFailed(failure: Failure) {
        _state.update { it.copy(actionState = UiState.Error(failure)) }
    }

    /** "12.500" / "Rp12.500" -> 12500. Only the digits carry meaning. */
    private fun String.toRupiahAmount(): Int = filter { it.isDigit() }.toIntOrNull() ?: 0

    /** "1,5" and "1.5" both mean one and a half. */
    private fun String.toQty(): Double? = trim().replace(',', '.').toDoubleOrNull()
}
