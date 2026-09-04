package com.yudha.catatanbelanja.core.catalog

import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.CatalogCategory
import com.yudha.catatanbelanja.core.domain.model.CatalogItem
import com.yudha.catatanbelanja.core.domain.model.CatalogSeed

/**
 * The prototype's CATS / UNITS / UNIT_DEFAULT tables, verbatim. Data, not UI copy.
 *
 * [categories] and [defaultUnits] are only the *defaults* now: they are written into the
 * database once, by [defaultCatalog], and every later read goes through
 * [CatalogRepository][com.yudha.catatanbelanja.core.domain.repository.CatalogRepository]
 * so that Pengaturan > Preset can edit them. [units] and [FALLBACK_EMOJI] stay fixed — a
 * unit is a measurement, not a preference.
 */
object CatalogData {
    val categories: List<CatalogSeed> = listOf(
        CatalogSeed(
            name = "Sembako",
            emoji = "🍚",
            items = listOf(
                "Beras",
                "Minyak Goreng",
                "Gula Pasir",
                "Garam",
                "Tepung Terigu",
                "Telur",
                "Mie Instan",
                "Kecap Manis",
                "Saus Sambal",
                "Santan",
                "Kopi",
                "Teh",
                "Bumbu Dapur",
            ),
        ),
        CatalogSeed(
            name = "Sayur & Buah",
            emoji = "🥬",
            items = listOf(
                "Bawang Merah",
                "Bawang Putih",
                "Cabai",
                "Tomat",
                "Wortel",
                "Kentang",
                "Bayam",
                "Kangkung",
                "Sawi",
                "Pisang",
                "Jeruk",
                "Apel",
                "Semangka",
                "Pepaya",
            ),
        ),
        CatalogSeed(
            name = "Lauk",
            emoji = "🍗",
            items = listOf(
                "Ayam",
                "Daging Sapi",
                "Ikan",
                "Tahu",
                "Tempe",
                "Udang",
                "Sosis",
                "Nugget",
                "Bakso",
            ),
        ),
        CatalogSeed(
            name = "Susu & Roti",
            emoji = "🥛",
            items = listOf(
                "Susu UHT",
                "Susu Bubuk",
                "Roti Tawar",
                "Keju",
                "Yogurt",
                "Mentega",
                "Sereal",
            ),
        ),
        CatalogSeed(
            name = "Rumah Tangga",
            emoji = "🧴",
            items = listOf(
                "Sabun Mandi",
                "Sampo",
                "Pasta Gigi",
                "Deterjen",
                "Pewangi Pakaian",
                "Sabun Cuci Piring",
                "Tisu",
                "Pembalut",
                "Popok",
                "Gas LPG",
                "Air Galon",
                "Kantong Sampah",
            ),
        ),
        CatalogSeed(
            name = "Camilan",
            emoji = "🍪",
            items = listOf(
                "Biskuit",
                "Keripik",
                "Cokelat",
                "Permen",
                "Air Mineral",
                "Sirup",
                "Minuman Kaleng",
            ),
        ),
    )

    val units: List<String> = listOf(
        "pcs",
        "kg",
        "gram",
        "liter",
        "ml",
        "bungkus",
        "ikat",
        "sisir",
        "buah",
        "botol",
        "kotak",
        "galon",
        "tabung",
    )

    /** Keys are already `normalized()` item names. */
    val defaultUnits: Map<String, String> = mapOf(
        "beras" to "kg",
        "minyak goreng" to "liter",
        "gula pasir" to "kg",
        "telur" to "kg",
        "tepung terigu" to "kg",
        "susu uht" to "liter",
        "air galon" to "galon",
        "gas lpg" to "tabung",
        "ayam" to "kg",
        "daging sapi" to "kg",
        "ikan" to "kg",
        "udang" to "kg",
        "bawang merah" to "kg",
        "bawang putih" to "kg",
        "cabai" to "kg",
        "tomat" to "kg",
        "wortel" to "kg",
        "kentang" to "kg",
        "bayam" to "ikat",
        "kangkung" to "ikat",
        "sawi" to "ikat",
        "pisang" to "sisir",
        "jeruk" to "kg",
        "apel" to "kg",
        "semangka" to "buah",
        "pepaya" to "buah",
        "mie instan" to "bungkus",
        "roti tawar" to "bungkus",
        "bakso" to "bungkus",
    )

    const val FALLBACK_EMOJI = "🛍️"
}

/**
 * The built-in catalog as rows, ready to be written to an empty database: [CatalogData.categories]
 * with its [CatalogData.defaultUnits] folded in, and ids slugged from the names.
 *
 * The ids are derived rather than generated so that two installs seed the same catalog — a
 * "beras" row means the same thing on both, which keeps a future export honest.
 */
fun CatalogData.defaultCatalog(): List<CatalogCategory> =
    categories.mapIndexed { categoryIndex, seed ->
        val categoryId = seed.name.toCatalogId()
        CatalogCategory(
            id = categoryId,
            name = seed.name,
            emoji = seed.emoji,
            position = categoryIndex,
            items = seed.items.mapIndexed { itemIndex, name ->
                CatalogItem(
                    id = name.toCatalogId(),
                    categoryId = categoryId,
                    name = name,
                    defaultUnit = defaultUnits[name.normalized()].orEmpty(),
                    position = itemIndex,
                )
            },
        )
    }

/** "Minyak Goreng" -> "minyak-goreng". Only used for the built-in rows, whose names are ASCII. */
private fun String.toCatalogId(): String = normalized().replace(' ', '-')
