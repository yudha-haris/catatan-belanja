package com.yudha.catatanbelanja.core.domain.repository

import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockCheckEntry
import com.yudha.catatanbelanja.core.domain.model.StockCheckLog
import com.yudha.catatanbelanja.core.domain.model.StockItem

interface StockRepository {
    suspend fun getStockItems(): Resource<List<StockItem>>
    suspend fun upsertStockItem(item: StockItem): Resource<Unit>
    suspend fun deleteStockItem(id: String): Resource<Unit>

    /** Adds every item of [session] that has a qty into stock. Returns how many were added. */
    suspend fun addSessionToStock(session: ShoppingSession): Resource<Int>

    suspend fun getCheckLogs(): Resource<List<StockCheckLog>>

    /** Upserts the log for the current month and updates every stock item's qty. */
    suspend fun saveStockCheck(entries: List<StockCheckEntry>): Resource<Unit>

    suspend fun deleteCheckLog(id: String): Resource<Unit>
}
