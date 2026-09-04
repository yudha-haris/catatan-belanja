package com.yudha.catatanbelanja.core.catalog

import com.yudha.catatanbelanja.core.common.normalized

/**
 * How the catalog's units relate to each other, so a price trend can put 500 gram and 2 kg on the
 * same axis. Data, like [CatalogData] — not a rule anyone applies by hand.
 *
 * Only mass and volume actually convert. Everything else ("bungkus", "ikat", "botol", …) is a
 * count of something whose size the app does not know, so each of those is its own family and
 * converts to nothing but itself. Guessing that one botol is one liter would quietly invent data.
 */
object UnitConversion {

    /** Every convertible unit, mapped to its family and its size in that family's base unit. */
    private val factors: Map<String, Pair<String, Double>> = mapOf(
        "kg" to (MASS to 1000.0),
        "gram" to (MASS to 1.0),
        "liter" to (VOLUME to 1000.0),
        "ml" to (VOLUME to 1.0),
    )

    /** [qty] of [from] expressed in [to], or null when the two units cannot be compared. */
    fun convert(qty: Double, from: String, to: String): Double? {
        val fromKey = from.normalized()
        val toKey = to.normalized()
        if (fromKey == toKey) return qty

        val source = factors[fromKey] ?: return null
        val target = factors[toKey] ?: return null
        if (source.first != target.first) return null

        return qty * source.second / target.second
    }

    /** Every unit [unit] can be quoted in, itself first. A count unit answers with just itself. */
    fun family(unit: String): List<String> {
        val key = unit.normalized()
        val family = factors[key]?.first ?: return listOf(key)
        val siblings = factors.filterValues { it.first == family }.keys
        return listOf(key) + siblings.filter { it != key }
    }

    private const val MASS = "mass"
    private const val VOLUME = "volume"
}
