package com.yudha.catatanbelanja.features.shopping.domain.usecase

import com.yudha.catatanbelanja.core.common.Clock

/** Wall-clock access for the live session — a use case, so no ViewModel has to hold a [Clock]. */
class CurrentTime(private val clock: Clock) {
    operator fun invoke(): Long = clock.nowMillis()
}
