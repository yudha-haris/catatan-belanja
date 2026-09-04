package com.yudha.catatanbelanja.features.receipt.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.features.receipt.domain.model.ScannedItemRow

/**
 * [scanState] covers reading the photo, [actionState] saving the result — two separate flows on
 * one screen, so they get the two states `docs/architecture.md` §3 allows rather than sharing one
 * and making a save failure look like a failed scan.
 *
 * [hasScan] rather than checking whether [rows] is empty: a scan whose every row the user then
 * deleted is still a scan, and must not drop the screen back to its "take a photo" state.
 *
 * [scanId] counts scans. The review screen's name, store and date fields are Compose-local text
 * buffers — editing one must not be fought by a reseed on the next state emission — so they are
 * keyed on this rather than on the values they were seeded from, which can repeat between scans.
 */
data class ScanReceiptState(
    val scanState: UiState<Unit> = UiState.Initial,
    val actionState: UiState<Unit> = UiState.Initial,
    val available: Boolean = true,
    val hasScan: Boolean = false,
    val scanId: Int = 0,
    val store: String = "",
    val purchasedAt: Long = 0L,
    val dateWasRead: Boolean = false,
    val rows: List<ScannedItemRow> = emptyList(),
    val itemCount: Int = 0,
    val total: Int = 0,
    val canSave: Boolean = false,
)
