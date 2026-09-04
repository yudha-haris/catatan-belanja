package com.yudha.catatanbelanja.core.di

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.IdGenerator
import com.yudha.catatanbelanja.core.common.RandomIdGenerator
import com.yudha.catatanbelanja.core.common.SystemClock
import com.yudha.catatanbelanja.core.domain.usecase.BuildNameChips
import com.yudha.catatanbelanja.core.domain.usecase.BuildNameSuggestions
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Qualifier for the background [kotlinx.coroutines.CoroutineDispatcher] every DAO takes in its
 * constructor.
 *
 * A named qualifier — not `expect`/`actual` — because the dispatcher differs per target only in
 * its value, never in its type: Android binds `Dispatchers.IO`, iOS binds `Dispatchers.Default`
 * (there is no separate IO pool on Native). Each platform module registers exactly one
 * `CoroutineDispatcher` under this qualifier; `dataModule` resolves it with
 * `get(ioDispatcherQualifier)`. Tests swap it for a test dispatcher by overriding the same
 * qualifier.
 */
val ioDispatcherQualifier: StringQualifier = named("io-dispatcher")

/** Stateless helpers shared by every layer. Use cases are factories, services are singles. */
val coreModule: Module = module {
    singleOf(::SystemClock) bind Clock::class
    singleOf(::RandomIdGenerator) bind IdGenerator::class

    factoryOf(::FindItemCategory)

    // Item-name suggestions are shared: the live session offers them while shopping, the
    // list screen while planning. `features.list` may not import `features.shopping`.
    factoryOf(::BuildNameSuggestions)
    factoryOf(::BuildNameChips)
}
