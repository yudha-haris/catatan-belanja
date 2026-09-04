package com.yudha.catatanbelanja.android.format

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

// The patterns are the prototype's (24h "HH.mm", d-MMM order); the day and month names follow
// the device locale, so the English build reads "Thu, 13 Aug" beside its English copy.
private fun patternOf(pattern: String): DateTimeFormatter =
    DateTimeFormatter.ofPattern(pattern, Locale.getDefault())

private val dayFormatter: DateTimeFormatter = patternOf("EEE, d MMM")
private val longDateFormatter: DateTimeFormatter = patternOf("EEEE, d MMMM yyyy")
private val timeFormatter: DateTimeFormatter = patternOf("HH.mm")
private val shortDateFormatter: DateTimeFormatter = patternOf("d MMM")
private val monthFormatter: DateTimeFormatter = patternOf("MMMM yyyy")

private fun Long.atSystemZone(): ZonedDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault())

/** "Sen, 4 Sep" */
fun Long.toDayLabel(): String = dayFormatter.format(this.atSystemZone())

/** "Senin, 4 September 2026" */
fun Long.toLongDateLabel(): String = longDateFormatter.format(this.atSystemZone())

/** "10.15" */
fun Long.toTimeLabel(): String = timeFormatter.format(this.atSystemZone())

/** "4 Sep" */
fun Long.toShortDateLabel(): String = shortDateFormatter.format(this.atSystemZone())

/** "2026-09" -> "September 2026". An unparseable key is returned untouched. */
fun String.monthKeyToLabel(): String {
    val month = try {
        YearMonth.parse(this)
    } catch (error: DateTimeParseException) {
        return this
    }
    return monthFormatter.format(month)
}
