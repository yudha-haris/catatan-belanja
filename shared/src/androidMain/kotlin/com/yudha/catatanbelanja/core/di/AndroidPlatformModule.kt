package com.yudha.catatanbelanja.core.di

import android.content.Context
import com.yudha.catatanbelanja.core.data.database.DatabaseDriverFactory
import com.yudha.catatanbelanja.core.data.service.AndroidClipboardWriter
import com.yudha.catatanbelanja.core.data.service.AndroidFileSharer
import com.yudha.catatanbelanja.core.domain.service.ClipboardWriter
import com.yudha.catatanbelanja.core.domain.service.FileSharer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android side of the graph. [context] should be the application context. */
fun androidPlatformModule(context: Context): Module = module {
    single<CoroutineDispatcher>(ioDispatcherQualifier) { Dispatchers.IO }

    single { DatabaseDriverFactory(context) }
    single<FileSharer> { AndroidFileSharer(context) }
    single<ClipboardWriter> { AndroidClipboardWriter(context) }
}
