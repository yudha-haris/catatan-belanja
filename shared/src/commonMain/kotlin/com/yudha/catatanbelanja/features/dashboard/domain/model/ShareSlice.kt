package com.yudha.catatanbelanja.features.dashboard.domain.model

/**
 * One arc of the share donut. [fraction] is the slice's cut of the whole ring (0..1) and
 * [colorIndex] its position in the screen's ramp — the ViewModel picks the order, the composable
 * picks the colours, so no palette leaks into the shared module.
 */
data class ShareSlice(
    val key: String,
    val label: String,
    val emoji: String,
    val percent: Int,
    val fraction: Float,
    val colorIndex: Int,
    val isOther: Boolean,
)
