package com.yudha.catatanbelanja.features.dashboard.domain.model

/** One price sample of the trend chart. [ratio] is 0..1 between the cheapest and priciest sample. */
data class TrendPoint(
    val endedAt: Long,
    val price: Int,
    val ratio: Float,
)
