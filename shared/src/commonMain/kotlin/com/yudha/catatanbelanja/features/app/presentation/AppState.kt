package com.yudha.catatanbelanja.features.app.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor

/**
 * The boot state. [hasActiveSession] is read once, at cold start, so the shell can open straight
 * into the running session the way the prototype's boot block does.
 */
data class AppState(
    val loadState: UiState<Unit> = UiState.Initial,
    val themeFlavor: ThemeFlavor = ThemeFlavor.PURPLE,
    val hasActiveSession: Boolean = false,
)
