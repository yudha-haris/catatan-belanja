package com.yudha.catatanbelanja.core.data.database

import com.yudha.catatanbelanja.core.domain.model.QtyOverride
import com.yudha.catatanbelanja.core.domain.model.TrendSetting
import com.yudha.catatanbelanja.db.CatatanBelanjaDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * The price-trend corrections. [getSetting] answers for every name, saved or not: an item nobody
 * has touched is measured the default way, which is a fact about the item rather than a missing row.
 */
class TrendDao(
    private val database: CatatanBelanjaDatabase,
    private val dispatcher: CoroutineDispatcher,
) {

    private val settings = database.trendSettingQueries
    private val overrides = database.trendQtyOverrideQueries

    suspend fun getSetting(nameKey: String): TrendSetting = withContext(dispatcher) {
        settings.selectByName(nameKey).executeAsOneOrNull()?.toDomain()
            ?: TrendSetting(nameKey = nameKey)
    }

    suspend fun saveSetting(setting: TrendSetting, updatedAt: Long) = withContext(dispatcher) {
        settings.upsert(
            name_key = setting.nameKey,
            basis = setting.basis.name,
            base_unit = setting.baseUnit,
            updated_at = updatedAt,
        )
    }

    suspend fun getOverrides(nameKey: String): List<QtyOverride> = withContext(dispatcher) {
        overrides.selectByName(nameKey).executeAsList().map { it.toDomain() }
    }

    suspend fun saveOverride(override: QtyOverride) = withContext(dispatcher) {
        overrides.upsert(
            item_id = override.itemId,
            name_key = override.nameKey,
            qty = override.qty,
            unit = override.unit,
        )
    }

    suspend fun deleteOverride(itemId: String) = withContext(dispatcher) {
        overrides.deleteByItemId(itemId)
    }

    suspend fun deleteAll() = withContext(dispatcher) {
        database.transaction {
            overrides.deleteAll()
            settings.deleteAll()
        }
    }
}
