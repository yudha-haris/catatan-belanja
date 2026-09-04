package com.yudha.catatanbelanja.android.format

import kotlin.math.floor
import kotlin.math.round

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

/**
 * A drain rate. Rounded hard, because the arithmetic behind it happily produces
 * "33,333333333333336 gram" and no household is accurate to the picogram. Falls to three decimals
 * only when two would round a real rate down to nothing.
 */
fun Double.toRateLabel(): String {
    if (!isFinite()) return ""
    val coarse = round(this * 100) / 100
    if (coarse != 0.0 || this == 0.0) return coarse.toQtyLabel()
    return (round(this * 1000) / 1000).toQtyLabel()
}
