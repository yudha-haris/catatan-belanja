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
            items.deleteBySessionId(sessionId)
            sessions.deleteById(sessionId)
        }
    }

    suspend fun deleteAllSessions() = withContext(dispatcher) {
        database.transaction {
            items.deleteAll()
            sessions.deleteAll()
        }
    }

    suspend fun insertItem(sessionId: String, item: ShoppingItem, position: Long) =
        withContext(dispatcher) {
            insertItemRow(sessionId, item, position)
        }

    suspend fun updateItem(sessionId: String, item: ShoppingItem) = withContext(dispatcher) {
        items.update(
            name = item.name,
            qty = item.qty,
            unit = item.unit,
            price = item.price.toLong(),
            note = item.note,
            id = item.id,
            sessionId = sessionId,
        )
    }

    suspend fun deleteItem(sessionId: String, itemId: String) = withContext(dispatcher) {
        items.deleteById(id = itemId, sessionId = sessionId)
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
