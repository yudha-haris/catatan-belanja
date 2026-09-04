package com.yudha.catatanbelanja.features.dashboard.di

import com.yudha.catatanbelanja.features.dashboard.domain.usecase.BuildDashboardData
import com.yudha.catatanbelanja.features.dashboard.presentation.DashboardViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Populated by the dashboard feature: its view models and use cases. */
val dashboardModule: Module = module {
    factoryOf(::BuildDashboardData)

    factoryOf(::DashboardViewModel)
}
