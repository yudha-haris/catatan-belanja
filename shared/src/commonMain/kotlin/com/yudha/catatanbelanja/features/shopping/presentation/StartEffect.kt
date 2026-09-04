package com.yudha.catatanbelanja.features.shopping.presentation

import com.yudha.catatanbelanja.core.common.Failure

sealed interface StartEffect {
    data class SessionStarted(val sessionId: String) : StartEffect

    /** One session is already running — the screen says so and opens it instead. */
    data object ActiveSessionExists : StartEffect

    data object DemoSeeded : StartEffect

    data class ShowError(val failure: Failure) : StartEffect
}
