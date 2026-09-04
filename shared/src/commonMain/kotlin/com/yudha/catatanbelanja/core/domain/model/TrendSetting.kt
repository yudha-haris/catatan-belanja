package com.yudha.catatanbelanja.core.domain.model

/**
 * How one item's price trend is measured. [nameKey] is the item's `name.normalized()`.
 * [baseUnit] is only meaningful for [PriceBasis.PER_UNIT]; it is null until the user picks one.
 */
data class TrendSetting(
    val nameKey: String,
    val basis: PriceBasis = PriceBasis.RAW,
    val baseUnit: String? = null,
)
