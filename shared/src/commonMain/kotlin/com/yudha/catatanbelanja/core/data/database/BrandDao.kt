package com.yudha.catatanbelanja.core.data.database

import com.yudha.catatanbelanja.core.domain.model.BrandPreset
import com.yudha.catatanbelanja.db.CatatanBelanjaDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class BrandDao(
    private val database: CatatanBelanjaDatabase,
    private val dispatcher: CoroutineDispatcher,
) {

    private val brands = database.brandPresetQueries

    suspend fun getBrands(): List<BrandPreset> = withContext(dispatcher) {
        brands.selectAll().executeAsList().map { it.toDomain() }
    }

    /** Appends after whatever is already there, so the list keeps the order they were added in. */
    suspend fun insertBrand(id: String, name: String) = withContext(dispatcher) {
        database.transaction {
            val tail = brands.selectMaxPosition().executeAsOneOrNull() ?: FIRST_POSITION - 1L
            brands.insert(id, name, tail + 1L)
        }
    }

    suspend fun renameBrand(id: String, name: String) = withContext(dispatcher) {
        brands.update(name = name, id = id)
    }

    suspend fun deleteBrand(id: String) = withContext(dispatcher) {
        brands.deleteById(id)
    }

    private companion object {
        const val FIRST_POSITION = 0L
    }
}
