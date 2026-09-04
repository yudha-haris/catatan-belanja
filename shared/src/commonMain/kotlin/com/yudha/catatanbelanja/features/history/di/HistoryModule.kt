package com.yudha.catatanbelanja.features.history.di

import com.yudha.catatanbelanja.features.history.domain.usecase.BuildCompareResult
import com.yudha.catatanbelanja.features.history.domain.usecase.BuildSessionDetail
import com.yudha.catatanbelanja.features.history.domain.usecase.BuildSessionRowView
import com.yudha.catatanbelanja.features.history.domain.usecase.GroupSessionsByMonth
import com.yudha.catatanbelanja.features.history.presentation.CompareViewModel
import com.yudha.catatanbelanja.features.history.presentation.HistoryViewModel
import com.yudha.catatanbelanja.features.history.presentation.SessionDetailViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Populated by the history feature: its view models and use cases.
 *
 * View models are plain factories — `viewModelOf` lives in `koin-core-viewmodel`, which `:shared`
 * does not depend on. `koinViewModel()` resolves a factory definition just the same.
 */
val historyModule: Module = module {
    factoryOf(::BuildSessionRowView)
    factoryOf(::GroupSessionsByMonth)
    factoryOf(::BuildSessionDetail)
    factoryOf(::BuildCompareResult)

    factoryOf(::HistoryViewModel)
    factoryOf(::SessionDetailViewModel)
    factoryOf(::CompareViewModel)
}
