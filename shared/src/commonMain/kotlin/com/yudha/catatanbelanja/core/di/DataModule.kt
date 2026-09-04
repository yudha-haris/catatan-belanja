package com.yudha.catatanbelanja.core.di

import com.yudha.catatanbelanja.core.data.backup.BackupCodec
import com.yudha.catatanbelanja.core.data.backup.DemoDataFactory
import com.yudha.catatanbelanja.core.data.database.BrandDao
import com.yudha.catatanbelanja.core.data.database.CatalogDao
import com.yudha.catatanbelanja.core.data.database.DatabaseProvider
import com.yudha.catatanbelanja.core.data.database.SessionDao
import com.yudha.catatanbelanja.core.data.database.SettingsDao
import com.yudha.catatanbelanja.core.data.database.ShoppingListDao
import com.yudha.catatanbelanja.core.data.database.StockDao
import com.yudha.catatanbelanja.core.data.database.TrendDao
import com.yudha.catatanbelanja.core.data.repository.BackupRepositoryImpl
import com.yudha.catatanbelanja.core.data.repository.BrandRepositoryImpl
import com.yudha.catatanbelanja.core.data.repository.CatalogRepositoryImpl
import com.yudha.catatanbelanja.core.data.repository.SessionRepositoryImpl
import com.yudha.catatanbelanja.core.data.repository.SettingsRepositoryImpl
import com.yudha.catatanbelanja.core.data.repository.ShoppingListRepositoryImpl
import com.yudha.catatanbelanja.core.data.repository.StockRepositoryImpl
import com.yudha.catatanbelanja.core.data.repository.TrendRepositoryImpl
import com.yudha.catatanbelanja.core.domain.repository.BackupRepository
import com.yudha.catatanbelanja.core.domain.repository.BrandRepository
import com.yudha.catatanbelanja.core.domain.repository.CatalogRepository
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.core.domain.repository.SettingsRepository
import com.yudha.catatanbelanja.core.domain.repository.ShoppingListRepository
import com.yudha.catatanbelanja.core.domain.repository.StockRepository
import com.yudha.catatanbelanja.core.domain.repository.TrendRepository
import com.yudha.catatanbelanja.db.CatatanBelanjaDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Database, DAOs and repository implementations. The driver factory itself is platform-specific
 * and comes from the platform module.
 */
val dataModule: Module = module {
    singleOf(::DatabaseProvider)
    single<CatatanBelanjaDatabase> { get<DatabaseProvider>().create() }

    single { SessionDao(get(), get(ioDispatcherQualifier)) }
    single { StockDao(get(), get(ioDispatcherQualifier)) }
    single { SettingsDao(get(), get(ioDispatcherQualifier)) }
    single { ShoppingListDao(get(), get(ioDispatcherQualifier)) }
    single { TrendDao(get(), get(ioDispatcherQualifier)) }
    single { CatalogDao(get(), get(ioDispatcherQualifier)) }
    single { BrandDao(get(), get(ioDispatcherQualifier)) }

    singleOf(::BackupCodec)
    singleOf(::DemoDataFactory)

    singleOf(::SessionRepositoryImpl) bind SessionRepository::class
    singleOf(::StockRepositoryImpl) bind StockRepository::class
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    singleOf(::ShoppingListRepositoryImpl) bind ShoppingListRepository::class
    singleOf(::TrendRepositoryImpl) bind TrendRepository::class
    singleOf(::BackupRepositoryImpl) bind BackupRepository::class
    singleOf(::BrandRepositoryImpl) bind BrandRepository::class

    // The catalog keeps a snapshot warm for the synchronous emoji / default-unit lookups, so
    // it owns a scope that outlives any one screen. A SupervisorJob, so a failed refresh does
    // not take the next one down with it.
    single<CatalogRepository> {
        CatalogRepositoryImpl(
            catalogDao = get(),
            settingsDao = get(),
            idGenerator = get(),
            scope = CoroutineScope(
                SupervisorJob() + get<CoroutineDispatcher>(ioDispatcherQualifier),
            ),
        )
    }
}
