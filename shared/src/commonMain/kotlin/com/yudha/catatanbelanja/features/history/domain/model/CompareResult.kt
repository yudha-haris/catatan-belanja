package com.yudha.catatanbelanja.features.history.domain.model

/** Everything the compare screen renders. A rising price is bad here, a falling one is good. */
data class CompareResult(
    val inBoth: List<CompareRow> = emptyList(),
    val onlyInA: List<CompareRow> = emptyList(),
    val onlyInB: List<CompareRow> = emptyList(),
    val onlyInATotal: Int = 0,
    val onlyInBTotal: Int = 0,
    val sameCount: Int = 0,
    val differentCount: Int = 0,
    val totalA: Int = 0,
    val totalB: Int = 0,
    val delta: Int = 0,
    val deltaPercent: Int = 0,
    val upCount: Int = 0,
    val downCount: Int = 0,
)
