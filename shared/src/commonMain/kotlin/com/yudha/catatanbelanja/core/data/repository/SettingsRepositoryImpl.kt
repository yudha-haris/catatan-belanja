package com.yudha.catatanbelanja.core.data.repository

import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.resourceOf
import com.yudha.catatanbelanja.core.data.database.SettingsDao
import com.yudha.catatanbelanja.core.domain.model.AppLanguage
import com.yudha.catatanbelanja.core.domain.model.AppSettings
import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor
import com.yudha.catatanbelanja.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class SettingsRepositoryImpl(
    private val settingsDao: SettingsDao,
) : SettingsRepository {

    override suspend fun getSettings(): Resource<AppSettings> =
        resourceOf(MSG_LOAD) { settingsDao.getSettings() }

    // A theme that cannot be read is not worth an error dialog — fall back to the default.
    override fun observeSettings(): Flow<AppSettings> = settingsDao.observeSettings()
        .catch { emit(AppSettings()) }

    override suspend fun saveThemeFlavor(flavor: ThemeFlavor): Resource<Unit> =
        resourceOf(MSG_SAVE_THEME) { settingsDao.saveThemeFlavor(flavor) }

    override suspend fun saveLanguage(language: AppLanguage): Resource<Unit> =
        resourceOf(MSG_SAVE_LANGUAGE) { settingsDao.saveLanguage(language) }

    private companion object {
        const val MSG_LOAD = "Failed to load settings"
        const val MSG_SAVE_THEME = "Failed to save the theme flavor"
        const val MSG_SAVE_LANGUAGE = "Failed to save the language"
    }
}
