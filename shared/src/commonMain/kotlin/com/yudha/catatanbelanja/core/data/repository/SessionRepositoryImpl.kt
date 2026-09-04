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
import com.yudha.catatanbelanja.core.domain.service.FileSharer
import com.yudha.catatanbelanja.core.domain.service.ImageStore

/**
 * Item ordering mirrors the prototype's `items.unshift(it)`: `session_item.position` ascending
 * means newest first. A new item takes `min(position) - 1`, or `0` when the session is empty, so
 * deleting an item never forces a renumber and the remaining order stays intact.
 */
class SessionRepositoryImpl(
    private val sessionDao: SessionDao,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
    private val imageStore: ImageStore,
    private val fileSharer: FileSharer,
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
        return resourceOf(MSG_CANCEL_SESSION) { deleteSessionAndPhoto(session.id) }
    }

    override suspend fun deleteSession(sessionId: String): Resource<Unit> =
        resourceOf(MSG_DELETE_SESSION) { deleteSessionAndPhoto(sessionId) }

    /**
     * The id is generated up front so the photo can be written *before* the row that points at it,
     * which lets the session, its items and its photo row all land in [SessionDao]'s one
     * transaction. Import the other way round — insert, then attach — and a failed write leaves a
     * saved trip the user would have to re-scan a picture onto.
     */
    override suspend fun importFinishedSession(
        name: String,
        store: String,
        purchasedAt: Long,
        items: List<ShoppingItem>,
        photo: ByteArray?,
    ): Resource<String> = resourceOf(MSG_IMPORT_SESSION) {
        val sessionId = idGenerator.next()
        val path = photo?.let { imageStore.save(PHOTO_PREFIX + sessionId + PHOTO_SUFFIX, it) }
        sessionDao.insertSession(
            ShoppingSession(
                id = sessionId,
                name = name,
                store = store,
                startedAt = purchasedAt,
                endedAt = purchasedAt,
                items = items,
                receiptPhoto = path,
            ),
        )
        sessionId
    }

    /**
     * The picture replaces whatever was there, and the old file goes with it. The file is named
     * after the session itself, so a trip owns exactly one receipt photo however many times the
     * shot is retaken.
     */
    override suspend fun attachReceiptPhoto(sessionId: String, bytes: ByteArray): Resource<Unit> =
        resourceOf(MSG_ATTACH_PHOTO) {
            val previous = sessionDao.getPhotoPath(sessionId)
            val path = imageStore.save(PHOTO_PREFIX + sessionId + PHOTO_SUFFIX, bytes)
            sessionDao.setPhoto(sessionId, path, clock.nowMillis())
            // Retaking the shot writes the same name, so this only bites if the store ever hands
            // back a different one. Deleting the file just written is the bug worth avoiding.
            if (previous != null && previous != path) imageStore.delete(previous)
        }

    override suspend fun removeReceiptPhoto(sessionId: String): Resource<Unit> =
        resourceOf(MSG_REMOVE_PHOTO) {
            val path = sessionDao.getPhotoPath(sessionId)
            sessionDao.clearPhoto(sessionId)
            if (path != null) imageStore.delete(path)
        }

    override suspend fun shareReceiptImage(sessionId: String, image: ByteArray): Resource<Unit> =
        resourceOf(MSG_SHARE_RECEIPT) {
            fileSharer.shareImage(
                fileName = SHARE_PREFIX + sessionId + SHARE_SUFFIX,
                mimeType = SHARE_MIME,
                bytes = image,
            )
        }

    /**
     * The row goes inside the DAO's transaction; the file cannot, so its path is read out first
     * and the image deleted after. An image left behind by a failed delete is invisible clutter,
     * while a surviving row pointing at a deleted file would be a receipt that will not open.
     */
    private suspend fun deleteSessionAndPhoto(sessionId: String) {
        val path = sessionDao.getPhotoPath(sessionId)
        sessionDao.deleteSession(sessionId)
        if (path != null) imageStore.delete(path)
    }

    companion object {
        /** [Failure.code] returned when a second session is started while one is still open. */
        const val ACTIVE_SESSION_EXISTS = "ACTIVE_SESSION_EXISTS"

        private const val FIRST_POSITION = 0L
        private const val PHOTO_PREFIX = "struk-"
        private const val PHOTO_SUFFIX = ".jpg"
        private const val SHARE_PREFIX = "struk-catatan-belanja-"
        private const val SHARE_SUFFIX = ".png"
        private const val SHARE_MIME = "image/png"
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
        private const val MSG_IMPORT_SESSION = "Failed to save the scanned receipt"
        private const val MSG_ATTACH_PHOTO = "Failed to save the receipt photo"
        private const val MSG_REMOVE_PHOTO = "Failed to remove the receipt photo"
        private const val MSG_SHARE_RECEIPT = "Failed to share the receipt"
    }
}
