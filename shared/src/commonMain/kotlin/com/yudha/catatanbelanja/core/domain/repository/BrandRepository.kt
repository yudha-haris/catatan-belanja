package com.yudha.catatanbelanja.core.domain.repository

import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.domain.model.BrandPreset

/**
 * The "merk" preset: one flat list of brands, offered for any item. It sits beside — never
 * instead of — the per-item brand chips the live session already builds out of past trips.
 */
interface BrandRepository {
    suspend fun getBrands(): Resource<List<BrandPreset>>

    suspend fun addBrand(name: String): Resource<Unit>

    suspend fun renameBrand(id: String, name: String): Resource<Unit>

    suspend fun deleteBrand(id: String): Resource<Unit>
}
