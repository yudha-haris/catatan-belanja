package com.yudha.catatanbelanja.features.list.di

import com.yudha.catatanbelanja.features.list.domain.usecase.BuildListItemViews
import com.yudha.catatanbelanja.features.list.domain.usecase.BuildListSources
import com.yudha.catatanbelanja.features.list.presentation.ShoppingListViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** The Daftar feature: the plan for the next trip and the templates it can be built from. */
val listModule: Module = module {
    factoryOf(::BuildListItemViews)
    factoryOf(::BuildListSources)

    factoryOf(::ShoppingListViewModel)
}
