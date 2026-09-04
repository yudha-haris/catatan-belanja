package com.yudha.catatanbelanja.core.data.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.yudha.catatanbelanja.core.domain.service.ClipboardWriter

class AndroidClipboardWriter(private val context: Context) : ClipboardWriter {

    override fun write(label: String, text: String) {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: return
        manager.setPrimaryClip(ClipData.newPlainText(label, text))
    }
}
