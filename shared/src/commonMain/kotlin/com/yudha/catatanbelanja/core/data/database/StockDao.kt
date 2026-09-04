package com.yudha.catatanbelanja.core.data.database

import com.yudha.catatanbelanja.core.domain.model.StockCheckEntry
import com.yudha.catatanbelanja.core.domain.model.StockCheckLog
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.db.CatatanBelanjaDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class StockDao(
    private val database: CatatanBelanjaDatabase,
    private val dispatcher: CoroutineDispatcher,
) {

    private val stock = database.stockItemQueries
    private val logs = database.stockCheckLogQueries
    private val logItems = database.stockCheckLogItemQueries

    suspend fun getStockItems(): List<StockItem> = withContext(dispatcher) {
        stock.selectAll().executeAsList().map { it.toDomain() }
    }

    suspend fun getStockItem(id: String): StockItem? = withContext(dispatcher) {
        stock.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    suspend fun countStockItems(): Int = withContext(dispatcher) {
        stock.countAll().executeAsOne().toInt()
    }

    suspend fun upsertStockItem(item: StockItem) = withContext(dispatcher) {
        upsertStockRow(item)
    }

    suspend fun upsertStockItems(items: List<StockItem>) = withContext(dispatcher) {
        database.transaction {
            items.forEach { upsertStockRow(it) }
        }
    }

    suspend fun deleteStockItem(id: String) = withContext(dispatcher) {
        stock.deleteById(id)
    }

    suspend fun deleteAllStockItems() = withContext(dispatcher) {
        stock.deleteAll()
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
}
