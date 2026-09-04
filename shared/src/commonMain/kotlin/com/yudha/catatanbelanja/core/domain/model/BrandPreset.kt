package com.yudha.catatanbelanja.core.domain.model

/** A brand the user keeps typing, kept so it can be tapped instead. One flat, item-agnostic list. */
data class BrandPreset(
    val id: String,
    val name: String,
    val position: Int = 0,
)
