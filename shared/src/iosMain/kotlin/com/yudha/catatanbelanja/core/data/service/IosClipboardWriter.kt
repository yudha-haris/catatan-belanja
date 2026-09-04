package com.yudha.catatanbelanja.core.data.service

import com.yudha.catatanbelanja.core.domain.service.ClipboardWriter
import platform.UIKit.UIPasteboard

class IosClipboardWriter : ClipboardWriter {

    override fun write(label: String, text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}
