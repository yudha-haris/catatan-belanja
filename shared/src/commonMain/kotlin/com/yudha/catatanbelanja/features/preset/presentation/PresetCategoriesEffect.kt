package com.yudha.catatanbelanja.features.preset.presentation

sealed interface PresetCategoriesEffect {
    data object NameRequired : PresetCategoriesEffect

    data object DuplicateName : PresetCategoriesEffect

    data object Saved : PresetCategoriesEffect

    /** The category and everything filed under it are gone. */
    data object Deleted : PresetCategoriesEffect

    data object ResetToDefaults : PresetCategoriesEffect
}
