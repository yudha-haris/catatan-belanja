package com.yudha.catatanbelanja.core.domain.model

/**
 * How fast one stock item is used up. An item with nothing saved reads back as this default:
 * [RateMode.AUTO], where the app infers the rate from [StockReading]s and asks the user nothing.
 * The three `manual*` fields only mean anything under [RateMode.MANUAL].
 */
data class StockRate(
    val itemId: String,
    val mode: RateMode = RateMode.AUTO,
    val manualQty: Double? = null,
    val manualUnit: String? = null,
    val manualPeriod: RatePeriod = RatePeriod.WEEK,
    val updatedAt: Long = 0L,
)

/** Who decides the drain rate of an item. */
enum class RateMode {
    /** Inferred from the item's own history. The default, and the one nobody has to choose. */
    AUTO,

    /** The user stated it outright — an exact figure, never overruled by the inferred one. */
    MANUAL,

    /** No estimate at all. For the shelf items that sit there for a year. */
    OFF,
    ;

    companion object {
        fun fromStorage(value: String): RateMode =
            entries.firstOrNull { it.name == value.trim().uppercase() } ?: AUTO
    }
}

/**
 * The span a manual rate is quoted over. People say "a kilo a month", not "33 grams a day", so
 * the user's own framing is what gets stored and shown back; [days] is what the maths uses.
 */
enum class RatePeriod(val days: Double) {
    DAY(1.0),
    WEEK(7.0),
    MONTH(30.0),
    ;

    companion object {
        fun fromStorage(value: String?): RatePeriod =
            entries.firstOrNull { it.name == value?.trim()?.uppercase() } ?: WEEK
    }
}
