package com.yudha.catatanbelanja.core.domain.repository

import com.yudha.catatanbelanja.core.domain.model.CatalogCategory
import com.yudha.catatanbelanja.core.common.Resource

/**
 * The item catalog — the "belanjaan" and "kategori" presets. Stored, and editable from
 * Pengaturan > Preset; [CatalogData][com.yudha.catatanbelanja.core.catalog.CatalogData] only
 * supplies what an empty database is seeded with.
 *
 * [current] is the odd one out: a plain, non-suspending read of the catalog as it stands. The
 * emoji and default-unit lookups run inside pure mappers — `emojiFor(name)` is called once per
 * receipt row — and threading a suspend call through every one of them would buy nothing, since
 * the whole catalog is some eighty rows the app keeps in memory anyway. The implementation warms
 * it at startup and refreshes it after every write.
 */
interface CatalogRepository {

    /**
     * The catalog as it stands, readable without suspending. Answers with the built-in defaults
     * until the first database read lands, which is the same catalog a fresh install is seeded
     * with — so an early lookup is right rather than empty.
     */
    val current: List<CatalogCategory>

    suspend fun getCatalog(): Resource<List<CatalogCategory>>

    suspend fun addCategory(name: String, emoji: String): Resource<Unit>

    suspend fun updateCategory(id: String, name: String, emoji: String): Resource<Unit>

    /** Takes the category's items with it — an item cannot sit outside a category. */
    suspend fun deleteCategory(id: String): Resource<Unit>

    suspend fun addItem(categoryId: String, name: String, defaultUnit: String): Resource<Unit>

    /** [categoryId] may differ from the item's current one: that moves it between categories. */
    suspend fun updateItem(
        id: String,
        categoryId: String,
        name: String,
        defaultUnit: String,
    ): Resource<Unit>

    suspend fun deleteItem(id: String): Resource<Unit>

    /** Throws the edits away and writes the built-in catalog back. */
    suspend fun resetToDefaults(): Resource<Unit>
}
