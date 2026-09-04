package com.yudha.catatanbelanja.android.designsystem.component.display

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yudha.catatanbelanja.android.designsystem.theme.AppTheme

/**
 * The barcode along the foot of the receipt. It encodes nothing — no scanner will ever read a
 * shopping note — but a receipt without one does not look like a receipt, and the bars are derived
 * from [seed] so the same trip always prints the same code instead of reshuffling on every frame.
 */
@Composable
fun AppBarcode(
    seed: String,
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.ink,
) {
    val widths = remember(seed) { barWidthsOf(seed) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(BARCODE_HEIGHT),
    ) {
        val unit = size.width / widths.sum().toFloat()
        var x = 0f
        widths.forEachIndexed { index, width ->
            val span = width * unit
            // Even runs are ink, odd runs are the paper showing through.
            if (index % 2 == 0) {
                drawRect(
                    color = color,
                    topLeft = Offset(x, 0f),
                    size = Size(span, size.height),
                )
            }
            x += span
        }
    }
}

/**
 * A run-length pattern of ink and gap widths, 1..4 units each, from a deterministic LCG seeded by
 * the string — the same generator shape the demo data uses.
 */
private fun barWidthsOf(seed: String): List<Int> {
    var state = seed.fold(SEED_START) { acc, char -> (acc * SEED_MULTIPLIER + char.code) % SEED_MODULUS }
    return List(BAR_COUNT) {
        state = (state * LCG_MULTIPLIER + LCG_INCREMENT) % SEED_MODULUS
        (state % MAX_BAR_UNITS) + 1
    }
}

private const val BAR_COUNT = 58
private const val MAX_BAR_UNITS = 4
private const val SEED_START = 7
private const val SEED_MULTIPLIER = 31
private const val LCG_MULTIPLIER = 9301
private const val LCG_INCREMENT = 49297
private const val SEED_MODULUS = 233280
private val BARCODE_HEIGHT = 40.dp
