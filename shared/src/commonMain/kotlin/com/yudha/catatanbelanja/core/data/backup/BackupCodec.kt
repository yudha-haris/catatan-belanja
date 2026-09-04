package com.yudha.catatanbelanja.core.data.backup

import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.model.ShoppingList
import com.yudha.catatanbelanja.core.domain.model.ShoppingListItem
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockCheckEntry
import com.yudha.catatanbelanja.core.domain.model.StockCheckLog
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray

/**
 * Turns the local database into the prototype's backup JSON and back.
 * [decode] is deliberately forgiving: a hand-edited file, a bare array of sessions or a
 * document with missing keys must still load. Genuinely malformed input throws, and the
 * repository turns that into a `Resource.Error`.
 */
class BackupCodec {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun encode(
        sessions: List<ShoppingSession>,
        stockItems: List<StockItem>,
        checkLogs: List<StockCheckLog>,
        lists: List<ShoppingList>,
        themeFlavor: ThemeFlavor,
        exportedAt: Long,
    ): String {
        val document = BackupDocument(
            version = BACKUP_VERSION,
            exportedAt = exportedAt,
            theme = themeFlavor.storageValue,
            sessions = sessions.map { it.toDto() },
            stok = stockItems.map { it.toDto() },
            stokLog = checkLogs.map { it.toDto() },
            daftar = lists.map { it.toDto() },
        )
        return json.encodeToString(BackupDocument.serializer(), document)
    }

    /** A bare top-level array is read as the `sessions` list, matching the prototype's importer. */
    fun decode(rawJson: String): BackupDocument {
        val element = json.parseToJsonElement(rawJson.trim())
        if (element is JsonArray) {
            val sessions = json.decodeFromJsonElement(ListSerializer(BackupSessionDto.serializer()), element)
            return BackupDocument(sessions = sessions)
        }
        return json.decodeFromJsonElement(BackupDocument.serializer(), element)
    }

    private fun ShoppingSession.toDto(): BackupSessionDto = BackupSessionDto(
        id = id,
        name = name,
        store = store,
        startedAt = startedAt,
        endedAt = endedAt,
        items = items.map { it.toDto() },
    )

    private fun ShoppingItem.toDto(): BackupItemDto = BackupItemDto(
        id = id,
        name = name,
        qty = qty,
        unit = unit,
        price = price,
        note = note,
    )

    private fun StockItem.toDto(): BackupStockDto = BackupStockDto(
        id = id,
        name = name,
        qty = qty,
        unit = unit,
        min = minQty,
        full = fullQty,
        updatedAt = updatedAt,
    )

    private fun StockCheckLog.toDto(): BackupStockLogDto = BackupStockLogDto(
        id = id,
        month = month,
        at = checkedAt,
        items = entries.map { it.toDto() },
    )

    private fun ShoppingList.toDto(): BackupListDto = BackupListDto(
        id = id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isTemplate = isTemplate,
        archivedAt = archivedAt,
        items = items.map { it.toDto() },
    )

    private fun ShoppingListItem.toDto(): BackupListItemDto = BackupListItemDto(
        name = name,
        note = note,
        checked = isChecked,
    )

    private fun StockCheckEntry.toDto(): BackupStockLogItemDto = BackupStockLogItemDto(
        name = name,
        qty = qty,
        unit = unit,
    )
}
