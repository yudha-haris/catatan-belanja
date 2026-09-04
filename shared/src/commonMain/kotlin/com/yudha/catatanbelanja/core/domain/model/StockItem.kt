package com.yudha.catatanbelanja.core.domain.model

data class StockItem(
    val id: String,
    val name: String,
    val qty: Double = 0.0,
    val unit: String = "pcs",
    val minQty: Double? = null,
    val fullQty: Double = 0.0,
    val updatedAt: Long,
)
