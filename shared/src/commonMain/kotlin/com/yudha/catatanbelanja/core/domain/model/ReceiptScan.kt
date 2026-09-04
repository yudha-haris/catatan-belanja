package com.yudha.catatanbelanja.core.domain.model

/**
 * What a photographed receipt was read as — a *draft*, never a fact. Every field is the scanner's
 * best guess off a picture of thermal paper, so nothing here is written to the database until the
 * user has looked at it on the review screen and pressed save.
 *
 * [purchasedAt] is the date printed on the paper, in epoch millis at the start of that day. Null
 * when the receipt carried no readable date, which is the review screen's cue to offer today.
 */
data class ReceiptScan(
    val store: String = "",
    val purchasedAt: Long? = null,
    val items: List<ShoppingItem> = emptyList(),
)
