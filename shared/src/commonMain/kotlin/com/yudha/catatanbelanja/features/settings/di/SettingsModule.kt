package com.yudha.catatanbelanja.features.settings.di

import com.yudha.catatanbelanja.features.settings.domain.usecase.LoadSettingsOverview
import com.yudha.catatanbelanja.features.settings.presentation.SettingsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Populated by the settings feature: its view models and use cases.
 *
 * `factoryOf`, not `viewModelOf`: the KMP viewModel DSL ships in koin-core-viewmodel, which
 * `:shared` does not depend on. `koinViewModel()` resolves a factory definition all the same.
 */
val settingsModule: Module = module {
    factoryOf(::LoadSettingsOverview)

    factoryOf(::SettingsViewModel)
}
