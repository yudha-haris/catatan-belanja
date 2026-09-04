package com.yudha.catatanbelanja.android.format

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private const val RUPIAH = "Rp"
private const val MILLION_SUFFIX = "jt"
private const val THOUSAND_SUFFIX = "rb"

/** Indonesian grouping: 72000 -> "72.000". Symbols are pinned so a device locale cannot shift them. */
private val groupingFormat: DecimalFormat = DecimalFormat(
    "#,##0",
    DecimalFormatSymbols(Locale.forLanguageTag("id-ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    },
)

/** "Rp72.000" — the prototype's fmt(). */
fun Int.toRupiah(): String = RUPIAH + groupingFormat.format(this.toLong())

/** "Rp1,3jt" / "Rp72rb" / "Rp900" — the prototype's fmtShort(). */
fun Int.toRupiahShort(): String {
    if (this >= 1_000_000) {
        val millions = String.format(Locale.US, "%.1f", this / 1_000_000.0)
        return RUPIAH + millions.removeSuffix(".0").replace('.', ',') + MILLION_SUFFIX
    }
    if (this >= 1_000) return RUPIAH + (this / 1_000.0).roundToInt() + THOUSAND_SUFFIX
    return RUPIAH + this
}

/** "+Rp2.000" / "-Rp2.000"; a zero delta carries no sign. */
fun Int.toRupiahSigned(): String {
    if (this > 0) return "+" + this.toRupiah()
    if (this < 0) return "-" + abs(this).toRupiah()
    return this.toRupiah()
}
