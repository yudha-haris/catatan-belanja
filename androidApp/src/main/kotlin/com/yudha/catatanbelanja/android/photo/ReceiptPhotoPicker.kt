package com.yudha.catatanbelanja.android.photo

import androidx.compose.runtime.Stable

/**
 * The two ways a receipt picture gets into the app. Handed out by [rememberReceiptPhotoPicker];
 * a screen calls [takePhoto] or [pickFromGallery] and the bytes arrive on the callback.
 *
 * [canTakePhoto] is false on a device with no camera at all — an emulator, a tablet — and the
 * sheet drops that button rather than offering one that opens nothing.
 */
@Stable
class ReceiptPhotoPicker internal constructor(
    val canTakePhoto: Boolean,
    private val onTakePhoto: () -> Unit,
    private val onPickFromGallery: () -> Unit,
) {
    fun takePhoto() {
        if (!canTakePhoto) return
        onTakePhoto()
    }

    fun pickFromGallery() {
        onPickFromGallery()
    }
}
