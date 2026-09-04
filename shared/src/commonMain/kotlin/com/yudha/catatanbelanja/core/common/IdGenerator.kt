package com.yudha.catatanbelanja.core.common

import kotlin.random.Random

private const val BASE36_DIGITS = "0123456789abcdefghijklmnopqrstuvwxyz"
private const val RANDOM_PREFIX_LENGTH = 6

interface IdGenerator {
    fun next(): String
}

/** Mirrors the prototype's `uid()`: 6 random base36 chars + the current millis in base36. */
class RandomIdGenerator(private val clock: Clock) : IdGenerator {
    override fun next(): String {
        val prefix = buildString(RANDOM_PREFIX_LENGTH) {
            repeat(RANDOM_PREFIX_LENGTH) { append(BASE36_DIGITS[Random.nextInt(BASE36_DIGITS.length)]) }
        }
        return prefix + clock.nowMillis().toBase36()
    }
}

private fun Long.toBase36(): String {
    if (this == 0L) return "0"
    val negative = this < 0L
    var remaining = if (negative) -this else this
    val digits = StringBuilder()
    while (remaining > 0L) {
        digits.append(BASE36_DIGITS[(remaining % 36L).toInt()])
        remaining /= 36L
    }
    if (negative) digits.append('-')
    return digits.reverse().toString()
}
