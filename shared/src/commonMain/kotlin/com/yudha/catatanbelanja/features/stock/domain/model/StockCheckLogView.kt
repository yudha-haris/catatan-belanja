package com.yudha.catatanbelanja.features.stock.domain.model

import com.yudha.catatanbelanja.core.domain.model.StockCheckLog

/** A row in "Riwayat cek stok" — the log plus the two counts its subtitle shows. */
data class StockCheckLogView(
    val log: StockCheckLog,
    val itemCount: Int,
    val outCount: Int,
)
