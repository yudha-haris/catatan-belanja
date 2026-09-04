package com.yudha.catatanbelanja.core.domain.repository

import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.domain.model.QtyOverride
import com.yudha.catatanbelanja.core.domain.model.TrendSetting

/**
 * The manual corrections behind the price trend. Everything is keyed by `name.normalized()`, so a
 * setting survives the user writing the same item down with a different capitalisation next trip.
 */
interface TrendRepository {
    /** Never null: a name with nothing saved reads back as the [TrendSetting] default. */
    suspend fun getSetting(nameKey: String): Resource<TrendSetting>

    suspend fun saveSetting(setting: TrendSetting): Resource<Unit>

    suspend fun getOverrides(nameKey: String): Resource<List<QtyOverride>>

    suspend fun saveOverride(override: QtyOverride): Resource<Unit>

    suspend fun deleteOverride(itemId: String): Resource<Unit>

    /** Part of "hapus semua data" — the corrections are worthless without the receipts. */
    suspend fun clearAll(): Resource<Unit>
}
