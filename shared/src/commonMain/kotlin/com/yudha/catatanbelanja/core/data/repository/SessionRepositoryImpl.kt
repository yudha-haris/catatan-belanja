package com.yudha.catatanbelanja.core.data.repository

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.Failure
import com.yudha.catatanbelanja.core.common.IdGenerator
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.dataOrNull
import com.yudha.catatanbelanja.core.common.resourceOf
import com.yudha.catatanbelanja.core.data.database.SessionDao
import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository

/**
 * Item ordering mirrors the prototype's `items.unshift(it)`: `session_item.position` ascending
 * means newest first. A new item takes `min(position) - 1`, or `0` when the session is empty, so
 * deleting an item never forces a renumber and the remaining order stays intact.
 */
class SessionRepositoryImpl(
    private val sessionDao: SessionDao,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) : SessionRepository {

    override suspend fun getFinishedSessions(): Resource<List<ShoppingSession>> =
        resourceOf(MSG_LOAD_FINISHED) { sessionDao.getFinishedSessions() }

    override suspend fun getActiveSession(): Resource<ShoppingSession?> =
        resourceOf(MSG_LOAD_ACTIVE) { sessionDao.getActiveSession() }

    override suspend fun getSession(id: String): Resource<ShoppingSession?> =
        resourceOf(MSG_LOAD_SESSION) { sessionDao.getSession(id) }

    override suspend fun startSession(store: String): Resource<ShoppingSession> {
        val active = getActiveSession()
        if (active is Resource.Error) return active
        if (active.dataOrNull() != null) {
            return Resource.Error(
                Failure(message = MSG_ACTIVE_SESSION_EXISTS, code = ACTIVE_SESSION_EXISTS),
            )
        }
        return resourceOf(MSG_START_SESSION) {
            val session = ShoppingSession(
                id = idGenerator.next(),
                store = store,
                startedAt = clock.nowMillis(),
            )
            sessionDao.insertSession(session)
            session
        }
    }

    override suspend fun updateStore(sessionId: String, store: String): Resource<Unit> =
        resourceOf(MSG_UPDATE_STORE) { sessionDao.updateStore(sessionId, store) }

    override suspend fun addItem(sessionId: String, item: ShoppingItem): Resource<Unit> =
        resourceOf(MSG_ADD_ITEM) {
            val head = sessionDao.minItemPosition(sessionId)
            val position = head?.minus(1L) ?: FIRST_POSITION
            sessionDao.insertItem(sessionId, item, position)
        }

    override suspend fun updateItem(sessionId: String, item: ShoppingItem): Resource<Unit> =
        resourceOf(MSG_UPDATE_ITEM) { sessionDao.updateItem(sessionId, item) }

    override suspend fun deleteItem(sessionId: String, itemId: String): Resource<Unit> =
        resourceOf(MSG_DELETE_ITEM) { sessionDao.deleteItem(sessionId, itemId) }

    override suspend fun finishSession(sessionId: String, name: String): Resource<Unit> =
        resourceOf(MSG_FINISH_SESSION) {
            sessionDao.finishSession(sessionId, name, clock.nowMillis())
        }

    override suspend fun cancelActiveSession(): Resource<Unit> {
        val active = getActiveSession()
        if (active is Resource.Error) return active
        val session = active.dataOrNull() ?: return Resource.Success(Unit)
        return resourceOf(MSG_CANCEL_SESSION) { sessionDao.deleteSession(session.id) }
    }

    override suspend fun deleteSession(sessionId: String): Resource<Unit> =
        resourceOf(MSG_DELETE_SESSION) { sessionDao.deleteSession(sessionId) }

    companion object {
        /** [Failure.code] returned when a second session is started while one is still open. */
        const val ACTIVE_SESSION_EXISTS = "ACTIVE_SESSION_EXISTS"

        private const val FIRST_POSITION = 0L
        private const val MSG_LOAD_FINISHED = "Failed to load finished sessions"
        private const val MSG_LOAD_ACTIVE = "Failed to load the active session"
        private const val MSG_LOAD_SESSION = "Failed to load the session"
        private const val MSG_START_SESSION = "Failed to start a shopping session"
        private const val MSG_ACTIVE_SESSION_EXISTS = "A shopping session is already active"
        private const val MSG_UPDATE_STORE = "Failed to update the store name"
        private const val MSG_ADD_ITEM = "Failed to add the item"
        private const val MSG_UPDATE_ITEM = "Failed to update the item"
        private const val MSG_DELETE_ITEM = "Failed to delete the item"
        private const val MSG_FINISH_SESSION = "Failed to finish the session"
        private const val MSG_CANCEL_SESSION = "Failed to cancel the active session"
        private const val MSG_DELETE_SESSION = "Failed to delete the session"
    }
}
