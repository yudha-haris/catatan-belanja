package com.yudha.catatanbelanja.core.data.repository

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.IdGenerator
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.capitalizeWords
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.common.resourceOf
import com.yudha.catatanbelanja.core.data.database.ShoppingListDao
import com.yudha.catatanbelanja.core.domain.model.ShoppingList
import com.yudha.catatanbelanja.core.domain.model.ShoppingListItem
import com.yudha.catatanbelanja.core.domain.repository.ShoppingListRepository

/**
 * Names are the list's identity: everything here dedupes on `normalized()`, so adding "beras"
 * to a list that already holds "Beras" is a no-op rather than a second line to tick off.
 */
class ShoppingListRepositoryImpl(
    private val shoppingListDao: ShoppingListDao,
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) : ShoppingListRepository {

    override suspend fun getActiveList(): Resource<ShoppingList?> =
        resourceOf(MSG_LOAD_ACTIVE) { shoppingListDao.getActiveList() }

    override suspend fun getTemplates(): Resource<List<ShoppingList>> =
        resourceOf(MSG_LOAD_TEMPLATES) { shoppingListDao.getTemplates() }

    override suspend fun startList(names: List<String>): Resource<ShoppingList> =
        resourceOf(MSG_START) {
            // One plan at a time. The screen only offers "buat daftar" when there is none, so
            // this only fires if two entry points race each other.
            shoppingListDao.getActiveList()?.let { shoppingListDao.deleteList(it.id) }

            val now = clock.nowMillis()
            val list = ShoppingList(
                id = idGenerator.next(),
                createdAt = now,
                updatedAt = now,
                items = names.toItems(),
            )
            shoppingListDao.insertList(list)
            list
        }

    override suspend fun addItems(listId: String, names: List<String>): Resource<Int> =
        resourceOf(MSG_ADD_ITEMS) {
            val list = shoppingListDao.getList(listId) ?: return@resourceOf 0
            val taken = list.items.mapTo(mutableSetOf()) { it.name.normalized() }
            val fresh = names.toItems(taken)
            if (fresh.isEmpty()) return@resourceOf 0

            shoppingListDao.appendItems(listId, fresh, clock.nowMillis())
            fresh.size
        }

    override suspend fun updateItem(
        listId: String,
        itemId: String,
        name: String,
        note: String,
    ): Resource<Unit> = resourceOf(MSG_UPDATE_ITEM) {
        shoppingListDao.updateItem(listId, itemId, name.trim().capitalizeWords(), note.trim())
    }

    override suspend fun setItemChecked(
        listId: String,
        itemId: String,
        isChecked: Boolean,
    ): Resource<Unit> = resourceOf(MSG_CHECK_ITEM) {
        shoppingListDao.setItemChecked(listId, itemId, isChecked)
    }

    override suspend fun checkItemByName(name: String): Resource<ShoppingList?> =
        resourceOf(MSG_CHECK_ITEM) {
            val list = shoppingListDao.getActiveList() ?: return@resourceOf null
            val key = name.normalized()
            val match = list.items.firstOrNull { !it.isChecked && it.name.normalized() == key }
                ?: return@resourceOf list

            shoppingListDao.setItemChecked(list.id, match.id, isChecked = true)
            shoppingListDao.getList(list.id)
        }

    override suspend fun uncheckItemByName(name: String): Resource<ShoppingList?> =
        resourceOf(MSG_CHECK_ITEM) {
            val list = shoppingListDao.getActiveList() ?: return@resourceOf null
            val key = name.normalized()
            val match = list.items.firstOrNull { it.isChecked && it.name.normalized() == key }
                ?: return@resourceOf list

            shoppingListDao.setItemChecked(list.id, match.id, isChecked = false)
            shoppingListDao.getList(list.id)
        }

    override suspend fun deleteItem(listId: String, itemId: String): Resource<Unit> =
        resourceOf(MSG_DELETE_ITEM) { shoppingListDao.deleteItem(listId, itemId) }

    /** A template starts clean: whatever was already ticked off is copied back as unticked. */
    override suspend fun saveAsTemplate(listId: String, name: String): Resource<Unit> =
        resourceOf(MSG_SAVE_TEMPLATE) {
            val source = shoppingListDao.getList(listId) ?: return@resourceOf
            val now = clock.nowMillis()
            shoppingListDao.insertList(
                ShoppingList(
                    id = idGenerator.next(),
                    name = name.trim(),
                    createdAt = now,
                    updatedAt = now,
                    isTemplate = true,
                    items = source.items.map { item ->
                        ShoppingListItem(id = idGenerator.next(), name = item.name, note = item.note)
                    },
                ),
            )
        }

    override suspend fun deleteList(listId: String): Resource<Unit> =
        resourceOf(MSG_DELETE_LIST) { shoppingListDao.deleteList(listId) }

    override suspend fun archiveActiveList(carryOverUnchecked: Boolean): Resource<Int> =
        resourceOf(MSG_ARCHIVE) {
            val list = shoppingListDao.getActiveList() ?: return@resourceOf 0
            val now = clock.nowMillis()
            shoppingListDao.archiveList(list.id, now)

            if (!carryOverUnchecked) return@resourceOf 0
            val leftovers = list.items.filter { !it.isChecked }
            if (leftovers.isEmpty()) return@resourceOf 0

            // Archived first, so the carried-over plan is the only active list when this returns.
            shoppingListDao.insertList(
                ShoppingList(
                    id = idGenerator.next(),
                    createdAt = now,
                    updatedAt = now,
                    items = leftovers.map { item ->
                        ShoppingListItem(id = idGenerator.next(), name = item.name, note = item.note)
                    },
                ),
            )
            leftovers.size
        }

    /** Blank names dropped, repeats collapsed, each stored the way the app writes item names. */
    private fun List<String>.toItems(
        taken: MutableSet<String> = mutableSetOf(),
    ): List<ShoppingListItem> = mapNotNull { raw ->
        val name = raw.trim()
        if (name.isEmpty()) return@mapNotNull null
        if (!taken.add(name.normalized())) return@mapNotNull null

        ShoppingListItem(id = idGenerator.next(), name = name.capitalizeWords())
    }

    private companion object {
        const val MSG_LOAD_ACTIVE = "Failed to load the shopping list"
        const val MSG_LOAD_TEMPLATES = "Failed to load the list templates"
        const val MSG_START = "Failed to start the shopping list"
        const val MSG_ADD_ITEMS = "Failed to add to the shopping list"
        const val MSG_UPDATE_ITEM = "Failed to update the list item"
        const val MSG_CHECK_ITEM = "Failed to tick off the list item"
        const val MSG_DELETE_ITEM = "Failed to remove the list item"
        const val MSG_SAVE_TEMPLATE = "Failed to save the template"
        const val MSG_DELETE_LIST = "Failed to delete the shopping list"
        const val MSG_ARCHIVE = "Failed to close the shopping list"
    }
}
