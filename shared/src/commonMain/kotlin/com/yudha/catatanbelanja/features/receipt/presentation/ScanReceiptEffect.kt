package com.yudha.catatanbelanja.features.receipt.presentation

sealed interface ScanReceiptEffect {
    /** There is a connection; the screen may open the camera / gallery sheet. */
    data object OpenPhotoSource : ScanReceiptEffect

    /**
     * There is no connection, so the sheet does not open at all. Said before the photo is taken
     * rather than after: a scan needs the network, and finding that out having already
     * photographed the receipt and waited is the same news delivered too late to be useful.
     */
    data object Offline : ScanReceiptEffect

    /** The draft is on screen; the picker sheet can close. */
    data object ScanReady : ScanReceiptEffect

    /** The typed date is not a date. The save is refused and nothing is written. */
    data object InvalidDate : ScanReceiptEffect

    /** Written. [sessionId] is the trip the screen opens next. */
    data class Saved(val sessionId: String) : ScanReceiptEffect

    data object ItemDeleted : ScanReceiptEffect
}
