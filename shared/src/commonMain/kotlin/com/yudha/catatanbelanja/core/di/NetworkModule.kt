package com.yudha.catatanbelanja.core.di

import com.yudha.catatanbelanja.core.data.repository.ReceiptScanRepositoryImpl
import com.yudha.catatanbelanja.core.data.service.OpenRouterConfig
import com.yudha.catatanbelanja.core.data.service.OpenRouterReceiptScanner
import com.yudha.catatanbelanja.core.domain.repository.ReceiptScanRepository
import com.yudha.catatanbelanja.core.domain.service.ReceiptScanner
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * The app's only outbound HTTP, kept in a module of its own so the one feature that needs it is
 * obvious from the graph. [config] carries the key and the model slug in from the platform entry
 * point — `androidApp` reads both out of `BuildConfig`.
 *
 * There is no engine named here: Ktor picks the one on the target's classpath, which is OkHttp on
 * Android and Darwin on iOS.
 */
fun networkModule(config: OpenRouterConfig): Module = module {
    single { config }

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
            install(HttpTimeout) {
                // A vision model reading a photograph is slow by the standards of a phone request
                // — the default 15s cuts a perfectly good scan off well before it lands.
                requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                socketTimeoutMillis = REQUEST_TIMEOUT_MILLIS
                connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
            }
        }
    }

    single<ReceiptScanner> {
        OpenRouterReceiptScanner(get(), get(), get(), get(), get(ioDispatcherQualifier))
    }
    single<ReceiptScanRepository> { ReceiptScanRepositoryImpl(get(), get(), get()) }
}

private const val REQUEST_TIMEOUT_MILLIS = 90_000L
private const val CONNECT_TIMEOUT_MILLIS = 30_000L
