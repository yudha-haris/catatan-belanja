package com.yudha.catatanbelanja.android.format

import kotlin.math.floor

/**
 * The prototype's fmtQty(): whole numbers lose their decimals ("5"), everything else
 * switches to the Indonesian decimal comma ("0,5"). A null quantity renders as nothing.
 */
fun Double?.toQtyLabel(): String {
    val value = this ?: return ""
    if (!value.isFinite()) return ""
    if (value == floor(value)) return value.toLong().toString()
    return value.toString().replace('.', ',')
}
