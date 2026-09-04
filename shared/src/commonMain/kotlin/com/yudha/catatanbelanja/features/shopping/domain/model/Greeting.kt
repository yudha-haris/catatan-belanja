package com.yudha.catatanbelanja.features.shopping.domain.model

/** The prototype's `greet()`. The screen picks the copy; the ViewModel only picks the slot. */
enum class Greeting {
    MORNING,
    NOON,
    AFTERNOON,
    EVENING,
    ;

    companion object {
        private const val MORNING_UNTIL = 11
        private const val NOON_UNTIL = 15
        private const val AFTERNOON_UNTIL = 18

        fun forHour(hour: Int): Greeting {
            if (hour < MORNING_UNTIL) return MORNING
            if (hour < NOON_UNTIL) return NOON
            if (hour < AFTERNOON_UNTIL) return AFTERNOON
            return EVENING
        }
    }
}
