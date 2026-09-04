package com.yudha.catatanbelanja.features.receipt.di

import com.yudha.catatanbelanja.features.receipt.domain.usecase.BuildScannedRows
import com.yudha.catatanbelanja.features.receipt.presentation.ScanReceiptViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** Populated by the receipt-scanning feature: its one view model and the row builder behind it. */
val receiptModule: Module = module {
    factoryOf(::BuildScannedRows)

    factoryOf(::ScanReceiptViewModel)
}
