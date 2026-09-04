package com.yudha.catatanbelanja.core.data.repository

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.IdGenerator
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.resourceOf
import com.yudha.catatanbelanja.core.common.toFileStamp
import com.yudha.catatanbelanja.core.data.backup.BackupCodec
import com.yudha.catatanbelanja.core.data.backup.BackupItemDto
import com.yudha.catatanbelanja.core.data.backup.BackupListDto
import com.yudha.catatanbelanja.core.data.backup.BackupSessionDto
import com.yudha.catatanbelanja.core.data.backup.BackupStockDto
import com.yudha.catatanbelanja.core.data.backup.BackupStockLogDto
import com.yudha.catatanbelanja.core.data.backup.DemoDataFactory
import com.yudha.catatanbelanja.core.data.database.SessionDao
import com.yudha.catatanbelanja.core.data.database.SettingsDao
import com.yudha.catatanbelanja.core.data.database.ShoppingListDao
import com.yudha.catatanbelanja.core.data.database.StockDao
import com.yudha.catatanbelanja.core.data.database.TrendDao
import com.yudha.catatanbelanja.core.domain.model.ImportSummary
import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.model.ShoppingList
import com.yudha.catatanbelanja.core.domain.model.ShoppingListItem
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockCheckEntry
import com.yudha.catatanbelanja.core.domain.model.StockCheckLog
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.repository.BackupRepository
import com.yudha.catatanbelanja.core.domain.service.ClipboardWriter
import com.yudha.catatanbelanja.core.domain.service.FileSharer
import com.yudha.catatanbelanja.core.domain.service.ImageStore

/**
 * Import merges, it never overwrites: sessions are matched by id, stock rows by normalized name
 * and check logs by month — anything already present is skipped, exactly like the prototype's
 * "Tempel data". The theme is a preference rather than data, so [clearAllData] leaves it alone.
 */
