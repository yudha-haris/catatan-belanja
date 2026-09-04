package com.yudha.catatanbelanja.core.domain.usecase

import com.yudha.catatanbelanja.core.domain.model.NameChipView

/** The prototype's `chip()` helper: every suggestion chip carries `icon(name)`, never nothing. */
class BuildNameChips(private val findItemCategory: FindItemCategory) {
    operator fun invoke(names: List<String>): List<NameChipView> = names.map { name ->
        NameChipView(name = name, emoji = findItemCategory.emojiFor(name))
    }
}
