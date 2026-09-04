package com.yudha.catatanbelanja.core.domain.model

data class ShoppingSession(
    val id: String,
    val name: String = "",
    val store: String = "",
    val startedAt: Long,
    val endedAt: Long? = null,
    val items: List<ShoppingItem> = emptyList(),
    /**
     * Absolute path of the photographed paper receipt, or null when the trip has none. The file
     * lives outside the database — see [com.yudha.catatanbelanja.core.domain.service.ImageStore] —
     * so a path that no longer resolves means the picture was cleared from underneath the app, and
     * the UI falls back to its "no photo" state rather than treating it as an error.
     */
    val receiptPhoto: String? = null,
)
