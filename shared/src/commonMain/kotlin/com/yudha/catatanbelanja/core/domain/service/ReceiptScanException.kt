package com.yudha.catatanbelanja.core.domain.service

/**
 * A scan that failed in a way the screen should phrase differently. [code] survives into
 * [Failure.code][com.yudha.catatanbelanja.core.common.Failure.code], which is what the review
 * screen switches on to choose its message — "paste your key in local.properties" and "the model
 * could not read that photo" are not the same problem and must not share a dialog.
 */
class ReceiptScanException(
    override val message: String,
    val code: String,
) : Exception(message) {

    companion object {
        /** No OpenRouter key was compiled in — `local.properties` still holds the placeholder. */
        const val MISSING_KEY = "SCAN_MISSING_KEY"

        /** The request never landed: no network, a rejected key, a bad model slug, a timeout. */
        const val REQUEST_FAILED = "SCAN_REQUEST_FAILED"

        /** The reply arrived but was not the JSON that was asked for. */
        const val UNREADABLE_REPLY = "SCAN_UNREADABLE_REPLY"

        /** The reply parsed fine and held no items — usually a photo that is not a receipt. */
        const val NO_ITEMS = "SCAN_NO_ITEMS"
    }
}
