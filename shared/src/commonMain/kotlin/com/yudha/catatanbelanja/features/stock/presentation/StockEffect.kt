package com.yudha.catatanbelanja.features.stock.presentation

/** One-shot outcomes of the Stok screen. The screen picks the copy; this only says what happened. */
sealed interface StockEffect {
    data object NameRequired : StockEffect

    data object ItemSaved : StockEffect

    data object ItemDeleted : StockEffect

    data class ItemMarkedEmpty(val name: String) : StockEffect

    /** The month-end check landed — the screen celebrates and toasts [month] ("YYYY-MM"). */
    data class CheckSaved(val month: String) : StockEffect

    data object LogDeleted : StockEffect
}
