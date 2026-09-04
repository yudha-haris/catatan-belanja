package com.yudha.catatanbelanja.features.dashboard.domain.model

/**
 * How far back a report reaches. Each page offers only the windows that make sense for it and
 * publishes them as `rangeOptions`, so the chip row is data rather than a hardcoded list.
 */
enum class ReportRange {
    MONTH,
    THREE_MONTHS,
    SIX_MONTHS,
    ALL,
}
