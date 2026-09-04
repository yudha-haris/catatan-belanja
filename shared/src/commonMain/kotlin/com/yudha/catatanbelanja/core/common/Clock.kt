package com.yudha.catatanbelanja.core.common

import kotlinx.datetime.Clock as DateTimeClock

interface Clock {
    fun nowMillis(): Long
}

class SystemClock : Clock {
    override fun nowMillis(): Long = DateTimeClock.System.now().toEpochMilliseconds()
}
