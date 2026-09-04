package com.yudha.catatanbelanja.core.domain.model

data class StockCheckLog(
    val id: String,
    val month: String,
    val checkedAt: Long,
    val entries: List<StockCheckEntry> = emptyList(),
)
