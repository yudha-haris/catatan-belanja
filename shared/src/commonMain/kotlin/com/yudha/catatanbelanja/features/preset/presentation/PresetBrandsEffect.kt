package com.yudha.catatanbelanja.features.preset.presentation

sealed interface PresetBrandsEffect {
    data object NameRequired : PresetBrandsEffect

    /** The name is already on the list; the editor stays open on it. */
    data object DuplicateName : PresetBrandsEffect

    data object Saved : PresetBrandsEffect

    data object Deleted : PresetBrandsEffect
}
