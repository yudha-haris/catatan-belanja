package com.yudha.catatanbelanja.features.preset.domain.usecase

import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.domain.repository.BrandRepository
import com.yudha.catatanbelanja.core.domain.repository.CatalogRepository
import com.yudha.catatanbelanja.core.domain.repository.SettingsRepository
import com.yudha.catatanbelanja.features.preset.domain.model.PresetOverview

/**
 * Reads the three preset stores the hub summarises. The first failure wins: a row that quietly
 * showed "0 item" because its read failed would read as "you have nothing", which is worse than
 * an error the user can retry.
 */
class LoadPresetOverview(
    private val catalogRepository: CatalogRepository,
    private val brandRepository: BrandRepository,
    private val settingsRepository: SettingsRepository,
) {

    suspend operator fun invoke(): Resource<PresetOverview> {
        val categories = when (val result = catalogRepository.getCatalog()) {
            is Resource.Error -> return result
            is Resource.Success -> result.value
        }

        val brands = when (val result = brandRepository.getBrands()) {
            is Resource.Error -> return result
            is Resource.Success -> result.value
        }

        val settings = when (val result = settingsRepository.getSettings()) {
            is Resource.Error -> return result
            is Resource.Success -> result.value
        }

        return Resource.Success(
            PresetOverview(
                itemCount = categories.sumOf { it.items.size },
                categoryCount = categories.size,
                brandCount = brands.size,
                language = settings.language,
            ),
        )
    }
}
