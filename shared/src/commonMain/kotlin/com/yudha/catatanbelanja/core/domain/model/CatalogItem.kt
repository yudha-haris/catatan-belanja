package com.yudha.catatanbelanja.core.domain.model

/**
 * One item of the catalog — the "belanjaan" preset. [defaultUnit] is the prototype's
 * `UNIT_DEFAULT` entry for the name; empty means the app has no opinion and the first unit of
 * [CatalogData.units][com.yudha.catatanbelanja.core.catalog.CatalogData.units] stands in.
 */
data class CatalogItem(
    val id: String,
    val categoryId: String,
    val name: String,
    val defaultUnit: String = "",
    val position: Int = 0,
)
