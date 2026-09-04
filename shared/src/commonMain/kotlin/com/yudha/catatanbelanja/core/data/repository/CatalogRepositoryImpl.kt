package com.yudha.catatanbelanja.core.data.repository

import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.catalog.defaultCatalog
import com.yudha.catatanbelanja.core.common.IdGenerator
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.resourceOf
import com.yudha.catatanbelanja.core.data.database.CatalogDao
import com.yudha.catatanbelanja.core.data.database.SettingsDao
import com.yudha.catatanbelanja.core.domain.model.CatalogCategory
import com.yudha.catatanbelanja.core.domain.repository.CatalogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Seeds the catalog on first use and keeps [current] pointing at the stored one.
 *
 * The snapshot is warmed in [scope] as soon as this repository is first injected — which is at
 * the first screen, since every emoji lookup goes through it — and refreshed after each write.
 * Until that first read lands it answers with the built-in catalog, which is exactly what an
 * empty database is about to be seeded with, so nothing ever sees an empty one.
 */
class CatalogRepositoryImpl(
    private val catalogDao: CatalogDao,
    private val settingsDao: SettingsDao,
    private val idGenerator: IdGenerator,
    scope: CoroutineScope,
) : CatalogRepository {

    private val snapshot = MutableStateFlow(CatalogData.defaultCatalog())

    override val current: List<CatalogCategory>
        get() = snapshot.value

    init {
        scope.launch { refresh() }
    }

    override suspend fun getCatalog(): Resource<List<CatalogCategory>> =
        resourceOf(MSG_LOAD) { refresh() }

    override suspend fun addCategory(name: String, emoji: String): Resource<Unit> =
        write(MSG_SAVE_CATEGORY) {
            catalogDao.insertCategory(id = idGenerator.next(), name = name, emoji = emoji)
        }

    override suspend fun updateCategory(id: String, name: String, emoji: String): Resource<Unit> =
        write(MSG_SAVE_CATEGORY) {
            catalogDao.updateCategory(id = id, name = name, emoji = emoji)
        }

    override suspend fun deleteCategory(id: String): Resource<Unit> =
        write(MSG_DELETE_CATEGORY) { catalogDao.deleteCategory(id) }

    override suspend fun addItem(
        categoryId: String,
        name: String,
        defaultUnit: String,
    ): Resource<Unit> = write(MSG_SAVE_ITEM) {
        catalogDao.insertItem(
            id = idGenerator.next(),
            categoryId = categoryId,
            name = name,
            defaultUnit = defaultUnit,
        )
    }

    override suspend fun updateItem(
        id: String,
        categoryId: String,
        name: String,
        defaultUnit: String,
    ): Resource<Unit> = write(MSG_SAVE_ITEM) {
        catalogDao.updateItem(
            id = id,
            categoryId = categoryId,
            name = name,
            defaultUnit = defaultUnit,
        )
    }

    override suspend fun deleteItem(id: String): Resource<Unit> =
        write(MSG_DELETE_ITEM) { catalogDao.deleteItem(id) }

    override suspend fun resetToDefaults(): Resource<Unit> = write(MSG_RESET) {
        catalogDao.replaceAll(CatalogData.defaultCatalog())
        settingsDao.markCatalogSeeded()
    }

    /** Every write is followed by a re-read, so [current] never lags behind what was just saved. */
    private suspend fun write(message: String, block: suspend () -> Unit): Resource<Unit> =
        resourceOf(message) {
            block()
            refresh()
        }

    private suspend fun refresh(): List<CatalogCategory> {
        seedIfNeeded()
        val stored = catalogDao.getCatalog()
        snapshot.value = stored
        return stored
    }

    /**
     * Writes the built-in catalog exactly once per install. The flag rather than an empty-table
     * check is deliberate: deleting every category is a decision, not a broken database.
     */
    private suspend fun seedIfNeeded() {
        if (settingsDao.isCatalogSeeded()) return
        if (catalogDao.isEmpty()) catalogDao.replaceAll(CatalogData.defaultCatalog())
        settingsDao.markCatalogSeeded()
    }

    private companion object {
        const val MSG_LOAD = "Failed to load the item catalog"
        const val MSG_SAVE_CATEGORY = "Failed to save the category"
        const val MSG_DELETE_CATEGORY = "Failed to delete the category"
        const val MSG_SAVE_ITEM = "Failed to save the catalog item"
        const val MSG_DELETE_ITEM = "Failed to delete the catalog item"
        const val MSG_RESET = "Failed to restore the default catalog"
    }
}
