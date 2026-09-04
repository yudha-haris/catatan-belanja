package com.yudha.catatanbelanja.android.format

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

// The patterns are the prototype's (24h "HH.mm", d-MMM order); the day and month names follow the
// language the app is drawn in, so the English build reads "Thu, 13 Aug" beside its English copy.
private const val DAY_PATTERN = "EEE, d MMM"
private const val LONG_DATE_PATTERN = "EEEE, d MMMM yyyy"
private const val TIME_PATTERN = "HH.mm"
private const val SHORT_DATE_PATTERN = "d MMM"
// The one editable date in the app. Numeric, because it is typed back in.
private const val INPUT_DATE_PATTERN = "d/M/yyyy"
private const val MONTH_PATTERN = "MMMM yyyy"
private const val SHORT_MONTH_PATTERN = "MMM"

/**
 * Formatters are resolved per call rather than held as top-level values: the language is a
 * setting now, so the locale a formatter was built for can stop being the current one. Building
 * one is not free, hence the cache — keyed on the locale as well as the pattern.
 */
private val formatters = ConcurrentHashMap<String, DateTimeFormatter>()

private fun patternOf(pattern: String): DateTimeFormatter {
    val locale = Locale.getDefault()
    return formatters.computeIfAbsent("$pattern|${locale.toLanguageTag()}") {
        DateTimeFormatter.ofPattern(pattern, locale)
    }
}

private fun Long.atSystemZone(): ZonedDateTime =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault())

/** "Sen, 4 Sep" */
fun Long.toDayLabel(): String = patternOf(DAY_PATTERN).format(this.atSystemZone())

/** "Senin, 4 September 2026" */
fun Long.toLongDateLabel(): String = patternOf(LONG_DATE_PATTERN).format(this.atSystemZone())

/** "10.15" */
fun Long.toTimeLabel(): String = patternOf(TIME_PATTERN).format(this.atSystemZone())

/** "4 Sep" */
fun Long.toShortDateLabel(): String = patternOf(SHORT_DATE_PATTERN).format(this.atSystemZone())

/**
 * "4/9/2026" — the shape the scan review screen seeds its date field with, and the one
 * `ScanReceiptViewModel.save` parses back. Day first, as every Indonesian receipt prints it.
 */
fun Long.toInputDateLabel(): String = patternOf(INPUT_DATE_PATTERN).format(this.atSystemZone())

/** "2026-09" -> "September 2026". An unparseable key is returned untouched. */
fun String.monthKeyToLabel(): String {
    val month = try {
        YearMonth.parse(this)
    } catch (error: DateTimeParseException) {
        return this
    }
    return patternOf(MONTH_PATTERN).format(month)
}

/** "2026-09" -> "Sep". The chart-axis form of [monthKeyToLabel] — a bar is 30dp wide. */
fun String.monthKeyToShortLabel(): String {
    val month = try {
        YearMonth.parse(this)
    } catch (error: DateTimeParseException) {
        return this
    }
    return patternOf(SHORT_MONTH_PATTERN).format(month)
}
