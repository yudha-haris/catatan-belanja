package com.yudha.catatanbelanja.features.shopping.domain.usecase

import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.common.dataOrNull
import com.yudha.catatanbelanja.core.common.returnWhen
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.core.domain.repository.ShoppingListRepository
import com.yudha.catatanbelanja.core.domain.repository.StockRepository
import com.yudha.catatanbelanja.features.shopping.domain.model.FinishResult

/**
 * Closes the running session under [name] and, when asked, pushes every item that carries a
 * quantity into the home stock and closes the shopping list behind it.
 */
class FinishShoppingSession(
    private val sessionRepository: SessionRepository,
    private val stockRepository: StockRepository,
    private val shoppingListRepository: ShoppingListRepository,
) {
    suspend operator fun invoke(
        session: ShoppingSession,
        name: String,
        addToStock: Boolean,
        carryOverList: Boolean,
    ): Resource<FinishResult> {
        val added = when (addToStock) {
            // Stock first: a failure there must not leave a session that is already closed.
            true -> when (val stocked = stockRepository.addSessionToStock(session)) {
                is Resource.Error -> return stocked
                is Resource.Success -> stocked.value
            }

            false -> 0
        }

        return sessionRepository.finishSession(session.id, name).returnWhen(
            onSuccess = { _ ->
                Resource.Success(
                    FinishResult(addedToStock = added, carriedOverToList = closeList(carryOverList)),
                )
            },
            onError = { Resource.Error(it) },
        )
    }

    /**
     * Best-effort, and deliberately last: the trip is already filed by now, so a list that
     * refuses to close must not turn a finished session into an error. It simply stays open,
     * which the user can see and fix on the Daftar screen.
     */
    private suspend fun closeList(carryOverUnchecked: Boolean): Int =
        shoppingListRepository.archiveActiveList(carryOverUnchecked).dataOrNull() ?: 0
}
