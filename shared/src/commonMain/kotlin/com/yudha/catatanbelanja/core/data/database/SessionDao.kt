package com.yudha.catatanbelanja.core.data.database

import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.db.CatatanBelanjaDatabase
import com.yudha.catatanbelanja.db.SelectFinished
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SessionDao(
    private val database: CatatanBelanjaDatabase,
    private val dispatcher: CoroutineDispatcher,
) {

    private val sessions = database.sessionQueries
    private val items = database.sessionItemQueries

    // The price-trend corrections hang off session items but carry no foreign key (see
    // TrendQtyOverride.sq), so every delete below takes them with it inside the same
    // transaction. A correction that outlived its receipt would be invisible and permanent.
    private val trendOverrides = database.trendQtyOverrideQueries

    suspend fun getFinishedSessions(): List<ShoppingSession> = withContext(dispatcher) {
        hydrate(sessions.selectFinished().executeAsList())
    }

    suspend fun getActiveSession(): ShoppingSession? = withContext(dispatcher) {
        val row = sessions.selectActive().executeAsOneOrNull() ?: return@withContext null
        row.toDomain(itemsOf(row.id))
    }

    suspend fun getSession(id: String): ShoppingSession? = withContext(dispatcher) {
        val row = sessions.selectById(id).executeAsOneOrNull() ?: return@withContext null
        row.toDomain(itemsOf(row.id))
    }

    /** Items of every id in [sessionIds] in one query, keyed by session id. */
    suspend fun getItemsForSessions(
        sessionIds: Collection<String>,
    ): Map<String, List<ShoppingItem>> = withContext(dispatcher) { groupItems(sessionIds) }

    suspend fun countSessions(): Int = withContext(dispatcher) {
        sessions.countAll().executeAsOne().toInt()
    }

    /** The head of the list — `null` when the session has no items yet. */
    suspend fun minItemPosition(sessionId: String): Long? = withContext(dispatcher) {
        items.selectMinPosition(sessionId).executeAsOneOrNull()
    }

    /** Inserts the session row and its items, keeping the list order as the item order. */
    suspend fun insertSession(session: ShoppingSession) = withContext(dispatcher) {
        database.transaction {
            sessions.insertSession(
                session.id,
                session.name,
                session.store,
                session.startedAt,
                session.endedAt,
            )
            session.items.forEachIndexed { index, item ->
                insertItemRow(session.id, item, index.toLong())
            }
        }
    }

    suspend fun updateStore(sessionId: String, store: String) = withContext(dispatcher) {
        sessions.updateStore(store = store, id = sessionId)
    }

    suspend fun finishSession(sessionId: String, name: String, endedAt: Long) =
        withContext(dispatcher) {
            sessions.finish(name = name, endedAt = endedAt, id = sessionId)
        }

    suspend fun deleteSession(sessionId: String) = withContext(dispatcher) {
        database.transaction {
            trendOverrides.deleteBySessionId(sessionId)
            items.deleteBySessionId(sessionId)
            sessions.deleteById(sessionId)
        }
    }

    suspend fun deleteAllSessions() = withContext(dispatcher) {
        database.transaction {
            trendOverrides.deleteAll()
            items.deleteAll()
            sessions.deleteAll()
        }
    }

    suspend fun insertItem(sessionId: String, item: ShoppingItem, position: Long) =
        withContext(dispatcher) {
            insertItemRow(sessionId, item, position)
        }

    /**
     * Editing the receipt drops any price-trend correction for that item. The correction only ever
     * stood in for what the receipt failed to record; once the receipt itself has been rewritten it
     * is a stale second opinion, and a stale one the user cannot see from the edit sheet.
     */
    suspend fun updateItem(sessionId: String, item: ShoppingItem) = withContext(dispatcher) {
        database.transaction {
            items.update(
                name = item.name,
                qty = item.qty,
                unit = item.unit,
                price = item.price.toLong(),
                note = item.note,
                id = item.id,
                sessionId = sessionId,
            )
            trendOverrides.deleteByItemId(item.id)
        }
    }

    suspend fun deleteItem(sessionId: String, itemId: String) = withContext(dispatcher) {
        database.transaction {
            trendOverrides.deleteByItemId(itemId)
            items.deleteById(id = itemId, sessionId = sessionId)
        }
    }

    private fun insertItemRow(sessionId: String, item: ShoppingItem, position: Long) {
        items.insert(
            item.id,
            sessionId,
            item.name,
            item.qty,
            item.unit,
            item.price.toLong(),
            item.note,
            position,
        )
    }

    private fun hydrate(rows: List<SelectFinished>): List<ShoppingSession> {
        if (rows.isEmpty()) return emptyList()
        val grouped = groupItems(rows.map { it.id })
        return rows.map { row -> row.toDomain(grouped[row.id].orEmpty()) }
    }

    private fun groupItems(sessionIds: Collection<String>): Map<String, List<ShoppingItem>> {
        if (sessionIds.isEmpty()) return emptyMap()
        return items.selectBySessionIds(sessionIds)
            .executeAsList()
            .groupBy({ it.session_id }, { it.toDomain() })
    }

    private fun itemsOf(sessionId: String): List<ShoppingItem> =
        items.selectBySessionId(sessionId).executeAsList().map { it.toDomain() }
}
