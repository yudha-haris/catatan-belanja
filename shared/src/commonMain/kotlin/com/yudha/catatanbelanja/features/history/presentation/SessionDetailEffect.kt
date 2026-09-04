package com.yudha.catatanbelanja.features.history.presentation

sealed interface SessionDetailEffect {
    data object NotFound : SessionDetailEffect

    data object Deleted : SessionDetailEffect

    data object ItemSaved : SessionDetailEffect

    data object ItemDeleted : SessionDetailEffect

    data object PhotoAttached : SessionDetailEffect

    data object PhotoRemoved : SessionDetailEffect

    /** The share sheet has been handed the rendered receipt; the screen can close its preview. */
    data object ReceiptShared : SessionDetailEffect

    /** [aId] is always the older session, [bId] the newer one. */
    data class OpenCompare(val aId: String, val bId: String) : SessionDetailEffect

    /** A fresh session was started; the live screen seeds its "sering dibeli" from [itemNames]. */
    data class OpenLiveSession(val itemNames: List<String>) : SessionDetailEffect

    data object ActiveSessionRunning : SessionDetailEffect
}
