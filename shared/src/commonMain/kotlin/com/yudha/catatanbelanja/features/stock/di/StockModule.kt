package com.yudha.catatanbelanja.features.stock.di

import com.yudha.catatanbelanja.features.stock.domain.usecase.BuildKnownStockNames
import com.yudha.catatanbelanja.features.stock.domain.usecase.BuildStockRows
import com.yudha.catatanbelanja.features.stock.domain.usecase.CalculateStockUsage
import com.yudha.catatanbelanja.features.stock.domain.usecase.CreateStockItem
import com.yudha.catatanbelanja.features.stock.domain.usecase.CurrentStockCheckStamp
import com.yudha.catatanbelanja.features.stock.presentation.StockViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Populated by the stock feature: its view models and use cases. */
val stockModule: Module = module {
    factoryOf(::BuildStockRows)
    factoryOf(::BuildKnownStockNames)
    factoryOf(::CalculateStockUsage)
    factoryOf(::CreateStockItem)
    factoryOf(::CurrentStockCheckStamp)
    factoryOf(::StockViewModel)
}
