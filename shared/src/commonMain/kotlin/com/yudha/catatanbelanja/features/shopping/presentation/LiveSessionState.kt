package com.yudha.catatanbelanja.features.shopping.presentation

import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.LastPurchase
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.NameChipView
import com.yudha.catatanbelanja.features.shopping.domain.model.ShoppingItemView

data class LiveSessionState(
    val session: ShoppingSession? = null,
    val total: Int = 0,
    val itemCount: Int = 0,
    /** Feeds the receipt's "terakhir: …" line; null while the cart is empty. */
    val lastItemName: String? = null,
    val itemViews: List<ShoppingItemView> = emptyList(),
    val query: String = "",
    val nameSuggestions: List<NameChipView> = emptyList(),
    /** True when nothing known matches the query, so the "＋ barang baru" chip shows. */
    val showNewItemChip: Boolean = false,
    val frequentNames: List<NameChipView> = emptyList(),
    /** The catalog's categories, as the browse row draws them. Stored, so the user's own show up. */
    val categoryChips: List<NameChipView> = emptyList(),
    val selectedCategory: String? = null,
    val categoryItems: List<NameChipView> = emptyList(),
    val lastPurchase: LastPurchase? = null,
    val brandSuggestions: List<String> = emptyList(),
    val selectedUnit: String = CatalogData.units.first(),
    val isNamePicked: Boolean = false,
    /** Drives the finish sheet's "tambahkan ke stok" toggle. */
    val stockableCount: Int = 0,
    /** Stamped when the finish sheet opens — the prototype dates the receipt at finish time. */
    val finishedAtMillis: Long = 0L,
    val loadState: UiState<Unit> = UiState.Initial,
    val actionState: UiState<Unit> = UiState.Initial,

    // The plan, ticked off as the cart fills. The chips are the same tap the user was going
    // to make anyway, so following a list costs nothing extra.
    val hasList: Boolean = false,
    val listRemaining: List<NameChipView> = emptyList(),
    /** The first few of [listRemaining]: a long plan must not push the add form off screen. */
    val listPreview: List<NameChipView> = emptyList(),
    /** How many [listRemaining] chips the preview leaves out — the "+12 lagi" chip. */
    val listHiddenCount: Int = 0,
    val listTotalCount: Int = 0,
    val listCheckedCount: Int = 0,
    val listRemainingCount: Int = 0,
    /** 0f..1f for the strip's progress bar — the composable never divides. */
    val listProgress: Float = 0f,
    val isListComplete: Boolean = false,
)
