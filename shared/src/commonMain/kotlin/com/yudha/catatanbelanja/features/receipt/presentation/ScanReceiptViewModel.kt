package com.yudha.catatanbelanja.features.receipt.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.Failure
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.common.capitalizeWords
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.ReceiptScan
import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.repository.ReceiptScanRepository
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.features.receipt.domain.usecase.BuildScannedRows
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

/**
 * Scan a paper receipt, correct what the model got wrong, save it as a trip that already happened.
 *
 * The draft never reaches the database on its own: everything below edits a list held here, and
 * only [save] writes — once, through
 * [SessionRepository.importFinishedSession][com.yudha.catatanbelanja.core.domain.repository.SessionRepository.importFinishedSession].
 * Backing out of the screen therefore costs nothing but the scan.
 */
class ScanReceiptViewModel(
    private val receiptScanRepository: ReceiptScanRepository,
    private val sessionRepository: SessionRepository,
    private val buildScannedRows: BuildScannedRows,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(ScanReceiptState())
    val state: StateFlow<ScanReceiptState> = _state.asStateFlow()

    private val _effects = Channel<ScanReceiptEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    /**
     * The draft, and the photo it was read from. Both are plain fields rather than state: a
     * `ByteArray` has identity equality, so putting one in a `data class` state would make every
     * `copy()` look like a change, and the item list is rebuilt into view rows on every edit
     * anyway.
     */
    private var draft: List<ShoppingItem> = emptyList()
    private var photo: ByteArray? = null

    fun load() {
        _state.update { it.copy(available = receiptScanRepository.isAvailable()) }
    }

    /** [image] is the photo, already scaled and JPEG-encoded by the screen. */
    fun scan(image: ByteArray) {
        if (_state.value.scanState is UiState.Loading) return

        photo = image
        _state.update { it.copy(scanState = UiState.Loading) }
        viewModelScope.launch {
            receiptScanRepository.scan(image).returnWhen(
                onSuccess = { scan -> onScanned(scan) },
                onError = ::onScanFailed,
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
        val trimmedName = name.trim()
        // The sheet already refuses a blank name; this only stops a stale submit.
        if (trimmedName.isEmpty()) return

        val qty = qtyText.toQty()?.takeIf { it > 0.0 }
        val edited = ShoppingItem(
            id = itemId,
            name = trimmedName.capitalizeWords(),
            price = priceText.toRupiahAmount(),
            qty = qty,
            unit = unit.takeIf { qty != null },
            note = note.trim(),
        )
        publish(draft.map { item -> if (item.id == itemId) edited else item })
    }

    fun deleteItem(itemId: String) {
        publish(draft.filterNot { it.id == itemId })
        viewModelScope.launch { _effects.send(ScanReceiptEffect.ItemDeleted) }
    }

    /**
     * [dateText] is what the user has in the date field, in the `d/M/yyyy` shape the screen seeded
     * it with. It is parsed here rather than kept in state as the user types: reseeding a text
     * buffer from state on every keystroke fights the person editing it.
     */
    fun save(name: String, store: String, dateText: String) {
        if (_state.value.actionState is UiState.Loading) return
        if (draft.isEmpty()) return

        val purchasedAt = dateText.toEpochMillis()
        if (purchasedAt == null) {
            viewModelScope.launch { _effects.send(ScanReceiptEffect.InvalidDate) }
            return
        }

        val trimmedStore = store.trim()
        val tripName = name.trim().ifEmpty { trimmedStore }
        _state.update { it.copy(actionState = UiState.Loading) }
        viewModelScope.launch {
            sessionRepository.importFinishedSession(
                name = tripName,
                store = trimmedStore,
                purchasedAt = purchasedAt,
                items = draft,
                photo = photo,
            ).returnWhen(
                onSuccess = { sessionId ->
                    _state.update { it.copy(actionState = UiState.Success(Unit)) }
                    _effects.send(ScanReceiptEffect.Saved(sessionId))
                },
                onError = { failure ->
                    _state.update { it.copy(actionState = UiState.Error(failure)) }
                },
            )
        }
    }

    /** Throws the draft away so the screen can offer another photo. The scan itself is spent. */
    fun discard() {
        draft = emptyList()
        photo = null
        _state.update {
            it.copy(
                scanState = UiState.Initial,
                actionState = UiState.Initial,
                hasScan = false,
                store = "",
                purchasedAt = 0L,
                dateWasRead = false,
                rows = emptyList(),
                itemCount = 0,
                total = 0,
                canSave = false,
            )
        }
    }

    private suspend fun onScanned(scan: ReceiptScan) {
        draft = scan.items
        _state.update {
            it.copy(
                scanState = UiState.Success(Unit),
                hasScan = true,
                scanId = it.scanId + 1,
                store = scan.store,
                // A receipt whose date would not read falls back to today, and says so, rather
                // than filing an old trip under a date nobody chose.
                purchasedAt = scan.purchasedAt ?: clock.nowMillis(),
                dateWasRead = scan.purchasedAt != null,
                rows = buildScannedRows(draft),
                itemCount = draft.size,
                total = draft.sumOf { item -> item.price },
                canSave = draft.isNotEmpty(),
            )
        }
        _effects.send(ScanReceiptEffect.ScanReady)
    }

    private fun onScanFailed(failure: Failure) {
        photo = null
        _state.update { it.copy(scanState = UiState.Error(failure)) }
    }

    private fun publish(items: List<ShoppingItem>) {
        draft = items
        _state.update {
            it.copy(
                rows = buildScannedRows(items),
                itemCount = items.size,
                total = items.sumOf { item -> item.price },
                canSave = items.isNotEmpty(),
            )
        }
    }

    /** "4/9/2026" and "04-09-2026" both mean the fourth of September. */
    private fun String.toEpochMillis(): Long? {
        val parts = trim().split('/', '-', '.').filter { it.isNotBlank() }
        if (parts.size != DATE_PART_COUNT) return null

        val day = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val year = parts[2].toIntOrNull() ?: return null
        return try {
            LocalDate(year, month, day)
                .atStartOfDayIn(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
        } catch (error: IllegalArgumentException) {
            null
        }
    }

    private fun String.toRupiahAmount(): Int = filter { it.isDigit() }.toIntOrNull() ?: 0

    /** "1,5" and "1.5" both mean one and a half. */
    private fun String.toQty(): Double? = trim().replace(',', '.').toDoubleOrNull()

    private companion object {
        const val DATE_PART_COUNT = 3
    }
}
