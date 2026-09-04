package com.yudha.catatanbelanja.core.data.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.yudha.catatanbelanja.core.domain.model.AppLanguage
import com.yudha.catatanbelanja.core.domain.model.AppSettings
import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor
import com.yudha.catatanbelanja.db.CatatanBelanjaDatabase
import com.yudha.catatanbelanja.db.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * The settings table is a key/value store, but — like every other DAO — this one speaks domain
 * models. Callers never see the raw keys; the flavor and the language are stored lowercase
 * ("purple", "id") so the values stay byte-compatible with the prototype's backup JSON.
 */
class SettingsDao(
    private val database: CatatanBelanjaDatabase,
    private val dispatcher: CoroutineDispatcher,
) {

    private val settings = database.settingsQueries

    suspend fun getSettings(): AppSettings = withContext(dispatcher) {
        settings.selectAll().executeAsList().toSettings()
    }

    /** Emits on every write to the table, so the app re-themes and re-languages without being asked to. */
    fun observeSettings(): Flow<AppSettings> = settings.selectAll()
        .asFlow()
        .mapToList(dispatcher)
        .map { it.toSettings() }

    suspend fun saveThemeFlavor(flavor: ThemeFlavor) = withContext(dispatcher) {
        settings.put(KEY_THEME_FLAVOR, flavor.storageValue)
    }

    suspend fun saveLanguage(language: AppLanguage) = withContext(dispatcher) {
        settings.put(KEY_LANGUAGE, language.storageValue)
    }

    /**
     * Whether the built-in catalog has already been written. Kept as a settings row rather than
     * inferred from an empty catalog table: a user who deletes every category means it, and must
     * not find the six defaults back on the next launch.
     */
    suspend fun isCatalogSeeded(): Boolean = withContext(dispatcher) {
        settings.get(KEY_CATALOG_SEEDED).executeAsOneOrNull() == FLAG_SET
    }

    suspend fun markCatalogSeeded() = withContext(dispatcher) {
        settings.put(KEY_CATALOG_SEEDED, FLAG_SET)
    }

    suspend fun clear() = withContext(dispatcher) {
        settings.deleteAll()
    }

    private fun List<Settings>.toSettings(): AppSettings {
        val values = associate { it.key to it.value_ }
        return AppSettings(
            themeFlavor = parseFlavor(values[KEY_THEME_FLAVOR]),
            language = parseLanguage(values[KEY_LANGUAGE]),
        )
    }

    /** An absent or unreadable row is not an error here: both enums fall back to their default. */
    private fun parseFlavor(raw: String?): ThemeFlavor {
        if (raw.isNullOrBlank()) return ThemeFlavor.PURPLE
        return ThemeFlavor.fromStorage(raw)
    }

    private fun parseLanguage(raw: String?): AppLanguage {
        if (raw.isNullOrBlank()) return AppLanguage.SYSTEM
        return AppLanguage.fromStorage(raw)
    }

    private companion object {
        const val KEY_THEME_FLAVOR = "theme_flavor"
        const val KEY_LANGUAGE = "language"
        const val KEY_CATALOG_SEEDED = "catalog_seeded"
        const val FLAG_SET = "1"
    }
}
