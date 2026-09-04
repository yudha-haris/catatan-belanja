package com.yudha.catatanbelanja.features.dashboard.domain.model

/**
 * Whether the ranking counts individual items or rolls them up into the catalog's categories.
 * The category view is what answers "where is the money actually going" — five kinds of vegetable
 * are five small rows as items and one large row as sayuran.
 */
enum class RankingMode {
    ITEM,
    CATEGORY,
}