class BackupRepositoryImpl(
    private val sessionDao: SessionDao,
    private val stockDao: StockDao,
    private val settingsDao: SettingsDao,
    private val shoppingListDao: ShoppingListDao,
    private val trendDao: TrendDao,
    private val codec: BackupCodec,
    private val demoDataFactory: DemoDataFactory,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
    private val fileSharer: FileSharer,
    private val clipboardWriter: ClipboardWriter,
    private val imageStore: ImageStore,
) : BackupRepository {

    override suspend fun buildBackupJson(): Resource<String> = resourceOf(MSG_BUILD) {
        codec.encode(
            sessions = sessionDao.getFinishedSessions(),
            stockItems = stockDao.getStockItems(),
            checkLogs = stockDao.getCheckLogs(),
            // The archived lists are spent shopping notes; only the live plan and the templates
            // are worth carrying to another device.
            lists = listOfNotNull(shoppingListDao.getActiveList()) + shoppingListDao.getTemplates(),
            themeFlavor = settingsDao.getSettings().themeFlavor,
            exportedAt = clock.nowMillis(),
        )
    }

    override suspend fun shareBackup(): Resource<Unit> = when (val backup = buildBackupJson()) {
        is Resource.Error -> backup
        is Resource.Success -> resourceOf(MSG_SHARE) {
            // Stamped so a second export does not overwrite the first in the user's files.
            val name = "$BACKUP_FILE_PREFIX-${clock.nowMillis().toFileStamp()}.json"
            fileSharer.shareText(name, BACKUP_MIME_TYPE, backup.value)
        }
    }

    override suspend fun copyBackupToClipboard(): Resource<Unit> =
        when (val backup = buildBackupJson()) {
            is Resource.Error -> backup
            is Resource.Success -> resourceOf(MSG_COPY) {
                clipboardWriter.write(CLIPBOARD_LABEL, backup.value)
            }
        }

    override suspend fun importFromJson(rawJson: String): Resource<ImportSummary> =
        resourceOf(MSG_IMPORT) {
            val document = codec.decode(rawJson)
            ImportSummary(
                sessionsAdded = importSessions(document.sessions),
                stockAdded = importStock(document.stok),
                logsAdded = importLogs(document.stokLog),
                listsAdded = importLists(document.daftar),
            )
        }

    override suspend fun clearAllData(): Resource<Unit> = resourceOf(MSG_CLEAR) {
        // Read the photo paths before the rows go: after `deleteAllSessions` there is nothing
        // left to say which files these were, and they would sit in app storage forever.
        val photoPaths = sessionDao.getAllPhotoPaths()
        // The bulk deletes cover the active session too — `session.deleteAll` spares no row.
        sessionDao.deleteAllSessions()
        photoPaths.forEach { path -> imageStore.delete(path) }
        stockDao.deleteAllStockItems()
        stockDao.deleteAllCheckLogs()
        shoppingListDao.deleteAllLists()
        // The session rows are gone, so the overrides cascaded with them; this drops the
        // per-item settings, which hang off names rather than rows and would otherwise
        // outlive every receipt they described.
        trendDao.deleteAll()
    }

    override suspend fun seedDemoData(): Resource<Unit> = resourceOf(MSG_SEED) {
        demoDataFactory.sessions().forEach { sessionDao.insertSession(it) }

        val takenNames = stockDao.getStockItems().mapTo(mutableSetOf()) { it.name.normalized() }
        demoDataFactory.stockItems()
            .filter { takenNames.add(it.name.normalized()) }
            .forEach { stockDao.upsertStockItem(it) }

        val hasLogs = stockDao.getCheckLogs().isNotEmpty()
        if (!hasLogs) stockDao.upsertCheckLog(demoDataFactory.checkLog())
    }

    /**
     * Only finished sessions are imported — an exported document's active session is ignored.
     * [SessionDao.insertSession] writes the row and its items in one transaction, assigning
     * `position` in list order, which is the backup's newest-first order.
     */
    private suspend fun importSessions(dtos: List<BackupSessionDto>): Int {
        val takenIds = sessionDao.getFinishedSessions().mapTo(mutableSetOf()) { it.id }
        sessionDao.getActiveSession()?.let { takenIds += it.id }
        var added = 0
        dtos.forEach { dto ->
            val session = dto.toSession() ?: return@forEach
            if (!takenIds.add(session.id)) return@forEach
            sessionDao.insertSession(session)
            added++
        }
        return added
    }

    private suspend fun importStock(dtos: List<BackupStockDto>): Int {
        val takenNames = stockDao.getStockItems().mapTo(mutableSetOf()) { it.name.normalized() }
        var added = 0
        dtos.forEach { dto ->
            val item = dto.toStockItem() ?: return@forEach
            if (!takenNames.add(item.name.normalized())) return@forEach
            stockDao.upsertStockItem(item)
            added++
        }
        return added
    }

    private suspend fun importLogs(dtos: List<BackupStockLogDto>): Int {
        val takenMonths = stockDao.getCheckLogs().mapTo(mutableSetOf()) { it.month }
        var added = 0
        dtos.forEach { dto ->
            val log = dto.toCheckLog() ?: return@forEach
            if (!takenMonths.add(log.month)) return@forEach
            stockDao.upsertCheckLog(log)
            added++
        }
        return added
    }

    /**
     * Lists merge by id like sessions do. An imported active list would fight the one already
     * being planned, so a second active list is filed as a template instead of replacing it.
     */
    private suspend fun importLists(dtos: List<BackupListDto>): Int {
        val takenIds = shoppingListDao.getTemplates().mapTo(mutableSetOf()) { it.id }
        var hasActive = shoppingListDao.getActiveList()?.also { takenIds += it.id } != null
        var added = 0
        dtos.forEach { dto ->
            val list = dto.toList(keepActive = !hasActive) ?: return@forEach
            if (!takenIds.add(list.id)) return@forEach
            shoppingListDao.insertList(list)
            if (!list.isTemplate) hasActive = true
            added++
        }
        return added
    }

    private fun BackupListDto.toList(keepActive: Boolean): ShoppingList? {
        if (id.isBlank()) return null
        val entries = items.mapNotNull { entry ->
            if (entry.name.isBlank()) return@mapNotNull null
            ShoppingListItem(
                id = idGenerator.next(),
                name = entry.name,
                note = entry.note,
                isChecked = entry.checked,
            )
        }
        if (entries.isEmpty()) return null

        val stamp = updatedAt ?: createdAt ?: clock.nowMillis()
        val wasArchived = archivedAt != null
        return ShoppingList(
            id = id,
            name = name,
            createdAt = createdAt ?: stamp,
            updatedAt = stamp,
            isTemplate = isTemplate || wasArchived || !keepActive,
            archivedAt = null,
            items = entries,
        )
    }

    private fun BackupSessionDto.toSession(): ShoppingSession? {
        if (id.isBlank()) return null
        val finishedAt = endedAt ?: return null
        return ShoppingSession(
            id = id,
            name = name,
            store = store,
            startedAt = if (startedAt > 0L) startedAt else finishedAt,
            endedAt = finishedAt,
            items = items.mapNotNull { it.toItem() },
        )
    }

    private fun BackupItemDto.toItem(): ShoppingItem? {
        if (name.isBlank()) return null
        return ShoppingItem(
            id = id.ifBlank { idGenerator.next() },
            name = name,
            price = price,
            qty = qty,
            unit = unit,
            note = note,
        )
    }

    /**
     * The merge key is the normalized name, so the file's own id is never trusted — an id that
     * happens to match a live row would make `INSERT OR REPLACE` overwrite that row.
     */
    private fun BackupStockDto.toStockItem(): StockItem? {
        if (name.isBlank()) return null
        val quantity = qty ?: 0.0
        return StockItem(
            id = idGenerator.next(),
            name = name,
            qty = quantity,
            unit = unit,
            minQty = min,
            fullQty = full ?: quantity,
            updatedAt = updatedAt ?: clock.nowMillis(),
        )
    }

    /** Logs merge by month, so — as with stock — the id is minted here rather than imported. */
    private fun BackupStockLogDto.toCheckLog(): StockCheckLog? {
        if (month.isBlank()) return null
        return StockCheckLog(
            id = idGenerator.next(),
            month = month,
            checkedAt = at ?: clock.nowMillis(),
            entries = items.mapNotNull { entry ->
                if (entry.name.isBlank()) return@mapNotNull null
                StockCheckEntry(name = entry.name, qty = entry.qty ?: 0.0, unit = entry.unit)
            },
        )
    }

    private companion object {
        const val BACKUP_FILE_PREFIX = "catatan-belanja"
        const val BACKUP_MIME_TYPE = "application/json"
        const val CLIPBOARD_LABEL = "Catatan Belanja"
        const val MSG_BUILD = "Failed to build the backup"
        const val MSG_SHARE = "Failed to share the backup"
        const val MSG_COPY = "Failed to copy the backup"
        const val MSG_IMPORT = "Failed to import the backup"
        const val MSG_CLEAR = "Failed to clear the stored data"
        const val MSG_SEED = "Failed to seed the demo data"
    }
}
