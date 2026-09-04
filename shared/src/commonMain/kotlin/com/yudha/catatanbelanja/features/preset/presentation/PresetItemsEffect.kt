package com.yudha.catatanbelanja.features.preset.presentation

sealed interface PresetItemsEffect {
    data object NameRequired : PresetItemsEffect

    /** No category to file it under — the catalog has none yet, or none was picked. */
    data object CategoryRequired : PresetItemsEffect

    /** The name is already in the catalog, under this category or another one. */
    data object DuplicateName : PresetItemsEffect

    data object Saved : PresetItemsEffect

    data object Deleted : PresetItemsEffect
}
