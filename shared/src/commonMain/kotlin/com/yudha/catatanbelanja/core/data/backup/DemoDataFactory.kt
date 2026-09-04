package com.yudha.catatanbelanja.core.data.backup

import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.IdGenerator
import com.yudha.catatanbelanja.core.common.monthKeyOf
import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockCheckEntry
import com.yudha.catatanbelanja.core.domain.model.StockCheckLog
import com.yudha.catatanbelanja.core.domain.model.StockItem
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max
import kotlin.math.round

/**
 * Ports the prototype's `seedDemo()` verbatim: the same base table, the same shops and brand
 * notes, and the same linear congruential generator. The generator is seeded once per
 * [sessions] call, so the same clock always produces the same six sessions.
 */
class DemoDataFactory(
    private val clock: Clock,
    private val idGenerator: IdGenerator,
) {

    /** Six finished sessions, oldest first, 13 days apart with a 2%-per-session inflation drift. */
    fun sessions(): List<ShoppingSession> {
        val random = Lcg()
        val zone = TimeZone.currentSystemDefault()
        val today = Instant.fromEpochMilliseconds(clock.nowMillis()).toLocalDateTime(zone).date
        val sessions = mutableListOf<ShoppingSession>()
        for (step in SESSION_COUNT - 1 downTo 0) {
            val date = today.minus(DatePeriod(days = step * SESSION_INTERVAL_DAYS + SESSION_OFFSET_DAYS))
            val endedAt = LocalDateTime(
                year = date.year,
                monthNumber = date.monthNumber,
                dayOfMonth = date.dayOfMonth,
                hour = FIRST_HOUR + step,
                minute = SESSION_MINUTE,
            ).toInstant(zone).toEpochMilliseconds()
            val drift = 1 + (SESSION_COUNT - 1 - step) * DRIFT_PER_SESSION
            // `filter` draws once per base row before `map` runs — keep that call order.
            val items = BASE_ITEMS
                .filter { random.next() > SKIP_THRESHOLD }
                .map { base -> demoItem(base, drift, random) }
            val store = SHOPS[step]
            sessions += ShoppingSession(
                id = idGenerator.next(),
                name = store,
                store = store,
                startedAt = endedAt - SESSION_LENGTH_MILLIS,
                endedAt = endedAt,
                items = items,
            )
        }
        return sessions
    }

    fun stockItems(): List<StockItem> {
        val updatedAt = clock.nowMillis()
        return DEMO_STOCK.map { row ->
            StockItem(
                id = idGenerator.next(),
                name = row.name,
                qty = row.qty,
                unit = row.unit,
                minQty = row.minQty,
                fullQty = max(row.qty, FULL_FLOOR),
                updatedAt = updatedAt,
            )
        }
    }

    /** One stock check for last month, dated the 28th at the current time of day. */
    fun checkLog(): StockCheckLog {
        val zone = TimeZone.currentSystemDefault()
        val now = Instant.fromEpochMilliseconds(clock.nowMillis()).toLocalDateTime(zone)
        val month = previousMonthOf(now.date)
        val checkedAt = LocalDateTime(
            year = month.year,
            monthNumber = month.monthNumber,
            dayOfMonth = CHECK_DAY_OF_MONTH,
            hour = now.hour,
            minute = now.minute,
            second = now.second,
            nanosecond = now.nanosecond,
        ).toInstant(zone).toEpochMilliseconds()
        return StockCheckLog(
            id = idGenerator.next(),
            month = monthKeyOf(month.year, month.monthNumber),
            checkedAt = checkedAt,
            entries = DEMO_STOCK.map { row ->
                StockCheckEntry(
                    name = row.name,
                    qty = round((row.qty + CHECK_QTY_BUMP) * 2) / 2,
                    unit = row.unit,
                )
            },
        )
    }

    private fun demoItem(base: BaseItem, drift: Double, random: Lcg): ShoppingItem {
        val jitter = PRICE_JITTER_FLOOR + random.next() * PRICE_JITTER_SPREAD
        val rounded = round(base.price * drift * jitter / PRICE_STEP).toInt() * PRICE_STEP
        val qty = if (base.name == RICE_NAME && random.next() > BULK_THRESHOLD) RICE_BULK_QTY else base.qty
        val note = NOTES[base.name]
        return ShoppingItem(
            id = idGenerator.next(),
            name = base.name,
            price = if (qty != base.qty) rounded * 2 else rounded,
            qty = qty,
            unit = base.unit,
            note = if (note != null && random.next() > NOTE_THRESHOLD) note else "",
        )
    }

    /**
     * Mirrors JavaScript's `setMonth(month - 1)`: a day-of-month the shorter previous month
     * cannot hold rolls forward into the month after it.
     */
    private fun previousMonthOf(date: LocalDate): LocalDate {
        val firstOfPrevious = LocalDate(date.year, date.monthNumber, 1).minus(DatePeriod(months = 1))
        val lengthOfPrevious = firstOfPrevious.plus(DatePeriod(months = 1)).toEpochDays() -
            firstOfPrevious.toEpochDays()
        if (date.dayOfMonth <= lengthOfPrevious) return firstOfPrevious
        return firstOfPrevious.plus(DatePeriod(months = 1))
    }

    /** The prototype's `seed = (seed * 9301 + 49297) % 233280` generator. */
    private class Lcg {
        private var seed = INITIAL_SEED

        fun next(): Double {
            seed = (seed * MULTIPLIER + INCREMENT) % MODULUS
            return seed / MODULUS.toDouble()
        }

        private companion object {
            const val INITIAL_SEED = 7L
            const val MULTIPLIER = 9301L
            const val INCREMENT = 49297L
            const val MODULUS = 233280L
        }
    }

    private data class BaseItem(
        val name: String,
        val qty: Double,
        val unit: String,
        val price: Int,
    )

    private data class StockRow(
        val name: String,
        val qty: Double,
        val unit: String,
        val minQty: Double,
    )

    private companion object {
        const val SESSION_COUNT = 6
        const val SESSION_INTERVAL_DAYS = 13
        const val SESSION_OFFSET_DAYS = 2
        const val FIRST_HOUR = 10
        const val SESSION_MINUTE = 15
        const val SESSION_LENGTH_MILLIS = 40L * 60L * 1000L
        const val DRIFT_PER_SESSION = 0.02
        const val SKIP_THRESHOLD = 0.22
        const val PRICE_JITTER_FLOOR = 0.92
        const val PRICE_JITTER_SPREAD = 0.16
        const val PRICE_STEP = 500
        const val RICE_NAME = "Beras"
        const val RICE_BULK_QTY = 10.0
        const val BULK_THRESHOLD = 0.5
        const val NOTE_THRESHOLD = 0.4
        const val FULL_FLOOR = 5.0
        const val CHECK_DAY_OF_MONTH = 28
        const val CHECK_QTY_BUMP = 1.5

        val BASE_ITEMS = listOf(
            BaseItem("Beras", 5.0, "kg", 72_000),
            BaseItem("Minyak Goreng", 2.0, "liter", 36_000),
            BaseItem("Gula Pasir", 1.0, "kg", 18_000),
            BaseItem("Telur", 1.0, "kg", 29_000),
            BaseItem("Mie Instan", 10.0, "bungkus", 32_000),
            BaseItem("Ayam", 1.0, "kg", 38_000),
            BaseItem("Bawang Merah", 0.5, "kg", 22_000),
            BaseItem("Cabai", 0.25, "kg", 15_000),
            BaseItem("Susu UHT", 2.0, "liter", 38_000),
            BaseItem("Sabun Mandi", 2.0, "pcs", 9_000),
            BaseItem("Deterjen", 1.0, "bungkus", 24_000),
            BaseItem("Tisu", 1.0, "pcs", 14_000),
            BaseItem("Roti Tawar", 1.0, "bungkus", 15_000),
            BaseItem("Tomat", 0.5, "kg", 7_000),
            BaseItem("Pisang", 1.0, "sisir", 18_000),
            BaseItem("Kecap Manis", 1.0, "botol", 13_000),
        )

        val SHOPS = listOf(
            "Superindo",
            "Pasar Rawa",
            "Indomaret",
            "Alfamart",
            "Pasar Rawa",
            "Superindo",
        )

        val NOTES = mapOf(
            "Minyak Goreng" to "Bimoli 2L",
            "Mie Instan" to "Indomie goreng",
            "Susu UHT" to "Ultra full cream",
            "Deterjen" to "Rinso 800g",
        )

        val DEMO_STOCK = listOf(
            StockRow("Beras", 3.0, "kg", 2.0),
            StockRow("Minyak Goreng", 0.5, "liter", 1.0),
            StockRow("Gula Pasir", 1.5, "kg", 0.5),
            StockRow("Telur", 0.25, "kg", 0.5),
            StockRow("Mie Instan", 6.0, "bungkus", 3.0),
            StockRow("Sabun Mandi", 1.0, "pcs", 1.0),
            StockRow("Deterjen", 0.0, "bungkus", 1.0),
            StockRow("Susu UHT", 2.0, "liter", 1.0),
            StockRow("Tisu", 2.0, "pcs", 1.0),
        )
    }
}
