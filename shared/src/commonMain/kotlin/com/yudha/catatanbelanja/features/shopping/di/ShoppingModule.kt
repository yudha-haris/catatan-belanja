package com.yudha.catatanbelanja.features.shopping.di

import com.yudha.catatanbelanja.features.shopping.domain.usecase.BuildSessionItemViews
import com.yudha.catatanbelanja.features.shopping.domain.usecase.BuildStartOverview
import com.yudha.catatanbelanja.features.shopping.domain.usecase.CreateShoppingItem
import com.yudha.catatanbelanja.features.shopping.domain.usecase.CurrentTime
import com.yudha.catatanbelanja.features.shopping.domain.usecase.FindBrandSuggestions
import com.yudha.catatanbelanja.features.shopping.domain.usecase.FindLastPurchase
import com.yudha.catatanbelanja.features.shopping.domain.usecase.FinishShoppingSession
import com.yudha.catatanbelanja.features.shopping.presentation.LiveSessionViewModel
import com.yudha.catatanbelanja.features.shopping.presentation.StartViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Populated by the shopping feature: its view models and use cases. */
val shoppingModule: Module = module {
    factoryOf(::BuildStartOverview)
    factoryOf(::BuildSessionItemViews)
    factoryOf(::CreateShoppingItem)
    factoryOf(::FindLastPurchase)
    factoryOf(::FindBrandSuggestions)
    factoryOf(::FinishShoppingSession)
    factoryOf(::CurrentTime)

    // `factoryOf`, not `viewModelOf`: the KMP viewModel DSL ships in koin-core-viewmodel, which
    // :shared does not depend on. `koinViewModel()` resolves a factory definition all the same.
    factoryOf(::StartViewModel)
    factoryOf(::LiveSessionViewModel)
}
