package com.yudha.catatanbelanja.core.domain.model

/**
 * One category of the built-in catalog, as it is written down in
 * [CatalogData][com.yudha.catatanbelanja.core.catalog.CatalogData] — before it is given ids and
 * stored. The catalog the app reads is [CatalogCategory]; this shape exists only so the defaults
 * stay readable as a plain table of names.
 */
data class CatalogSeed(
    val name: String,
    val emoji: String,
    val items: List<String>,
)
