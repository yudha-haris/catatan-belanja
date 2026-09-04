package com.yudha.catatanbelanja.features.stock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.common.Failure
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.dataOrNull
import com.yudha.catatanbelanja.core.common.failureOrNull
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockCheckEntry
import com.yudha.catatanbelanja.core.domain.model.StockCheckLog
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.core.domain.repository.StockRepository
import com.yudha.catatanbelanja.features.stock.domain.model.StockCheckLogView
import com.yudha.catatanbelanja.features.stock.domain.model.StockCheckRow
import com.yudha.catatanbelanja.features.stock.domain.usecase.BuildKnownStockNames
import com.yudha.catatanbelanja.features.stock.domain.usecase.BuildStockRows
import com.yudha.catatanbelanja.features.stock.domain.usecase.CalculateStockUsage
import com.yudha.catatanbelanja.features.stock.domain.usecase.CreateStockItem
import com.yudha.catatanbelanja.features.stock.domain.usecase.CurrentStockCheckStamp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StockViewModel(
    private val stockRepository: StockRepository,
    private val sessionRepository: SessionRepository,
    private val buildStockRows: BuildStockRows,
    private val buildKnownStockNames: BuildKnownStockNames,
    private val calculateStockUsage: CalculateStockUsage,
    private val createStockItem: CreateStockItem,
    private val currentStockCheckStamp: CurrentStockCheckStamp,
) : ViewModel() {

    private val _state = MutableStateFlow(StockState())
    val state: StateFlow<StockState> = _state.asStateFlow()

    private val _effects = Channel<StockEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun load() {
        if (state.value.loadState is UiState.Loading) return

        _state.update { it.copy(loadState = UiState.Loading) }
        viewModelScope.launch {
            val items = stockRepository.getStockItems()
            val logs = stockRepository.getCheckLogs()
            val sessions = sessionRepository.getFinishedSessions()
            // The three reads are independent; the first failure is the one worth showing.
            val failure = items.failureOrNull() ?: logs.failureOrNull() ?: sessions.failureOrNull()
            if (failure != null) {
                _state.update { it.copy(loadState = UiState.Error(failure)) }
                return@launch
            }
            applyLoaded(
                items = items.dataOrNull().orEmpty(),
                logs = logs.dataOrNull().orEmpty(),
                sessions = sessions.dataOrNull().orEmpty(),
            )
        }
    }

    fun openEditor(stockItemId: String?) {
        val existing = stockItemId?.let(::findItem)
        _state.update {
            it.copy(
                isEditorOpen = true,
                isEditorNew = existing == null,
                editorItem = existing,
                editorUnit = existing?.unit ?: DEFAULT_UNIT,
            )
        }
    }

    fun closeEditor() {
        _state.update { it.copy(isEditorOpen = false, editorItem = null, isEditorNew = true) }
    }

    /** A brand-new row takes its unit from the catalog default the moment the name is known. */
    fun onEditorNameChanged(name: String) {
        if (!state.value.isEditorNew) return

        val unit = CatalogData.defaultUnits[name.normalized()] ?: return
        _state.update { it.copy(editorUnit = unit) }
    }

    fun saveStockItem(name: String, qtyText: String, unit: String, minText: String) {
        if (state.value.actionState is UiState.Loading) return

        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            _effects.trySend(StockEffect.NameRequired)
            return
        }
        // Saving under an existing name edits that row instead of creating a twin of it.
        val target = state.value.editorItem ?: findItemByName(trimmed)
        val item = createStockItem(
            name = trimmed,
            qtyText = qtyText,
            unit = unit,
            minText = minText,
            target = target,
        )
        upsert(item, StockEffect.ItemSaved)
    }

    fun markEditorItemEmpty() {
        val item = state.value.editorItem ?: return
        if (state.value.actionState is UiState.Loading) return

        upsert(
            item = createStockItem.markedEmpty(item),
            effect = StockEffect.ItemMarkedEmpty(item.name),
        )
    }

    fun deleteEditorItem() {
        val item = state.value.editorItem ?: return
        if (state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            stockRepository.deleteStockItem(item.id).returnWhen(
                onSuccess = {
                    closeEditor()
                    finishAction(StockEffect.ItemDeleted)
                },
                onError = ::failAction,
            )
        }
    }

    fun openCheckSheet() {
        val stamp = currentStockCheckStamp()
        val rows = (state.value.lowRows + state.value.okRows)
            .sortedBy { it.item.name.normalized() }
            .map { row ->
                StockCheckRow(
                    id = row.item.id,
                    name = row.item.name,
                    emoji = row.emoji,
                    unit = row.item.unit,
                    previousQty = row.item.qty,
                )
            }
        _state.update {
            it.copy(
                isCheckOpen = true,
                checkRows = rows,
                checkMonth = stamp.month,
                checkedAtMillis = stamp.checkedAtMillis,
            )
        }
    }

    fun closeCheckSheet() {
        _state.update { it.copy(isCheckOpen = false, checkRows = emptyList()) }
    }

    /** [quantities] maps a [StockCheckRow.id] to the raw text the user typed for it. */
    fun saveStockCheck(quantities: Map<String, String>) {
        if (state.value.actionState is UiState.Loading) return

        val rows = state.value.checkRows
        if (rows.isEmpty()) return

        val month = state.value.checkMonth
        val entries = rows.map { row ->
            StockCheckEntry(
                name = row.name,
                qty = quantities[row.id].orEmpty().toQty(),
                unit = row.unit,
            )
        }
        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            stockRepository.saveStockCheck(entries).returnWhen(
                onSuccess = {
                    closeCheckSheet()
                    finishAction(StockEffect.CheckSaved(month))
                },
                onError = ::failAction,
            )
        }
    }

    fun openLog(logId: String) {
        val logView = state.value.logs.firstOrNull { it.log.id == logId } ?: return
        if (state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.getFinishedSessions().returnWhen(
                onSuccess = { sessions -> applyLogDetail(logView, sessions) },
                onError = ::failAction,
            )
        }
    }

    fun closeLog() {
        _state.update {
            it.copy(logDetail = null, usageRows = emptyList(), usagePreviousMonth = null)
        }
    }

    fun deleteLog(logId: String) {
        if (state.value.actionState is UiState.Loading) return

        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            stockRepository.deleteCheckLog(logId).returnWhen(
                onSuccess = {
                    closeLog()
                    finishAction(StockEffect.LogDeleted)
                },
                onError = ::failAction,
            )
        }
    }

    private fun applyLoaded(
        items: List<StockItem>,
        logs: List<StockCheckLog>,
        sessions: List<ShoppingSession>,
    ) {
        val rows = buildStockRows(items)
        val lowRows = rows.filter { it.isLow }
        val okRows = rows.filterNot { it.isLow }
        _state.update {
            it.copy(
                loadState = UiState.Success(Unit),
                lowRows = lowRows,
                okRows = okRows,
                totalCount = rows.size,
                lowCount = lowRows.size,
                hasAny = rows.isNotEmpty(),
                logs = logs.sortedByDescending { log -> log.checkedAt }.map(::toLogView),
                knownNames = buildKnownStockNames(sessions),
            )
        }
    }

    private fun applyLogDetail(logView: StockCheckLogView, sessions: List<ShoppingSession>) {
        val allLogs = state.value.logs.map { it.log }
        _state.update {
            it.copy(
                actionState = UiState.Success(Unit),
                logDetail = logView,
                usageRows = calculateStockUsage(logView.log, allLogs, sessions),
                usagePreviousMonth = calculateStockUsage.previousLogOf(logView.log, allLogs)?.month,
            )
        }
    }

    private fun upsert(item: StockItem, effect: StockEffect) {
        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            stockRepository.upsertStockItem(item).returnWhen(
                onSuccess = {
                    closeEditor()
                    finishAction(effect)
                },
                onError = ::failAction,
            )
        }
    }

    private fun finishAction(effect: StockEffect) {
        _state.update { it.copy(actionState = UiState.Success(Unit)) }
        _effects.trySend(effect)
        load()
    }

    private fun failAction(failure: Failure) {
        _state.update { it.copy(actionState = UiState.Error(failure)) }
    }

    private fun toLogView(log: StockCheckLog): StockCheckLogView = StockCheckLogView(
        log = log,
        itemCount = log.entries.size,
        outCount = log.entries.count { it.qty <= 0.0 },
    )

    private fun findItem(stockItemId: String): StockItem? =
        allRows().firstOrNull { it.item.id == stockItemId }?.item

    private fun findItemByName(name: String): StockItem? {
        val key = name.normalized()
        return allRows().firstOrNull { it.item.name.normalized() == key }?.item
    }

    private fun allRows() = state.value.lowRows + state.value.okRows

    /** The prototype accepts "1,5" as well as "1.5"; anything unreadable counts as none. */
    private fun String.toQty(): Double = trim().replace(',', '.').toDoubleOrNull() ?: 0.0

    private companion object {
        const val DEFAULT_UNIT = "pcs"
    }
}
