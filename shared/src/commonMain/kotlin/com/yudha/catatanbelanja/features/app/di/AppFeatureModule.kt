package com.yudha.catatanbelanja.features.app.di

import com.yudha.catatanbelanja.features.app.presentation.AppViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Populated by the app-shell feature: its view models and use cases.
 *
 * `factoryOf`, not `viewModelOf`: the KMP viewModel DSL ships in koin-core-viewmodel, which
 * `:shared` does not depend on. `koinViewModel()` resolves a factory definition all the same.
 */
val appFeatureModule: Module = module {
    factoryOf(::AppViewModel)
}
