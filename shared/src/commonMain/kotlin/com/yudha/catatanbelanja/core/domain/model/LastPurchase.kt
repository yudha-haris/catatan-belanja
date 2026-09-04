package com.yudha.catatanbelanja.core.domain.model

data class LastPurchase(
    val price: Int,
    val qty: Double?,
    val unit: String?,
    val note: String,
    val whenMillis: Long,
    val store: String,
)
