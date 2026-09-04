package com.yudha.catatanbelanja.features.preset.domain.model

import com.yudha.catatanbelanja.core.domain.model.CatalogItem

/** One category's worth of the "Belanjaan" list, already filtered by whatever was searched for. */
data class PresetItemSection(
    val categoryId: String,
    val categoryName: String,
    val emoji: String,
    val items: List<CatalogItem>,
)
