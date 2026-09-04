package com.yudha.catatanbelanja.core.data.database

import com.yudha.catatanbelanja.core.domain.model.PriceBasis
import com.yudha.catatanbelanja.core.domain.model.QtyOverride
import com.yudha.catatanbelanja.core.domain.model.RateMode
import com.yudha.catatanbelanja.core.domain.model.RatePeriod
import com.yudha.catatanbelanja.core.domain.model.ReadingSource
import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.model.ShoppingList
import com.yudha.catatanbelanja.core.domain.model.ShoppingListItem
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.model.StockCheckEntry
import com.yudha.catatanbelanja.core.domain.model.StockCheckLog
import com.yudha.catatanbelanja.core.domain.model.StockItem
import com.yudha.catatanbelanja.core.domain.model.StockRate
import com.yudha.catatanbelanja.core.domain.model.StockReading
import com.yudha.catatanbelanja.core.domain.model.TrendSetting
import com.yudha.catatanbelanja.db.SelectFinished
import com.yudha.catatanbelanja.db.Session
import com.yudha.catatanbelanja.db.Session_item
import com.yudha.catatanbelanja.db.Shopping_list
import com.yudha.catatanbelanja.db.Shopping_list_item
import com.yudha.catatanbelanja.db.Stock_check_log
import com.yudha.catatanbelanja.db.Stock_check_log_item
import com.yudha.catatanbelanja.db.Stock_item
import com.yudha.catatanbelanja.db.Stock_rate
import com.yudha.catatanbelanja.db.Stock_reading
import com.yudha.catatanbelanja.db.Trend_qty_override
import com.yudha.catatanbelanja.db.Trend_setting

internal fun Session.toDomain(items: List<ShoppingItem>): ShoppingSession = ShoppingSession(
    id = id,
    name = name,
    store = store,
    startedAt = started_at,
    endedAt = ended_at,
    items = items,
)

internal fun Session_item.toDomain(): ShoppingItem = ShoppingItem(
    id = id,
    name = name,
    price = price.toInt(),
    qty = qty,
    unit = unit,
    note = note,
)

internal fun Stock_item.toDomain(): StockItem = StockItem(
    id = id,
    name = name,
    qty = qty,
    unit = unit,
    minQty = min_qty,
    fullQty = full_qty,
    updatedAt = updated_at,
)

internal fun Stock_check_log.toDomain(entries: List<StockCheckEntry>): StockCheckLog = StockCheckLog(
    id = id,
    month = month,
    checkedAt = checked_at,
    entries = entries,
)

internal fun Stock_check_log_item.toDomain(): StockCheckEntry = StockCheckEntry(
    name = name,
    qty = qty,
    unit = unit,
)

internal fun Stock_reading.toDomain(): StockReading = StockReading(
    itemId = item_id,
    qty = qty,
    unit = unit,
    at = at,
    source = ReadingSource.fromStorage(source),
)

internal fun Stock_rate.toDomain(): StockRate = StockRate(
    itemId = item_id,
    mode = RateMode.fromStorage(mode),
    manualQty = manual_qty,
    manualUnit = manual_unit,
    manualPeriod = RatePeriod.fromStorage(manual_period),
    updatedAt = updated_at,
)

/**
 * `selectFinished` filters on `ended_at IS NOT NULL`, so SQLDelight narrows that column to
 * non-null and generates its own row type instead of reusing [Session].
 */
internal fun SelectFinished.toDomain(items: List<ShoppingItem>): ShoppingSession = ShoppingSession(
    id = id,
    name = name,
    store = store,
    startedAt = started_at,
    endedAt = ended_at,
    items = items,
)

internal fun Shopping_list.toDomain(items: List<ShoppingListItem>): ShoppingList = ShoppingList(
    id = id,
    name = name,
    createdAt = created_at,
    updatedAt = updated_at,
    isTemplate = is_template.toBooleanFlag(),
    archivedAt = archived_at,
    items = items,
)

internal fun Shopping_list_item.toDomain(): ShoppingListItem = ShoppingListItem(
    id = id,
    name = name,
    note = note,
    isChecked = checked.toBooleanFlag(),
)

/** SQLite has no boolean: every flag column is an INTEGER that is 0 or 1. */
internal fun Boolean.toFlag(): Long = if (this) 1L else 0L

internal fun Long.toBooleanFlag(): Boolean = this != 0L

internal fun Trend_setting.toDomain(): TrendSetting = TrendSetting(
    nameKey = name_key,
    basis = parsePriceBasis(basis),
    baseUnit = base_unit,
)

internal fun Trend_qty_override.toDomain(): QtyOverride = QtyOverride(
    itemId = item_id,
    nameKey = name_key,
    qty = qty,
    unit = unit,
)

/** An unknown basis string can only come from a hand-edited database; read it as the default. */
private fun parsePriceBasis(raw: String): PriceBasis =
    PriceBasis.entries.firstOrNull { it.name == raw } ?: PriceBasis.RAW
