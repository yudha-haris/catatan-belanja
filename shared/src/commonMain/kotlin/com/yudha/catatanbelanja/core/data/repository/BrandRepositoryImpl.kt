package com.yudha.catatanbelanja.core.data.repository

import com.yudha.catatanbelanja.core.common.IdGenerator
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.resourceOf
import com.yudha.catatanbelanja.core.data.database.BrandDao
import com.yudha.catatanbelanja.core.domain.model.BrandPreset
import com.yudha.catatanbelanja.core.domain.repository.BrandRepository

class BrandRepositoryImpl(
    private val brandDao: BrandDao,
    private val idGenerator: IdGenerator,
) : BrandRepository {

    override suspend fun getBrands(): Resource<List<BrandPreset>> =
        resourceOf(MSG_LOAD) { brandDao.getBrands() }

    override suspend fun addBrand(name: String): Resource<Unit> =
        resourceOf(MSG_SAVE) { brandDao.insertBrand(id = idGenerator.next(), name = name) }

    override suspend fun renameBrand(id: String, name: String): Resource<Unit> =
        resourceOf(MSG_SAVE) { brandDao.renameBrand(id = id, name = name) }

    override suspend fun deleteBrand(id: String): Resource<Unit> =
        resourceOf(MSG_DELETE) { brandDao.deleteBrand(id) }

    private companion object {
        const val MSG_LOAD = "Failed to load the brand presets"
        const val MSG_SAVE = "Failed to save the brand preset"
        const val MSG_DELETE = "Failed to delete the brand preset"
    }
}
