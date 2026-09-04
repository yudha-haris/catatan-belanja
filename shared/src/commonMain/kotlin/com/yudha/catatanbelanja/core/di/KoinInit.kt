package com.yudha.catatanbelanja.core.di

import com.yudha.catatanbelanja.core.data.service.OpenRouterConfig
import com.yudha.catatanbelanja.features.app.di.appFeatureModule
import com.yudha.catatanbelanja.features.dashboard.di.dashboardModule
import com.yudha.catatanbelanja.features.history.di.historyModule
import com.yudha.catatanbelanja.features.list.di.listModule
import com.yudha.catatanbelanja.features.preset.di.presetModule
import com.yudha.catatanbelanja.features.receipt.di.receiptModule
import com.yudha.catatanbelanja.features.settings.di.settingsModule
import com.yudha.catatanbelanja.features.shopping.di.shoppingModule
import com.yudha.catatanbelanja.features.stock.di.stockModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

/**
 * The single composition root. [platformModule] is `androidPlatformModule(context)` or
 * `iosPlatformModule()`; [appDeclaration] lets the caller add logging or `androidContext`.
 *
 * [openRouter] carries the receipt scanner's API key in from outside the source tree — it is a
 * secret, so it reaches the graph as an argument rather than as a constant anywhere in `:shared`.
 * Its default leaves the key blank, which is what iOS passes today: the scanner then reports
 * itself unavailable and the entry point never appears.
 */
fun initKoin(
    platformModule: Module,
    openRouter: OpenRouterConfig = OpenRouterConfig(),
    appDeclaration: KoinAppDeclaration = {},
): KoinApplication = startKoin {
    appDeclaration()
    modules(
        platformModule,
        coreModule,
        dataModule,
        networkModule(openRouter),
        appFeatureModule,
        shoppingModule,
        historyModule,
        listModule,
        stockModule,
        dashboardModule,
        settingsModule,
        presetModule,
        receiptModule,
    )
}
