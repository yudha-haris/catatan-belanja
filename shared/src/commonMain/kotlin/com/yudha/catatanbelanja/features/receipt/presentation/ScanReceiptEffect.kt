package com.yudha.catatanbelanja.features.receipt.presentation

sealed interface ScanReceiptEffect {
    /** The draft is on screen; the picker sheet can close. */
    data object ScanReady : ScanReceiptEffect

    /** The typed date is not a date. The save is refused and nothing is written. */
    data object InvalidDate : ScanReceiptEffect

    /** Written. [sessionId] is the trip the screen opens next. */
    data class Saved(val sessionId: String) : ScanReceiptEffect

    data object ItemDeleted : ScanReceiptEffect
}
