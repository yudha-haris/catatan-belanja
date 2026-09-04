package com.yudha.catatanbelanja.features.dashboard.presentation

sealed interface PriceTrendEffect {
    /**
     * The screen picks the copy. [Kind.INVALID_QTY] is the only refusal here — every other action
     * on this page is a toggle that cannot fail on the user's side.
     */
    data class ShowMessage(val kind: Kind) : PriceTrendEffect

    enum class Kind {
        INVALID_QTY,
        ADJUSTMENT_SAVED,
        ADJUSTMENT_CLEARED,
    }
}
