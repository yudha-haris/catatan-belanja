package com.yudha.catatanbelanja.features.stock.presentation

import com.yudha.catatanbelanja.core.domain.model.RateMode

/** One-shot outcomes of the Stok screen. The screen picks the copy; this only says what happened. */
sealed interface StockEffect {
    data object NameRequired : StockEffect

    data object ItemSaved : StockEffect

    data object ItemDeleted : StockEffect

    data class ItemMarkedEmpty(val name: String) : StockEffect

    /**
     * The saved quantity landed close to what the estimate had predicted. Emitted instead of
     * [ItemSaved], so a hit reads as one piece of good news rather than two notifications.
     */
    data class EstimateHit(val accuracyPercent: Int) : StockEffect

    /** The drain rate changed; [mode] decides which of the three confirmations the screen shows. */
    data class RateSaved(val mode: RateMode) : StockEffect

    /** The month-end check landed — the screen celebrates and toasts [month] ("YYYY-MM"). */
    data class CheckSaved(val month: String) : StockEffect

    data object LogDeleted : StockEffect
}
