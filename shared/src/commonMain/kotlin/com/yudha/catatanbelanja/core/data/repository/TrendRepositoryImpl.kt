package com.yudha.catatanbelanja.core.data.repository

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.resourceOf
import com.yudha.catatanbelanja.core.data.database.TrendDao
import com.yudha.catatanbelanja.core.domain.model.QtyOverride
import com.yudha.catatanbelanja.core.domain.model.TrendSetting
import com.yudha.catatanbelanja.core.domain.repository.TrendRepository

class TrendRepositoryImpl(
    private val trendDao: TrendDao,
    private val clock: Clock,
) : TrendRepository {

    override suspend fun getSetting(nameKey: String): Resource<TrendSetting> =
        resourceOf(MSG_LOAD_SETTING) { trendDao.getSetting(nameKey) }

    override suspend fun saveSetting(setting: TrendSetting): Resource<Unit> =
        resourceOf(MSG_SAVE_SETTING) { trendDao.saveSetting(setting, clock.nowMillis()) }

    override suspend fun getOverrides(nameKey: String): Resource<List<QtyOverride>> =
        resourceOf(MSG_LOAD_OVERRIDES) { trendDao.getOverrides(nameKey) }

    override suspend fun saveOverride(override: QtyOverride): Resource<Unit> =
        resourceOf(MSG_SAVE_OVERRIDE) { trendDao.saveOverride(override) }

    override suspend fun deleteOverride(itemId: String): Resource<Unit> =
        resourceOf(MSG_DELETE_OVERRIDE) { trendDao.deleteOverride(itemId) }

    override suspend fun clearAll(): Resource<Unit> =
        resourceOf(MSG_CLEAR) { trendDao.deleteAll() }

    private companion object {
        const val MSG_LOAD_SETTING = "Failed to load the trend setting"
        const val MSG_SAVE_SETTING = "Failed to save the trend setting"
        const val MSG_LOAD_OVERRIDES = "Failed to load the trend quantity overrides"
        const val MSG_SAVE_OVERRIDE = "Failed to save the trend quantity override"
        const val MSG_DELETE_OVERRIDE = "Failed to remove the trend quantity override"
        const val MSG_CLEAR = "Failed to clear the trend adjustments"
    }
}
