package com.yudha.catatanbelanja.core.di

import com.yudha.catatanbelanja.features.app.di.appFeatureModule
import com.yudha.catatanbelanja.features.dashboard.di.dashboardModule
import com.yudha.catatanbelanja.features.history.di.historyModule
import com.yudha.catatanbelanja.features.list.di.listModule
import com.yudha.catatanbelanja.features.preset.di.presetModule
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
 */
fun initKoin(
    platformModule: Module,
    appDeclaration: KoinAppDeclaration = {},
): KoinApplication = startKoin {
    appDeclaration()
    modules(
        platformModule,
        coreModule,
        dataModule,
        appFeatureModule,
        shoppingModule,
        historyModule,
        listModule,
        stockModule,
        dashboardModule,
        settingsModule,
        presetModule,
    )
}
