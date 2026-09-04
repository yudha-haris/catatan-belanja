package com.yudha.catatanbelanja.features.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.PriceBasis
import com.yudha.catatanbelanja.core.domain.model.QtyOverride
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.TrendSetting
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.core.domain.repository.TrendRepository
import com.yudha.catatanbelanja.features.dashboard.domain.model.TrendCandidate
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

/**
 * "Tren harga" — one item's price over time, and the page that makes it honest.
 *
 * The chart starts dumb ([PriceBasis.RAW]) for every item and stays that way until the user says
 * otherwise, per item. Switching an item to "per satuan" is saved the moment it is tapped, as is
 * every quantity typed into the adjust sheet: this page *is* the settings, so there is no Simpan
 * to forget to press.
 */
class PriceTrendViewModel(
    private val sessionRepository: SessionRepository,
    private val trendRepository: TrendRepository,
    private val buildTrendCandidates: BuildTrendCandidates,
    private val buildPriceTrend: BuildPriceTrend,
) : ViewModel() {

    private val _state = MutableStateFlow(PriceTrendState())
    val state: StateFlow<PriceTrendState> = _state.asStateFlow()

    private val _effects = Channel<PriceTrendEffect>(Channel.BUFFERED)
    val effects: Flow<PriceTrendEffect> = _effects.receiveAsFlow()

    /** The receipts, the tracked item's setting and its overrides — re-derived, never re-queried. */
    private var sessions: List<ShoppingSession> = emptyList()
    private var setting: TrendSetting = TrendSetting(nameKey = "")
    private var overrides: List<QtyOverride> = emptyList()

    /** [initialName] is the item the caller came in on; blank falls back to the most bought one. */
    fun load(initialName: String?) {
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
                            candidates = candidates,
                            visibleCandidates = candidates,
                            hasAnyCandidate = candidates.isNotEmpty(),
                            query = "",
                        )
                    }
                    val name = resolveName(candidates, initialName)
                    if (name == null) return@returnWhen

                    track(name)
                },
                onError = { failure ->
                    _state.update { it.copy(loadState = UiState.Error(failure)) }
                },
            )
        }
    }

    fun openPicker() {
        _state.update { it.copy(isPickerOpen = true, query = "", visibleCandidates = it.candidates) }
    }

    fun dismissPicker() {
        _state.update { it.copy(isPickerOpen = false) }
    }

    fun onQueryChanged(query: String) {
        _state.update { current ->
            current.copy(query = query, visibleCandidates = filter(current.candidates, query))
        }
    }

    fun selectName(name: String) {
        _state.update { it.copy(isPickerOpen = false) }
        if (_state.value.data.name.normalized() == name.normalized()) return

        viewModelScope.launch { track(name) }
    }

    fun selectBasis(basis: PriceBasis) {
        if (_state.value.data.basis == basis) return

        save(setting.copy(basis = basis))
    }

    fun selectBaseUnit(unit: String) {
        if (_state.value.data.baseUnit == unit.normalized()) return

        save(setting.copy(baseUnit = unit.normalized()))
    }

    fun openQtySheet(itemId: String) {
        val purchase = _state.value.data.purchases.firstOrNull { it.itemId == itemId } ?: return

        _state.update { current ->
            current.copy(editing = purchase, editingUnitOptions = unitOptionsFor(current))
        }
    }

    fun dismissQtySheet() {
        _state.update { it.copy(editing = null, editingUnitOptions = emptyList()) }
    }

    fun saveQtyOverride(qtyText: String, unit: String) {
        val editing = _state.value.editing ?: return
        if (_state.value.actionState is UiState.Loading) return

        val qty = qtyText.toQty()
        if (qty == null || qty <= 0.0 || unit.isBlank()) {
            viewModelScope.launch {
                _effects.send(PriceTrendEffect.ShowMessage(PriceTrendEffect.Kind.INVALID_QTY))
            }
            return
        }

        val override = QtyOverride(
            itemId = editing.itemId,
            nameKey = setting.nameKey,
            qty = qty,
            unit = unit.normalized(),
        )
        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            trendRepository.saveOverride(override).returnWhen(
                onSuccess = { _ ->
                    finishAdjustment(PriceTrendEffect.Kind.ADJUSTMENT_SAVED)
                },
                onError = { failure ->
                    _state.update { it.copy(actionState = UiState.Error(failure)) }
                },
            )
        }
    }

    /** Drops the manual quantity and puts the purchase back on whatever the receipt recorded. */
    fun clearQtyOverride() {
        val editing = _state.value.editing ?: return
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            trendRepository.deleteOverride(editing.itemId).returnWhen(
                onSuccess = { _ ->
                    finishAdjustment(PriceTrendEffect.Kind.ADJUSTMENT_CLEARED)
                },
                onError = { failure ->
                    _state.update { it.copy(actionState = UiState.Error(failure)) }
                },
            )
        }
    }

    /** Reads the item's saved measurement and corrections, then draws it. */
    private suspend fun track(name: String) {
        val key = name.normalized()
        trendRepository.getSetting(key).returnWhen(
            onSuccess = { loaded -> setting = loaded },
            onError = { failure ->
                _state.update { it.copy(loadState = UiState.Error(failure)) }
                return
            },
        )
        trendRepository.getOverrides(key).returnWhen(
            onSuccess = { loaded -> overrides = loaded },
            onError = { failure ->
                _state.update { it.copy(loadState = UiState.Error(failure)) }
                return
            },
        )
        redraw(name)
    }

    private fun save(updated: TrendSetting) {
        if (_state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            trendRepository.saveSetting(updated).returnWhen(
                onSuccess = { _ ->
                    setting = updated
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    redraw(_state.value.data.name)
                },
                onError = { failure ->
                    _state.update { it.copy(actionState = UiState.Error(failure)) }
                },
            )
        }
    }

    private suspend fun finishAdjustment(kind: PriceTrendEffect.Kind) {
        val name = _state.value.data.name
        trendRepository.getOverrides(setting.nameKey).returnWhen(
            onSuccess = { loaded ->
                overrides = loaded
                _state.update { it.copy(actionState = UiState.Success(Unit), editing = null) }
                redraw(name)
                _effects.send(PriceTrendEffect.ShowMessage(kind))
            },
            onError = { failure ->
                _state.update { it.copy(actionState = UiState.Error(failure)) }
            },
        )
    }

    private fun redraw(name: String) {
        val data = buildPriceTrend(
            sessions = sessions,
            name = name,
            basis = setting.basis,
            requestedBaseUnit = setting.baseUnit,
            overrides = overrides,
        )
        _state.update { it.copy(data = data) }
    }

    private fun resolveName(candidates: List<TrendCandidate>, requested: String?): String? {
        val key = requested?.normalized().orEmpty()
        val match = candidates.firstOrNull { it.name.normalized() == key }
        if (match != null) return match.name
        return requested?.takeIf { it.isNotBlank() } ?: candidates.firstOrNull()?.name
    }

    private fun filter(candidates: List<TrendCandidate>, query: String): List<TrendCandidate> {
        val key = query.normalized()
        if (key.isEmpty()) return candidates
        return candidates.filter { it.name.normalized().contains(key) }
    }

    /**
     * The sheet offers the units this item is measured in. An item no trip ever put a unit on has
     * none to offer, so the whole catalog stands in — the user is establishing the unit, not
     * matching an existing one.
     */
    private fun unitOptionsFor(current: PriceTrendState): List<String> {
        val fromData = current.data.baseUnitOptions
        if (fromData.isNotEmpty()) return fromData
        return CatalogData.units
    }

    private fun String.toQty(): Double? = trim().replace(',', '.').toDoubleOrNull()
}
