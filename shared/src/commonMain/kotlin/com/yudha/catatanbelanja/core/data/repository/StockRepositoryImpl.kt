package com.yudha.catatanbelanja.core.data.repository

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.IdGenerator
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.resourceOf
import com.yudha.catatanbelanja.core.common.toMonthKey
import com.yudha.catatanbelanja.core.data.database.StockDao
import com.yudha.catatanbelanja.core.domain.model.ReadingSource
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockCheckEntry
import com.yudha.catatanbelanja.core.domain.model.StockCheckLog
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.model.StockRate
import com.yudha.catatanbelanja.core.domain.model.StockReading
import com.yudha.catatanbelanja.core.domain.repository.StockRepository

class StockRepositoryImpl(
    private val stockDao: StockDao,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) : StockRepository {

    override suspend fun getStockItems(): Resource<List<StockItem>> =
        resourceOf(MSG_LOAD_STOCK) { stockDao.getStockItems() }

    /**
     * A quantity that moved is an observation and leaves a reading behind. A quantity that did
     * not is not: saving the row to change its reminder threshold keeps the old `updatedAt`, so
     * the estimate goes on counting from when the amount was last actually known rather than
     * silently resetting itself to "as of just now".
     */
    override suspend fun upsertStockItem(item: StockItem): Resource<Unit> =
        resourceOf(MSG_SAVE_STOCK) {
            val existing = stockDao.getStockItem(item.id)
            val moved = existing == null || existing.qty != item.qty || existing.unit != item.unit
            if (!moved) {
                stockDao.upsertStockItem(item.copy(updatedAt = existing.updatedAt))
                return@resourceOf
            }
            stockDao.upsertStockItem(item, readingOf(item, ReadingSource.MANUAL))
        }

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
                // A restock is the start of the next consumption window, not part of the last one.
                stockDao.upsertStockItem(folded, readingOf(folded, ReadingSource.PURCHASE))
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
     * Backfills lazily, per item, for anything that arrived without a history: an install that
     * predates smart stock, a restored backup, the demo data. Such an item usually still has
     * months of month-end checks behind it — each one a dated record of what was left, which is
     * exactly the observation a rate is derived from. Doing this here rather than in 3.sqm is what
     * lets it match names with [normalized] the way the check sheet itself does.
     *
     * Self-limiting: seeding an item always writes at least its current quantity, so an item is
     * only ever backfilled once.
     */
    override suspend fun getReadings(): Resource<Map<String, List<StockReading>>> =
        resourceOf(MSG_LOAD_READINGS) {
            val existing = stockDao.getReadings()
            val missing = stockDao.getStockItems().filterNot { existing.containsKey(it.id) }
            if (missing.isEmpty()) return@resourceOf existing
            stockDao.insertReadings(backfilledReadings(missing))
            stockDao.getReadings()
        }

    override suspend fun getRates(): Resource<Map<String, StockRate>> =
        resourceOf(MSG_LOAD_RATES) { stockDao.getRates() }

    override suspend fun saveRate(rate: StockRate): Resource<Unit> =
        resourceOf(MSG_SAVE_RATE) {
            stockDao.upsertRate(rate.copy(updatedAt = clock.nowMillis()))
        }

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

    /**
     * The cek-stok sheet is also the moment the real remaining amounts land on the stock rows —
     * and the single richest source of readings the app has, since it dates every item at once.
     */
    private suspend fun writeBackQuantities(entries: List<StockCheckEntry>, now: Long) {
        if (entries.isEmpty()) return
        val byName = entries.associateBy { it.name.normalized() }
        val updated = mutableListOf<StockItem>()
        val readings = mutableListOf<StockReading>()
        stockDao.getStockItems().forEach { item ->
            val entry = byName[item.name.normalized()] ?: return@forEach
            val next = item.copy(qty = entry.qty, updatedAt = now)
            updated += next
            readings += readingOf(next, ReadingSource.CHECK)
        }
        stockDao.upsertStockItems(updated, readings)
    }

    /**
     * Every past check that names a stock item, plus the quantity each row carries right now. The
     * current-state reading is skipped where a check already spoke for that same instant, so the
     * newest reading of an item is always the one the estimate counts down from.
     */
    private suspend fun backfilledReadings(items: List<StockItem>): List<StockReading> {
        val byName = items.associateBy { it.name.normalized() }
        val fromLogs = stockDao.getCheckLogs().flatMap { log ->
            log.entries.mapNotNull { entry ->
                val item = byName[entry.name.normalized()] ?: return@mapNotNull null
                if (log.checkedAt == item.updatedAt) return@mapNotNull null
                StockReading(
                    itemId = item.id,
                    qty = entry.qty,
                    unit = entry.unit,
                    at = log.checkedAt,
                    source = ReadingSource.CHECK,
                )
            }
        }
        return fromLogs + items.map { readingOf(it, ReadingSource.MANUAL) }
    }

    private fun readingOf(item: StockItem, source: ReadingSource): StockReading = StockReading(
        itemId = item.id,
        qty = item.qty,
        unit = item.unit,
        at = item.updatedAt,
        source = source,
    )

    private companion object {
        const val DEFAULT_UNIT = "pcs"
        const val MSG_LOAD_STOCK = "Failed to load stock items"
        const val MSG_SAVE_STOCK = "Failed to save the stock item"
        const val MSG_DELETE_STOCK = "Failed to delete the stock item"
        const val MSG_ADD_TO_STOCK = "Failed to add the session to stock"
        const val MSG_LOAD_LOGS = "Failed to load stock check logs"
        const val MSG_SAVE_CHECK = "Failed to save the stock check"
        const val MSG_DELETE_LOG = "Failed to delete the stock check log"
        const val MSG_LOAD_READINGS = "Failed to load stock readings"
        const val MSG_LOAD_RATES = "Failed to load stock usage rates"
        const val MSG_SAVE_RATE = "Failed to save the stock usage rate"
    }
}
