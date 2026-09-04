package com.yudha.catatanbelanja.core.domain.model

data class ShoppingItem(
    val id: String,
    val name: String,
    val price: Int,
    val qty: Double? = null,
    val unit: String? = null,
    val note: String = "",
)
