package com.yudha.catatanbelanja.features.dashboard.di

import com.yudha.catatanbelanja.features.dashboard.domain.usecase.BuildDashboardData
import com.yudha.catatanbelanja.features.dashboard.domain.usecase.BuildPriceTrend
import com.yudha.catatanbelanja.features.dashboard.domain.usecase.BuildSpendingRanking
import com.yudha.catatanbelanja.features.dashboard.domain.usecase.BuildSpendingReport
import com.yudha.catatanbelanja.features.dashboard.domain.usecase.BuildTrendCandidates
import com.yudha.catatanbelanja.features.dashboard.presentation.DashboardViewModel
import com.yudha.catatanbelanja.features.dashboard.presentation.PriceTrendViewModel
import com.yudha.catatanbelanja.features.dashboard.presentation.SpendingRankingViewModel
import com.yudha.catatanbelanja.features.dashboard.presentation.SpendingReportViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Populated by the dashboard feature: the summary tab, its three report pages and their use cases. */
val dashboardModule: Module = module {
    factoryOf(::BuildDashboardData)
    factoryOf(::BuildSpendingReport)
    factoryOf(::BuildSpendingRanking)
    factoryOf(::BuildTrendCandidates)
    factoryOf(::BuildPriceTrend)

    factoryOf(::DashboardViewModel)
    factoryOf(::SpendingReportViewModel)
    factoryOf(::SpendingRankingViewModel)
    factoryOf(::PriceTrendViewModel)
}
