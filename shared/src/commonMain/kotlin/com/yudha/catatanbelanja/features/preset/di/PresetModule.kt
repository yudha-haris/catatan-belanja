package com.yudha.catatanbelanja.features.preset.di

import com.yudha.catatanbelanja.features.preset.domain.usecase.LoadPresetOverview
import com.yudha.catatanbelanja.features.preset.presentation.PresetBrandsViewModel
import com.yudha.catatanbelanja.features.preset.presentation.PresetCategoriesViewModel
import com.yudha.catatanbelanja.features.preset.presentation.PresetHubViewModel
import com.yudha.catatanbelanja.features.preset.presentation.PresetItemsViewModel
import com.yudha.catatanbelanja.features.preset.presentation.PresetLanguageViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Populated by the preset feature: its view models and use cases.
 *
 * `factoryOf`, not `viewModelOf`: the KMP viewModel DSL ships in koin-core-viewmodel, which
 * `:shared` does not depend on. `koinViewModel()` resolves a factory definition all the same.
 */
val presetModule: Module = module {
    factoryOf(::LoadPresetOverview)

    factoryOf(::PresetHubViewModel)
    factoryOf(::PresetItemsViewModel)
    factoryOf(::PresetCategoriesViewModel)
    factoryOf(::PresetBrandsViewModel)
    factoryOf(::PresetLanguageViewModel)
}
