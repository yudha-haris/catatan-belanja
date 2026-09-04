package com.yudha.catatanbelanja.core.domain.model

/**
 * One moment the app knew how much of a stock item was in the house. Readings are the only
 * evidence behind an automatic drain rate — nothing else in the app records *when* a quantity
 * was true, only what it currently is.
 */
data class StockReading(
    val itemId: String,
    val qty: Double,
    val unit: String,
    val at: Long,
    val source: ReadingSource,
)

/**
 * Why a reading was taken. It matters because a [PURCHASE] raises the quantity: the drop that
 * follows it is consumption measured from the new, higher shelf rather than from the old one.
 */
enum class ReadingSource {
    MANUAL,
    CHECK,
    PURCHASE,
    ;

    companion object {
        /** Unknown storage values read as [MANUAL] — a reading with a lost reason is still a reading. */
        fun fromStorage(value: String): ReadingSource =
            entries.firstOrNull { it.name == value.trim().uppercase() } ?: MANUAL
    }
}
