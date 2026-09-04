package com.yudha.catatanbelanja.features.settings.domain.usecase

import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.domain.repository.SessionRepository
import com.yudha.catatanbelanja.core.domain.repository.SettingsRepository
import com.yudha.catatanbelanja.core.domain.repository.StockRepository
import com.yudha.catatanbelanja.features.settings.domain.model.SettingsOverview

/**
 * Reads the three sources the settings screen renders — the saved theme, the finished sessions
 * and the stock rows — and folds them into one result. The first failure wins: a half-filled
 * screen would misreport how much data "Hapus semua data" is about to erase.
 */
class LoadSettingsOverview(
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
    private val stockRepository: StockRepository,
) {

    suspend operator fun invoke(): Resource<SettingsOverview> {
        val settings = when (val result = settingsRepository.getSettings()) {
            is Resource.Error -> return result
            is Resource.Success -> result.value
        }

        val sessions = when (val result = sessionRepository.getFinishedSessions()) {
            is Resource.Error -> return result
            is Resource.Success -> result.value
        }

        val stockItems = when (val result = stockRepository.getStockItems()) {
            is Resource.Error -> return result
            is Resource.Success -> result.value
        }

        return Resource.Success(
            SettingsOverview(
                themeFlavor = settings.themeFlavor,
                sessionCount = sessions.size,
                stockCount = stockItems.size,
            ),
        )
    }
}
