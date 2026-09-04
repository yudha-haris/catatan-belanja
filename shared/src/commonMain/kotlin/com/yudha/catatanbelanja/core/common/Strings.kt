package com.yudha.catatanbelanja.core.common

private val WHITESPACE = Regex("""\s+""")
private val WORD_START = Regex("""\b\w""")

/** trim + lowercase + collapse whitespace — the app's canonical item-name key. */
fun String.normalized(): String = trim().lowercase().replace(WHITESPACE, " ")

fun String.capitalizeWords(): String = WORD_START.replace(this) { it.value.uppercase() }
