package com.yudha.catatanbelanja.core.domain.model

data class ShoppingSession(
    val id: String,
    val name: String = "",
    val store: String = "",
    val startedAt: Long,
    val endedAt: Long? = null,
    val items: List<ShoppingItem> = emptyList(),
)
