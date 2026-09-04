package com.yudha.catatanbelanja.core.domain.model

/** One category of the catalog, with the items filed under it in [position] order. */
data class CatalogCategory(
    val id: String,
    val name: String,
    val emoji: String,
    val position: Int = 0,
    val items: List<CatalogItem> = emptyList(),
)
