package com.yudha.catatanbelanja.core.domain.model

/**
 * What a price point on the trend chart actually measures.
 *
 * [RAW] is the default and stays the default on purpose: a trip is a pattern, and what the user
 * recognises is what the item cost that trip. [PER_UNIT] divides by the quantity, which is the
 * only honest comparison once the same item is bought 0.5 kg one month and 2 kg the next — but it
 * needs a quantity on every purchase, which the receipt does not always have.
 */
enum class PriceBasis {
    RAW,
    PER_UNIT,
}
