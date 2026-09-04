package com.yudha.catatanbelanja.features.dashboard.domain.usecase

import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.common.Clock
import com.yudha.catatanbelanja.core.common.normalized
import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.usecase.FindItemCategory
import com.yudha.catatanbelanja.features.dashboard.domain.model.RankedEntry
import com.yudha.catatanbelanja.features.dashboard.domain.model.RankingMode
import com.yudha.catatanbelanja.features.dashboard.domain.model.ReportRange
import com.yudha.catatanbelanja.features.dashboard.domain.model.ShareSlice
import com.yudha.catatanbelanja.features.dashboard.domain.model.SpendingRankingData

private const val SLICE_COUNT = 5

/**
 * The "Pengeluaran terbesar" page: the summary tab's top five, opened out into the whole ranking
 * plus the share ring that says how concentrated the spending actually is.
 *
 * [RankingMode.CATEGORY] rolls items up through the catalog. Anything the catalog does not know
 * lands in one "lain-lain" row rather than being dropped — an unranked half of the money would
 * make every percentage on the page a lie.
 */
class BuildSpendingRanking(
    private val clock: Clock,
    private val findItemCategory: FindItemCategory,
) {
    operator fun invoke(
        sessions: List<ShoppingSession>,
        range: ReportRange,
        mode: RankingMode,
    ): SpendingRankingData {
        val scoped = sessions.inRange(range, clock.nowMillis())
        val items = scoped.flatMap { it.items }
        if (items.isEmpty()) return SpendingRankingData(range = range, mode = mode)

        val total = items.sumOf { it.price }
        val entries = when (mode) {
            RankingMode.ITEM -> itemEntries(items, total)
            RankingMode.CATEGORY -> categoryEntries(items, total)
        }
        val leader = entries.first()

        return SpendingRankingData(
            range = range,
            mode = mode,
            total = total,
            entryCount = entries.size,
            tripCount = scoped.size,
            entries = entries,
            slices = slices(entries, total),
            leaderLabel = leader.label,
            leaderPercent = leader.sharePercent,
            hasEntries = true,
        )
    }

    private fun itemEntries(items: List<ShoppingItem>, total: Int): List<RankedEntry> {
        val groups = items
            .groupBy { it.name.normalized() }
            .values
            .sortedByDescending { group -> group.sumOf { it.price } }
        val leaderTotal = groups.first().sumOf { it.price }

        return groups.map { group ->
            val groupTotal = group.sumOf { it.price }
            val name = group.first().name
            RankedEntry(
                key = name.normalized(),
                label = name,
                emoji = findItemCategory.emojiFor(name),
                total = groupTotal,
                purchaseCount = group.size,
                averagePrice = averageOf(groupTotal, group.size),
                ratio = ratioOf(groupTotal, leaderTotal),
                sharePercent = percentOf(groupTotal, total),
                trendName = name,
                canOpenTrend = group.size >= 2,
                isOther = false,
            )
        }
    }

    private fun categoryEntries(items: List<ShoppingItem>, total: Int): List<RankedEntry> {
        val groups = items
            .groupBy { findItemCategory(it.name)?.name ?: OTHER_KEY }
            .toList()
            .sortedByDescending { group -> group.second.sumOf { it.price } }
        val leaderTotal = groups.first().second.sumOf { it.price }

        return groups.map { (categoryName, group) ->
            val groupTotal = group.sumOf { it.price }
            val isOther = categoryName == OTHER_KEY
            RankedEntry(
                key = categoryName,
                label = if (isOther) "" else categoryName,
                emoji = emojiOfCategory(categoryName),
                total = groupTotal,
                purchaseCount = group.size,
                averagePrice = averageOf(groupTotal, group.size),
                ratio = ratioOf(groupTotal, leaderTotal),
                sharePercent = percentOf(groupTotal, total),
                trendName = "",
                canOpenTrend = false,
                isOther = isOther,
            )
        }
    }

    /**
     * The five biggest, plus one "lainnya" arc for the tail. The fractions come off the totals
     * rather than off the rounded percentages, so the ring always closes exactly.
     */
    private fun slices(entries: List<RankedEntry>, total: Int): List<ShareSlice> {
        val leaders = entries.take(SLICE_COUNT).mapIndexed { index, entry ->
            ShareSlice(
                key = entry.key,
                label = entry.label,
                emoji = entry.emoji,
                percent = entry.sharePercent,
                fraction = fractionOf(entry.total, total),
                colorIndex = index,
                isOther = false,
            )
        }
        val restTotal = entries.drop(SLICE_COUNT).sumOf { it.total }
        if (restTotal <= 0) return leaders

        return leaders + ShareSlice(
            key = OTHER_KEY,
            label = "",
            emoji = CatalogData.FALLBACK_EMOJI,
            percent = percentOf(restTotal, total),
            fraction = fractionOf(restTotal, total),
            colorIndex = SLICE_COUNT,
            isOther = true,
        )
    }

    private fun emojiOfCategory(name: String): String =
        CatalogData.categories.firstOrNull { it.name == name }?.emoji ?: CatalogData.FALLBACK_EMOJI

    private fun fractionOf(part: Int, whole: Int): Float {
        if (whole <= 0) return 0f
        return part.toFloat() / whole.toFloat()
    }

    private companion object {
        /** The catalog has no row for it, so the label is resolved in the composable. */
        const val OTHER_KEY = "__other__"
    }
}
