package com.yudha.catatanbelanja.core.common

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** "YYYY-MM" in the system time zone — the key sessions and stock check logs are grouped by. */
fun Long.toMonthKey(): String = toLocalDate().let { monthKeyOf(it.year, it.monthNumber) }

fun Long.toLocalDate(): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).date

fun monthKeyOf(year: Int, month: Int): String = "$year-${month.toString().padStart(2, '0')}"

/**
 * "YYYY-MM-DD-HHmm" in the system time zone — a sortable stamp for backup file names, so two
 * exports on the same day do not land on top of each other.
 */
fun Long.toFileStamp(): String {
    val at = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
    val month = at.monthNumber.pad()
    val day = at.dayOfMonth.pad()
    return "${at.year}-$month-$day-${at.hour.pad()}${at.minute.pad()}"
}

private fun Int.pad(): String = toString().padStart(2, '0')
