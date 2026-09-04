package com.yudha.catatanbelanja.core.domain.repository

import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.catalog.defaultCatalog
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.domain.model.CatalogCategory

/**
 * The built-in catalog, served without a database. Use cases only ever read
 * [CatalogRepository.current], so the writes here are what an unused half of the interface looks
 * like rather than something a test is expected to exercise.
 */
class FakeCatalogRepository(
    override val current: List<CatalogCategory> = CatalogData.defaultCatalog(),
) : CatalogRepository {

    override suspend fun getCatalog(): Resource<List<CatalogCategory>> = Resource.Success(current)

    override suspend fun addCategory(name: String, emoji: String): Resource<Unit> = unsupported()

    override suspend fun updateCategory(
        id: String,
        name: String,
        emoji: String,
    ): Resource<Unit> = unsupported()

    override suspend fun deleteCategory(id: String): Resource<Unit> = unsupported()

    override suspend fun addItem(
        categoryId: String,
        name: String,
        defaultUnit: String,
    ): Resource<Unit> = unsupported()

    override suspend fun updateItem(
        id: String,
        categoryId: String,
        name: String,
        defaultUnit: String,
    ): Resource<Unit> = unsupported()

    override suspend fun deleteItem(id: String): Resource<Unit> = unsupported()

    override suspend fun resetToDefaults(): Resource<Unit> = unsupported()

    private fun unsupported(): Nothing =
        throw UnsupportedOperationException("FakeCatalogRepository is read-only")
}
