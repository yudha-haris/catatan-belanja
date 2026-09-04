package com.yudha.catatanbelanja.features.history.presentation

sealed interface HistoryEffect {
    data class OpenDetail(val sessionId: String) : HistoryEffect

    /** [aId] is always the older session, [bId] the newer one. */
    data class OpenCompare(val aId: String, val bId: String) : HistoryEffect

    data object DemoSeeded : HistoryEffect
}
