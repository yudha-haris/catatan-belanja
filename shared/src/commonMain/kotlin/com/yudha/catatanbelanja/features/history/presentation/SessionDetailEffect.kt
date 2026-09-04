package com.yudha.catatanbelanja.features.history.presentation

sealed interface SessionDetailEffect {
    data object NotFound : SessionDetailEffect

    data object Deleted : SessionDetailEffect

    data object ItemSaved : SessionDetailEffect

    data object ItemDeleted : SessionDetailEffect

    /** [aId] is always the older session, [bId] the newer one. */
    data class OpenCompare(val aId: String, val bId: String) : SessionDetailEffect

    /** A fresh session was started; the live screen seeds its "sering dibeli" from [itemNames]. */
    data class OpenLiveSession(val itemNames: List<String>) : SessionDetailEffect

    data object ActiveSessionRunning : SessionDetailEffect
}
