package com.yudha.catatanbelanja.core.data.database

import com.yudha.catatanbelanja.core.domain.model.ShoppingList
import com.yudha.catatanbelanja.core.domain.model.ShoppingListItem
import com.yudha.catatanbelanja.db.CatatanBelanjaDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ShoppingListDao(
    private val database: CatatanBelanjaDatabase,
    private val dispatcher: CoroutineDispatcher,
) {

    private val lists = database.shoppingListQueries
    private val items = database.shoppingListItemQueries

    suspend fun getActiveList(): ShoppingList? = withContext(dispatcher) {
        val row = lists.selectActive().executeAsOneOrNull() ?: return@withContext null
        row.toDomain(itemsOf(row.id))
    }

    suspend fun getTemplates(): List<ShoppingList> = withContext(dispatcher) {
        val rows = lists.selectTemplates().executeAsList()
        if (rows.isEmpty()) return@withContext emptyList()
        val grouped = items.selectByListIds(rows.map { it.id })
            .executeAsList()
            .groupBy({ it.list_id }, { it.toDomain() })
        rows.map { row -> row.toDomain(grouped[row.id].orEmpty()) }
    }

    suspend fun getList(id: String): ShoppingList? = withContext(dispatcher) {
        val row = lists.selectById(id).executeAsOneOrNull() ?: return@withContext null
        row.toDomain(itemsOf(row.id))
    }

    /** Writes the list row and its items in one transaction, list order becoming `position`. */
    suspend fun insertList(list: ShoppingList) = withContext(dispatcher) {
        database.transaction {
            lists.insert(
                list.id,
                list.name,
                list.createdAt,
                list.updatedAt,
                list.isTemplate.toFlag(),
                list.archivedAt,
            )
            list.items.forEachIndexed { index, item -> insertItemRow(list.id, item, index.toLong()) }
        }
    }

    /** Appends [newItems] after whatever the list already holds. */
    suspend fun appendItems(listId: String, newItems: List<ShoppingListItem>, updatedAt: Long) =
        withContext(dispatcher) {
            database.transaction {
                val tail = items.selectMaxPosition(listId).executeAsOneOrNull() ?: FIRST_POSITION - 1L
                newItems.forEachIndexed { index, item ->
                    insertItemRow(listId, item, tail + 1L + index)
                }
                lists.touch(updatedAt = updatedAt, id = listId)
            }
        }

    suspend fun updateItem(listId: String, itemId: String, name: String, note: String) =
        withContext(dispatcher) {
            items.update(name = name, note = note, id = itemId, listId = listId)
        }

    suspend fun setItemChecked(listId: String, itemId: String, isChecked: Boolean) =
        withContext(dispatcher) {
            items.setChecked(checked = isChecked.toFlag(), id = itemId, listId = listId)
        }

    suspend fun deleteItem(listId: String, itemId: String) = withContext(dispatcher) {
        items.deleteById(id = itemId, listId = listId)
    }

    suspend fun renameList(listId: String, name: String, updatedAt: Long) = withContext(dispatcher) {
        lists.rename(name = name, updatedAt = updatedAt, id = listId)
    }

    suspend fun archiveList(listId: String, archivedAt: Long) = withContext(dispatcher) {
        lists.archive(archivedAt = archivedAt, id = listId)
    }

    suspend fun deleteList(listId: String) = withContext(dispatcher) {
        database.transaction {
            items.deleteByListId(listId)
            lists.deleteById(listId)
        }
    }

    suspend fun deleteAllLists() = withContext(dispatcher) {
        database.transaction {
            items.deleteAll()
            lists.deleteAll()
        }
    }

    private fun insertItemRow(listId: String, item: ShoppingListItem, position: Long) {
        items.insert(item.id, listId, item.name, item.note, item.isChecked.toFlag(), position)
    }

    private fun itemsOf(listId: String): List<ShoppingListItem> =
        items.selectByListId(listId).executeAsList().map { it.toDomain() }

    private companion object {
        const val FIRST_POSITION = 0L
    }
}
