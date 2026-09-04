package com.yudha.catatanbelanja.core.di

import com.yudha.catatanbelanja.core.data.database.DatabaseDriverFactory
import com.yudha.catatanbelanja.core.data.service.IosClipboardWriter
import com.yudha.catatanbelanja.core.data.service.IosFileSharer
import com.yudha.catatanbelanja.core.data.service.IosImageStore
import com.yudha.catatanbelanja.core.data.service.IosNetworkMonitor
import com.yudha.catatanbelanja.core.domain.service.ClipboardWriter
import com.yudha.catatanbelanja.core.domain.service.FileSharer
import com.yudha.catatanbelanja.core.domain.service.ImageStore
import com.yudha.catatanbelanja.core.domain.service.NetworkMonitor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS side of the graph. Native has no separate IO pool, so the default pool backs the DAOs. */
fun iosPlatformModule(): Module = module {
    single<CoroutineDispatcher>(ioDispatcherQualifier) { Dispatchers.Default }

    single { DatabaseDriverFactory() }
    single<FileSharer> { IosFileSharer() }
    single<ClipboardWriter> { IosClipboardWriter() }
    single<ImageStore> { IosImageStore() }
    single<NetworkMonitor> { IosNetworkMonitor() }
}
