package com.yudha.catatanbelanja.features.stock.domain.model

/** One pre-filled line of the month-end "Cek sisa stok" sheet. */
data class StockCheckRow(
    val id: String,
    val name: String,
    val emoji: String,
    val unit: String,
    val previousQty: Double,
    /**
     * What the app reckons is left, offered as the line's placeholder so a fifteen-item check is
     * mostly confirming numbers rather than inventing them. Null when there is no estimate — the
     * line then starts blank, exactly as it always has.
     */
    val estimatedQty: Double? = null,
)
