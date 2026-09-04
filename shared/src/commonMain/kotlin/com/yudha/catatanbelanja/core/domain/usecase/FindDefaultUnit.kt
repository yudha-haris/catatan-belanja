package com.yudha.catatanbelanja.core.domain.usecase

import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.repository.CatalogRepository

/**
 * The unit the catalog files an item under — the prototype's `UNIT_DEFAULT[name]`. Null when the
 * catalog has no opinion, which is what the "Satuan" dropdown treats as "leave it as it is".
 */
class FindDefaultUnit(private val catalogRepository: CatalogRepository) {
    operator fun invoke(name: String): String? {
        val key = name.normalized()
        if (key.isEmpty()) return null
        return catalogRepository.current
            .asSequence()
            .flatMap { it.items.asSequence() }
            .firstOrNull { it.name.normalized() == key }
            ?.defaultUnit
            ?.takeIf { it.isNotBlank() }
    }
}
