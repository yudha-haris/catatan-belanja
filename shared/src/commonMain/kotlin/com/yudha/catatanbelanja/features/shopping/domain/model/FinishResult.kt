package com.yudha.catatanbelanja.features.shopping.domain.model

/**
 * What closing a trip produced besides the receipt: how many items went into the home stock,
 * and how many list lines were never bought and so became the next plan.
 */
data class FinishResult(
    val addedToStock: Int = 0,
    val carriedOverToList: Int = 0,
)
