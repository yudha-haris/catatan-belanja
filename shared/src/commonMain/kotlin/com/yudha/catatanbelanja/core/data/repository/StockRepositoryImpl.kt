package com.yudha.catatanbelanja.core.data.repository

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.IdGenerator
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.resourceOf
import com.yudha.catatanbelanja.core.common.toMonthKey
import com.yudha.catatanbelanja.core.data.database.StockDao
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockCheckEntry
import com.yudha.catatanbelanja.core.domain.model.StockCheckLog
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.repository.StockRepository

class StockRepositoryImpl(
    private val stockDao: StockDao,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) : StockRepository {

    override suspend fun getStockItems(): Resource<List<StockItem>> =
        resourceOf(MSG_LOAD_STOCK) { stockDao.getStockItems() }

    override suspend fun upsertStockItem(item: StockItem): Resource<Unit> =
        resourceOf(MSG_SAVE_STOCK) { stockDao.upsertStockItem(item) }

    override suspend fun deleteStockItem(id: String): Resource<Unit> =
        resourceOf(MSG_DELETE_STOCK) { stockDao.deleteStockItem(id) }

    override suspend fun addSessionToStock(session: ShoppingSession): Resource<Int> =
        resourceOf(MSG_ADD_TO_STOCK) {
            val purchased = session.items.filter { (it.qty ?: 0.0) > 0.0 }
            if (purchased.isEmpty()) return@resourceOf 0

            val now = clock.nowMillis()
            val byName = stockDao.getStockItems().associateByTo(
                destination = mutableMapOf(),
                keySelector = { it.name.normalized() },
            )
            purchased.forEach { item ->
                val key = item.name.normalized()
                val folded = foldIntoStock(
                    existing = byName[key],
                    name = item.name,
                    qty = item.qty ?: 0.0,
                    unit = item.unit ?: DEFAULT_UNIT,
                    now = now,
                )
                stockDao.upsertStockItem(folded)
                byName[key] = folded
            }
            purchased.size
        }

    override suspend fun getCheckLogs(): Resource<List<StockCheckLog>> =
        resourceOf(MSG_LOAD_LOGS) { stockDao.getCheckLogs() }

    override suspend fun saveStockCheck(entries: List<StockCheckEntry>): Resource<Unit> =
        resourceOf(MSG_SAVE_CHECK) {
            val now = clock.nowMillis()
            val month = now.toMonthKey()
            val existing = stockDao.getCheckLog(month)
            stockDao.upsertCheckLog(
                StockCheckLog(
                    id = existing?.id ?: idGenerator.next(),
                    month = month,
                    checkedAt = now,
                    entries = entries,
                ),
            )
            writeBackQuantities(entries, now)
        }

    override suspend fun deleteCheckLog(id: String): Resource<Unit> =
        resourceOf(MSG_DELETE_LOG) { stockDao.deleteCheckLog(id) }

    /**
     * One purchased line folded into its stock row. Same unit accumulates; a different unit
     * replaces qty and unit with the purchased ones, because the old amount is no longer
     * comparable. `fullQty` is the high-water mark that drives the level bar.
     */
    private fun foldIntoStock(
        existing: StockItem?,
        name: String,
        qty: Double,
        unit: String,
        now: Long,
    ): StockItem {
        if (existing == null) {
            return StockItem(
                id = idGenerator.next(),
                name = name,
                qty = qty,
                unit = unit,
                fullQty = qty,
                updatedAt = now,
            )
        }
        val newQty = if (existing.unit == unit) existing.qty + qty else qty
        return existing.copy(
            qty = newQty,
            unit = unit,
            fullQty = maxOf(existing.fullQty, newQty),
            updatedAt = now,
        )
    }

    /** The cek-stok sheet is also the moment the real remaining amounts land on the stock rows. */
    private suspend fun writeBackQuantities(entries: List<StockCheckEntry>, now: Long) {
        if (entries.isEmpty()) return
        val byName = entries.associateBy { it.name.normalized() }
        stockDao.getStockItems().forEach { item ->
            val entry = byName[item.name.normalized()] ?: return@forEach
            stockDao.upsertStockItem(item.copy(qty = entry.qty, updatedAt = now))
        }
    }

    private companion object {
        const val DEFAULT_UNIT = "pcs"
        const val MSG_LOAD_STOCK = "Failed to load stock items"
        const val MSG_SAVE_STOCK = "Failed to save the stock item"
        const val MSG_DELETE_STOCK = "Failed to delete the stock item"
        const val MSG_ADD_TO_STOCK = "Failed to add the session to stock"
        const val MSG_LOAD_LOGS = "Failed to load stock check logs"
        const val MSG_SAVE_CHECK = "Failed to save the stock check"
        const val MSG_DELETE_LOG = "Failed to delete the stock check log"
    }
}
