package com.yudha.catatanbelanja.android

import android.app.Application
import com.yudha.catatanbelanja.core.di.androidPlatformModule
import com.yudha.catatanbelanja.core.di.initKoin
import org.koin.android.ext.koin.androidContext

class CatatanBelanjaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(androidPlatformModule(this)) {
            androidContext(this@CatatanBelanjaApp)
        }
    }
}
