package com.yudha.catatanbelanja.features.stock.domain.model

/** One pre-filled line of the month-end "Cek sisa stok" sheet. */
data class StockCheckRow(
    val id: String,
    val name: String,
    val emoji: String,
    val unit: String,
    val previousQty: Double,
)
