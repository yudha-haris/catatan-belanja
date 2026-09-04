package com.yudha.catatanbelanja.core.domain.repository

import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.domain.model.AppLanguage
import com.yudha.catatanbelanja.core.domain.model.AppSettings
import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun getSettings(): Resource<AppSettings>

    /**
     * The live settings, re-emitted on every write. A stream rather than the usual
     * `Resource`-returning suspend call: the only consumer is the theme, and a theme that cannot
     * be read degrades to the default rather than surfacing an error to the user.
     */
    fun observeSettings(): Flow<AppSettings>
    suspend fun saveThemeFlavor(flavor: ThemeFlavor): Resource<Unit>

    suspend fun saveLanguage(language: AppLanguage): Resource<Unit>
}
