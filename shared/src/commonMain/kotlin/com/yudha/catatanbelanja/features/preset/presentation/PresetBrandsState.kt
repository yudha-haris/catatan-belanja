package com.yudha.catatanbelanja.features.preset.presentation

import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.BrandPreset

/**
 * [loadState] covers the list, [actionState] the writes — so a save in flight disables the rows
 * rather than blanking the list under the sheet the user is looking at.
 */
data class PresetBrandsState(
    val loadState: UiState<Unit> = UiState.Initial,
    val actionState: UiState<Unit> = UiState.Initial,
    val brands: List<BrandPreset> = emptyList(),
    val isEditorOpen: Boolean = false,
    /** Null while the editor is adding rather than editing. */
    val editorBrand: BrandPreset? = null,
)
