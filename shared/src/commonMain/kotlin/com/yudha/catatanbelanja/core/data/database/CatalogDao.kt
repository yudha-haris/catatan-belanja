package com.yudha.catatanbelanja.core.data.database

import com.yudha.catatanbelanja.core.domain.model.CatalogCategory
import com.yudha.catatanbelanja.db.CatatanBelanjaDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * The item catalog: categories and the items filed under them. Reads hand back whole categories
 * with their items already attached, because that is the only shape anything asks for.
 */
class CatalogDao(
    private val database: CatatanBelanjaDatabase,
    private val dispatcher: CoroutineDispatcher,
) {

    private val categories = database.catalogCategoryQueries
    private val items = database.catalogItemQueries

    suspend fun getCatalog(): List<CatalogCategory> = withContext(dispatcher) {
        val itemRows = items.selectAll().executeAsList().groupBy { it.category_id }
        categories.selectAll().executeAsList().map { row ->
            row.toDomain(itemRows[row.id].orEmpty().map { it.toDomain() })
        }
    }

    suspend fun isEmpty(): Boolean = withContext(dispatcher) {
        categories.countAll().executeAsOne() == 0L
    }

    /** Wipes both tables and writes [catalog] back in one transaction — used to seed and to reset. */
    suspend fun replaceAll(catalog: List<CatalogCategory>) = withContext(dispatcher) {
        database.transaction {
            items.deleteAll()
            categories.deleteAll()
            catalog.forEach { category ->
                categories.insert(
                    category.id,
                    category.name,
                    category.emoji,
                    category.position.toLong(),
                )
                category.items.forEach { item ->
                    items.insert(
                        item.id,
                        category.id,
                        item.name,
                        item.defaultUnit,
                        item.position.toLong(),
                    )
                }
            }
        }
    }

    /** Appends a category after whatever is already there. */
    suspend fun insertCategory(id: String, name: String, emoji: String) = withContext(dispatcher) {
        database.transaction {
            val tail = categories.selectMaxPosition().executeAsOneOrNull() ?: FIRST_POSITION - 1L
            categories.insert(id, name, emoji, tail + 1L)
        }
    }

    suspend fun updateCategory(id: String, name: String, emoji: String) = withContext(dispatcher) {
        categories.update(name = name, emoji = emoji, id = id)
    }

    /**
     * The items go first: the foreign key declares `ON DELETE CASCADE`, but SQLite only honours
     * that with `foreign_keys` on, which is not something this app's drivers promise.
     */
    suspend fun deleteCategory(id: String) = withContext(dispatcher) {
        database.transaction {
            items.deleteByCategoryId(id)
            categories.deleteById(id)
        }
    }

    suspend fun insertItem(
        id: String,
        categoryId: String,
        name: String,
        defaultUnit: String,
    ) = withContext(dispatcher) {
        database.transaction {
            val tail = items.selectMaxPosition(categoryId).executeAsOneOrNull()
                ?: FIRST_POSITION - 1L
            items.insert(id, categoryId, name, defaultUnit, tail + 1L)
        }
    }

    suspend fun updateItem(
        id: String,
        categoryId: String,
        name: String,
        defaultUnit: String,
    ) = withContext(dispatcher) {
        items.update(categoryId = categoryId, name = name, defaultUnit = defaultUnit, id = id)
    }

    suspend fun deleteItem(id: String) = withContext(dispatcher) {
        items.deleteById(id)
    }

    private companion object {
        const val FIRST_POSITION = 0L
    }
}
