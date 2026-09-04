package com.yudha.catatanbelanja.features.history.domain.model

/**
 * One line of the A/B comparison, aggregated by normalized name. A null price means the item is
 * absent from that side, and each side keeps the unit it was actually logged with.
 * [deltaAmount] is the unsigned B − A gap; [delta] says how to read it.
 */
data class CompareRow(
    val name: String,
    val emoji: String,
    val priceA: Int?,
    val qtyA: Double?,
    val unitA: String?,
    val priceB: Int?,
    val qtyB: Double?,
    val unitB: String?,
    val deltaAmount: Int?,
    val delta: Delta,
) {
    /** Which way B moved against A. [NONE] means the row exists on one side only. */
    enum class Delta { NONE, SAME, UP, DOWN }
}
