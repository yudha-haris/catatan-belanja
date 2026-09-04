package com.yudha.catatanbelanja.core.domain.repository

import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.domain.model.ShoppingItem
import com.yudha.catatanbelanja.core.domain.model.ShoppingSession

interface SessionRepository {
    suspend fun getFinishedSessions(): Resource<List<ShoppingSession>>
    suspend fun getActiveSession(): Resource<ShoppingSession?>
    suspend fun getSession(id: String): Resource<ShoppingSession?>
    suspend fun startSession(store: String): Resource<ShoppingSession>
    suspend fun updateStore(sessionId: String, store: String): Resource<Unit>
    suspend fun addItem(sessionId: String, item: ShoppingItem): Resource<Unit>
    suspend fun updateItem(sessionId: String, item: ShoppingItem): Resource<Unit>
    suspend fun deleteItem(sessionId: String, itemId: String): Resource<Unit>
    suspend fun finishSession(sessionId: String, name: String): Resource<Unit>
    suspend fun cancelActiveSession(): Resource<Unit>
    suspend fun deleteSession(sessionId: String): Resource<Unit>
}
