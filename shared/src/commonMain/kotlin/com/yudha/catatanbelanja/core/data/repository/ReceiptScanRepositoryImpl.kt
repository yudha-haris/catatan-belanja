package com.yudha.catatanbelanja.core.data.repository

import com.yudha.catatanbelanja.core.common.Failure
import com.yudha.catatanbelanja.core.common.Resource
import com.yudha.catatanbelanja.core.data.service.OpenRouterConfig
import com.yudha.catatanbelanja.core.domain.model.ReceiptScan
import com.yudha.catatanbelanja.core.domain.repository.ReceiptScanRepository
import com.yudha.catatanbelanja.core.domain.service.NetworkMonitor
import com.yudha.catatanbelanja.core.domain.service.ReceiptScanException
import com.yudha.catatanbelanja.core.domain.service.ReceiptScanner
import kotlin.coroutines.cancellation.CancellationException

/**
 * The one repository in the app that does not touch the database.
 *
 * It does not use `resourceOf`: that wrapper flattens every throw into one message, and the whole
 * point of [ReceiptScanException.code] is that the review screen tells a missing key apart from a
 * photo the model could not read. Anything that is *not* a scan failure still collapses the usual
 * way, since an unexpected throw here is a bug rather than something to phrase.
 */
class ReceiptScanRepositoryImpl(
    private val scanner: ReceiptScanner,
    private val config: OpenRouterConfig,
    private val networkMonitor: NetworkMonitor,
) : ReceiptScanRepository {

    override suspend fun scan(image: ByteArray): Resource<ReceiptScan> = try {
        Resource.Success(scanner.scan(image))
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (scanFailure: ReceiptScanException) {
        Resource.Error(
            Failure(
                message = scanFailure.message,
                code = scanFailure.code,
                cause = scanFailure,
            ),
        )
    } catch (error: Throwable) {
        Resource.Error(Failure(message = MSG_SCAN_FAILED, cause = error))
    }

    override fun isAvailable(): Boolean = config.isConfigured

    override fun isOnline(): Boolean = networkMonitor.isOnline()

    private companion object {
        const val MSG_SCAN_FAILED = "Failed to scan the receipt"
    }
}
