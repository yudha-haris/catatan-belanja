package com.yudha.catatanbelanja.core.domain.service

import com.yudha.catatanbelanja.core.domain.model.ReceiptScan

/**
 * Reads the line items off a photographed paper receipt. The only part of this app that touches
 * the network, and it is reached from exactly one screen.
 *
 * [image] is already scaled and JPEG-encoded by the caller: what a receipt photo should be
 * downsized to is a platform concern, and shipping a 12-megapixel original would cost far more
 * per scan without reading any better.
 *
 * Throws [ReceiptScanException] for the failures worth telling the user apart.
 */
interface ReceiptScanner {
    suspend fun scan(image: ByteArray): ReceiptScan
}
