package com.yudha.catatanbelanja.core.domain.repository

import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockCheckEntry
import com.yudha.catatanbelanja.core.domain.model.StockCheckLog
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.model.StockRate
import com.yudha.catatanbelanja.core.domain.model.StockReading

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

    /**
     * Every item's readings, oldest first, keyed by stock item id — the evidence an automatic
     * drain rate is derived from. Written by this repository itself: a quantity that moves leaves
     * a reading behind, and a save that only changes the reminder threshold leaves none.
     */
    suspend fun getReadings(): Resource<Map<String, List<StockReading>>>

    /** Saved rates keyed by item id. An item with no entry is on [StockRate]'s own defaults. */
    suspend fun getRates(): Resource<Map<String, StockRate>>

    suspend fun saveRate(rate: StockRate): Resource<Unit>
}
