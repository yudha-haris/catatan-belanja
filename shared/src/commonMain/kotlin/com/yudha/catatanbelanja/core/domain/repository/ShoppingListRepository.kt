package com.yudha.catatanbelanja.core.domain.repository

import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.domain.model.ShoppingList

/**
 * The plan for the next trip, plus the templates it can be built from.
 *
 * Lives in `core` rather than in the list feature because the live session ticks items off as
 * they land in the cart, and `features.shopping` may not import `features.list`.
 */
interface ShoppingListRepository {

    /** The one list currently being planned, or null when the user has not started one. */
    suspend fun getActiveList(): Resource<ShoppingList?>

    suspend fun getTemplates(): Resource<List<ShoppingList>>

    /**
     * Starts a fresh plan from [names]. Blanks are dropped and repeats collapsed, so seeding
     * from "stok menipis" on top of "belanja terakhir" cannot produce the same item twice.
     * Any list already being planned is discarded — the UI only offers this when there is none.
     */
    suspend fun startList(names: List<String>): Resource<ShoppingList>

    /** Appends [names] to [listId], skipping any it already holds. Returns how many landed. */
    suspend fun addItems(listId: String, names: List<String>): Resource<Int>

    suspend fun updateItem(
        listId: String,
        itemId: String,
        name: String,
        note: String,
    ): Resource<Unit>

    suspend fun setItemChecked(listId: String, itemId: String, isChecked: Boolean): Resource<Unit>

    /**
     * Ticks off the first unchecked line whose name matches [name] — what the live session calls
     * when an item lands in the cart. Returns the list as it now stands (null when there is no
     * active list), so the caller sees the new progress without a second read.
     */
    suspend fun checkItemByName(name: String): Resource<ShoppingList?>

    /**
     * The mirror of [checkItemByName]: un-ticks the first ticked line matching [name], for when
     * the item that ticked it is taken back out of the cart. Without it the plan drifts — a line
     * stays crossed off for something that was never bought.
     */
    suspend fun uncheckItemByName(name: String): Resource<ShoppingList?>

    suspend fun deleteItem(listId: String, itemId: String): Resource<Unit>

    /** Copies [listId] into a reusable template called [name]. */
    suspend fun saveAsTemplate(listId: String, name: String): Resource<Unit>

    suspend fun deleteList(listId: String): Resource<Unit>

    /**
     * Closes the active list at the end of a trip. When [carryOverUnchecked] is true the lines
     * that were never bought become the next plan instead of being lost. Returns how many were
     * carried over; 0 when everything was bought, or when there was no list at all.
     */
    suspend fun archiveActiveList(carryOverUnchecked: Boolean): Resource<Int>
}
