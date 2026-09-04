package com.yudha.catatanbelanja.android

import android.app.Application
import com.yudha.catatanbelanja.BuildConfig
import com.yudha.catatanbelanja.core.data.service.OpenRouterConfig
import com.yudha.catatanbelanja.core.di.androidPlatformModule
import com.yudha.catatanbelanja.core.di.initKoin
import org.koin.android.ext.koin.androidContext

class CatatanBelanjaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // The receipt scanner's key and model come from `local.properties` by way of BuildConfig,
        // which is the only place in the build that knows them. Left unset there, the key arrives
        // as its placeholder and the scan entry point reports itself unavailable.
        initKoin(
            platformModule = androidPlatformModule(this),
            openRouter = OpenRouterConfig(
                apiKey = BuildConfig.OPENROUTER_API_KEY,
                model = BuildConfig.OPENROUTER_MODEL,
            ),
        ) {
            androidContext(this@CatatanBelanjaApp)
        }
    }
}
