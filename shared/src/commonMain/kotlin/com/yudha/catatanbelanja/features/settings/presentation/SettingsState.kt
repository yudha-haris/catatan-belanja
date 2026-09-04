package com.yudha.catatanbelanja.features.settings.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.ThemeFlavor

/**
 * [loadState] covers reading the screen's own content; [actionState] covers the data operations
 * (demo, export, import, wipe) so a running import can disable the rows without blanking them.
 */
data class SettingsState(
    val loadState: UiState<Unit> = UiState.Initial,
    val actionState: UiState<Unit> = UiState.Initial,
    val themeFlavor: ThemeFlavor = ThemeFlavor.PURPLE,
    val sessionCount: Int = 0,
    val stockCount: Int = 0,
)
