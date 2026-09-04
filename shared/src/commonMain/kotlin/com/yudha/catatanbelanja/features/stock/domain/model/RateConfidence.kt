package com.yudha.catatanbelanja.features.stock.domain.model

/**
 * How much the app is entitled to claim about a drain rate. This is the one thing the estimate
 * must always be honest about: a guess drawn from a single observation and a guess drawn from
 * six months of them look identical on screen unless the difference is said out loud.
 */
enum class RateConfidence {
    /** One usable window. Enough to show, not enough to lean on. */
    LOW,

    /** Two windows that agree well enough to average. */
    MEDIUM,

    /** Three or more. The rate has survived being wrong a few times. */
    HIGH,

    /** Not a guess at all — the user stated the rate outright. */
    EXACT,
}
