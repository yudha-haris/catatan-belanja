package com.yudha.catatanbelanja.core.di

import com.yudha.catatanbelanja.core.data.backup.BackupCodec
import com.yudha.catatanbelanja.core.data.backup.DemoDataFactory
import com.yudha.catatanbelanja.core.data.database.DatabaseProvider
import com.yudha.catatanbelanja.core.data.database.SessionDao
import com.yudha.catatanbelanja.core.data.database.SettingsDao
import com.yudha.catatanbelanja.core.data.database.ShoppingListDao
import com.yudha.catatanbelanja.core.data.database.StockDao
import com.yudha.catatanbelanja.core.data.repository.BackupRepositoryImpl
import com.yudha.catatanbelanja.core.data.repository.SessionRepositoryImpl
import com.yudha.catatanbelanja.core.data.repository.SettingsRepositoryImpl
import com.yudha.catatanbelanja.core.data.repository.ShoppingListRepositoryImpl
import com.yudha.catatanbelanja.core.data.repository.StockRepositoryImpl
import com.yudha.catatanbelanja.core.domain.repository.BackupRepository
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.core.domain.repository.SettingsRepository
import com.yudha.catatanbelanja.core.domain.repository.ShoppingListRepository
import com.yudha.catatanbelanja.core.domain.repository.StockRepository
import com.yudha.catatanbelanja.db.CatatanBelanjaDatabase
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

    singleOf(::BackupCodec)
    singleOf(::DemoDataFactory)

    singleOf(::SessionRepositoryImpl) bind SessionRepository::class
    singleOf(::StockRepositoryImpl) bind StockRepository::class
    singleOf(::SettingsRepositoryImpl) bind SettingsRepository::class
    singleOf(::ShoppingListRepositoryImpl) bind ShoppingListRepository::class
    singleOf(::BackupRepositoryImpl) bind BackupRepository::class
}
