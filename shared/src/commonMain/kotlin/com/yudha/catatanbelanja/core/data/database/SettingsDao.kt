package com.yudha.catatanbelanja.core.data.database

import com.yudha.catatanbelanja.core.domain.model.AppSettings
import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.yudha.catatanbelanja.db.CatatanBelanjaDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * The settings table is a key/value store, but — like every other DAO — this one speaks domain
 * models. Callers never see the raw keys; the flavor is stored lowercase ("purple") so the value
 * stays byte-compatible with the prototype's backup JSON.
 */
class SettingsDao(
    private val database: CatatanBelanjaDatabase,
    private val dispatcher: CoroutineDispatcher,
) {

    private val settings = database.settingsQueries

    suspend fun getSettings(): AppSettings = withContext(dispatcher) {
        AppSettings(themeFlavor = parseFlavor(settings.get(KEY_THEME_FLAVOR).executeAsOneOrNull()))
    }

    /** Emits on every write to the theme key, so the app re-themes without being asked to. */
    fun observeSettings(): Flow<AppSettings> = settings.get(KEY_THEME_FLAVOR)
        .asFlow()
        .mapToOneOrNull(dispatcher)
        .map { AppSettings(themeFlavor = parseFlavor(it)) }

    suspend fun saveThemeFlavor(flavor: ThemeFlavor) = withContext(dispatcher) {
        settings.put(KEY_THEME_FLAVOR, flavor.name.lowercase())
    }

    suspend fun clear() = withContext(dispatcher) {
        settings.deleteAll()
    }

    private fun parseFlavor(raw: String?): ThemeFlavor {
        if (raw.isNullOrBlank()) return DEFAULT_FLAVOR
        val key = raw.trim()
        return ThemeFlavor.entries.firstOrNull { it.name.equals(key, ignoreCase = true) }
            ?: DEFAULT_FLAVOR
    }

    private companion object {
        const val KEY_THEME_FLAVOR = "theme_flavor"
        val DEFAULT_FLAVOR = ThemeFlavor.PURPLE
    }
}
