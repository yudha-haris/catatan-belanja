package com.yudha.catatanbelanja.core.data.database

import com.yudha.catatanbelanja.core.domain.model.StockCheckEntry
import com.yudha.catatanbelanja.core.domain.model.StockCheckLog
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.model.StockRate
import com.yudha.catatanbelanja.core.domain.model.StockReading
import com.yudha.catatanbelanja.db.CatatanBelanjaDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Stock rows, the month-end check logs, and the two tables behind the estimate: the readings a
 * quantity leaves behind over time and the per-item drain rate. Whether a write deserves a
 * reading is the repository's call — this only guarantees the row and its reading land together.
 */
class StockDao(
    private val database: CatatanBelanjaDatabase,
    private val dispatcher: CoroutineDispatcher,
) {

    private val stock = database.stockItemQueries
    private val logs = database.stockCheckLogQueries
    private val logItems = database.stockCheckLogItemQueries
    private val readings = database.stockReadingQueries
    private val rates = database.stockRateQueries

    suspend fun getStockItems(): List<StockItem> = withContext(dispatcher) {
        stock.selectAll().executeAsList().map { it.toDomain() }
    }

    suspend fun getStockItem(id: String): StockItem? = withContext(dispatcher) {
        stock.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    suspend fun countStockItems(): Int = withContext(dispatcher) {
        stock.countAll().executeAsOne().toInt()
    }

    /** [reading] is written in the same transaction, so the row and its history never disagree. */
    suspend fun upsertStockItem(item: StockItem, reading: StockReading? = null) =
        withContext(dispatcher) {
            database.transaction {
                upsertStockRow(item)
                if (reading == null) return@transaction
                insertReading(reading)
            }
        }

    suspend fun upsertStockItems(items: List<StockItem>, readings: List<StockReading> = emptyList()) =
        withContext(dispatcher) {
            database.transaction {
                items.forEach { upsertStockRow(it) }
                readings.forEach { insertReading(it) }
            }
        }

    /**
     * Neither stock_reading nor stock_rate can declare a foreign key onto stock_item — see
     * StockReading.sq — so the cascade SQLite would have done for free is done here instead.
     */
    suspend fun deleteStockItem(id: String) = withContext(dispatcher) {
        database.transaction {
            readings.deleteByItemId(id)
            rates.deleteByItemId(id)
            stock.deleteById(id)
        }
    }

    suspend fun deleteAllStockItems() = withContext(dispatcher) {
        database.transaction {
            readings.deleteAll()
            rates.deleteAll()
            stock.deleteAll()
        }
    }

    /** Every item's readings, oldest first, keyed by stock item id. */
    suspend fun getReadings(): Map<String, List<StockReading>> = withContext(dispatcher) {
        readings.selectAll().executeAsList().map { it.toDomain() }.groupBy { it.itemId }
    }

    suspend fun getReadings(itemId: String): List<StockReading> = withContext(dispatcher) {
        readings.selectByItemId(itemId).executeAsList().map { it.toDomain() }
    }

    suspend fun insertReadings(entries: List<StockReading>) = withContext(dispatcher) {
        if (entries.isEmpty()) return@withContext
        database.transaction { entries.forEach { insertReading(it) } }
    }

    suspend fun deleteAllReadings() = withContext(dispatcher) {
        readings.deleteAll()
    }

    /** Answers for every item, saved or not: an untouched item is on [StockRate]'s own defaults. */
    suspend fun getRates(): Map<String, StockRate> = withContext(dispatcher) {
        rates.selectAll().executeAsList().associate { row -> row.item_id to row.toDomain() }
    }

    suspend fun getRate(itemId: String): StockRate = withContext(dispatcher) {
        rates.selectByItemId(itemId).executeAsOneOrNull()?.toDomain() ?: StockRate(itemId = itemId)
    }

    suspend fun upsertRate(rate: StockRate) = withContext(dispatcher) {
        rates.upsert(
            item_id = rate.itemId,
            mode = rate.mode.name,
            manual_qty = rate.manualQty,
            manual_unit = rate.manualUnit,
            manual_period = rate.manualPeriod.name,
            updated_at = rate.updatedAt,
        )
    }

    suspend fun deleteAllRates() = withContext(dispatcher) {
        rates.deleteAll()
    }

    suspend fun getCheckLogs(): List<StockCheckLog> = withContext(dispatcher) {
        val rows = logs.selectAll().executeAsList()
        if (rows.isEmpty()) return@withContext emptyList()
        val grouped = logItems.selectByLogIds(rows.map { it.id })
            .executeAsList()
            .groupBy({ it.log_id }, { it.toDomain() })
        rows.map { row -> row.toDomain(grouped[row.id].orEmpty()) }
    }

    /** The log of [month] (`YYYY-MM`) — one log per month, as the prototype keeps it. */
    suspend fun getCheckLog(month: String): StockCheckLog? = withContext(dispatcher) {
        val row = logs.selectByMonth(month).executeAsOneOrNull() ?: return@withContext null
        row.toDomain(entriesOf(row.id))
    }

    suspend fun getCheckLogById(id: String): StockCheckLog? = withContext(dispatcher) {
        val row = logs.selectById(id).executeAsOneOrNull() ?: return@withContext null
        row.toDomain(entriesOf(row.id))
    }

    /** Writes the log row and its entries in one transaction. Returns the id actually stored. */
    suspend fun upsertCheckLog(log: StockCheckLog): String = withContext(dispatcher) {
        database.transactionWithResult {
            val logId = upsertLogRow(log.id, log.month, log.checkedAt)
            replaceEntries(logId, log.entries)
            logId
        }
    }

    suspend fun replaceCheckLogEntries(logId: String, entries: List<StockCheckEntry>) =
        withContext(dispatcher) {
            database.transaction { replaceEntries(logId, entries) }
        }

    suspend fun deleteCheckLog(id: String) = withContext(dispatcher) {
        database.transaction {
            logItems.deleteByLogId(id)
            logs.deleteById(id)
        }
    }

    suspend fun deleteAllCheckLogs() = withContext(dispatcher) {
        database.transaction {
            logItems.deleteAll()
            logs.deleteAll()
        }
    }

    private fun upsertStockRow(item: StockItem) {
        stock.upsert(
            item.id,
            item.name,
            item.qty,
            item.unit,
            item.minQty,
            item.fullQty,
            item.updatedAt,
        )
    }

    /** Every insert trims the item's own tail: a year-old reading cannot describe today's habit. */
    private fun insertReading(reading: StockReading) {
        readings.insert(
            item_id = reading.itemId,
            qty = reading.qty,
            unit = reading.unit,
            at = reading.at,
            source = reading.source.name,
        )
        readings.pruneItem(itemId = reading.itemId, keep = MAX_READINGS_PER_ITEM)
    }

    /** Keeps the month unique: an existing month keeps its id and only moves [checkedAt]. */
    private fun upsertLogRow(id: String, month: String, checkedAt: Long): String {
        val existing = logs.selectByMonth(month).executeAsOneOrNull()
        if (existing == null) {
            logs.insert(id, month, checkedAt)
            return id
        }
        logs.updateCheckedAt(checkedAt = checkedAt, id = existing.id)
        return existing.id
    }

    private fun replaceEntries(logId: String, entries: List<StockCheckEntry>) {
        logItems.deleteByLogId(logId)
        entries.forEach { logItems.insert(logId, it.name, it.qty, it.unit) }
    }

    private fun entriesOf(logId: String): List<StockCheckEntry> =
        logItems.selectByLogId(logId).executeAsList().map { it.toDomain() }

    private companion object {
        const val MAX_READINGS_PER_ITEM = 40L
    }
}
