package com.yudha.catatanbelanja.features.stock.presentation

import com.yudha.catatanbelanja.core.catalog.CatalogData
import com.yudha.catatanbelanja.core.common.UiState
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.features.stock.domain.model.StockCheckLogView
import com.yudha.catatanbelanja.features.stock.domain.model.StockCheckRow
import com.yudha.catatanbelanja.features.stock.domain.model.StockRowView
import com.yudha.catatanbelanja.features.stock.domain.model.StockUsageRow

private const val DEFAULT_UNIT = "pcs"

/**
 * The Stok tab and its three sheets. Each sheet owns its own named fields so nothing has to be
 * re-derived while a sheet is open.
 */
data class StockState(
    val loadState: UiState<Unit> = UiState.Initial,
    val actionState: UiState<Unit> = UiState.Initial,

    // list
    val lowRows: List<StockRowView> = emptyList(),
    val okRows: List<StockRowView> = emptyList(),
    val totalCount: Int = 0,
    val lowCount: Int = 0,
    val hasAny: Boolean = false,
    val logs: List<StockCheckLogView> = emptyList(),

    // add / edit sheet
    val isEditorOpen: Boolean = false,
    val isEditorNew: Boolean = true,
    val editorItem: StockItem? = null,
    val editorUnit: String = DEFAULT_UNIT,
    val knownNames: List<String> = emptyList(),
    val units: List<String> = CatalogData.units,

    // month-end check sheet
    val isCheckOpen: Boolean = false,
    val checkRows: List<StockCheckRow> = emptyList(),
    val checkMonth: String = "",
    val checkedAtMillis: Long = 0L,

    // log detail sheet
    val logDetail: StockCheckLogView? = null,
    val usageRows: List<StockUsageRow> = emptyList(),
    val usagePreviousMonth: String? = null,
)
